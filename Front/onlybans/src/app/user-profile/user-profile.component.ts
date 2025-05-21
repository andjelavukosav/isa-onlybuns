import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../service/user.service';
import { PostService } from '../service/post.service';
import { AuthUser, UserDTO } from '../model/registered-user';
import { Post } from '../model/post.model';
import { PagedResults } from '../model/paged-result.model';
import { AuthService } from '../service';
import { BehaviorSubject } from 'rxjs';
import { filter, take, switchMap, tap } from 'rxjs/operators';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {
  userId: number | null = null;
  user: UserDTO | null = null;
  posts: Post[] = [];
  friends: UserDTO[] = []; // Lista prijatelja
  currentUser: AuthUser | null = null;
  currentUserId: number | null = null;
  activeTab: string = 'posts'; // Kontrolni mehanizam za prikaz sadržaja (podrazumevano: Objave)
  editingName = false;
  editingLastName = false;
  editingAddress = false;
  currentPassword: string = ''; // Novo polje za trenutnu lozinku
  invalidPassword: boolean = false; // Indikator za neispravnu lozinku

  editing: boolean = false;
  currentPasswordInput: string = ''; // Stara lozinka koju korisnik unosi
  oldPasswordVerified: boolean = false; // Da li je stara lozinka potvrđena
  newPassword: string = ''; // Nova lozinka
  confirmPassword: string = ''; // Potvrda nove lozinke
  passwordErrorMessage: string = ''; // Poruka o grešci kod lozinke


  editableUser: UserDTO = {
    id: 0,
    username: '',
    email: '',
    followersCount: 0,
    followingCount: 0,
    firstname: '',
    lastname: '',
    postsCount: 0, // Dodato svojstvo
    address: {
      street: '',
      streetNumber: '',
      city: '',
      country: ''
    },
    // Dodaj ostala svojstva iz UserDTO modela prema potrebi
  };
  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private postService: PostService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {

    this.route.paramMap.subscribe(params =>{
      this.userId = Number(params.get('userId'));
      console.log('user id in parent: ', this.userId)
      if(this.userId){
        this.loadUser();
        if (this.user) {
          this.editableUser = { ...this.user };
        }
        this.getPosts();
      }
    });

    this.authService.user$.subscribe({
      next: (user) => {
        if(user){
          this.currentUser = user;
          this.currentUserId = user.id;
        }
      },
      error: (err) => {
        console.log('An error occurred while trying to log in. ', err);
      }
    });

 }

 startEditing() {
    this.editing = true;
    this.oldPasswordVerified = false;
    this.currentPasswordInput = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.passwordErrorMessage = '';
    // Napravi kopiju user podataka u editableUser da ne menjaš odmah original
    if (this.user) {
      this.editableUser = JSON.parse(JSON.stringify(this.user));
    }
  }

  verifyOldPassword() {
    if (!this.currentPasswordInput || !this.userId) {
      this.passwordErrorMessage = 'Please enter your current password.';
      return;
    }
    this.userService.verifyPassword(this.userId, this.currentPasswordInput).subscribe({
      next: (isValid) => {
        if (isValid) {
          this.oldPasswordVerified = true;
          this.passwordErrorMessage = '';
        } else {
          this.passwordErrorMessage = 'Current password is incorrect.';
          this.oldPasswordVerified = false;
        }
      },
      error: () => {
        this.passwordErrorMessage = 'Error verifying password. Please try again.';
        this.oldPasswordVerified = false;
      }
    });
  }


  saveChanges() {
    if (!this.userId) return;

    if (this.oldPasswordVerified) {
      this.userService.updatePassword(this.userId, this.newPassword).pipe(
        tap((response: any) => {
          if (response.token) {
            this.authService.setToken(response.token);
          }
        }),
        // Sačekaj da tokenSubject emituje novi token koji smo upravo postavili
        switchMap((response: any) => this.authService.tokenSubject.pipe(
          filter(token => token === response.token), // čekaj da tokenSubject emituje novi token
          take(1)
        )),
        // Kada se potvrdi da je token ažuriran, pošalji updateUserData
        switchMap(() => this.userService.updateUserData(this.userId!, this.editableUser))
      ).subscribe({
        next: () => {
          alert('User updated successfully!');
          this.resetPasswordFields();
          this.editing = false;
          this.loadUser();
        },
        error: () => {
          alert('Failed to update password or user data.');
        }
      });
    } else {
      this.saveUserDataAfterPasswordChange();
    }
  }



  saveUserDataAfterPasswordChange() {
    this.userService.updateUserData(this.userId!, this.editableUser).subscribe({
      next: () => {
        alert('User updated successfully!');
        this.editing = false;
        this.loadUser();
      },
      error: () => {
        alert('Failed to update user data.');
      }
    });
  }

  cancelEditing() {
    this.editing = false;
    this.oldPasswordVerified = false;
    this.currentPasswordInput = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.passwordErrorMessage = '';
    if (this.user) {
      this.editableUser = JSON.parse(JSON.stringify(this.user));
    }
  }

  // Učitavanje korisničkih podataka
  loadUser(): void {
    this.userService.getUserById(this.userId!).subscribe({
      next: (user) => {
        this.user = user;
        this.editableUser = {
          ...user,
          address: user.address || {
            street: '',
            streetNumber: '',
            city: '',
            country: ''
          }
        };
      },
      error: (err) => console.error(`Failed to load user with ID ${this.userId}`, err)
    });
  }

  // Učitavanje objava korisnika
  getPosts(): void {
    this.postService.getPostsByUser(this.userId || 0).subscribe({
      next: (result: PagedResults<Post>) => {
        const sortedPosts = result.results.sort((a, b) => {
          const dateA = new Date(a.creationDateTime);
          const dateB = new Date(b.creationDateTime);
          return dateB.getTime() - dateA.getTime();
        });

        sortedPosts.forEach(post => {
          post.usernameDisplay = this.user?.username;
        });

        this.posts = sortedPosts;
      },
      error: () => {
        console.error('Failed to load posts.');
      }
    });
  }

  // Ažuriranje lozinke
  updatePassword(): void {
    if (!this.oldPasswordVerified) {
      alert('You must verify your current password first.');
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      alert('Passwords do not match!');
      return;
    }

    if (!this.userId) {
      alert('User ID is missing!');
      return;
    }

    this.userService.updatePassword(this.userId, this.newPassword).subscribe({
      next: (response) => {
        if (response.token) {
        // Sačuvaj novi token, npr. u localStorage
        localStorage.setItem('token', response.token);
        // Ažuriraj header Authorization za buduće zahteve, ako koristiš interceptor
      }
        alert('Password updated successfully!');
        this.resetPasswordFields();
      },
      error: (error) => {
        console.error('Failed to update password:', error);
        alert('Failed to update password.');
      }
    });
  }

  resetPasswordFields(): void {
    this.currentPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.oldPasswordVerified = false;
  }

  

 
}


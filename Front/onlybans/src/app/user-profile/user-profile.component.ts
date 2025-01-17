import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../service/user.service';
import { PostService } from '../service/post.service';
import { UserDTO } from '../model/registered-user';
import { Post } from '../model/post.model';
import { PagedResults } from '../model/paged-result.model';

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
  newPassword: string = '';
  confirmPassword: string = '';
  currentUserId: number | null = null;
  whoamIResponse = {};
  activeTab: string = 'posts'; // Kontrolni mehanizam za prikaz sadržaja (podrazumevano: Objave)
  editingName = false;
  editingLastName = false;
  editingAddress = false;
  currentPassword: string = ''; // Novo polje za trenutnu lozinku
  oldPasswordVerified: boolean = false; // Praćenje da li je lozinka potvrđena
  invalidPassword: boolean = false; // Indikator za neispravnu lozinku

  editableUser: UserDTO = {
    id: 0,
    username: '',
    email: '',
    followersCount: 0,
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
    private postService: PostService
  ) {}

  ngOnInit(): void {
    this.getCurrentUser('http://localhost:8080');
    this.userId = Number(this.route.snapshot.paramMap.get('userId'));

    if (this.userId) {
      this.loadUser();
    }
    if (this.user) {
    this.editableUser = { ...this.user };
  }
    
    this.getPosts();
    this.getFriends(); // Učitavanje prijatelja
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
    this.postService.getPostsByUserId(this.userId || 0).subscribe({
      next: (result: PagedResults<Post>) => {
        const sortedPosts = result.results.sort((a, b) => {
          const dateA = new Date(a.creationDateTime);
          const dateB = new Date(b.creationDateTime);
          return dateB.getTime() - dateA.getTime();
        });

        sortedPosts.forEach(post => {
          this.userService.getUserById(post.user?.id || 0).subscribe({
            next: (user) => {
              post.usernameDisplay = user.username;

              if (post.imagePath) {
                console.log('Image path: ', post.imagePath);
              }
            },
            error: () => {
              console.error(`Failed to load user for post ID ${post.id}`);
            }
          });
        });

        this.posts = sortedPosts;
      },
      error: () => {
        console.error('Failed to load posts.');
      }
    });
  }

  // Učitavanje liste prijatelja
  getFriends(): void {
  /*  this.userService.getFriendsByUserId(this.userId || 0).subscribe({
      next: (friends) => {
        this.friends = friends;
      },
      error: (err) => console.error(`Failed to load friends for user ID ${this.userId}`, err)
    });*/
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
      next: () => {
        alert('Password updated successfully!');
        this.resetPasswordFields();
      },
      error: (error) => {
        console.error('Failed to update password:', error);
        alert('Failed to update password.');
      }
    });
  }

  verifyOldPassword(): void {
    if (!this.currentPassword || !this.userId) {
      alert('Current password is required.');
      return;
    }

    this.userService.verifyPassword(this.userId, this.currentPassword).subscribe({
      next: (isVerified) => {
        if (isVerified) {
          this.oldPasswordVerified = true;
          this.invalidPassword = false;
        } else {
          this.invalidPassword = true;
        }
      },
      error: (error) => {
        console.error('Failed to verify password:', error);
        alert('An error occurred while verifying the password.');
      }
    });
  }

  resetPasswordFields(): void {
    this.currentPassword = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.oldPasswordVerified = false;
  }

  // Dohvatanje trenutnog korisnika
  getCurrentUser(path: any): void {
    this.userService.getMyInfo()
      .subscribe(res => {
        this.forgeResonseObj(this.whoamIResponse, res, path);
        this.currentUserId = res.id;
      }, err => {
        this.forgeResonseObj(this.whoamIResponse, err, path);
      });
  }

  forgeResonseObj(obj: any, res: any, path: any): void {
    obj['path'] = path;
    obj['method'] = 'GET';
    if (res.ok === false) {
      obj['status'] = res.status;
      try {
        obj['body'] = JSON.stringify(JSON.parse(res._body), null, 2);
      } catch (err) {
        console.log(res);
        obj['body'] = res.error.message;
      }
    } else {
      obj['status'] = 200;
      obj['body'] = JSON.stringify(res, null, 2);
    }
  }

  // Postavljanje aktivnog taba
  setActiveTab(tab: string): void {
    this.activeTab = tab;
  
    if (tab === 'posts') {
      this.getPosts(); // Ponovo učitava objave kada je "Objave" tab aktivan
    }
  }

  editField(field: string) {
    if (field === 'firstname') this.editingName = true;
    if (field === 'lastname') this.editingLastName = true;
    if (field === 'address') this.editingAddress = true;
  }
  
  cancelEdit(field: string): void {
    // Isključivanje edit moda za polje koje se otkazuje
    if (field === 'firstname') this.editingName = false;
    if (field === 'lastname') this.editingLastName = false;
    if (field === 'address') this.editingAddress = false;
  
    // Resetovanje editableUser na originalne vrednosti iz user objekta
    this.editableUser = { 
      id: this.user?.id || 0,
      username: this.user?.username || '',
      email: this.user?.email || '',
      followersCount: this.user?.followersCount || 0,
      postsCount: this.user?.postsCount || 0,
      firstname: this.user?.firstname || '',
      lastname: this.user?.lastname || '',
      address: this.user?.address ? { 
        street: this.user.address.street || '',
        streetNumber: this.user.address.streetNumber || '',
        city: this.user.address.city || '',
        country: this.user.address.country || ''
      } : {
        street: '',
        streetNumber: '',
        city: '',
        country: ''
      }
    };
    this.loadUser();
  }
  
  
  
  saveField(field: string) {
    // Ažuriranje korisnika na serveru
    this.userService.updateUserData(this.userId || 0, this.editableUser).subscribe({
      next: (response: string) => {
        console.log(response); // Očekivani tekstualni odgovor
        alert('Updated successfully!');
        this.cancelEdit(field); // Zatvaranje edit moda
      },
      error: (error) => {
        console.error('Error saving user data', error);
        alert('Greška pri čuvanju podataka');
      }
    });
    
  }
  

}


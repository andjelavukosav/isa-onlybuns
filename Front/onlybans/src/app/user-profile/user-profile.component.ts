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
    this.getPosts();
    this.getFriends(); // Učitavanje prijatelja
  }

  // Učitavanje korisničkih podataka
  loadUser(): void {
    this.userService.getUserById(this.userId!).subscribe({
      next: (user) => {
        this.user = user;
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
    if (this.newPassword !== this.confirmPassword) {
      alert('Passwords do not match!');
      return;
    }

    if (!this.userId) {
      alert('User ID is missing!');
      return;
    }

    this.userService.updatePassword(this.userId, this.newPassword).subscribe(
      () => {
        alert('Password updated successfully!');
        this.newPassword = '';
        this.confirmPassword = '';
      },
      (error) => {
        console.error(error);
        alert('Failed to update password.');
      }
    );
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

}

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../service/user.service';
import { UserDTO } from '../model/registered-user'; // Import UserDTO if needed
import { PostService } from '../service/post.service';
import { Post } from '../model/post.model';
import { PagedResults } from '../model/paged-result.model';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {
  userId: number | null = null;
  user: UserDTO | null = null; // Define user variable to store user data
  posts: Post[] = [];
  newPassword: string = ''; // Nova lozinka
  confirmPassword: string = ''; // Potvrda nove lozinke
  currentUserId: number | null = null; // Trenutni korisnik ID
  whoamIResponse = {};
  
  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private postService: PostService
  ) {}

  ngOnInit(): void {
    // Retrieve the userId from the route parameters
    this.getCurrentUser('http://localhost:8080');
    this.userId = Number(this.route.snapshot.paramMap.get('userId'));
  
    // Fetch user data and set currentUserId
    if (this.userId) {
      this.userService.getUserById(this.userId).subscribe({
        next: (user) => {
          this.user = user; // Store user data
        },
        error: (err) => console.error(`Failed to load user with ID ${this.userId}`, err)
      });
    }
  
    this.getPosts();
  }
  

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
                console.log('Image path: ', post.imagePath)
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

  updatePassword() {
  if (this.newPassword !== this.confirmPassword) {
    alert('Passwords do not match!');
    return;
  }

  if (!this.userId) {
    alert('User ID is missing!');
    return;
  }

  // Call the API to update the password
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

getCurrentUser(path: any): void {
  this.userService.getMyInfo()
    .subscribe(res => {
      this.forgeResonseObj(this.whoamIResponse, res, path);
      this.currentUserId = res.id; // Assign user info to currentUser
    }, err => {
      this.forgeResonseObj(this.whoamIResponse, err, path);
    });
}
forgeResonseObj(obj: any, res: any, path: any) {
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
    
}

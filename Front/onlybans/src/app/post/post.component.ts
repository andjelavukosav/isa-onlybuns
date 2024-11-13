import { Component, OnInit } from '@angular/core';
import { Post } from '../model/post.model';
import { PostService } from '../service/post.service';
import { UserService } from '../service/user.service';
import { PagedResults } from '../model/paged-result.model';
import { MatSnackBar } from '@angular/material/snack-bar'; // Import MatSnackBar

@Component({
  selector: 'app-post',
  templateUrl: './post.component.html',
  styleUrls: ['./post.component.css']
})
export class PostComponent implements OnInit {
  post: Post[] = [];
  currentUser: any;
  whoamIResponse = {};

  constructor(
    private postService: PostService,
    private userService: UserService,
    private snackBar: MatSnackBar // Inject MatSnackBar
  ) {}

  ngOnInit(): void {
    this.getPosts();
  }

  getPosts(): void {
    this.postService.getPosts().subscribe({
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

        this.post = sortedPosts;
      },
      error: () => {
        console.error('Failed to load posts.');
      }
    });
  }

  likePost(post: Post): void {
    // Check if the user is logged in
    if (!this.currentUser) {
      alert("You need to log in to access this feature.");
      return;
    }

    // Increment the like count locally
    post.likeCount = (post.likeCount || 0) + 1;

    // Call the service to save the like on the backend
    this.postService.likePost(post.id).subscribe({
      next: () => {
        console.log(`Post ${post.id} liked successfully.`);
      },
      error: () => {
        console.error(`Failed to like post ${post.id}.`);
      }
    });
  }

  getCurrentUser(path: any): void {
    this.userService.getMyInfo()
      .subscribe(res => {
        this.forgeResonseObj(this.whoamIResponse, res, path);
        this.currentUser = res; // Assign user info to currentUser
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

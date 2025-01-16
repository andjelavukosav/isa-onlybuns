import { Component, OnInit } from '@angular/core';
import { Post } from '../model/post.model';
import { PostService } from '../service/post.service';
import { UserService } from '../service/user.service';
import { PagedResults } from '../model/paged-result.model';
import { MatSnackBar } from '@angular/material/snack-bar'; // Import MatSnackBar
import { Observable } from 'rxjs';

@Component({
  selector: 'app-post',
  templateUrl: './post.component.html',
  styleUrls: ['./post.component.css']
})
export class PostComponent implements OnInit {
  post: Post[] = [];
  currentUser: any;
  whoamIResponse = {};

  commentText: {[key: number]: string} = {};

  constructor(
    private postService: PostService,
    private userService: UserService,
    private snackBar: MatSnackBar,
  ) {}
  
  ngOnInit(): void {
    this.getCurrentUser('http://localhost:8080');
    this.getPosts();
  }

  getPosts(): void {
    this.postService.getPosts().subscribe({
      next: (result: PagedResults<Post>) => {
        // Sortiranje postova po datumu kreiranja (od najnovijeg ka najstarijem)
        const sortedPosts = result.results.sort((a, b) => {
          const dateA = new Date(a.creationDateTime);
          const dateB = new Date(b.creationDateTime);
          return dateB.getTime() - dateA.getTime();
        });
  
        // Obrada svakog posta
        sortedPosts.forEach(post => {
          // Dohvatanje korisničkog imena autora posta
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

          this.postService.getLikesByPostId(post.id).subscribe({
            next: (likeCount: number) => {
              post.likeCount = likeCount; // Setovanje broja lajkova
            },
            error: () => {
              console.error(`Failed to check like count for post ID ${post.id}`);
            }
          });
  
          // Provera da li je trenutni korisnik lajkovao post
          if (this.currentUser) {
            this.postService.getLikeByPostIdAndUserId(post.id, this.currentUser.id).subscribe({
              next: (isLiked: boolean) => {
                console.log(`Post ${post.id} - Server like status: ${isLiked}`);
                post.isLikedByCurrentUser = isLiked;
              },
              error: () => {
                console.error(`Failed to check like for post ID ${post.id}`);
              }
            });
            
            
          } else {
            // Ako korisnik nije prijavljen, postavite `isLikedByCurrentUser` na false
            post.isLikedByCurrentUser = false;
          }
        });
  
        // Postavljanje sortirane liste postova
        this.post = sortedPosts;
      },
      error: () => {
        console.error('Failed to load posts.');
      }
    });
  }
  
  likePost(post: Post): void {
    if (!this.currentUser) {
      alert("You need to log in to access this feature.");
      return;
    }
  
    if (post.isLikedByCurrentUser) {
      // Ako je već lajkovan, uklonite lajk
      this.postService.unlikePost(post.id, this.currentUser.id).subscribe({
        next: () => {
          console.log(`Post ${post.id} unliked successfully on server.`);
          post.isLikedByCurrentUser = false; // Obeležite kao nelajkovano
          post.likeCount = (post.likeCount || 1) - 1; // Smanjite broj lajkova
        },
        error: (err) => {
          console.error(`Failed to unlike post ${post.id} on server.`, err);
        },
      });
    } else {
      // Ako nije lajkovan, dodajte lajk
      this.postService.likePost(post.id, this.currentUser.id).subscribe({
        next: () => {
          console.log(`Post ${post.id} liked successfully on server.`);
          post.isLikedByCurrentUser = true; // Obeležite kao lajkovano
          post.likeCount = (post.likeCount || 0) + 1; // Povećajte broj lajkova
        },
        error: (err) => {
          console.error(`Failed to like post ${post.id} on server.`, err);
        },
      });
    }
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

  addComment(post: any): void{
    if(!this.currentUser){
      alert('Please, login first.')
      this.commentText[post.id] = '';
      return;
    } 
  }

  
  
    

}  
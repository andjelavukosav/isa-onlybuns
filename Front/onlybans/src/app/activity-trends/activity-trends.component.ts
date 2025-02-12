
import { Component, OnInit } from '@angular/core';
import { Post } from '../model/post.model';
import { PostService } from '../service/post.service';
import { UserService } from '../service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PagedResults } from '../model/paged-result.model';
import { UserDTO } from '../model/registered-user';

@Component({
  selector: 'app-activity-trends',
  templateUrl: './activity-trends.component.html',
  styleUrls: ['./activity-trends.component.css']
})

export class ActivityTrendsComponent implements OnInit {
  allPosts: Post[] = [];
  postsLastMonth: Post[] = []; 
  currentUser: any;
  whoamIResponse = {};
  postsMostPopularLast7Days: Post[] = [];
  postsMostPopularEver: Post[] = [];
  usersMostPopular: UserDTO[] = [];

  commentText: {[key: number]: string} = {};

  constructor(
    private postService: PostService,
    private userService: UserService,
    private snackBar: MatSnackBar,
  ) {}
    
  ngOnInit(): void {
    this.getCurrentUser('http://localhost:8080');
    this.getPosts();
    this.getPostsLastMonth();
    this.getPostsMostPopular();
    this.getPostsMostPopularEver();
    this.getUsersMostPopular();
  }

  getUsersMostPopular(): void {
    //this.loading = true; // Postavi loading na true dok se podaci učitavaju
    this.userService.getTopUsersMostLikes().subscribe({
        next: (result: PagedResults<UserDTO>) => {
          // Direktno dodeljujemo dobijene postove bez sortiranja
          this.usersMostPopular = result.results;
    },
      error: (err) => {
      //  this.errorMessage = 'Error fetching top users. Please try again later.'; // Postavi poruku o grešci
        console.error(err); // Ispiši grešku za potrebe debugovanja
     //   this.loading = false; // Uklanjanje loading stanja čak i u slučaju greške
      },
    });
  }
  

  getPostsMostPopularEver(): void{
    this.postService.getPostsMostPopularEver().subscribe({
      next: (result: PagedResults<Post>) => {
        // Direktno dodeljujemo dobijene postove bez sortiranja
        this.postsMostPopularEver = result.results;
  
        // Obrada svakog posta
        this.postsMostPopularEver.forEach(post => {
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
      },
      error: () => {
        console.error('Failed to load posts.');
      }
    });
  }

  getPostsMostPopular(): void{
    this.postService.getPostsMostPopular().subscribe({
      next: (result: PagedResults<Post>) => {
        // Direktno dodeljujemo dobijene postove bez sortiranja
        this.postsMostPopularLast7Days = result.results;
  
        // Obrada svakog posta
        this.postsMostPopularLast7Days.forEach(post => {
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
      },
      error: () => {
        console.error('Failed to load posts.');
      }
    });
  }
  
  getPosts(): void {
  this.postService.getPostsWithoutSort().subscribe({
    next: (result: PagedResults<Post>) => {
      // Direktno dodeljujemo dobijene postove bez sortiranja
      this.allPosts = result.results;

      // Obrada svakog posta
      this.allPosts.forEach(post => {
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
    },
    error: () => {
      console.error('Failed to load posts.');
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
  
    addComment(post: any): void{
    if(!this.currentUser){
      alert('Please, login first.')
      this.commentText[post.id] = '';
      return;
    } 
  }

  getPostsLastMonth(): void {
    this.postService.getPostsLastMonth().subscribe({
      next: (result: PagedResults<Post>) => {
        // Direktno dodeljujemo dobijene postove bez sortiranja
        this.postsLastMonth = result.results;
  
        // Obrada svakog posta
        this.postsLastMonth.forEach(post => {
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
      },
      error: () => {
        console.error('Failed to load posts.');
      }
    });
  }

  reloadData(): void {
    this.getPosts();
    this.getPostsLastMonth();
    this.getPostsMostPopular();
    this.getPostsMostPopularEver();
    this.getUsersMostPopular();
  }
  
}

import { Component, OnInit } from '@angular/core';
import { FooService } from '../service';
import { ConfigService } from '../service';
import { UserService } from '../service/user.service';
import { Post } from '../model/post.model';
import { PostService } from '../service/post.service';
import { PagedResults } from '../model/paged-result.model';
@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  post: Post[] = [];
  fooResponse = {};
  whoamIResponse = {};
  allUserResponse = {};
  currentUser: any;

  constructor(
    private config: ConfigService,
    private fooService: FooService,
    private userService: UserService,
    private postService: PostService
  ) {
  }

  ngOnInit() {
    this.getCurrentUser('http://localhost:8080');
    this.getPosts();
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
  makeRequest(path:any) {
    if (this.config.foo_url.endsWith(path)) {
      this.fooService.getFoo()
        .subscribe(res => {
          this.forgeResonseObj(this.fooResponse, res, path);
        }, err => {
          this.forgeResonseObj(this.fooResponse, err, path);
        });
    } else if (this.config.whoami_url.endsWith(path)) {
      this.userService.getMyInfo()
        .subscribe(res => {
          this.forgeResonseObj(this.whoamIResponse, res, path);
        }, err => {
          this.forgeResonseObj(this.whoamIResponse, err, path);
        });
    } else {
      this.userService.getAll()
        .subscribe(res => {
          this.forgeResonseObj(this.allUserResponse, res, path);
        }, err => {
          this.forgeResonseObj(this.allUserResponse, err, path);
        });
    }
  }


  forgeResonseObj(obj:any, res:any, path:any) {
    obj['path'] = path;
    obj['method'] = 'GET';
    if (res.ok === false) {
      // err
      obj['status'] = res.status;
      try {
        obj['body'] = JSON.stringify(JSON.parse(res._body), null, 2);
      } catch (err) {
        console.log(res);
        obj['body'] = res.error.message;
      }
    } else {
      // 200
      obj['status'] = 200;
      obj['body'] = JSON.stringify(res, null, 2);
    }
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
}

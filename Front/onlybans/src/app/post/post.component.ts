import { Component, Input, OnInit, Output, EventEmitter, SimpleChanges, OnChanges } from '@angular/core';
import { Post } from '../model/post.model';
import { PostService } from '../service/post.service';
import { UserService } from '../service/user.service';
import { PagedResults } from '../model/paged-result.model';
import { MatSnackBar } from '@angular/material/snack-bar'; // Import MatSnackBar
import { BehaviorSubject, Observable } from 'rxjs';
import { AuthUser, UserDTO } from '../model/registered-user';
import { AuthService } from '../service';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-post',
  templateUrl: './post.component.html',
  styleUrls: ['./post.component.css']
})
export class PostComponent implements OnInit, OnChanges {
  @Input() userId?: number; //ako se postavi userId, uzmi postove od tog korisnika
  posts: Post[] = [];
  @Input() inputPosts: Post[] = [];
  @Output() refreshLists: EventEmitter<void> = new EventEmitter(); // Dodaj output event
  currentUser: AuthUser | null = null;
  whoamIResponse = {};
  commentText: {[key: number]: string} = {};
  isLikesListOpened: boolean = false;
  likes$: BehaviorSubject<UserDTO[]> = new BehaviorSubject<UserDTO[]>([]);
  likedPostIds: number[] = [];

  constructor(
    private postService: PostService,
    private userService: UserService,
    private snackBar: MatSnackBar, // Inject MatSnackBar
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
  this.authService.user$.subscribe({
    next: (user) => {
      if(user){
        this.currentUser = user;
        console.log("Logged in user in post component: ", this.currentUser);
      }
    }
  });

  this.postService.getLikedPostIds().subscribe(ids => {
    this.likedPostIds = ids;
    this.updateLikedStatus(); // Pozovi nakon što su postavljeni ID-jevi
  });
}



  /*private updateLikedStatus(posts: Post[]): Post[] {
  const likedSet = new Set(this.likedPostIds);
  return posts.map(post => {
    post.isLikedByCurrentUser = likedSet.has(post.id);
    return post;
  });
}
*/
private updateLikedStatus(): void {
  if (!this.inputPosts || this.inputPosts.length === 0 || !this.likedPostIds) {
    return;
  }

  this.inputPosts.forEach(post => {
    post.isLikedByCurrentUser = this.likedPostIds.includes(post.id);
  });
}


  get displayedPosts(): Post[] {
    return this.inputPosts; // Direktno koristite inputPosts za prikaz
  }
  
  ngOnChanges(changes: SimpleChanges): void {
  if (changes['inputPosts']) {
    console.log('Primljeni postovi:', changes['inputPosts'].currentValue ? changes['inputPosts'].currentValue.length : 0);
    this.posts = changes['inputPosts'].currentValue || [];
    this.updateLikedStatus(); // Ažuriraj status lajkova kad se promene postovi
    this.cdr.detectChanges();
  }
}



  getPosts(): void {
    this.inputPosts = [];
    if(this.userId){
      this.postService.getPostsByUser(this.userId).subscribe({
        next: (result: PagedResults<Post>) => {
          this.handlePosts(result);
        },
        error: () => {
          console.error(`Failed to load posts for user ${this.userId}.`);
        }
      });
    }
    else{

      this.userService.getFollowingPosts().subscribe({
        next: (result: PagedResults<Post>) => {
          this.handlePosts(result);
        },
        error: () => {
          console.error('Failed to load following posts.');
        }
      });
    }
  }

  private handlePosts(result: PagedResults<Post>) {
    if (!result || !result.results || result.results.length === 0) {
      console.log('No posts to display.');
      this.posts = [];
      return;
    }
    
    const sortedPosts = result.results.sort((a, b) => {
      const dateA = new Date(a.creationDateTime);
      const dateB = new Date(b.creationDateTime);
      return dateB.getTime() - dateA.getTime();
    });

    // Obrada svakog posta, npr. dohvat korisničkog imena
    sortedPosts.forEach(post => {
      post.usernameDisplay = post.user?.username;
    });

    // Ažuriraj postove sa statusom lajka
  //  this.posts = this.updateLikedStatus(sortedPosts);
  }



  likePost(post: Post): void {
    if (!this.currentUser) {
      alert("You need to log in to access this feature.");
      return;
    }

    if (post.isLikedByCurrentUser) {
      // Ako je već lajkovan, uklonite lajk
      this.postService.unlikePost(post.id).subscribe({
        next: (response: string) => {
          console.log(response);
          post.isLikedByCurrentUser = false;
          post.likeCount = (post.likeCount || 1) - 1;
          // Ažuriraj lokalni likedPostIds niz
          this.likedPostIds = this.likedPostIds.filter(id => id !== post.id);
          this.refreshLists.emit();
        },
        error: (err) => {
          console.error(`Failed to unlike post ${post.id} on server.`, err);
        },
      });
    } else {
      // Ako nije lajkovan, dodajte lajk
      this.postService.likePost(post.id).subscribe({
        next: (response: string) => {
          console.log(response);
          post.isLikedByCurrentUser = true;
          post.likeCount = (post.likeCount || 0) + 1;
          // Dodaj novi ID u lokalni niz
          this.likedPostIds.push(post.id);
          this.refreshLists.emit();
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

  toggleLikesList(postId: number): void{
    this.isLikesListOpened = !this.isLikesListOpened;

    this.postService.getLikesFromPost(postId).subscribe({
      next: (response: UserDTO[]) => {
        this.likes$.next(response);
      },
      error: (err) => {
        console.log(`An error occurred while fetching likes from post with id ${postId}, `, err );
      }
    })
  }

  closeLikesList(): void{
    this.isLikesListOpened = false;
  }

trackByPostId(index: number, post: Post): number {
  return post.id;
}


}

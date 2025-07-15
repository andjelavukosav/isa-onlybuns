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
import { CommentService } from '../service/comment.service';
import { CommentDTO } from '../model/commentDto';

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
  commentsMap: { [postId: number]: CommentDTO[] } = {};



  constructor(
    private postService: PostService,
    private userService: UserService,
    private commentService: CommentService,
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

      this.posts.forEach(post => this.loadComments(post.id));

      // Ponovo učitaj lajkovane postove i ažuriraj status
      this.postService.getLikedPostIds().subscribe(ids => {
        this.likedPostIds = ids;
        this.updateLikedStatus(); // Nakon što se dobiju novi ID-jevi, ažuriraj prikaz
        this.cdr.detectChanges(); // Ako koristiš ChangeDetectionStrategy.OnPush
      });
    }
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

submitComment(postId: number): void {
  const trimmedText = this.commentText[postId]?.trim();
  if (!trimmedText) {
    this.snackBar.open('Komentar ne može biti prazan.', 'Zatvori', { duration: 3000 });
    return;
  }

  const commentDTO = {
    text: trimmedText
  };

  this.commentService.addComment(postId, commentDTO).subscribe({
    next: (response) => {
      this.snackBar.open('Comment added!', 'Close', { duration: 3000 });
      this.commentText[postId] = ''; 
      this.posts.forEach(post => this.loadComments(post.id));
    },
    error: (error) => {
      console.error('Error submitting comment:', error);
      if (error.status === 404) {
        this.snackBar.open('The allowed number of comments is 60 per hour.', 'Close', { duration: 3000 });
      }
      else if (error.status === 403) {
        this.snackBar.open('You cannot comment on a post from a user you do not follow!', 'Close', { duration: 3000 });
      } 
      else if (error.status === 429) {
        this.snackBar.open('Too many comments in a short period. Please wait a moment and try again!', 'Close', { duration: 3000 });
      } else {
        this.snackBar.open('Error submitting comment.', 'Close', { duration: 3000 });
      }
    }
  });
  
}

loadComments(postId: number): void {
  this.commentService.getCommentsByPost(postId).subscribe({
    next: (comments) => {
      // sortiraj po datumu DESC
      this.commentsMap[postId] = comments.sort((a, b) => {
        const dateA = a.creationDateTime ? new Date(a.creationDateTime).getTime() : 0;
        const dateB = b.creationDateTime ? new Date(b.creationDateTime).getTime() : 0;
        return dateB - dateA;
      });
      
    },
    error: (err) => {
      console.error('Greška pri učitavanju komentara:', err);
    }
  });
}

toDate(array: any): Date {
  if (Array.isArray(array)) {
    return new Date(array[0], array[1] - 1, array[2], array[3] || 0, array[4] || 0, array[5] || 0, array[6] ? array[6] / 1000000 : 0);
  }
  return new Date(array);
}

}

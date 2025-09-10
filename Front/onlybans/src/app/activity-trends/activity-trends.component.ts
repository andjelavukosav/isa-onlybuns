
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
  currentUser: UserDTO | null = null;
  whoamIResponse = {};
  postsMostPopularLast7Days: Post[] = [];
  postsMostPopularEver: Post[] = [];
  usersMostPopular: UserDTO[] = [];
  activeTab: string = 'all';

  commentText: {[key: number]: string} = {};

  constructor(
    private postService: PostService,
    private userService: UserService,
  ) {}
    
  ngOnInit(): void {
    this.getCurrentUser('http://localhost:8080');
    /*this.reloadDataAllPosts();
    this.reloadDataLastMonthPosts();
    this.reloadData();*/
    this.loadDataForActiveTab(); // Učitaj podatke za inicijalni tab

  }

  loadDataForActiveTab(): void {
    switch (this.activeTab) {
      case 'all':
        this.getPosts();
        break;
      case 'month':
        this.getPostsLastMonth();
        break;
      case 'popular7':
        this.getPostsMostPopular();
        break;
      case 'popularEver':
        this.getPostsMostPopularEver();
        break;
      case 'topUsers':
        this.getUsersMostPopular();  // ← DODAJ OVO
        break;
    }
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
    this.loadDataForActiveTab(); // Učitaj podatke kada se promijeni tab
  }

  getUsersMostPopular(): void {
    this.userService.getTopUsersMostLikes().subscribe({
        next: (result: PagedResults<UserDTO>) => {
          this.usersMostPopular = result.results;
    },
      error: (err) => {
        console.error(err); // Ispiši grešku za potrebe debugovanja
      },
    });
  }
  

  getPostsMostPopularEver(): void {
    this.postService.getPostsMostPopularEver().subscribe({
      next: (result: PagedResults<Post>) => {
        this.postsMostPopularEver = [...result.results];
        this.postsMostPopularEver.forEach(post => post.usernameDisplay = post.user?.username);
      },
      error: () => console.error('Failed to load popular ever posts.')
    });
  }

  getPostsMostPopular(): void {
    this.postService.getPostsMostPopular().subscribe({
      next: (result: PagedResults<Post>) => {
        this.postsMostPopularLast7Days = [...result.results];
        this.postsMostPopularLast7Days.forEach(post => post.usernameDisplay = post.user?.username);
      },
      error: () => console.error('Failed to load popular posts.')
    });
  }

  getPosts(): void {
    this.postService.getPostsWithoutSort().subscribe({
      next: (result: PagedResults<Post>) => {
        this.allPosts = [...result.results];
        this.allPosts.forEach(post => post.usernameDisplay = post.user?.username);
      },
      error: () => console.error('Failed to load all posts.')
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
        this.postsLastMonth = [...result.results];
        this.postsLastMonth.forEach(post => post.usernameDisplay = post.user?.username);
      },
      error: () => console.error('Failed to load last month posts.')
    });
  }


  reloadData() : void{
    this.getPostsMostPopular();
    this.getPostsMostPopularEver();    
  }

  reloadDataAllPosts(): void {
    this.getPosts();
  /*  this.getPostsLastMonth();
    this.getPostsMostPopular();
    this.getPostsMostPopularEver();
    this.getUsersMostPopular();*/
  }

  reloadDataLastMonthPosts():void{
    this.getPostsLastMonth();
  }

  
  
}

import { Component, OnInit } from '@angular/core';
import { Post } from '../model/post.model';
import { PostService } from '../service/post.service';
import { UserService } from '../service/user.service'; // Import UserService
import { PagedResults } from '../model/paged-result.model';

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
    private userService: UserService 
  ) {}

  ngOnInit(): void {
    this.getPosts();
    this.getCurrentUser('http://localhost:8080/api'); 
  }

  getPosts(): void {
    this.postService.getPosts().subscribe({
      next: (result: PagedResults<Post>) => {
        this.post = result.results;
      },
      error: () => {
        console.error('Failed to load posts.');
      }
    });
  }

  getCurrentUser(path:any): void {
    this.userService.getMyInfo()
      .subscribe(res => {
        this.forgeResonseObj(this.whoamIResponse, res, path);
      }, err => {
        this.forgeResonseObj(this.whoamIResponse, err, path);
      });
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
}

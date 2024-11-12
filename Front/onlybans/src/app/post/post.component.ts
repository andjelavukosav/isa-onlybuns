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
  }

  getPosts(): void {
    this.postService.getPosts().subscribe({
      next: (result: PagedResults<Post>) => {
        const sortedPosts = result.results.sort((a, b) => {
          const dateA = new Date(a.creationDateTime);
          const dateB = new Date(b.creationDateTime);
          return dateB.getTime() - dateA.getTime();
        });

        // For each post, get the userName based on userId
        sortedPosts.forEach(post => {

          this.userService.getUserById(post.user?.id || 0).subscribe({
            next: (user) => {
              post.usernameDisplay = user.username;
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

}

import { Component, OnInit } from '@angular/core';
import { Post } from 'src/app/model/post.model';
import { PostService } from 'src/app/service/post.service';

@Component({
  selector: 'app-users-posts',
  templateUrl: './users-posts.component.html',
  styleUrls: ['./users-posts.component.css']
})
export class UsersPostsComponent implements OnInit{

  posts: Post[] = [];

  constructor(private postService: PostService) {}

  ngOnInit(): void {
    this.loadAllPosts();
  }

  loadAllPosts(): void {
    this.postService.getPosts().subscribe({
      next: result => {
        // Sortiranje od najnovijih ka najstarijima
        const sorted = result.results.sort((a, b) => new Date(b.creationDateTime).getTime() - new Date(a.creationDateTime).getTime());

        // Dodaj username
        sorted.forEach(post => {
          post.usernameDisplay = post.user?.username;
        });

        this.posts = sorted;
      },
      error: err => {
        console.error('Failed to load all posts for admin.', err);
      }
    });
  }
}

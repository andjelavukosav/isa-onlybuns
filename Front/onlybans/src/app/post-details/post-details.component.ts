import { Component } from '@angular/core';
import { Post } from '../model/post.model';
import { ActivatedRoute } from '@angular/router';
import { PostService } from '../service/post.service';

@Component({
  selector: 'app-post-details',
  templateUrl: './post-details.component.html',
  styleUrls: ['./post-details.component.css']
})
export class PostDetailsComponent {
  post: Post | undefined;

  constructor(private route: ActivatedRoute, private postService: PostService) {}

  ngOnInit(): void {
    const postId = this.route.snapshot.paramMap.get('id');
    if (postId) {
      this.postService.getPostById(+postId).subscribe(
        (post: Post) => {
          this.post = post;
        },
        (error) =>{
          console.log('Error occured while loading post: ', error);
        }
      
      );
    }
  }
}

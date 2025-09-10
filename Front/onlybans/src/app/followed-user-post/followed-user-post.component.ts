import { Component, OnInit } from '@angular/core';
import { UserService } from '../service';
import { PostService } from '../service/post.service';
import { Post } from '../model/post.model';
import { UserDTO } from '../model/registered-user';
import { PagedResults } from '../model/paged-result.model';

@Component({
  selector: 'app-followed-user-post',
  templateUrl: './followed-user-post.component.html',
  styleUrls: ['./followed-user-post.component.css']
})
export class FollowedUserPostComponent implements OnInit {
  
  followingPosts: Post[] = [];
  currentUser: UserDTO | null = null;
  whoamIResponse = {};


  constructor(
      private userService: UserService,
      private postService: PostService,
    ) {}

    ngOnInit(): void {
      this.getCurrentUser('http://localhost:8080');
      this.getFollowingPosts();
  }
  
    getFollowingPosts(): void {
      if(this.currentUser?.id){
        this.postService.getPostsByUser(this.currentUser.id).subscribe({
          next: (result: PagedResults<Post>) => {
            this.handlePosts(result);
          },
          error: () => {
            console.error(`Failed to load posts for user ${this.currentUser?.id}.`);
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
        this.followingPosts = [];
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
      this.followingPosts = sortedPosts; // ← VAŽNO!
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
    
  
}

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Post } from '../model/post.model';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { TokenInterceptor } from '../interceptor/TokenInterceptor';
import { PagedResults } from '../model/paged-result.model';

@Injectable({
  providedIn: 'root'
})
export class PostService {

  //constructor(private http: HttpClient, private tokenStorage: TokenInterceptor, private router: Router) { }
  constructor(private http: HttpClient) { }

  addPost(createPost: Post, imageFile: File | null): Observable<Post> {
    const formData = new FormData();
    formData.append('description', createPost.description);
  
    if (createPost.location) {
      formData.append('location.latitude', createPost.location.latitude.toString());
      formData.append('location.longitude', createPost.location.longitude.toString());
    }
  
    if (imageFile) {
      formData.append('imageFile', imageFile);
    }
  
    return this.http.post<Post>('http://localhost:8080/api/posts/create', formData);
  }
  
  getPosts(): Observable<PagedResults<Post>> {
    return this.http.get<PagedResults<Post>>('http://localhost:8080/api/' + 'posts/all');
  }

  likePost(postId: number): Observable<any> {
    return this.http.post<any>('http://localhost:8080/api/' + 'posts/' + postId + '/like', {});
  }
  
}

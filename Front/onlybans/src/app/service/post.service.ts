import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Post } from '../model/post.model';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { TokenInterceptor } from '../interceptor/TokenInterceptor';
import { PagedResults } from '../model/paged-result.model';
import { environment } from '../env/enviroment';

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

  getPostById(id: number): Observable<Post> {
    return this.http.get<Post>(`${environment.apiHost}/posts/${id}`);
  }


  likePost(postId: number): Observable<any> {
    return this.http.post<any>('http://localhost:8080/api/' + 'posts/' + postId + '/like', {});
  }

  deletePost(postId: number, userId: number){
    const url = `${environment.apiHost}/posts/${postId}?userId=${userId}`;
    return this.http.delete(url);  
  }

  updatePost(updatedPost: Post, imageFile: File | null): Observable<Post> {
    const formData = new FormData();
    formData.append('id', updatedPost.id.toString());
    formData.append('description', updatedPost.description);
    
    // Dodavanje `likeCount` sa podrazumevanom vrednošću `0` ako je `undefined`
    formData.append('likeCount', (updatedPost.likeCount ?? 0).toString());
    
    if (updatedPost.location) {
      formData.append('location.latitude', updatedPost.location.latitude.toString());
      formData.append('location.longitude', updatedPost.location.longitude.toString());
    }
    
    // Provera `imageFile` i dodavanje u `FormData` ako postoji
    if (imageFile) {
      formData.append('imageFile', imageFile);
    }
    
    formData.append('imagePath', updatedPost.imagePath || '');
    formData.append('creationDateTime', updatedPost.creationDateTime);
    
    // Osiguravanje da `user` i `user.id` postoje pre dodavanja u `FormData`
    formData.append('userId', updatedPost.user?.id?.toString() ?? '0'); // Dodajemo '0' kao podrazumevanu vrednost
    
    const url = `${environment.apiHost}/posts/`;  // URL za backend
    return this.http.put<Post>(url, formData);  // Poziv PUT metode sa PostDTO objektom
  }

}

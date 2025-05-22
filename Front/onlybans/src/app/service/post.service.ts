import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Post } from '../model/post.model';
import { map, Observable } from 'rxjs';
import { Router } from '@angular/router';
import { TokenInterceptor } from '../interceptor/TokenInterceptor';
import { PagedResults } from '../model/paged-result.model';
import { environment } from '../env/enviroment';
import { UserDTO } from '../model/registered-user';

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
      formData.append('latitude', createPost.location.latitude.toString());
      formData.append('longitude', createPost.location.longitude.toString());
    }

    if (imageFile) {
      console.log('Image file: ', imageFile);
      formData.append('imageFile', imageFile);
    }

    return this.http.post<Post>('http://localhost:8080/api/posts/create', formData).pipe(
      map(post => this.convertPostDate(post))
    );
  }

  getPosts(): Observable<PagedResults<Post>> {
    return this.http.get<PagedResults<Post>>('http://localhost:8080/api/' + 'posts/all').pipe(
      map(response => ({
        ...response,
        results: response.results.map(post => this.convertPostDate(post))
      })));
  }

  getPostsWithoutSort(): Observable<PagedResults<Post>> {
    return this.http.get<PagedResults<Post>>('http://localhost:8080/api/' + 'posts/allPosts');
  }

  getPostsLastMonth(): Observable<PagedResults<Post>> {
    return this.http.get<PagedResults<Post>>('http://localhost:8080/api/' + 'posts/allPostsLastMonth');
  }

  getPostsMostPopular(): Observable<PagedResults<Post>> {
    return this.http.get<PagedResults<Post>>('http://localhost:8080/api/' + 'posts/allPostsMostPopular');
  }

  getPostsMostPopularEver(): Observable<PagedResults<Post>> {
    return this.http.get<PagedResults<Post>>('http://localhost:8080/api/' + 'posts/top10PostsMostPopular');
  }
  getPostById(id: number): Observable<Post> {
    return this.http.get<Post>(`${environment.apiHost}/posts/${id}`).pipe(
      map(post => this.convertPostDate(post))
    );
  }

  getPostsByUser(userId: number):Observable<PagedResults<Post>> {
    return this.http.get<PagedResults<Post>>(`${environment.apiHost}/posts/user/${userId}`).pipe(
      map(response =>({
        ...response,
        results: response.results.map(post => this.convertPostDate(post))
      }))
    );
  }


  likePost(postId: number): Observable<any> {
    return this.http.post<any>(`${environment.apiHost}/likes/like-post/${postId}`, {});
  }

  getLikeByPostIdAndUserId(postId: number, userId: number): Observable<boolean> {
    return this.http.get<boolean>(`http://localhost:8080/api/likes/${postId}/${userId}`);
  }

  getLikesByPostId(postId: number): Observable<number> {
    return this.http.get<number>(`http://localhost:8080/api/likes/countLikes/${postId}`);
  }

  unlikePost(postId: number): Observable<any> {
    return this.http.delete<any>(`${environment.apiHost}/likes/unlike-post/${postId}`);
  }

  getLikedPostIds(): Observable<number[]> {
    return this.http.get<number[]>('http://localhost:8080/api/likes/liked-post-ids');
  }

  deletePost(postId: number): Observable<any> {
    return this.http.delete(`${environment.apiHost}/posts/${postId}`, {
      responseType: 'text'  
    });
  }
  

  updatePost(updatedPost: Post, imageFile: File | null): Observable<Post> {
    const formData = new FormData();

    formData.append('description', updatedPost.description);

    if (updatedPost.location) {
      formData.append('location.latitude', updatedPost.location.latitude.toString());
      formData.append('location.longitude', updatedPost.location.longitude.toString());
    }

    if (imageFile) {
      formData.append('imageFile', imageFile);
    }

    return this.http.put<Post>(`${environment.apiHost}/posts/update/${updatedPost.id}`, formData).pipe(
      map(post => this.convertPostDate(post))
    );
  }


  getNearbyPosts(latitude: number, longitude: number, radius: number = 100000) {
    return this.http.get<any>(`http://localhost:8080/api/posts/nearby?latitude=${latitude}&longitude=${longitude}&radius=${radius}`);
  }

  getLikesFromPost(postId: number): Observable<UserDTO[]> {
    return this.http.get<UserDTO[]>(`${environment.apiHost}/likes/post/${postId}`);
  }

  
   convertPostDate(post: any): Post {
    if (Array.isArray(post.creationDateTime)) {
      const arr = post.creationDateTime;
      post.creationDateTime = new Date(
        arr[0],
        arr[1] - 1,
        arr[2],
        arr[3],
        arr[4],
        arr[5],
        Math.floor(arr[6] / 1000000)
      );
    }
    return post;
  }
  


}

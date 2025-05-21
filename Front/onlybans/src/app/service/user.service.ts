import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {ConfigService} from './config.service';
import {map} from 'rxjs/operators';
import { Observable } from 'rxjs';
import { UserDTO, UserFollowStateDTO, UserSearchCriteria } from '../model/registered-user';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../env/enviroment';
import { Post } from '../model/post.model';
import { Page } from '../model/pagination.model';
import { PagedResults } from '../model/paged-result.model';
import { PostService } from './post.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  currentUser!:any;

  constructor(
    private apiService: ApiService,
    private postService: PostService,
    private config: ConfigService,
    private http: HttpClient
  ) {
  }

  getMyInfo() {
    return this.apiService.get(this.config.whoami_url)
      .pipe(map(user => {
        this.currentUser = user;
        return user;
      }));
  }

  getAll() {
    return this.apiService.get(this.config.users_url);
  }


  getUsersPage(page: number, size: number, sortBy: string, direction: string): Observable<Page<UserDTO>>{
    return this.http.get<Page<UserDTO>>(`${environment.apiHost}/users?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`);
  }

  searchUsers(criteria: UserSearchCriteria, page: number, size: number, sortBy: string, direction: string): Observable<Page<UserDTO>>{
    return this.http.post<Page<UserDTO>>(`${environment.apiHost}/users/search?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`, criteria);
  }

  getUserById(userId: number): Observable<UserDTO> {
    return this.http.get<UserDTO>(environment.apiHost + `/users/${userId}`);
  }


  updateUserData(userId: number, updatedUser: UserDTO) {
    return this.http.put(environment.apiHost + `/users/update/${userId}`,updatedUser, {
      responseType: 'text'  // Očekuje se tekstualni odgovor umesto JSON
    });
  }

  getUserPostCount(userId: number): Observable<number> {
    return this.http.get<number>(environment.apiHost + `/posts/user/${userId}/count`);
  }

  getTopUsersMostLikes(): Observable<PagedResults<UserDTO>> {
    return this.http.get<PagedResults<UserDTO>>(environment.apiHost + '/likes/top10UsersMostLikes');
  }
  updatePassword(userId: number, newPassword: string): Observable<{ token: string }> {
    return this.http.put<{ token: string }>(
      `${environment.apiHost}/users/update-password/${userId}`,
      { newPassword }  // šalješ kao objekat, ne kao plain string
    );
  }



  verifyPassword(userId: number, currentPassword: string): Observable<boolean> {
    return this.http.post<boolean>(`${environment.apiHost}/users/verify-password`, {
      userId: userId,
      currentPassword: currentPassword
    });
  }


  getUserLocation(userId: number): Observable<{ latitude: number, longitude: number }> {
    return this.http.get<{ latitude: number, longitude: number }>(`${environment.apiHost}/users/location/${userId}`);
  }

  getAllLocations(): Observable<any> {
    return this.http.get<any>(`${environment.apiHost}/users/asylums-veterinarians`);
  }
  
  searchUsersByUsername(username: string): Observable<UserDTO[]>{
    return this.http.get<UserDTO[]>(environment.apiHost +  `/users/searchBy?username=${username}`);
  }

  followUser(followerId: number, followedId: number): Observable<UserFollowStateDTO>{
    const body = { followerId, followedId}
    return this.http.post<UserFollowStateDTO>(`${environment.apiHost}/users/follow`, body);
  }

  unfollowUser(followerId: number, followedId: number): Observable<UserFollowStateDTO>{
    const body = { followerId, followedId}
    return this.http.post<UserFollowStateDTO>(environment.apiHost + `/users/unfollow`, body);
  }

  checkIfFollowing(followerId: number, followedId: number): Observable<boolean> {
    return this.http.get<any>(`${environment.apiHost}/users/${followerId}/is-following/${followedId}`)
      .pipe(
        map(response => response.isFollowing) // Ekstraktujemo samo 'isFollowing' vrednost
      );
  }

  getFollowingPosts(): Observable<PagedResults<Post>>{
    return this.http.get<PagedResults<Post>>(`${environment.apiHost}/users/following-posts`).pipe(
      map(response => ({
        ...response,
        results: response.results.map(post => this.postService.convertPostDate(post))
      })
      )
    );
  }

  getUserFollowing(userId: number): Observable<PagedResults<UserDTO>>{
    return this.http.get<PagedResults<UserDTO>>(`${environment.apiHost}/users/${userId}/following`);
  }

  getUserFollowers(userId: number): Observable<PagedResults<UserDTO>>{
    return this.http.get<PagedResults<UserDTO>>(`${environment.apiHost}/users/${userId}/followers`);
  }


  

}

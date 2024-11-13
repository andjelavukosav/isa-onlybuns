import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {ConfigService} from './config.service';
import {map} from 'rxjs/operators';
import { Observable } from 'rxjs';
import { UserDTO } from '../model/registered-user';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../env/enviroment';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  currentUser!:any;

  constructor(
    private apiService: ApiService,
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

  getAllRegisteredUsers(): Observable<UserDTO[]> {
    return this.http.get<UserDTO[]>(environment.apiHost + '/users');
  }

  searchUsers(firstName: string, lastName: string, email: string, sortBy: string, sortDirection: string ,minPosts?: number | null, maxPosts?: number | null): Observable<UserDTO[]> {
    let params = new HttpParams();
    if(firstName) params = params.set('firstName', firstName);
    if(lastName) params = params.set('lastName', lastName);
    if(email) params = params.set('email', email);


    if (minPosts !== null && minPosts !== undefined) {
      params = params.set('minPosts', minPosts.toString()); // Pretvaramo broj u string
    }

    if (maxPosts !== null && maxPosts !== undefined) {
      params = params.set('maxPosts', maxPosts.toString()); // Pretvaramo broj u string
    }

    if (sortBy) {
      params = params.set('sortBy', sortBy);
    }
    if (sortDirection) {
      params = params.set('sortDirection', sortDirection);
    }

    return this.http.get<UserDTO[]>(environment.apiHost + '/users/search', { params});
  }

  getUserById(userId: number): Observable<UserDTO> {
    return this.http.get<UserDTO>(environment.apiHost + `/users/${userId}`);
  }

  getUserPostCount(userId: number): Observable<number> {
    return this.http.get<number>(environment.apiHost + `/posts/user/${userId}/count`);
  }

}

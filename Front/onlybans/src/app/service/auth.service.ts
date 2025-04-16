import { Injectable } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { ApiService } from './api.service';
import { UserService } from './user.service';
import { ConfigService } from './config.service';
import { catchError, map } from 'rxjs/operators';
import { Router } from '@angular/router';
import { of } from 'rxjs/internal/observable/of';
import { BehaviorSubject, Observable } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { AuthUser } from '../model/registered-user';
import { JwtHelperService } from '@auth0/angular-jwt';
@Injectable()
export class AuthService {

  constructor(
    private apiService: ApiService,
    private userService: UserService,
    private config: ConfigService,
    private router: Router
  ) {
  }

  private access_token : string | null = null;
  user$ = new BehaviorSubject<AuthUser | null>(null);

  login(user:any) {
    const loginHeaders = new HttpHeaders({
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    });
    // const body = `username=${user.username}&password=${user.password}`;
    const body = {
      'email': user.email,
      'password': user.password
    };
    return this.apiService.post(this.config.login_url, JSON.stringify(body), loginHeaders)
      .pipe(map((res) => {
        const token = res.body?.accessToken;
        if (!token) {
          throw new Error('No access token received');
        }
        console.log('Login success');
        this.access_token = token;
        localStorage.setItem("jwt", token);
        this.setUser(token);
      }));
  }

  signup(user:any) {
    const signupHeaders = new HttpHeaders({
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    });
    return this.apiService.post(this.config.signup_url, JSON.stringify(user), signupHeaders)
      .pipe(map(() => {
        console.log('Sign up success');
      }));
  }

  logout() {
    localStorage.removeItem("jwt");
    this.access_token = null;
    this.user$.next(null);
    this.router.navigate(['/login']);
  }

  tokenIsPresent() {
    return this.access_token != undefined && this.access_token != null;
  }

  getToken() {
    return this.access_token;
  }

  getCurrentUser(): any {
    const token = this.getToken();
    if (token) {
      const decodedToken = jwtDecode(token);
      return decodedToken; 
    }
    return null;
  }

  
  private decodeToken(token: string): AuthUser | null{
    try{
      const jwtHelperService = new JwtHelperService();
      const decodedToken = jwtHelperService.decodeToken(token);

      let roles = decodedToken['roles'] || [];
      if(typeof roles === 'string'){
        roles = [roles];
      }

      const user: AuthUser = {
        id: decodedToken.id,
        username: decodedToken.username,
        roles: roles
      };
      return user;
    }catch(err){
      console.log('Error during decoding token: ', err);
      return null;
    }
  }

  private setUser(token: string): void{
    const decodedUser = this.decodeToken(token);
    this.user$.next(decodedUser);
  }
}

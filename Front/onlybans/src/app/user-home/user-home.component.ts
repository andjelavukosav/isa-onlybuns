import { Component } from '@angular/core';
import { AuthService, UserService } from '../service';
import { AuthUser, UserDTO } from '../model/registered-user';
import { catchError, debounceTime, filter, of, Subject, switchMap } from 'rxjs';
import { query } from '@angular/animations';
import { Router } from '@angular/router';

@Component({
  selector: 'app-user-home',
  templateUrl: './user-home.component.html',
  styleUrls: ['./user-home.component.css']
})
export class UserHomeComponent {
  searchQuery: string = '';
  searchResults: UserDTO[] = [];
  searchSubject = new Subject<string>(); //prati promjene u polju
  errorMessage: string = '';
  fleg: boolean = false;

  userId: number | null = null;
  user: AuthUser | null = null;
  whoamIResponse = {};

  constructor(private userService: UserService,
            private router: Router,
            private authService: AuthService){}

  ngOnInit() {

    this.authService.user$.subscribe({
      next: (user) => {
        if(user){
          this.user = user;
          this.userId = user.id;
          console.log('Login user: ', user); 
        }
      },
      error: (err) => {
        console.log('An error occurred during logging in.');
      }
    })
    this.searchSubject.pipe(
      debounceTime(300),
      filter(query => query.length >= 1),
      switchMap(query =>
        this.userService.searchUsersByUsername(query).pipe(
          catchError(err => {
            if (err.status === 401) {
              this.errorMessage = "You are not authorized to perform this action.";
            } else if (err.status === 403) {
              this.errorMessage = "You don't have permission to search users.";
            } else if (err.status === 500) {
              this.errorMessage = "Server error. Please try again later.";
            } else {
              this.errorMessage = "Failed to load users.";
            }
            console.log("Failed to load users: ", err);
            this.fleg = false;
            return of([]);
          })
        )
      )
    )
    .subscribe(
      results => {
        this.searchResults = results;
        this.errorMessage = '';
        this.fleg = true;
        console.log('searchResults:', this.searchResults);
        console.log('errorMessage:', this.errorMessage);


    });


  }

  searchUsers() {
    this.searchSubject.next(this.searchQuery); // Svaki unos se šalje kroz Subject
  }


}

import { Component, OnInit, ViewChild } from '@angular/core';
import { UserService } from '../service/user.service';
import { AuthService } from '../service/auth.service';
import { BehaviorSubject } from 'rxjs';
import { AuthUser } from '../model/registered-user';
import { MatMenuTrigger } from '@angular/material/menu';
@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  fullName$ = new BehaviorSubject<string>('');
  user$ = new BehaviorSubject<AuthUser | null>(null);
  menuOpen = false;

  @ViewChild(MatMenuTrigger) menuTrigger!: MatMenuTrigger;


  constructor( private userService: UserService, private authService: AuthService) { }

  ngOnInit() {

    this.authService.user$.subscribe({
      next: (user: AuthUser | null) => {
        this.user$.next(user);
        console.log(user);
       // this.userName();
      }
    })
  }
  ngAfterViewInit() {
    // Ovdje možemo proveriti da li je menuTrigger inicijalizovan
    console.log('menuTrigger initialized:', this.menuTrigger);
  }

  isAdmin(): boolean{
    return this.user$.value?.roles.includes('ROLE_ADMIN') ?? false;
  }

  hasSignedIn() {
    return !!this.userService.currentUser;
  }

  userName(){
    const user = this.userService.currentUser;
    this.fullName$.next(user.firstName + ' ' + user.lastName);
  } 
  logout() {
    this.authService.logout();
  }

  
  
}

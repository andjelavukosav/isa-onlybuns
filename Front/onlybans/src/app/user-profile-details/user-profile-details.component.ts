import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../service/user.service';
import { AuthUser, UserDTO, UserFollowStateDTO } from '../model/registered-user'; // Import UserDTO if needed
 import { AuthService } from '../service';
 import { BehaviorSubject } from 'rxjs';
 import { PagedResults } from '../model/paged-result.model';
 import { Post } from '../model/post.model';
 import { MatSnackBar } from '@angular/material/snack-bar';


@Component({
  selector: 'app-user-profile-details',
  templateUrl: './user-profile-details.component.html',
  styleUrls: ['./user-profile-details.component.css']
})
export class UserProfileDetailsComponent implements OnChanges{
  @Input() userId: number | null = null; //id korisnika ciji se profil posjecuje
  @Input() user: UserDTO | null = null;
   userProfile$ : BehaviorSubject<UserDTO | null> = new BehaviorSubject<UserDTO | null>(null);
  @Input() loggedInUser: AuthUser | null = null; 
   isFollowing$ = new BehaviorSubject<boolean>(false);
   isNotLoggedInUserProfile: boolean = true;
   dropdownOpen: boolean = false;
   
   followers$ = new BehaviorSubject<UserDTO[]>([]); 
   following$ = new BehaviorSubject<UserDTO[]>([]); 
   followersCount$ = new BehaviorSubject<number>(0); 
   followingCount$ = new BehaviorSubject<number>(0); 
 
   isFollowersListOpened: boolean = false;
   isFollowingListOpened: boolean = false;
 
   userProfileId$ = new BehaviorSubject<number>(0);

   constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private snackBar: MatSnackBar
  ) {}


  ngOnChanges(changes: SimpleChanges): void {
    if (changes['userId'] && this.userId) {
      this.loadUserData(this.userId);
    }

    if (changes['user'] && this.user) {
      this.userProfile$.next(this.user);
    }
  }

  private loadUserData(userId: number): void {
    this.userProfileId$.next(userId);

    if (this.user) {
      this.userProfile$.next(this.user);
    }

    this.isFollowersListOpened = false;
    this.isFollowingListOpened = false;
    this.dropdownOpen = false;

    console.log('Logged in user: ', this.loggedInUser)
  
    if (this.loggedInUser && userId && this.loggedInUser.id !== userId && !this.loggedInUser.roles.includes('ROLE_ADMIN')) {
      this.isNotLoggedInUserProfile = true;
  
      this.userService.checkIfFollowing(this.loggedInUser.id, userId).subscribe({
        next: (isFollowing) => this.isFollowing$.next(isFollowing),
        error: (err) => console.error("Error checking if user is following:", err)
      });
    } else {
      this.isNotLoggedInUserProfile = false;
    }
  }
  

  toggleFollow(): void{
    if(this.loggedInUser && this.userId){
      if(this.isFollowing$.value){
        this.dropdownOpen = !this.dropdownOpen;
      }
      else{
      
        this.userService.followUser(this.loggedInUser?.id, this.userId).subscribe({
          next: (response: UserFollowStateDTO) => {
            this.isFollowing$.next(true);
            if(this.user){
              this.user.followersCount = response.followersCount;
              this.user.followingCount = response.followingCount;
              this.userProfile$.next(this.user);
            }
            
            console.log('Successfully follow.')
          },
          error: (err) => {
            console.log('An error occurred: ', err);
            if(err.status === 429){
              this.showToast('warning', 'You have exceeded the limit of follows per minute. Please try again later.');
            }
          }
        });
      }
    }
  }


  unfollow(): void {
    if(this.loggedInUser && this.userId){
      this.dropdownOpen = false; 
      this.userService.unfollowUser(this.loggedInUser?.id, this.userId).subscribe({
        next: (response: UserFollowStateDTO) => {
          this.isFollowing$.next(false);
          if(this.user){
            this.user.followersCount = response.followersCount;
            this.user.followingCount = response.followingCount;

            this.userProfile$.next(this.user);
          }
          console.log('Successfully unfollow.');
        },
        error: (err) => {
          console.log('An error occured: ', err);
        }
      })
    }
    
  }

  toggleFollowersList() {
    this.isFollowersListOpened = !this.isFollowersListOpened;

    if(this.isFollowersListOpened){

      if(this.user){
        
        this.followers$.next([]);

        this.userService.getUserFollowers(this.user.id).subscribe({
          next: (response: PagedResults<UserDTO>) => {
            if (response && response.results) {
              this.followersCount$.next(response.totalCount);
              this.followers$.next(response.results);
            }
          },
          error: (err) => {
            console.log(`Failed to load followers for user ID ${this.userId}: `, err);
          }
        });
      
      }
    }
  }

  toggleFollowingList() {
    this.isFollowingListOpened = !this.isFollowingListOpened;

    if(this.isFollowingListOpened){
      if(this.user){

        this.following$.next([]);

        this.userService.getUserFollowing(this.user.id).subscribe({
          next: (response: PagedResults<UserDTO>) => {
            if (response && response.results) {
              this.followingCount$.next(response.totalCount);
              this.following$.next(response.results);
            }
          },
          error: (err) => {
            console.log(`Failed to load following for user ID ${this.userId}: `, err);
          }
        });
      }
    }

  }

  showToast(type: string, message: string): void {
    this.snackBar.open(message, type, {
      duration: 3000, // Trajanje obavještenja
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: type, 
    });
  }

  closeFollowersList() {
    this.isFollowersListOpened = false;
  }
  
  closeFollowingList() {
    this.isFollowingListOpened = false;
  }
  
}


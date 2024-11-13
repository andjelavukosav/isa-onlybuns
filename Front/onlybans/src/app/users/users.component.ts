import { Component } from '@angular/core';
import { UserDTO } from '../model/registered-user';
import { UserService } from '../service';
import { min } from 'date-fns';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css']
})

export class UsersComponent {

  registeredUsers: UserDTO[] = [];

  searchCriteria = {
    firstName: '',
    lastName: '',
    email: '',
    minPosts: null,
    maxPosts: null,
    sortBy: 'email',
    sortDirection: 'asc'
  };

  
  constructor(private userService: UserService){}

  ngOnInit(): void{
    this.userService.getAllRegisteredUsers().subscribe(
      (data: UserDTO[]) => {
        this.registeredUsers = data;
        this.registeredUsers.forEach(user => {
          this.getPostCountForUser(user.id);
        }
  
        );
      },
      (error) => {
        console.error('Greška prilikom učitavanja korisnika:', error);

      }
    )
  }

  searchUsers(): void{
    const {firstName, lastName, email, minPosts, maxPosts, sortBy, sortDirection} = this.searchCriteria;

    let minPost: number | null = null;
    let maxPost: number | null = null;
    
    // Provera da li su minPosts i maxPosts uneti i da li su validni
    if (this.searchCriteria.minPosts != null && this.searchCriteria.minPosts >= 0) {
        minPost = this.searchCriteria.minPosts;
    }
    
    if (this.searchCriteria.maxPosts != null && this.searchCriteria.maxPosts >= 0) {
        maxPost = this.searchCriteria.maxPosts;
    }
    
    // Provera validnosti unetog opsega (ako su oba unesena)
    if (minPost !== null && maxPost !== null && minPost > maxPost) {
        console.error('Min post count cannot be lower than max post count.');
        return;
    }
      


    this.userService.searchUsers(firstName, lastName, email, sortBy, sortDirection, minPosts, maxPosts).subscribe(
      (data: UserDTO[]) => {
        this.registeredUsers = data; 

      },
      (error) => {
        console.error('Error during searching registered users:', error);
      }
    );
  }


  getPostCountForUser(userId: number): void {
    this.userService.getUserPostCount(userId).subscribe(
      (postCount: number) => {
        const user = this.registeredUsers.find( u => u.id === userId );
        if(user){
          user.postsCount = postCount;
        }
      },
      (error) => {
        console.log('Error while loading posts for user: ', error);
      }
    )
  }


}

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
    this.userService.getAllUsersForAdmin().subscribe(
      (data: UserDTO[]) => {
        this.registeredUsers = data;
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
        console.error('Greška: Minimalni broj postova ne može biti veći od maksimalnog.');
        return;  // Zaustavlja dalji tok ako opseg nije validan
    }
      


    this.userService.searchUsers(firstName, lastName, email, sortBy, sortDirection, minPosts, maxPosts).subscribe(
      (data: UserDTO[]) => {
        this.registeredUsers = data;  // Ažuriranje liste sa rezultatima pretrage
      },
      (error) => {
        console.error('Greška prilikom pretrage korisnika:', error);
      }
    );
  }


}

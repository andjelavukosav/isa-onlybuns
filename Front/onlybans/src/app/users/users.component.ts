import { Component } from '@angular/core';
import { UserDTO } from '../model/registered-user';
import { UserService } from '../service';

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
    email: ''
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
    const {firstName, lastName, email} = this.searchCriteria;

    this.userService.searchUsers(firstName, lastName, email).subscribe(
      (data: UserDTO[]) => {
        this.registeredUsers = data;  // Ažuriranje liste sa rezultatima pretrage
      },
      (error) => {
        console.error('Greška prilikom pretrage korisnika:', error);
      }
    );
  }


}

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
}

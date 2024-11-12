import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../service/user.service';
import { UserDTO } from '../model/registered-user'; // Import UserDTO if needed

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {
  userId: number | null = null;
  user: UserDTO | null = null; // Define user variable to store user data

  constructor(
    private route: ActivatedRoute,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    // Retrieve the userId from the route parameters
    this.userId = Number(this.route.snapshot.paramMap.get('userId'));

    // If userId is valid, fetch user details
    if (this.userId) {
      this.userService.getUserById(this.userId).subscribe({
        next: (user) => this.user = user,
        error: (err) => console.error(`Failed to load user with ID ${this.userId}`, err)
      });
    }
  }
}

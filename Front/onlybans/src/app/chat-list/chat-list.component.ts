import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AuthUser, UserDTO } from '../model/registered-user';
import { UserService } from '../service';

@Component({
  selector: 'app-chat-list',
  templateUrl: './chat-list.component.html',
  styleUrls: ['./chat-list.component.css']
})
export class ChatListComponent implements OnInit{
  @Input() currentUser: AuthUser | null = null;
  @Output() userSelected = new EventEmitter<UserDTO>();
  @Output() followingLoaded =  new EventEmitter<UserDTO[]>();

  users: UserDTO[] = [];

  constructor(private userService: UserService) {}

  ngOnInit(): void{
    if(this.currentUser){
      this.userService.getUserFollowing(this.currentUser.id).subscribe(users => {
        this.users = users.results;
        this.followingLoaded.emit(users.results);
      });
    }
    else{
      console.log("Something went wrong while loading the current user.");
    }
  }

  selectUser(user: UserDTO): void{
    this.userSelected.emit(user);
  }
}

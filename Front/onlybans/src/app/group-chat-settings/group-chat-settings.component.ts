import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AuthUser, UserDTO } from '../model/registered-user';
import { GroupChatMemberDTO, GroupChatRoomDTO } from '../model/group-chat';
import { ChatService } from '../service/chat.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-group-chat-settings',
  templateUrl: './group-chat-settings.component.html',
  styleUrls: ['./group-chat-settings.component.css']
})
export class GroupChatSettingsComponent implements OnInit {

  @Input() currentUser!: AuthUser | null;
  @Input() following: UserDTO[] = [];
  @Input() groupChat!: GroupChatRoomDTO | null;

  @Output() closeModal = new EventEmitter<void>();
  @Output() membersUpdated = new EventEmitter<GroupChatRoomDTO>();

  usersToAdd: UserDTO[] = [];
  usersToRemove: GroupChatMemberDTO[] = [];

  isOwner: boolean = false;

  selectedToAdd = new Set<number>();
  selectedToRemove = new Set<number>();

 selectedTab: 'add' | 'remove' | null = null;


  constructor(private chatService: ChatService, private router: Router){}

  ngOnInit(): void {
  if (!this.groupChat || !this.currentUser) return;

  const chatGroupMemberIds = this.groupChat.members.map(m => m.id);
  this.isOwner = this.currentUser.username === this.groupChat.ownerUsername;
  this.usersToRemove = this.groupChat.members.filter(m => m.id !== this.currentUser!.id);
  this.usersToAdd = this.following.filter(user => !chatGroupMemberIds.includes(user.id));
  }


  toggleAdd(userId: number, event: Event): void{
    (event.target as HTMLInputElement).checked
    ? this.selectedToAdd.add(userId)
    : this.selectedToAdd.delete(userId);
  } 
  
  toggleRemove(memberId: number, event: Event): void{
    (event.target as HTMLInputElement).checked
    ? this.selectedToRemove.add(memberId)
    :this.selectedToRemove.delete(memberId);
  }

  addSelectedUser(): void{
    const membersToAdd = Array.from(this.selectedToAdd);
    if(membersToAdd.length === 0 || !this.groupChat) return; 

    this.chatService.addMembersToGroupChat(this.groupChat.id, membersToAdd).subscribe({
    next: updatedGroup => {
      this.membersUpdated.emit(updatedGroup);
      this.selectedToAdd.clear();
      alert('Članovi su uspešno dodati.');
    },
    error: err => {
      console.error('Greška prilikom dodavanja članova', err);
    }
  });
  
  }

  removeSelectedMember(): void{
    const memberToRemove = Array.from(this.selectedToRemove);
    if(memberToRemove.length === 0 || !this.groupChat) return;

    this.chatService.removeMembersFromGroupChat(this.groupChat.id, memberToRemove).subscribe({
      next: updatedGroup => {
        this.membersUpdated.emit(updatedGroup);
        this.selectedToRemove.clear();
        alert('Clanovi sy uklonjeni');
      },
      error: err => {
        console.error('Greška prilikom dodavanja članova', err);
      }
    });
  }

  cancel(): void {
    this.selectedTab = null;
  }

  saveAdd(): void {
    // pozovi backend za dodavanje članova
    this.addSelectedUser();
    console.log('Saving added members:', Array.from(this.selectedToAdd));
    this.cancel();
  }

  saveRemove(): void {
    // pozovi backend za uklanjanje članova
    this.removeSelectedMember();
    console.log('Saving removed members:', Array.from(this.selectedToRemove));
    this.cancel();
  }

  goToUserProfile(userId: number): void {
    this.router.navigate(['/profile', userId]);
  }
}

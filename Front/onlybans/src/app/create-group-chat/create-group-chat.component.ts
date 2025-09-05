import { Component, EventEmitter, Input, Output } from '@angular/core';
import { AuthUser, UserDTO } from '../model/registered-user';

@Component({
  selector: 'app-create-group-chat',
  templateUrl: './create-group-chat.component.html',
  styleUrls: ['./create-group-chat.component.css']
})
export class CreateGroupChatComponent {

  @Input() currentUser: AuthUser | null = null;
  @Input() following: UserDTO[] = [];

  @Output() closeModal = new EventEmitter<void>();
  @Output() groupCreated = new EventEmitter<{ groupName: string, memberIds: number[] }>(); 

  groupName: string = '';
  selectedUserIds: Set<number> = new Set<number>();

  toggleUserSelection(userId: number): void{
    if(this.selectedUserIds.has(userId)){
      this.selectedUserIds.delete(userId);
    }
    else{
      this.selectedUserIds.add(userId);
    }
  }

  confirm(): void{
    if(!this.groupName.trim()){
      alert('You have to enter the group name.');
      return;
    }
    if(this.selectedUserIds.size===0){
      alert('You have to add the group member.');
      return;
    }
    
    this.groupCreated.emit({
      groupName: this.groupName.trim(),
      memberIds: Array.from(this.selectedUserIds)
    });

    this.closeModal.emit();
  }

  cancel(): void{
    this.selectedUserIds.clear();
    this.closeModal.emit();
  }
}

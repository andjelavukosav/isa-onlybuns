import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, SimpleChange, SimpleChanges } from '@angular/core';
import { AuthUser } from '../model/registered-user';
import { GroupChatRoomDTO } from '../model/group-chat';
import { ChatService } from '../service/chat.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-group-chat-list',
  templateUrl: './group-chat-list.component.html',
  styleUrls: ['./group-chat-list.component.css']
})
export class GroupChatListComponent implements OnInit, OnDestroy{

  @Input() currentUser: AuthUser | null = null;
  @Output() groupChats = new EventEmitter<GroupChatRoomDTO[]>();
  @Output() groupChatSelected = new EventEmitter<GroupChatRoomDTO>();
  groupChatsList: GroupChatRoomDTO[] = [];

  private groupChatSub?: Subscription;

  constructor(private chatService: ChatService) {}

  ngOnInit(): void {
    if (this.currentUser) {
      console.log('Korisnik je ulogovan Group Chat List Komponenta')
      this.groupChatSub = this.chatService.subscribeToGroupChatAdditions(this.currentUser.username).subscribe({
        next: (newGroupChat: GroupChatRoomDTO) => {
          newGroupChat.createdAt = this.chatService.parseTimestamp(newGroupChat.createdAt);
          this.groupChatsList.unshift(newGroupChat);
        },
        error: (err: any) =>{
          console.error('Greška pri obradi nove grupe:', err);
        }
      })
      this.loadGroupChats();
    }
  }

  ngOnDestroy(): void{
    this.groupChatSub?.unsubscribe();
  }

  initSubscriptions(): void{
    this.loadGroupChats();

    //Pretplata na behaviorSubject koji emituje nove grupne chatove
    this.chatService.getNewGroupChat().subscribe({
      next : (newGroupChat) => {
        if(newGroupChat){
          newGroupChat.createdAt = this.chatService.parseTimestamp(newGroupChat.createdAt);
          this.groupChatsList.unshift(newGroupChat);
        }
      
    },
    error: (err) => {
    }
    })
  }

  loadGroupChats(): void{
    this.chatService.loadGroupChatsForUser().subscribe({
      next: (groupChats: GroupChatRoomDTO[]) => {
        const parsedChats = groupChats.map(groupChat => ({
          ...groupChat,
          createdAt: this.chatService.parseTimestamp(groupChat.createdAt)
        }));
        
        this.groupChatsList = parsedChats;
        this.groupChats.emit(groupChats);
      },
      error: (err) =>{
        console.log('An error occured trying to load the group chats, ', err);
      }
    });
  }

  selectGroupChat(groupChat: GroupChatRoomDTO){
    this.groupChatSelected.emit(groupChat);
  }

}

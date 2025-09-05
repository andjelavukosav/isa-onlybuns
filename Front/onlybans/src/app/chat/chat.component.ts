import { Component, Input, OnInit } from '@angular/core';
import { ChatMessage } from '../model/message';
import { AuthUser, UserDTO } from '../model/registered-user';
import { ChatService } from '../service/chat.service';
import { AuthService } from '../service';
import { BehaviorSubject, timestamp } from 'rxjs';
import { GroupChatRoomDTO } from '../model/group-chat';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit{

  messages: ChatMessage[] = [];
  newMessageText: string = '';
  currentUser: AuthUser | null = null;
  recipient: UserDTO | null = null;
  
  currentPage: number = 0;
  pageSize: number = 10;
  hasMoreMessages: boolean = true;

  following: UserDTO[] = [];
  isGroupModalOpen : boolean = false;

  groupChatsList: GroupChatRoomDTO[] = [];
  groupChatSelected: GroupChatRoomDTO | null = null;
  groupChatSubscription: any; // Čuvaj referencu na pretplatu


  isGroupSettingsModalOpen : boolean = false;

  constructor(private chatService: ChatService,
    private authService: AuthService
  ) {}

  async ngOnInit(): Promise<void> {
    this.authService.user$.subscribe({
      next: (user) => {
        this.currentUser = user;
      }
    });

    try{
      await this.chatService.connect(); 
      console.log("The connection is established and subscription to the destination is completed.");
      this.chatService.getMessages().subscribe(msg => {
        if(!msg || !this.currentUser) return;

        //salji odredjenom chatu
        //privatna poruka
        if (this.recipient && 
          ((msg.senderId == this.recipient.username && msg.recipientId == this.currentUser?.username)
            ||
            (msg.senderId == this.currentUser?.username && msg.recipientId == this.recipient.username)))
        {
          this.messages.push(msg);
        }

        //grupna poruka
        if(this.groupChatSelected && msg.chatRoomId === this.groupChatSelected.id){
          this.messages.push(msg);
        }

      });
      
    }
    catch(err){
      alert(err);
    }

  }

  send(inputRef?: HTMLInputElement): void {
    if (!this.newMessageText.trim()) return;
    
    if(this.currentUser){
      if(this.recipient?.username){
        const msg: ChatMessage = {
          message: this.newMessageText,
          senderId: this.currentUser.username,
          recipientId: this.recipient.username,
        };

        this.chatService.sendMessage(msg);
      }
       
      else if(this.groupChatSelected){
        const groupMsg: ChatMessage = {
          message: this.newMessageText,
          senderId: this.currentUser.username,
          chatRoomId: this.groupChatSelected.id,
        };
        this.chatService.sendGroupMessage(groupMsg);
      }

      this.newMessageText = '';
      if (inputRef){
        inputRef.focus();
      }
    
    }
  }

  onUserSelected(user: UserDTO): void{
    this.recipient = user;
    console.log("Onaj kome salje je: ", this.recipient.username);
    this.groupChatSelected = null; //iskljuci grupni chat koji je otvoren
    this.messages = [];
    this.currentPage = 0;           // resetuj broj stranice
    this.hasMoreMessages = true;   // resetuj flag
    this.loadMessageHistory();     // opcionalno odmah učitaj poruke
  }

  loadMessageHistory(): void {
    if(this.currentUser){
      if(this.currentUser && this.recipient){
        const user1Id = this.currentUser.id;
        const user2Id = this.recipient.id;

        this.loadHistoryFromPrivateChat(user1Id, user2Id);
      }
      else if(this.groupChatSelected){
        this.loadHistoryFromGroupChat(this.groupChatSelected.id);
      }
      
    }
  }

  loadHistoryFromPrivateChat(user1Id: number, user2Id: number): void{
     
    this.chatService.getPrivateChatHistory(user1Id, user2Id, this.currentPage, this.pageSize)
        .subscribe({
          next: (response) => {
            if (!response || !response.content || response.content.length === 0) {
              this.hasMoreMessages = false;
              return;
            }

            const mappedMessages = response.content.map(msg => ({
              ...msg,
              timestamp: this.chatService.parseTimestamp(msg.timestamp)
            }));
            // dodajemo na početak niza jer su poruke stigle obrnutim redosledom (DESC)
            this.messages = [...mappedMessages.reverse(), ...this.messages];
            this.currentPage++;
          },
          error: (err) => {
            console.log('An error occured while loading the history: ', err);
            this.hasMoreMessages = false;
          }
        });

  }

  loadHistoryFromGroupChat(groupChatId: number): void{
      //const boundaryTimestamp = this.getBoundaryTimestamp();

    this.chatService.getHistoryFromGroupChat(groupChatId, this.currentPage, this.pageSize)
      .subscribe({
        next: (response) =>{
           if (!response || !response.content || response.content.length === 0) {
              this.hasMoreMessages = false;
              return;
            }

            const mappedMessages = response.content.map(msg => ({
              ...msg,
              timestamp: this.chatService.parseTimestamp(msg.timestamp)
            }));
            // dodajemo na početak niza jer su poruke stigle obrnutim redosledom (DESC)
            this.messages = [...mappedMessages.reverse(), ...this.messages];
            this.currentPage++;
        },
        error: (err) => {
          console.log('An error occured while loading the history: ', err);
          this.hasMoreMessages = false;
        }
      })
  }

  
  onFollowingLoaded(following: UserDTO[]): void{
    this.following = following;
  }

  handleGroupCreated(data: { groupName: string, memberIds: number[] }): void{

    this.chatService.createGroupChat(data.groupName, data.memberIds).subscribe({
      next: (newGroupChat: GroupChatRoomDTO) => {
      
        newGroupChat.createdAt = this.chatService.parseTimestamp(newGroupChat.createdAt);
        
      },
      error: (err) => {
        console.log('Error creating group chat', err);
      }
    });
  }


  onGroupChatsLoaded(groupChatsForUser: GroupChatRoomDTO[]): void{
    this.groupChatsList = groupChatsForUser;
  }

  onGroupChatSelected(groupChat: GroupChatRoomDTO){
    this.groupChatSelected = groupChat;
    console.log('GroupChat : ', this.groupChatSelected);
    this.recipient = null; //iskljuci se privatni chat sa korisnikom

    //otkazi prethodnu pretplatu ako postoji
    if(this.groupChatSubscription){
      this.groupChatSubscription.unsubscribe();
    }
    //pretplacujem se preko webSocketa
    this.groupChatSubscription = this.chatService.subscribeToGroupChatMemberUpdates(this.groupChatSelected.id)
      .subscribe({
        next: (updatedGroupChat: GroupChatRoomDTO) =>{
          console.log('postavljam objekat, ', updatedGroupChat);
          this.groupChatSelected = updatedGroupChat;
        },
        error: (err) => {
          console.error('An error while trying to subscribe on destination /topic/group-chat-members-updates: ', err);

        }
      })
      
    

    this.messages = [];
    this.currentPage = 0;           // resetuj broj stranice
    this.hasMoreMessages = true;   // resetuj flag
    this.loadHistoryFromGroupChat(this.groupChatSelected.id);
  }


  openGroupModal(): void{
    this.isGroupModalOpen = true;
  }

  closeGroupModal(): void{
    this.isGroupModalOpen = false;
  }

  openGroupSettingsModal(): void{
    this.isGroupSettingsModalOpen = true;
  }

  closeGroupSettingsModal(): void{
    this.isGroupSettingsModalOpen = false;
  }

  handleGroupMembersUpdated(updatedGroupChat: GroupChatRoomDTO): void{
    //logika sta se radi
    this.groupChatSelected = updatedGroupChat;
    this.closeGroupSettingsModal();
  }
  



}

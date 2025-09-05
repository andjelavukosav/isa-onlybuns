import { Component, Injectable, OnInit } from '@angular/core';
import { environment } from '../env/enviroment';
import * as SockJS from 'sockjs-client';
import { ChatMessage } from '../model/message';
import { BehaviorSubject, filter, map, Observable, switchMap, take } from 'rxjs';
import { IMessage, Stomp } from '@stomp/stompjs';
import { Page } from '../model/pagination.model';
import { HttpClient } from '@angular/common/http';
import { GroupChatRoomDTO } from '../model/group-chat';

@Injectable({
    providedIn: 'root'
})
export class ChatService {
    private serverUrl = environment.wwwRoot + '/ws';
    private stompClient: any;
    private isConnected = false;
    private messageSubject = new BehaviorSubject<ChatMessage | null>(null);
    private groupChatSubject = new BehaviorSubject<GroupChatRoomDTO | null>(null);
    private connectionReadySubject = new BehaviorSubject<boolean>(false);


    constructor(private http: HttpClient) {}

    connect(): Promise<void> {
        return new Promise((resolve, reject) => {
            if(this.isConnected){
                resolve();
                return;
            }

            const token = sessionStorage.getItem('jwt'); 

            let ws = new SockJS(this.serverUrl);
            this.stompClient = Stomp.over(ws);

            this.stompClient.connect(
                {
                    Authorization: 'Bearer ' + token

                },
                () => {
                    this.isConnected = true;
                    this.connectionReadySubject.next(true);
                    this.subscribeToMessages();
                    resolve();
                },
                (err: unknown) => {
                    if(err instanceof Error){
                        reject(err);
                    }
                    else{
                        reject(new Error('Unknown error establishing conncetion.'));
                    }
                }
            )

        })

        
    }

    getConnectionReady(): Observable<boolean> {
        return this.connectionReadySubject.asObservable();
    }

    subscribeToMessages(): void{
        console.log("Pretplaćujem se na /user/queue/messages. Registruj mi callback funkciju za ovu destinaciju.");

        this.stompClient.subscribe("/user/queue/messages", (message: {body: string}) => {
            console.log("Pozvana callback funckija registrovana na destinaciji /user/queue/messages");

            if(message.body){
                console.log("Poruka je: ", message.body)
                let result: ChatMessage = JSON.parse(message.body);
                result.timestamp = this.parseTimestamp(result.timestamp);
                this.messageSubject.next(result);
            }
        })
    }

   

    sendMessage(msg: ChatMessage): void {
        if (this.stompClient && this.isConnected) {
            this.stompClient.send("/app/chat/private", {}, JSON.stringify(msg));
        }
    }

    getMessages(): Observable<ChatMessage | null> {
        return this.messageSubject.asObservable();
    }

    getNewGroupChat(): Observable<GroupChatRoomDTO | null> {
        return this.groupChatSubject.asObservable();
    }

    getPrivateChatHistory(user1Id: number, user2Id:number, page:number, size:number): Observable<Page<ChatMessage>>{
        return this.http.get<Page<ChatMessage>>(`${environment.apiHost}/chat/history`, {
            params: {
                user1Id, user2Id, page, size}
            });
    }

    createGroupChat(groupName: string, memberIds: number[]): Observable<GroupChatRoomDTO>{
        return this.http.post<GroupChatRoomDTO>(`${environment.apiHost}/chat/create-group`, {
            name: groupName,
             memberIds: memberIds
        });
    }

    loadGroupChatsForUser(): Observable<GroupChatRoomDTO[]>{
        return this.http.get<GroupChatRoomDTO[]>(`${environment.apiHost}/chat/group-chats/my`);
    }

    sendGroupMessage(msg: ChatMessage): void{
        if(this.stompClient && this.isConnected){
            this.stompClient.send("/app/chat/group", {}, JSON.stringify(msg));
        }
    }

    getHistoryFromGroupChat(groupChatId: number, page:number, size:number): Observable<Page<ChatMessage>>{
        return this.http.get<Page<ChatMessage>>(`${environment.apiHost}/chat/group-chat/history`, {
            params:{
                groupChatId, page, size,
            }
        });
    }
   

    addMembersToGroupChat(groupChatId: number, memberIds: number[]): Observable<GroupChatRoomDTO> {
        return this.http.post<GroupChatRoomDTO>(`${environment.apiHost}/chat/group-chat/${groupChatId}/members`,
            memberIds
        );
    }


    removeMembersFromGroupChat(groupChatId: number, memberIds: number[]): Observable<GroupChatRoomDTO> {
        return this.http.post<GroupChatRoomDTO>(`${environment.apiHost}/chat/group-chat/${groupChatId}/remove-members`,
            memberIds
        );
    }

    //pretplata na notifikaciju o novoj grupi u koju je korisnik dodat
    /*subscribeToGroupChatAdditions(username: String): Observable<GroupChatRoomDTO>{
        console.log('Pretplacujem se na destinaciju. ')
        return this.stompClient.watch(`/topic/group-chat-added/${username}`).pipe(
            map((message: IMessage) => JSON.parse(message.body) as GroupChatRoomDTO)
        );
    }*/
   /*subscribeToGroupChatAdditions(username: string): void {
        if (!this.stompClient || !this.isConnected) {
            console.error('WebSocket nije povezan!');
            return;
        }

        console.log('Pretplaćujem se na destinaciju: ', `/topic/group-chat-added/${username}`);

        this.stompClient.subscribe(`/topic/group-chat-added/${username}`, (message: any) => {
            const newGroupChat: GroupChatRoomDTO = JSON.parse(message.body);
            newGroupChat.createdAt = this.parseTimestamp(newGroupChat.createdAt);

            // Ovde možeš emitovati event ili koristiti Subject
            this.groupChatSubject.next(newGroupChat); // dodaj ovo ako koristiš Subject pattern
        });
    }*/

        subscribeToGroupChatAdditions(username: string): Observable<GroupChatRoomDTO> {
            return this.getConnectionReady().pipe(
                filter(isReady => isReady === true),
                take(1),
                switchMap(() => 
                   new Observable<GroupChatRoomDTO> (observer => {
                        if(!this.stompClient || !this.isConnected){
                            observer.error('WebSocket is not established');
                            return;
                        }
                    console.log('Pretplacujem se i saljem objekat sa destinacije /topic/group-chat-added/' + username);
                    const subscription = this.stompClient.subscribe(`/topic/group-chat-added/${username}`, (message: any) => {
                        const body = JSON.parse(message.body);
                                        console.log('dobila sam poruku, ', message)

                        observer.next(body);
                    });
                      return () => {
                        subscription.unsubscribe();
                        console.log('Pretplata otkazana za /topic/group-chat-added/' + username);
                        };
                }))
            );
        }



    //pretplata za eventove clanstva u grupama
    subscribeToGroupChatMemberUpdates(groupChatId: number): Observable<GroupChatRoomDTO>{
        return new Observable(observer => {
            if(!this.stompClient || !this.isConnected){
                observer.error('WebSocket is not established');
                return;
            }
            console.log('Pretplacujem se i saljem objekat sa destinacije /topic/group-chat-member-updates');
            const subscription = this.stompClient.subscribe(`/topic/group-chat-members-updates/${groupChatId}`, (message: any) => {
                const body = JSON.parse(message.body);
                                console.log('dobila sam poruku, ', message)

                observer.next(body);
            });
            /*console.log('Odjavljujem se se te pretplate.');
            return () => subscription.unsubscribe();*/
        })
    }



    parseTimestamp(input?: string | number[] | Date): Date | undefined {
        if (!input) return undefined;

        if (input instanceof Date) {
            return input;
        }

        if (typeof input === 'string') {
            // parse ISO string direktno
            const d = new Date(input);
            return isNaN(d.getTime()) ? undefined : d;
        }

        if (Array.isArray(input)) {
            // parse niz brojeva u Date
            return new Date(
            input[0],
            input[1] - 1,
            input[2],
            input[3],
            input[4],
            input[5],
            Math.floor(input[6] / 1000000)
            );
        }

        return undefined;
    }

    disconnect(): void{
        if(this.stompClient && this.isConnected){
            this.stompClient.disconnect(() => {
                this.isConnected = false;
            });
        }
    }
}
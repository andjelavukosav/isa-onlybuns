// src/app/service/websocket.service.ts
import { Injectable } from '@angular/core';
import { Client, Message } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client;
  private messageSubject = new Subject<any>();

  public messages$ = this.messageSubject.asObservable();

    constructor() {
    this.client = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
        reconnectDelay: 5000
    });

    this.client.onConnect = () => {
        this.client.subscribe('/topic/new-asylum', (message: Message) => {
        const body = JSON.parse(message.body);
        this.messageSubject.next(body);
        });
    };

    this.client.activate();
    }

}

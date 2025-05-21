import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ChatMessage {
  sender: number;
  recipient: number;
  content: string;
  chatId: number;
  timestamp?: Date;
}

export interface ChatRoom {
  id: number;
  user1Id: number;
  user2Id: number;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private stompClient: Client | null = null;
  private messagesSubject = new BehaviorSubject<ChatMessage[]>([]);
  private chatRoomsSubject = new BehaviorSubject<ChatRoom[]>([]);
  private currentChatId: number | null = null;
  private currentRecipientId: number | null = null;

  constructor() {
    this.initializeWebSocketConnection();
  }

  private initializeWebSocketConnection(): void {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      console.error('No JWT token found');
      return;
    }

    this.stompClient = new Client({
      brokerURL: environment.wsUrl + '/ws-chat?token=' + token,
      connectHeaders: {
        token: token
      },
      debug: function (str) {
        console.log(str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = (frame) => {
      console.log('Connected to WebSocket');
      
      // Подписка на обновления списка чатов
      this.stompClient?.subscribe('/user/queue/chat.rooms', (message) => {
        console.log('Received chat rooms:', message.body);
        const chats = JSON.parse(message.body);
        this.chatRoomsSubject.next(chats);
      });

      // Подписка на историю сообщений
      this.stompClient?.subscribe('/user/queue/chat.history', (message) => {
        console.log('Received chat history:', message.body);
        const messages = JSON.parse(message.body);
        this.messagesSubject.next(messages);
      });

      // Запрашиваем список чатов
      this.getChatRooms();
    };

    this.stompClient.onStompError = (frame) => {
      console.error('STOMP error:', frame);
    };

    this.stompClient.onWebSocketError = (event) => {
      console.error('WebSocket error:', event);
    };

    this.stompClient.onWebSocketClose = (event) => {
      console.log('WebSocket connection closed:', event);
    };

    this.stompClient.activate();
  }

  public getChatRooms(): void {
    console.log('Requesting chat rooms');
    this.stompClient?.publish({
      destination: '/app/chat.rooms',
      body: ''
    });
  }

  public getChatRoomsObservable(): Observable<ChatRoom[]> {
    return this.chatRoomsSubject.asObservable();
  }

  public createChat(userId: number): void {
    console.log('Creating chat with user:', userId);
    this.stompClient?.publish({
      destination: '/app/chat.room.create',
      body: userId.toString()
    });
  }

  public openChat(chatId: number, recipientId: number): void {
    console.log('Opening chat:', { chatId, recipientId });
    this.currentChatId = chatId;
    this.currentRecipientId = recipientId;

    // Отписываемся от предыдущего чата, если был
    if (this.currentChatId) {
      this.stompClient?.unsubscribe(`/topic/chat.${this.currentChatId}`);
    }

    // Запрашиваем историю сообщений
    this.loadChatHistory();
  }

  public loadChatHistory(): void {
    if (this.currentRecipientId) {
      console.log('Loading chat history for user:', this.currentRecipientId);
      this.stompClient?.publish({
        destination: '/app/chat.history',
        body: this.currentRecipientId.toString()
      });
    }
  }

  public getMessagesObservable(): Observable<ChatMessage[]> {
    return this.messagesSubject.asObservable();
  }

  public sendMessage(content: string): void {
    if (!this.currentChatId || !this.currentRecipientId) {
      console.error('Cannot send message - no chat selected');
      return;
    }

    console.log('Sending message:', { content, chatId: this.currentChatId, recipientId: this.currentRecipientId });
    const message: ChatMessage = {
      sender: this.getCurrentUserId(),
      recipient: this.currentRecipientId,
      content: content,
      chatId: this.currentChatId
    };

    this.stompClient?.publish({
      destination: '/app/chat.send',
      body: JSON.stringify(message)
    });
  }

  public clearChat(chatId: number): void {
    console.log('Clearing chat:', chatId);
    this.stompClient?.publish({
      destination: '/app/chat.clear',
      body: chatId.toString()
    });
  }

  public deleteChat(chatId: number): void {
    console.log('Deleting chat:', chatId);
    this.stompClient?.publish({
      destination: '/app/chat.room.delete',
      body: chatId.toString()
    });
  }

  private getCurrentUserId(): number {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      console.error('Cannot get user ID - no token found');
      return 0;
    }
    
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      const userId = JSON.parse(jsonPayload).id;
      console.log('Current user ID:', userId);
      return userId;
    } catch (e) {
      console.error('Error parsing JWT:', e);
      return 0;
    }
  }

  public disconnect(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }
} 
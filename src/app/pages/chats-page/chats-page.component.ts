import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService, ChatMessage, ChatRoom } from '../../data/services/chat.service';
import { Subscription } from 'rxjs';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-chats-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chats-page.component.html',
  styleUrls: ['./chats-page.component.scss']
})
export class ChatsPageComponent implements OnInit, OnDestroy {
  chats: ChatRoom[] = [];
  messages: ChatMessage[] = [];
  currentChatId: number | null = null;
  currentRecipientId: number | null = null;
  newMessageContent: string = '';
  newChatUserId: string = '';
  currentUserId: number | null = null;

  private subscriptions: Subscription[] = [];

  constructor(
    private chatService: ChatService,
    private authService: AuthService
  ) {
    console.log('ChatsPageComponent: Инициализация компонента');
  }

  ngOnInit(): void {
    console.log('ChatsPageComponent: ngOnInit');
    
    // Получаем ID текущего пользователя
    this.subscriptions.push(
      this.authService.getCurrentUserId().subscribe(userId => {
        console.log('ChatsPageComponent: Получен ID пользователя:', userId);
        this.currentUserId = userId;
      })
    );

    // Подписываемся на обновления списка чатов
    this.subscriptions.push(
      this.chatService.getChatRoomsObservable().subscribe(chats => {
        console.log('ChatsPageComponent: Получен список чатов:', chats);
        this.chats = chats;
      })
    );

    // Подписываемся на обновления сообщений
    this.subscriptions.push(
      this.chatService.getMessagesObservable().subscribe(messages => {
        console.log('ChatsPageComponent: Получены сообщения:', messages);
        this.messages = messages;
      })
    );
  }

  ngOnDestroy(): void {
    console.log('ChatsPageComponent: ngOnDestroy');
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  createChat(): void {
    console.log('ChatsPageComponent: Попытка создания чата с ID:', this.newChatUserId);
    const userId = parseInt(this.newChatUserId);
    if (isNaN(userId)) {
      console.error('ChatsPageComponent: Некорректный ID пользователя');
      alert('Пожалуйста, введите корректный ID пользователя');
      return;
    }
    if (userId === this.currentUserId) {
      console.error('ChatsPageComponent: Попытка создать чат с самим собой');
      alert('Нельзя создать чат с самим собой');
      return;
    }
    this.chatService.createChat(userId);
    this.newChatUserId = '';
  }

  openChat(chat: ChatRoom): void {
    console.log('ChatsPageComponent: Открытие чата:', chat);
    this.currentChatId = chat.id;
    this.currentRecipientId = chat.user1Id === this.currentUserId ? chat.user2Id : chat.user1Id;
    this.chatService.openChat(chat.id, this.currentRecipientId);
  }

  sendMessage(): void {
    console.log('ChatsPageComponent: Попытка отправить сообщение:', this.newMessageContent);
    if (!this.newMessageContent.trim()) {
      console.log('ChatsPageComponent: Пустое сообщение, отправка отменена');
      return;
    }
    this.chatService.sendMessage(this.newMessageContent);
    this.newMessageContent = '';
  }

  clearChat(chatId: number): void {
    console.log('ChatsPageComponent: Попытка очистить чат:', chatId);
    if (confirm('Вы уверены, что хотите очистить историю чата?')) {
      this.chatService.clearChat(chatId);
    }
  }

  deleteChat(chatId: number): void {
    console.log('ChatsPageComponent: Попытка удалить чат:', chatId);
    if (confirm('Вы уверены, что хотите полностью удалить этот чат и все сообщения?')) {
      this.chatService.deleteChat(chatId);
      if (this.currentChatId === chatId) {
        this.currentChatId = null;
        this.currentRecipientId = null;
        this.messages = [];
      }
    }
  }

  getOtherUserId(chat: ChatRoom): number {
    return chat.user1Id === this.currentUserId ? chat.user2Id : chat.user1Id;
  }

  isMessageFromCurrentUser(message: ChatMessage): boolean {
    return message.sender === this.currentUserId;
  }
} 
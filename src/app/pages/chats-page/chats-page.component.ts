import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatComponent } from '../../common-ui/chat/chat.component';

@Component({
  selector: 'app-chats-page',
  standalone: true,
  imports: [CommonModule, ChatComponent],
  template: `
    <div class="chats-page">
      <h1>Чаты</h1>
      <app-chat></app-chat>
    </div>
  `,
  styles: [`
    .chats-page {
      padding: 20px;
      height: 100%;
    }

    h1 {
      margin-bottom: 20px;
      color: #333;
    }
  `]
})
export class ChatsPageComponent {} 
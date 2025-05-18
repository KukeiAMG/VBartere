import { Component } from '@angular/core';
import { AnimatedBackgroundComponent } from '../../my-shenanigans/animated-background/animated-background.component';
import { CommonModule } from '@angular/common';
import { SvgIconComponent } from '../../common-ui/svg-icon/svg-icon.component';
import { RouterLink } from '@angular/router';
import { Advertisement } from '../../data/Interfaces/advertisement.interface';



@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [
    CommonModule,
    AnimatedBackgroundComponent,
    SvgIconComponent,
    RouterLink
  ],
  templateUrl: './cart-page.component.html',
  styleUrl: './cart-page.component.scss'
})
export class CartPageComponent {
  cartItems: Partial<Advertisement>[] = [
    // Временные данные для примера
    {
      id: 1,
      title: 'iPhone 12',
      description: 'Отличное состояние, без царапин',
      imageList: ['/assets/images/iphone.jpg']
    },
    {
      id: 2,
      title: 'MacBook Pro',
      description: '2019 год, 16GB RAM',
      imageList: ['/assets/images/macbook.jpg']
    }
  ];

  removeFromCart(itemId: number) {
    this.cartItems = this.cartItems.filter(item => item.id !== itemId);
  }

  clearCart() {
    this.cartItems = [];
  }
} 
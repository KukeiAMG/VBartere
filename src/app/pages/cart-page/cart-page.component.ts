import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TovarCardComponent } from '../../common-ui/tovar-card/tovar-card.component';
import {AnimatedBackgroundComponent} from '../../my-shenanigans/animated-background/animated-background.component';
import { CartService } from '../../data/services/cart.service';
import { Advertisement } from '../../data/Interfaces/advertisement.interface';

@Component({
  selector: 'app-cart-page',
  templateUrl: './cart-page.component.html',
  styleUrls: ['./cart-page.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterModule, TovarCardComponent, AnimatedBackgroundComponent]
})
export class CartPageComponent implements OnInit {
  cartItems: Advertisement[] = [];

  constructor(private cartService: CartService) {}

  ngOnInit(): void {
    this.loadCartItems();
  }

  loadCartItems(): void {
    this.cartItems = this.cartService.getCartItems();
  }

  removeFromCart(itemId: number): void {
    this.cartService.removeFromCart(itemId);
    this.loadCartItems();
  }

  clearCart(): void {
    this.cartService.clearCart();
    this.loadCartItems();
  }
} 
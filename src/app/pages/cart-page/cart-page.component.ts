import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TovarCardComponent } from '../../common-ui/tovar-card/tovar-card.component';
import { AnimatedBackgroundComponent } from '../../my-shenanigans/animated-background/animated-background.component';
import { CartService, CartDTO } from '../../data/services/cart.service';
import { AuthService } from '../../auth/auth.service';
import { Advertisement } from '../../data/Interfaces/advertisement.interface';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    TovarCardComponent,
    AnimatedBackgroundComponent
  ],
  templateUrl: './cart-page.component.html',
  styleUrls: ['./cart-page.component.scss']
})
export class CartPageComponent implements OnInit, OnDestroy {
  cart: CartDTO | null = null;
  cartItems: Advertisement[] = [];
  isLoading = false;
  errorMessage = '';
  private subscription = new Subscription();

  constructor(
    private cartService: CartService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    console.log('CartPage: Инициализация компонента');
    this.loadCart();
    
    // Подписываемся на обновления корзины
    this.subscription.add(
      this.cartService.cartUpdate$.subscribe(() => {
        console.log('CartPage: Получен сигнал обновления корзины');
        this.loadCart();
      })
    );
  }

  ngOnDestroy() {
    console.log('CartPage: Уничтожение компонента');
    this.subscription.unsubscribe();
  }

  loadCart() {
    console.log('CartPage: Начало загрузки корзины');
    this.isLoading = true;
    this.authService.getCurrentUserId().subscribe((userId: number | null) => {
      console.log('CartPage: Получен ID пользователя:', userId);
      if (userId) {
        this.cartService.getCartByUserId(userId).subscribe({
          next: (cart) => {
            console.log('CartPage: Получена корзина:', cart);
            this.cart = cart;
            this.cartService.getCartItems(userId).subscribe({
              next: (items) => {
                console.log('CartPage: Получены товары корзины:', items);
                this.cartItems = items;
                this.isLoading = false;
              },
              error: (error) => {
                console.error('CartPage: Ошибка при получении товаров корзины:', error);
                this.errorMessage = 'Ошибка при загрузке товаров';
                this.isLoading = false;
              }
            });
          },
          error: (error) => {
            console.error('CartPage: Ошибка при получении корзины:', error);
            this.errorMessage = 'Ошибка при загрузке корзины';
            this.isLoading = false;
          }
        });
      }
    });
  }

  clearCart() {
    console.log('CartPage: Начало очистки корзины');
    if (this.cart) {
      this.cartService.clearCart(this.cart.id).subscribe({
        next: () => {
          console.log('CartPage: Корзина успешно очищена');
          this.cartItems = [];
          this.cart = null;
          this.cartService.updateCart();
        },
        error: (error) => {
          console.error('CartPage: Ошибка при очистке корзины:', error);
          this.errorMessage = 'Ошибка при очистке корзины';
        }
      });
    }
  }
} 
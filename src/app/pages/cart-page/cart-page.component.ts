import { Component, OnInit, OnDestroy, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { TovarCardComponent } from '../../common-ui/tovar-card/tovar-card.component';
import { AnimatedBackgroundComponent } from '../../my-shenanigans/animated-background/animated-background.component';
import { CartService } from '../../data/services/cart.service';
import { Advertisement } from '../../data/Interfaces/advertisement.interface';
import { Subscription, catchError, of, switchMap, tap } from 'rxjs';
import { ImageService } from '../../data/services/image.service';

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
  cartItems: Advertisement[] = [];
  isLoading = false;
  errorMessage = '';
  private subscription = new Subscription();
  imageService = inject(ImageService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private route = inject(ActivatedRoute);

  constructor(private cartService: CartService) {}

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
    this.errorMessage = '';

    this.cartService.getMyCart().pipe(
      switchMap(advertisementIds => {
        console.log('CartPage: Получены ID товаров:', advertisementIds);
        if (!advertisementIds.length) {
          return of([]);
        }
        return this.cartService.getCartItems(advertisementIds);
      }),
      catchError(error => {
        console.error('CartPage: Ошибка при загрузке корзины:', error);
        this.errorMessage = error.message || 'Ошибка при загрузке корзины';
        return of([]);
      })
    ).subscribe({
      next: (items) => {
        console.log('CartPage: Получены товары корзины:', items);
        this.cartItems = items;
        this.isLoading = false;
        // Принудительно обновляем компонент после загрузки
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('CartPage: Ошибка при получении товаров корзины:', error);
        this.errorMessage = 'Ошибка при загрузке товаров';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private reloadComponent() {
    const currentUrl = this.router.url;
    this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
      this.router.navigate([currentUrl]);
    });
  }

  clearCart() {
    console.log('CartPage: Начало очистки корзины');
    this.isLoading = true;
    this.errorMessage = '';

    this.cartService.clearCart().subscribe({
      next: () => {
        console.log('CartPage: Корзина успешно очищена');
        this.isLoading = false;
        this.reloadComponent(); // Полная перезагрузка компонента
      },
      error: (error) => {
        console.error('CartPage: Ошибка при очистке корзины:', error);
        if (error.status !== 200) {
          this.errorMessage = error.message || 'Ошибка при очистке корзины';
        }
        this.isLoading = false;
        this.reloadComponent(); // Перезагрузка даже в случае ошибки
      }
    });
  }

  removeFromCart(advertisementId: number) {
    console.log('CartPage: Удаление товара из корзины', advertisementId);
    this.isLoading = true;
    this.errorMessage = '';

    this.cartService.removeFromCart(advertisementId).subscribe({
      next: () => {
        console.log('CartPage: Товар успешно удален из корзины');
        this.isLoading = false;
        this.reloadComponent(); // Полная перезагрузка компонента
      },
      error: (error) => {
        console.error('CartPage: Ошибка при удалении товара из корзины:', error);
        if (error.status !== 200) {
          this.errorMessage = error.message || 'Ошибка при удалении товара из корзины';
        }
        this.isLoading = false;
        this.reloadComponent(); // Перезагрузка даже в случае ошибки
      }
    });
  }

  viewAdvertisement(id: number): void {
    this.router.navigate(['/advertisement', id]);
  }
} 
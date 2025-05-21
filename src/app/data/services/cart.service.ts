import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Advertisement } from '../Interfaces/advertisement.interface';
import { AdvertisementService } from './advertisement.service';

export interface CartDTO {
  id: number;
  userId: number;
  advertisementIds: number[];
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private baseUrl = `http://localhost:8081/api/`;
  private advertisementService = inject(AdvertisementService);
  private cartUpdateSubject = new BehaviorSubject<void>(undefined);
  cartUpdate$ = this.cartUpdateSubject.asObservable();

  constructor(private http: HttpClient) {}

  getCartByUserId(userId: number): Observable<CartDTO> {
    console.log('CartService: Получение корзины для пользователя', userId);
    return this.http.get<CartDTO>(`${this.baseUrl}cart/user/${userId}`).pipe(
      tap(response => {
        console.log('CartService: Получен ответ от сервера:', response);
      })
    );
  }

  clearCart(cartId: number): Observable<void> {
    console.log('CartService: Очистка корзины', cartId);
    return this.http.delete<void>(`${this.baseUrl}cart/${cartId}/clear`).pipe(
      tap(() => {
        console.log('CartService: Корзина успешно очищена');
      })
    );
  }

  addToCart(userId: number, advertisementId: number): Observable<CartDTO> {
    console.log('CartService: Добавление товара в корзину', { userId, advertisementId });
    return this.http.post<CartDTO>(`${this.baseUrl}users/advertisement/${advertisementId}/add`, {}).pipe(
      tap(response => {
        console.log('CartService: Получен ответ при добавлении в корзину:', response);
      }),
      switchMap(response => {
        console.log('CartService: Отправка сигнала обновления корзины');
        this.cartUpdateSubject.next();
        return [response];
      })
    );
  }

  getCartItems(userId: number): Observable<Advertisement[]> {
    return this.http.get<Advertisement[]>(`${this.baseUrl}cart/user/${userId}`);
  }

  updateCart() {
    console.log('CartService: Принудительное обновление корзины');
    this.cartUpdateSubject.next();
  }
} 
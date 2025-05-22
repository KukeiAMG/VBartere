import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, catchError, throwError, switchMap, of, map, forkJoin } from 'rxjs';
import { Advertisement } from '../Interfaces/advertisement.interface';
import { AdvertisementService } from './advertisement.service';
import { CookieService } from 'ngx-cookie-service';

export interface CartDTO {
  id: number;
  userId: number;
  advertisementIds: number[];
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private baseUrl = `http://localhost:8081/api/users/advertisement/`;
  private cartUrl = `http://localhost:8081/api/cart/`;
  private advertisementService = inject(AdvertisementService);
  private cookieService = inject(CookieService);
  private cartUpdateSubject = new BehaviorSubject<void>(undefined);
  cartUpdate$ = this.cartUpdateSubject.asObservable();

  constructor(private http: HttpClient) {}

  private getAuthToken(): string {
    return this.cookieService.get('token') || '';
  }

  private getAuthHeader(): { [header: string]: string } {
    return {
      'Authorization': `Bearer ${this.getAuthToken()}`
    };
  }

  getCartByUserId(): Observable<CartDTO> {
    console.log('CartService: Получение корзины');
    return this.http.get<CartDTO>(`${this.cartUrl}my-cart`, {
      headers: this.getAuthHeader()
    }).pipe(
      tap(response => {
        console.log('CartService: Получен ответ от сервера:', response);
      }),
      catchError(error => {
        console.error('CartService: Ошибка при получении корзины:', error);
        return throwError(() => new Error('Ошибка при получении корзины'));
      })
    );
  }

  getMyCart(): Observable<number[]> {
    return this.http.get<CartDTO>(`${this.cartUrl}my-cart`)
      .pipe(
        map(cart => cart.advertisementIds),
        catchError(error => {
          console.error('Ошибка при получении корзины:', error);
          return throwError(() => new Error('Ошибка при получении корзины'));
        })
      );
  }

  getCartItems(advertisementIds: number[]): Observable<Advertisement[]> {
    if (!advertisementIds.length) {
      return of([]);
    }
    
    const requests = advertisementIds.map(id => 
      this.advertisementService.getAdvertisementById(id).pipe(
        catchError(error => {
          console.error(`Ошибка при получении товара ${id}:`, error);
          return of(null);
        })
      )
    );

    return forkJoin(requests).pipe(
      map(results => results.filter((item): item is Advertisement => item !== null))
    );
  }

  addToCart(advertisementId: number): Observable<string> {
    console.log('CartService: Добавление товара в корзину', advertisementId);
    return this.http.post<string>(
      `${this.baseUrl}${advertisementId}/add`,
      {},
      { headers: this.getAuthHeader() }
    ).pipe(
      tap(response => {
        console.log('CartService: Получен ответ при добавлении в корзину:', response);
        this.cartUpdateSubject.next();
      }),
      catchError(error => {
        console.error('CartService: Ошибка при добавлении в корзину:', error);
        return throwError(() => new Error('Ошибка при добавлении в корзину'));
      })
    );
  }

  removeFromCart(advertisementId: number): Observable<string> {
    console.log('CartService: Удаление товара из корзины', advertisementId);
    return this.http.put<string>(
      `${this.baseUrl}${advertisementId}/remove`,
      {},
      { headers: this.getAuthHeader() }
    ).pipe(
      tap(response => {
        console.log('CartService: Получен ответ при удалении из корзины:', response);
        this.cartUpdateSubject.next();
      })
    );
  }

  clearCart(): Observable<string> {
    console.log('CartService: Очистка корзины');
    return this.http.delete<string>(
      `${this.baseUrl}cart/clear`,
      { headers: this.getAuthHeader() }
    ).pipe(
      tap(response => {
        console.log('CartService: Корзина успешно очищена:', response);
        this.cartUpdateSubject.next();
      }),
      catchError(error => {
        console.error('CartService: Ошибка при очистке корзины:', error);
        return throwError(() => new Error('Ошибка при очистке корзины'));
      })
    );
  }

  updateCart() {
    console.log('CartService: Принудительное обновление корзины');
    this.cartUpdateSubject.next();
  }
} 
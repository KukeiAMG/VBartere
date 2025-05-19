import { Injectable } from '@angular/core';
import { Advertisement } from '../Interfaces/advertisement.interface';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private readonly CART_KEY = 'cart_items';

  constructor() {}

  getCartItems(): Advertisement[] {
    const items = localStorage.getItem(this.CART_KEY);
    return items ? JSON.parse(items) : [];
  }

  addToCart(item: Advertisement): void {
    const items = this.getCartItems();
    if (!items.some(i => i.id === item.id)) {
      items.push(item);
      localStorage.setItem(this.CART_KEY, JSON.stringify(items));
    }
  }

  removeFromCart(itemId: number): void {
    const items = this.getCartItems();
    const updatedItems = items.filter(item => item.id !== itemId);
    localStorage.setItem(this.CART_KEY, JSON.stringify(updatedItems));
  }

  clearCart(): void {
    localStorage.removeItem(this.CART_KEY);
  }
} 
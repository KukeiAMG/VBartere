import {Component, inject, Input, Output, EventEmitter} from '@angular/core';
import { Advertisement, AdvertisementDTO } from '../../data/Interfaces/advertisement.interface';
import { AdvertisementService } from '../../data/services/advertisement.service';
import { AuthService } from '../../auth/auth.service';
import { RouterModule } from '@angular/router';
import { SvgIconComponent } from '../svg-icon/svg-icon.component';
import { ImageService } from '../../data/services/image.service';
import { CartService } from '../../data/services/cart.service';

@Component({
  selector: 'app-tovar-card',
  imports: [
    RouterModule,
    SvgIconComponent
  ],
  templateUrl: './tovar-card.component.html',
  styleUrl: './tovar-card.component.scss'
})
export class TovarCardComponent {
  @Input() public advertisement!: Advertisement;
  @Input() public isInProfile = false;
  @Input() public isMyProfile = false;
  @Input() public isInCart = false;
  @Input() public isMine = false;
  isAddedToCart = false;
  showDeleteConfirm = false;
  selectedFiles: File[] = [];
  authService = inject(AuthService);
  imageService = inject(ImageService);
  cartService = inject(CartService);

  constructor(private advertisementService: AdvertisementService) {
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.selectedFiles = Array.from(input.files);
    }
  }

  addToCart(): void {
    this.isAddedToCart = true;
    console.log('TovarCard: Начало добавления в корзину', this.advertisement);
    this.authService.getCurrentUserId().subscribe(userId => {
      console.log('TovarCard: Получен ID пользователя:', userId);
      if (userId) {
        this.cartService.addToCart(this.advertisement.id).subscribe({
          next: (response) => {
            console.log('TovarCard: Товар успешно добавлен в корзину', response);
            this.cartService.updateCart();
          },
          error: (error) => {
            console.error('TovarCard: Ошибка при добавлении в корзину:', error);
          }
        });
      } else {
        console.log('TovarCard: Пользователь не авторизован');
      }
    });
  }

  createNewAdvertisement(): void {
    const advertisementData = {
      title: this.advertisement.title,
      description: this.advertisement.description,
      //price: 1000,
      subCategoryId: 1,
      ownerId: this.advertisement.ownerId,
      imagesId: this.advertisement.imagesId,
      status: true
    };

    const userId = this.authService.currentUserId;
    if (!userId) {
      console.error('Пользователь не авторизован');
      return;
    }

    advertisementData.ownerId = userId;

    this.advertisementService.createAdvertisement(
      advertisementData as AdvertisementDTO,
      this.selectedFiles
    ).subscribe({
      next: (response) => {
        console.log('Объявление успешно создано:', response);
      },
      error: (error) => {
        console.error('Ошибка при создании объявления:', error);
      }
    });
  }

  deleteAdvertisement(): void {
    if (confirm('Вы уверены, что хотите удалить это объявление?')) {
      this.advertisementService.deleteAdvertisement(this.advertisement.id).subscribe({
        next: () => {
          console.log('Объявление успешно удалено');
          window.location.reload();
        },
        error: (error) => {
          console.error('Ошибка при удалении объявления:', error);
        }
      });
    }
  }
}

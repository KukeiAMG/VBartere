import {Component, inject, Input} from '@angular/core';
import {Profile} from '../../data/Interfaces/profile.interface';
import {ImgUrlsPipe} from '../../helpers/pipes/img-urls.pipe';
import { Advertisement, AdvertisementDTO, AdvertisementImage } from '../../data/Interfaces/advertisement.interface';
import { AdvertisementService } from '../../data/services/advertisement.service';
import { AuthService } from '../../auth/auth.service';
import { tap } from 'rxjs';
import { RouterModule } from '@angular/router';
import { SvgIconComponent } from '../svg-icon/svg-icon.component';
import { ImageService } from '../../data/services/image.service';

@Component({
  selector: 'app-tovar-card',
  imports: [
    ImgUrlsPipe,
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
  selectedFiles: File[] = [];
  authService = inject(AuthService);
  imageService = inject(ImageService);

  constructor(private advertisementService: AdvertisementService) {
    // Для тестирования добавляем случайные изображения
    if (this.advertisement && !this.advertisement.imageList) {
      this.advertisement.imageList = [{
        id: 1,
        size: 0,
        name: 'placeholder.png',
        originalFileName: 'placeholder.png',
        contentType: 'image/png',
        filePath: '',
        previewImage: true
      }];
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.selectedFiles = Array.from(input.files);
    }
  }

  createNewAdvertisement(): void {
    const advertisementData = {
      title: this.advertisement.title,
      description: this.advertisement.description,
      //price: 1000,
      subCategoryId: 1,
      ownerId: this.advertisement.ownerId,
      status: true
    };
    let userId = 1;
    // Здесь нужно получить userId из вашего сервиса авторизации
    this.authService.getCurrentUserId().pipe(
      tap((userId1) => {
        advertisementData.ownerId = userId1;
        userId = userId1;
      })
    );

    this.advertisementService.createAdvertisement(
      advertisementData as AdvertisementDTO,
      this.selectedFiles,
      userId
    ).subscribe({
      next: (response) => {
        console.log('Объявление успешно создано:', response);
        // Здесь можно добавить обработку успешного создания
      },
      error: (error) => {
        console.error('Ошибка при создании объявления:', error);
        // Здесь можно добавить обработку ошибки
      }
    });
  }
}

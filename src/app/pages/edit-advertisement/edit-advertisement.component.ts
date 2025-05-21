import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AdvertisementService } from '../../data/services/advertisement.service';
import { Advertisement, AdvertisementDTO } from '../../data/Interfaces/advertisement.interface';
import { switchMap } from 'rxjs';
import { ImageService } from '../../data/services/image.service';
@Component({
  selector: 'app-edit-advertisement',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './edit-advertisement.component.html',
  styleUrl: './edit-advertisement.component.scss'
})
export class EditAdvertisementComponent implements OnInit {
  advertisementService = inject(AdvertisementService);
  route = inject(ActivatedRoute);
  router = inject(Router);
  imageService = inject(ImageService);

  advertisement: Advertisement | null = null;
  advertisementData = {
    title: '',
    description: '',
    subCategoryId: 1,
    ownerId: 1,
    status: true
  };

  selectedFiles: File[] = [];
  previewUrls: string[] = [];
  errorMessage = '';
  isLoading = true;

  ngOnInit() {
    this.route.params.pipe(
      switchMap(params => this.advertisementService.getAdvertisementById(+params['id']))
    ).subscribe({
      next: (advertisement) => {
        this.advertisement = advertisement;
        this.advertisementData = {
          title: advertisement.title || '',
          description: advertisement.description || '',
          subCategoryId: advertisement.subCategoryId || 1,
          ownerId: advertisement.ownerId || 1,
          status: advertisement.status || true
        };
        if (advertisement.imagesId && advertisement.imagesId.length > 0) {
          for(let i = 0; i < advertisement.imagesId.length; i++) {
            this.previewUrls.push(this.imageService.getImage(advertisement.imagesId[i]));
          }
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Ошибка при получении объявления:', error);
        this.errorMessage = 'Не удалось загрузить объявление';
        this.isLoading = false;
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.selectedFiles = Array.from(input.files);
      const existingPreviews = [...this.previewUrls];
      this.previewUrls = [];
      this.selectedFiles.forEach(file => {
        const reader = new FileReader();
        reader.onload = (e) => {
          if (e.target?.result) {
            this.previewUrls.push(e.target.result as string);
          }
        };
        reader.readAsDataURL(file);
      });
      this.previewUrls = [...existingPreviews, ...this.previewUrls];
    }
  }

  removeImage(index: number): void {
    if (this.advertisement && this.advertisement.imagesId) {
      this.advertisement.imagesId.splice(index, 1);
    }
    this.selectedFiles.splice(index, 1);
    this.previewUrls.splice(index, 1);
  }

  updateAdvertisement(): void {
    if (!this.advertisement) {
      this.errorMessage = 'Объявление не найдено';
      return;
    }

    const advertisementDTO: AdvertisementDTO = {
      title: this.advertisementData.title,
      description: this.advertisementData.description,
      subCategoryId: this.advertisementData.subCategoryId,
      ownerId: this.advertisement.ownerId,
      status: this.advertisement.status
    };

    const advertisementId = this.advertisement.id;

    // Обновляем поля объявления
    this.advertisementService.updateAdvertisementFields(advertisementId, advertisementDTO).subscribe({
      next: (response) => {
        console.log('Поля объявления успешно обновлены:', response);
        
        // Если есть новые изображения, обновляем их
        if (this.selectedFiles.length > 0) {
          this.advertisementService.updateAdvertisementImages(advertisementId, this.selectedFiles).subscribe({
            next: (response) => {
              console.log('Изображения успешно обновлены:', response);
              this.router.navigate(['/profile/me']);
            },
            error: (error) => {
              console.error('Ошибка при обновлении изображений:', error);
              this.errorMessage = error.error?.error || 'Произошла ошибка при обновлении изображений';
            }
          });
        } else {
          // Если нет новых изображений, просто переходим к просмотру
          this.router.navigate(['/profile/me']);
        }
      },
      error: (error) => {
        console.error('Ошибка при обновлении полей объявления:', error);
        this.errorMessage = error.error?.error || 'Произошла ошибка при обновлении объявления';
      }
    });
  }
} 
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdvertisementService } from '../../data/services/advertisement.service';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { tap } from 'rxjs';
import { AdvertisementDTO } from '../../data/Interfaces/advertisement.interface';

@Component({
  selector: 'app-create-advertisement',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-advertisement.component.html',
  styleUrl: './create-advertisement.component.scss'
})
export class CreateAdvertisementComponent {
    authService = inject(AuthService);
  advertisementData = {
    title: '',
    description: '',
    //price: 0,
    subCategoryId: 1,
    ownerId: 1,
    status: true
  };

  selectedFiles: File[] = [];
  previewUrls: string[] = [];
  errorMessage = '';

  constructor(
    private advertisementService: AdvertisementService,
    private router: Router
  ) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.selectedFiles = Array.from(input.files);
      this.previewUrls = [];
      
      // Создаем превью для каждого файла
      this.selectedFiles.forEach(file => {
        const reader = new FileReader();
        reader.onload = (e: any) => {
          this.previewUrls.push(e.target.result);
        };
        reader.readAsDataURL(file);
      });
    }
  }

  removeImage(index: number): void {
    this.selectedFiles.splice(index, 1);
    this.previewUrls.splice(index, 1);
  }

createAdvertisement(): void {
  if (!this.advertisementData.title || !this.advertisementData.description) {
    this.errorMessage = 'Пожалуйста, заполните все обязательные поля';
    return;
  }

  this.errorMessage = '';
  
  this.authService.getCurrentUserId().subscribe({
    next: (userId) => {
      // Убедитесь, что advertisementData содержит все необходимые поля
      const advertisementDTO: AdvertisementDTO = {
        title: this.advertisementData.title,
        description: this.advertisementData.description,
        subCategoryId: this.advertisementData.subCategoryId,
        ownerId: userId,
        status: true
      };

      this.advertisementService.createAdvertisement(
        advertisementDTO as AdvertisementDTO,
        this.selectedFiles,
        userId
      ).subscribe({
        next: (response) => {
          console.log('Объявление успешно создано:', response);
          this.router.navigate(['/']);
        },
        error: (error) => {
          console.error('Ошибка при создании объявления:', error);
          this.errorMessage = error.error?.error || 'Произошла ошибка при создании объявления';
        }
      });
    },
    error: (err) => {
      console.error('Ошибка получения user ID:', err);
      this.errorMessage = 'Ошибка аутентификации';
    }
  });
}
} 
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AdvertisementService } from '../../data/services/advertisement.service';
import { Advertisement } from '../../data/Interfaces/advertisement.interface';
import { ImageService } from '../../data/services/image.service';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-view-advertisement',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './view-advertisement.component.html',
  styleUrl: './view-advertisement.component.scss'
})
export class ViewAdvertisementComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private advertisementService = inject(AdvertisementService);
  private imageService = inject(ImageService);
  private authService = inject(AuthService);

  advertisement: Advertisement | null = null;
  isLoading = true;
  errorMessage = '';
  isOwner = false;
  currentUserId: number | null = null;

  constructor() {
    this.authService.getCurrentUserId().subscribe({
      next: (userId) => {
        this.currentUserId = userId;
      }
    });

    this.route.params.subscribe(params => {
      const id = params['id'];
      if (id) {
        this.loadAdvertisement(id);
      }
    });
  }

  private loadAdvertisement(id: number): void {
    this.isLoading = true;
    this.advertisementService.getAdvertisementById(id).subscribe({
      next: (advertisement) => {
        this.advertisement = advertisement;
        this.isOwner = this.currentUserId === advertisement.ownerId;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Ошибка при загрузке объявления';
        this.isLoading = false;
        console.error('Ошибка при загрузке объявления:', error);
      }
    });
  }

  getImageUrl(imageId: number): string {
    console.log(imageId);
    return this.imageService.getImage(imageId);
  }

  navigateToEdit(): void {
    if (this.advertisement) {
      this.router.navigate(['/edit-advertisement', this.advertisement.id]);
    }
  }
} 
import {Component, inject, input, signal, effect} from '@angular/core';
import {Profile} from '../../data/Interfaces/profile.interface';
import { ImageService } from '../../data/services/image.service';
import { Observable, take, switchMap } from 'rxjs';
import { CommonModule, AsyncPipe } from '@angular/common';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-profile-header',
  standalone: true,
  imports: [CommonModule, AsyncPipe],
  templateUrl: './profile-header.component.html',
  styleUrl: './profile-header.component.scss'
})
export class ProfileHeaderComponent {
  profile = input.required<Profile>();
  imageService = inject(ImageService);
  authService = inject(AuthService);
  
  imageUrl = signal<string>('/assets/images/avatar_placeholder.png');

  constructor() {
    effect(() => {
      this.authService.getCurrentUserId().pipe(
        take(1),
        switchMap(userId => this.imageService.getProfileImage(userId))
      ).subscribe(url => {
        this.imageUrl.set(url);
      });
    });
  }
}

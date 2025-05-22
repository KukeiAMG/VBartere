import {Component, effect, inject, signal} from '@angular/core';
import {SvgIconComponent} from '../../../common-ui/svg-icon/svg-icon.component';
import {DndDirective} from '../../../common-ui/directives/dnd.directive';
import {FormsModule} from '@angular/forms';
import {ProfileService} from '../../../data/services/profile.service';
import {catchError, tap} from 'rxjs';
import {Profile} from '../../../data/Interfaces/profile.interface';

@Component({
  selector: 'app-avatar-upload',
  imports: [
    SvgIconComponent,
    DndDirective,
    FormsModule
  ],
  templateUrl: './avatar-upload.component.html',
  styleUrl: './avatar-upload.component.scss'
})
export class AvatarUploadComponent {
  profileService = inject(ProfileService);
  preview = signal<string>('/assets/images/avatar_placeholder.png');
  avatar: File | null = null;
  isUploading = signal(false);
  error = signal<string | null>(null);

  constructor() {
    // Устанавливаем начальное превью из текущего профиля
    effect(() => {
      const profile = this.profileService.me();
      if (profile?.avatarUrl) {
        this.preview.set(profile.avatarUrl);
      }
    });
  }

  fileBrowserHandler(event: Event) {
    const file = (event.target as HTMLInputElement)?.files?.[0];
    this.processFile(file);
  }

  onFileDropped(file: File) {
    this.processFile(file);
  }

  processFile(file: File | null | undefined) {
    if(!file || !file.type.match('image')) {
      this.error.set('Пожалуйста, выберите изображение');
      return;
    }

    this.error.set(null);
    this.isUploading.set(true);

    const reader = new FileReader();
    reader.onload = event => {
      this.preview.set(event.target?.result?.toString() ?? '');
    }

    reader.readAsDataURL(file);
    this.avatar = file;

    // Загружаем файл на сервер
    this.profileService.uploadAvatar(file).pipe(
      tap(() => {
        this.isUploading.set(false);
      }),
      catchError(error => {
        this.isUploading.set(false);
        this.error.set('Ошибка при загрузке изображения');
        throw error;
      })
    ).subscribe();
  }

  deleteAvatar() {
    this.isUploading.set(true);
    this.profileService.deleteAvatar().pipe(
      tap(() => {
        this.preview.set('/assets/images/avatar_placeholder.png');
        this.isUploading.set(false);
      }),
      catchError(error => {
        this.isUploading.set(false);
        this.error.set('Ошибка при удалении изображения');
        throw error;
      })
    ).subscribe();
  }
}

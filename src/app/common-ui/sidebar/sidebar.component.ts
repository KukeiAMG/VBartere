import {Component, inject, signal, effect} from '@angular/core';
import {SvgIconComponent} from '../svg-icon/svg-icon.component';
import {CommonModule} from '@angular/common';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {ProfileService} from '../../data/services/profile.service';
import {ImageService} from '../../data/services/image.service';
import {AuthService} from '../../auth/auth.service';
import {switchMap, take} from 'rxjs';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    SvgIconComponent,
    CommonModule,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  imageService = inject(ImageService);
  profileService = inject(ProfileService);
  authService = inject(AuthService);
  
  me = this.profileService.me;
  imageUrl = signal<string>('/assets/images/avatar_placeholder.png');
  balance = 0; // Временное значение, будет заменено на реальное с бэкенда

  menuItems = [
    {
      label: 'Мой профиль',
      icon: 'home',
      link: 'profile/me'
    },
    {
      label: 'Чаты',
      icon: 'chat',
      link: 'chat'
    },
    {
      label: 'Поиск',
      icon: 'search',
      link: ''
    },
    {
      label: 'Корзина',
      icon: 'cart',
      link: 'cart'
    }
  ];

  constructor() {
    this.profileService.loadMe(true);

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

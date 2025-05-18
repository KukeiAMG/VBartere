import {Component, inject} from '@angular/core';
import {SvgIconComponent} from '../svg-icon/svg-icon.component';
import {AsyncPipe, JsonPipe, NgForOf} from '@angular/common';
import {SubscribedItemsComponent} from './subscribed-items/subscribed-items.component';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {ProfileService} from '../../data/services/profile.service';
import {firstValueFrom} from 'rxjs';
import {ImgUrlsPipe} from '../../helpers/pipes/img-urls.pipe';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    SvgIconComponent,
    NgForOf,
    SubscribedItemsComponent,
    RouterLink,
    AsyncPipe,
    JsonPipe,
    ImgUrlsPipe,
    RouterLinkActive

  ],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  profileService: ProfileService = inject(ProfileService)

  subscribed$ = this.profileService.getSubscribedShortList()

  me = this.profileService.me;

  menuItems = [
    {
      label: 'Мой профиль',
      icon: 'home',
      link: 'profile/me'
    },
    {
      label: 'Чаты',
      icon: 'chat',
      link: 'chats'
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
  ]

  ngOnInit(): void {
    firstValueFrom(this.profileService.getMe())
  }

}

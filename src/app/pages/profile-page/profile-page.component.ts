import {Component, inject} from '@angular/core';
import {AnimatedBackgroundComponent} from '../../my-shenanigans/animated-background/animated-background.component';
import {ActivatedRoute, RouterLink, RouterOutlet} from '@angular/router';
import {ProfileHeaderComponent} from '../../common-ui/profile-header/profile-header.component';
import {ProfileService} from '../../data/services/profile.service';
import {combineLatest, map, switchMap} from 'rxjs';
import {toObservable} from '@angular/core/rxjs-interop';
import {AsyncPipe, NgForOf} from '@angular/common';
import {SvgIconComponent} from '../../common-ui/svg-icon/svg-icon.component';
import {SubscribedItemsComponent} from '../../common-ui/sidebar/subscribed-items/subscribed-items.component';
import {ImgUrlsPipe} from '../../helpers/pipes/img-urls.pipe';
import {AdvertisementService} from '../../data/services/advertisement.service';
import {TovarCardComponent} from '../../common-ui/tovar-card/tovar-card.component';
import {Advertisement} from '../../data/Interfaces/advertisement.interface';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-profile-page',
  imports: [
    AnimatedBackgroundComponent,
    RouterOutlet,
    ProfileHeaderComponent,
    AsyncPipe,
    RouterLink,
    SvgIconComponent,
    NgForOf,
    SubscribedItemsComponent,
    ImgUrlsPipe,
    TovarCardComponent
  ],
  templateUrl: './profile-page.component.html',
  styleUrl: './profile-page.component.scss'
})
export class ProfilePageComponent {
  profileService = inject(ProfileService);
  advertisementService = inject(AdvertisementService);
  authService = inject(AuthService);
  route = inject(ActivatedRoute);

  profileId = this.route.snapshot.params['id'];
  me$ = this.profileService.getMe()
  subscribed$ = this.profileService.getSubscribedShortList(5)

  userAdvertisements$ = this.route.params.pipe(
    switchMap(({id}) => {
      return this.authService.getCurrentUserId().pipe(
        switchMap(userId => {
          return this.advertisementService.getUserAdvertisements(userId);
        })
      );
    })
  );

  ngOnInit() {
    console.log('Profile ID from URL:', this.profileId);
    this.profileService.loadMe(true);
  }

  isMyProfile$ = combineLatest([
    this.route.params,
    this.me$
  ]).pipe(
    map(([params, me]) => {
      return params['id'] === 'me' || me?.id === params['id'];
    })
  );

  profile$ = this.route.params.pipe(
    switchMap(({id}) => {
      return id === 'me'
        ? this.me$
        : this.profileService.getAccount(id);
    })
  );
}

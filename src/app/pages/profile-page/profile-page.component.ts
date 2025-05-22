import {Component, inject} from '@angular/core';
import {AnimatedBackgroundComponent} from '../../my-shenanigans/animated-background/animated-background.component';
import {ActivatedRoute, RouterLink, RouterOutlet} from '@angular/router';
import {ProfileHeaderComponent} from '../../common-ui/profile-header/profile-header.component';
import {ProfileService} from '../../data/services/profile.service';
import {combineLatest, map, switchMap, of} from 'rxjs';
import {toObservable} from '@angular/core/rxjs-interop';
import {AsyncPipe, NgForOf} from '@angular/common';
import {SvgIconComponent} from '../../common-ui/svg-icon/svg-icon.component';
import {AdvertisementService} from '../../data/services/advertisement.service';
import {TovarCardComponent} from '../../common-ui/tovar-card/tovar-card.component';
import {Advertisement} from '../../data/Interfaces/advertisement.interface';
import { AuthService } from '../../auth/auth.service';
import {BehaviorSubject} from 'rxjs';

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

  private userId$ = new BehaviorSubject<number | null>(null);

  profileId = this.route.snapshot.params['id'];
  me$ = this.profileService.getMe()
  subscribed$ = this.profileService.getSubscribedShortList(5)

  userAdvertisements$ = this.route.params.pipe(
    switchMap(({id}) => {
      if (id === 'me') {
        if (!this.userId$.value) {
          this.authService.getCurrentUserId().subscribe(userId => {
            if (userId) {
              this.userId$.next(userId);
            }
          });
        }
        return this.userId$.pipe(
          switchMap(userId => {
            if (!userId) {
              return of([]);
            }
            return this.advertisementService.getAllAdvertisements().pipe(
              map(advertisements => advertisements.filter(adv => adv.ownerId === userId))
            );
          })
        );
      }
      // Для просмотра чужого профиля
      return this.advertisementService.getAllAdvertisements().pipe(
        map(advertisements => advertisements.filter(adv => adv.ownerId === +id))
      );
    })
  );

  ngOnInit() {
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

import {Component, inject} from '@angular/core';
import {TovarCardComponent} from '../../common-ui/tovar-card/tovar-card.component';
import {ProfileService} from '../../data/services/profile.service';
import {Profile} from '../../data/Interfaces/profile.interface';
import {AnimatedBackgroundComponent} from '../../my-shenanigans/animated-background/animated-background.component';
import {RouterOutlet} from '@angular/router';
import { AdvertisementService } from '../../data/services/advertisement.service';
import { Advertisement } from '../../data/Interfaces/advertisement.interface';
import { AdvertisementsFiltersComponent } from './advertisements-filters/advertisements-filters.component';
import { FilterService } from '../../data/services/filter.service';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-search-page',
  standalone: true,
  imports: [
    AdvertisementsFiltersComponent,
    TovarCardComponent,
    AnimatedBackgroundComponent,
    RouterOutlet
  ],
  templateUrl: './search-page.component.html',
  styleUrl: './search-page.component.scss'
})
export class SearchPageComponent {
  advertisementService = inject(AdvertisementService)
  filterService = inject(FilterService)
  authService = inject(AuthService)
  advertisements = this.filterService.getFilteredAdvertisements()
  currentUserId: number | null = null;

  constructor(){
    this.advertisementService.getAllAdvertisements()
      .subscribe(val => {
        this.filterService.setFilteredAdvertisements(val)
      })
    
    this.authService.getCurrentUserId().subscribe(userId => {
      this.currentUserId = userId;
    });
  }

  isMyAdvertisement(advertisement: Advertisement): boolean {
    return this.currentUserId === advertisement.ownerId;
  }
}

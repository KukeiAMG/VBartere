import {Component, inject} from '@angular/core';
import {TovarCardComponent} from '../../common-ui/tovar-card/tovar-card.component';
import {ProfileService} from '../../data/services/profile.service';
import {Profile} from '../../data/Interfaces/profile.interface';
import {AnimatedBackgroundComponent} from '../../my-shenanigans/animated-background/animated-background.component';
import {RouterOutlet} from '@angular/router';
import { AdvertisementService } from '../../data/services/advertisement.service';
import { Advertisement } from '../../data/Interfaces/advertisement.interface';

@Component({
  selector: 'app-search-page',
  standalone: true,
  imports: [
    TovarCardComponent,
    AnimatedBackgroundComponent,
    RouterOutlet
  ],
  templateUrl: './search-page.component.html',
  styleUrl: './search-page.component.scss'
})
export class SearchPageComponent {
  advertisementService = inject(AdvertisementService)
  advertisements : Advertisement[] = []

  constructor(){
    this.advertisementService.getAllAdvertisements()
      .subscribe(val => {
        this.advertisements = val
      })
  }
}

import {Component, inject} from '@angular/core';
import {TovarCardComponent} from '../../common-ui/tovar-card/tovar-card.component';
import {ProfileService} from '../../data/services/profile.service';
import {Profile} from '../../data/Interfaces/profile.interface';
import {AnimatedBackgroundComponent} from '../../my-shenanigans/animated-background/animated-background.component';
import {RouterOutlet} from '@angular/router';

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
  profileService = inject(ProfileService)
  profiles : Profile[] = []

  constructor(){
    this.profileService.getTestAccounts()
      .subscribe(val => {
        this.profiles = val
      })
  }
}

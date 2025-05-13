import { Component} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {TovarCardComponent} from './common-ui/tovar-card/tovar-card.component';
import {ProfileService} from './data/services/profile.service';
import {Profile} from './data/Interfaces/profile.interface';
import {AnimatedBackgroundComponent} from './my-shenanigans/animated-background/animated-background.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, TovarCardComponent, AnimatedBackgroundComponent],


  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title: string = 'barterlee';
}

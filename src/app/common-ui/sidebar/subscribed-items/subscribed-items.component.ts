import {Component, Input} from '@angular/core';
import {Profile} from '../../../data/Interfaces/profile.interface';
import {ImgUrlsPipe} from '../../../helpers/pipes/img-urls.pipe';

@Component({
  selector: 'app-subscribed-items',
  imports: [
    ImgUrlsPipe
  ],
  templateUrl: './subscribed-items.component.html',
  styleUrl: './subscribed-items.component.scss'
})
export class SubscribedItemsComponent {
  @Input() profile!: Profile;
}

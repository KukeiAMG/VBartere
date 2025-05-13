import {Component, Input} from '@angular/core';
import {Profile} from '../../data/Interfaces/profile.interface';
import {ImgUrlsPipe} from '../../helpers/pipes/img-urls.pipe';

@Component({
  selector: 'app-tovar-card',
  imports: [
    ImgUrlsPipe
  ],
  templateUrl: './tovar-card.component.html',
  styleUrl: './tovar-card.component.scss'
})
export class TovarCardComponent {
 @Input() profile!: Profile;


}

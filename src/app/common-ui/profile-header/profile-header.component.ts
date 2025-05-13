import {Component, input} from '@angular/core';
import {Profile} from '../../data/Interfaces/profile.interface';
import {ImgUrlsPipe} from '../../helpers/pipes/img-urls.pipe';

@Component({
  selector: 'app-profile-header',
  imports: [
    ImgUrlsPipe
  ],
  templateUrl: './profile-header.component.html',
  styleUrl: './profile-header.component.scss'
})
export class ProfileHeaderComponent {
  profile = input<Profile>()
}

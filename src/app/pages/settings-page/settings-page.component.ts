import {Component, effect, inject, ViewChild} from '@angular/core';
import {ProfileHeaderComponent} from '../../common-ui/profile-header/profile-header.component';
import {FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ProfileService} from '../../data/services/profile.service';
import {Profile} from '../../data/Interfaces/profile.interface';
import {first, firstValueFrom, tap} from 'rxjs';
import {AvatarUploadComponent} from './avatar-upload/avatar-upload.component';
import {Router, RouterLink} from '@angular/router';

@Component({
  selector: 'app-settings-page',
  imports: [
    ProfileHeaderComponent,
    ReactiveFormsModule,
    AvatarUploadComponent,
    RouterLink
  ],
  templateUrl: './settings-page.component.html',
  styleUrl: './settings-page.component.scss'
})
export class SettingsPageComponent {

  fb = inject(FormBuilder);
  profileService = inject(ProfileService);
  router = inject(Router);

  @ViewChild(AvatarUploadComponent) avatarUploader!: AvatarUploadComponent;

  form = this.fb.group({
    name: ['', [Validators.required]],
    surname: [''],
    phoneNumber: [{value: '', disabled: true},  [Validators.required]],
    description: [''],
    tags: [''],
  })

  constructor() {
    effect( () => {
      //@ts-ignore
      this.form.patchValue(this.profileService.me())
    });
  }

  ngAfterViewInit() {
  }

  async onSave() {
    if (this.form.invalid) return;

    try {
      // Ждём завершения запроса
      await firstValueFrom(
        this.profileService.patchProfile(this.form.value as Profile).pipe(first())
      );
      this.router.navigate(['profile/me']);
    } catch (err) {
      console.error('Ошибка:', err);
    }
  }
}

import {Component, effect, inject, ViewChild} from '@angular/core';
import {ProfileHeaderComponent} from '../../common-ui/profile-header/profile-header.component';
import {FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ProfileService} from '../../data/services/profile.service';
import {Profile} from '../../data/Interfaces/profile.interface';
import {catchError, first, firstValueFrom, lastValueFrom, take, tap} from 'rxjs';
import {AvatarUploadComponent} from './avatar-upload/avatar-upload.component';
import {Router, RouterLink} from '@angular/router';
import {AuthService} from '../../auth/auth.service';

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
  authService= inject(AuthService);

  @ViewChild(AvatarUploadComponent) avatarUploader!: AvatarUploadComponent;

  form = this.fb.group({
    name: ['', [Validators.required]],
    surname: [''],
    phoneNumber: [{value: '', disabled: true},  [Validators.required]],
    email: [{value:'', disabled:true}, [Validators.required]],
    description: [''],
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
    // Проверяем валидность формы и помечаем все поля как touched
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      console.warn('Форма невалидна', this.form.errors);
      return;
    }

    try {
      // Создаем DTO только с нужными полями (защита от лишних данных)
      const profileData = {
        name: this.form.value.name,
        surname: this.form.value.surname,
        email: this.form.value.email,
        phoneNumber: this.form.value.phoneNumber,
        description: this.form.value.description
      };

      // Отправляем запрос на обновление
      const updatedProfile = await lastValueFrom(
        //@ts-ignore
        this.profileService.patchProfile(profileData).pipe(
          take(1) // Берем только первый результат
        )
      );

      this.router.navigate(['profile/me']);

    } catch (error) {
        console.error('Неизвестная ошибка:', error);
      }
    }

}

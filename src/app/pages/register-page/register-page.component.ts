import {Component, inject, signal} from '@angular/core';
import {AnimatedBackgroundComponent} from '../../my-shenanigans/animated-background/animated-background.component';
import {AnimBack2Component} from '../../my-shenanigans/anim-back2/anim-back2.component';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../auth/auth.service';
import {from, map, take} from 'rxjs';
import {Router, RouterLink} from '@angular/router';
import {SvgIconComponent} from "../../common-ui/svg-icon/svg-icon.component";
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-register-page',
  imports: [
    AnimBack2Component,
    ReactiveFormsModule,
    RouterLink,
    SvgIconComponent,
    NgIf
  ],
  templateUrl: './register-page.component.html',
  styleUrl: './register-page.component.scss'
})
export class RegisterPageComponent {

  authService = inject(AuthService);
  router = inject(Router);

  isPasswordVisible = signal<boolean>(false)

  passwordMismatchError: string | null = null;

  form = new FormGroup({
    phoneNumber: new FormControl<string | null>(null, Validators.required),
    password: new FormControl<string | null>(null, Validators.required),
    password2: new FormControl<string | null>(null, Validators.required),
    email: new FormControl<string | null>(null, Validators.required),
    invitedByCode: new FormControl<string | null>(null)
  });

  onSubmit() {
    this.passwordMismatchError = null; // Сбрасываем ошибку перед проверкой

    if (this.form.valid) {
      if (this.form.value.password == this.form.value.password2) {
        const registerData = {
          phoneNumber: this.form.value.phoneNumber,
          password: this.form.value.password,
          email: this.form.value.email,
          invitedByCode: this.form.value.invitedByCode
        };
        //@ts-ignore
        this.authService.register(registerData)
          .subscribe({
            next: (res) => {
              this.router.navigate(['']);
            },
            error: (err) => {
              // Обработка ошибок от сервера
              console.error('Ошибка регистрации:', err);
            }
          });
      } else {
        this.passwordMismatchError = 'Пароли не совпадают';
      }
    }
  }

}

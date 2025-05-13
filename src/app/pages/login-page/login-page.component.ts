import {Component, inject, signal} from '@angular/core';
import {AnimatedBackgroundComponent} from '../../my-shenanigans/animated-background/animated-background.component';
import {AnimBack2Component} from '../../my-shenanigans/anim-back2/anim-back2.component';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../auth/auth.service';
import {from, map, take} from 'rxjs';
import {Router} from '@angular/router';

@Component({
  selector: 'app-login-page',
  imports: [
    AnimBack2Component,
    ReactiveFormsModule
  ],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.scss'
})
export class LoginPageComponent {

  authService = inject(AuthService);
  router = inject(Router);

  isPasswordVisible = signal<boolean>(false)

  form = new FormGroup({
    phoneNumber: new FormControl<string | null>(null, Validators.required),
    password: new FormControl<string | null>(null, Validators.required)
  });

  onSubmit() {
    if(this.form.valid) {
      //@ts-ignore
      this.authService.login(this.form.value)
        .subscribe(res =>{
          this.router.navigate(['']);
        })
    }

  }

}

import { Routes } from '@angular/router';
import {LoginPageComponent} from './pages/login-page/login-page.component';
import {SearchPageComponent} from './pages/search-page/search-page.component';
import {ProfilePageComponent} from './pages/profile-page/profile-page.component';
import {LayoutComponent} from './common-ui/layout/layout.component';
import {canActivateAuth} from './auth/access.guard';
import {SettingsPageComponent} from './pages/settings-page/settings-page.component';
import {RegisterPageComponent} from './pages/register-page/register-page.component';
import {CartPageComponent} from './pages/cart-page/cart-page.component';
import {CreateAdvertisementComponent} from './pages/create-advertisement/create-advertisement.component';


export const routes: Routes = [
  {
    path: '', component: LayoutComponent, children: [
      {path: '', component: SearchPageComponent},
      {path: 'profile', component: ProfilePageComponent},
      {path: 'profile/:id', component: ProfilePageComponent},
      {path: 'settings', component: SettingsPageComponent},
      {path: 'cart', component: CartPageComponent},
      {path: 'create-advertisement', component: CreateAdvertisementComponent},
    ],
    canActivate: [canActivateAuth]
  },
  {path: 'login', component: LoginPageComponent},
  {path: 'register', component: RegisterPageComponent},
];

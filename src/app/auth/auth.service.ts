import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {catchError, tap, throwError} from 'rxjs';
import {TokenResponse} from './auth.interface';
import {CookieService} from 'ngx-cookie-service';
import {Router} from '@angular/router';
import {jwtDecode} from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  http = inject(HttpClient)
  router = inject(Router)
  cookieService = inject(CookieService)
  baseApiUrl = `http://localhost:8081/api/users/`

  token: string | null = null;
  refreshToken: string | null = null;

  get isAuth(){
    if(!this.token){
      this.token = this.cookieService.get('token');
      this.refreshToken = this.cookieService.get('refreshToken');
    }
    return !! this.token;
  }

  login(payload: {phoneNumber:string,password:string}) {
    const fd = new FormData()

    return this.http.post<TokenResponse>(
      `${this.baseApiUrl}login`,
      payload,
    ).pipe(
      tap(val => this.saveTokens(val))
    )
  }

  refreshAuthToken(){
    return this.http.post<TokenResponse>(
      `${this.baseApiUrl}refresh-token`,
      {refresh_token: this.refreshToken}
    ).pipe(
      tap(val => this.saveTokens(val)),
      catchError(err =>{
        this.logout()
        return throwError(err)
      })
    )
  }

  logout(){
    this.cookieService.deleteAll()
    this.token = null;
    this.refreshToken = null;
    this.router.navigate(['/login'])
  }

  saveTokens(res: TokenResponse) {
    this.token = res.accessToken;
    this.refreshToken = res.refreshToken;

    this.cookieService.set('token', this.token);
    this.cookieService.set('refreshToken', this.refreshToken!);
  }

}


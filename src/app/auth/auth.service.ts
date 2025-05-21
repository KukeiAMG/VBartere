import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {catchError, Observable, tap, throwError} from 'rxjs';
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
  baseApiUrl2 = `http://localhost:8081/api/jwt/`

  token: string | null = null;
  refreshToken: string | null = null;
  currentUserId: number | null = null;

  get isAuth(){
    if(!this.token){
      this.token = this.cookieService.get('token');
      this.refreshToken = this.cookieService.get('refreshToken');
    }
    return !! this.token;
  }

  getCurrentUserId(): Observable<number> {
    return this.http.get<number>(
      `${this.baseApiUrl2}getCurrentUserId`
    )
  }

  private getAuthToken(): string {
    return this.cookieService.get('token') || '';
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

  register(payload: {
    phoneNumber: string,
    password: string,
    email: string,
    invitedByCode?: string
  }) {
    return this.http.post(
      `${this.baseApiUrl}register`,
      payload
    );
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
    this.currentUserId = null;
    this.router.navigate(['/login'])
  }

  saveTokens(res: TokenResponse) {
    this.token = res.accessToken;
    this.refreshToken = res.refreshToken;
    this.getCurrentUserId().subscribe({
      next: (userId) => {
        this.currentUserId = userId;
      }
    });

    this.cookieService.set('token', this.token);
    this.cookieService.set('refreshToken', this.refreshToken!);
  }

}


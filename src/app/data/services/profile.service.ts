import {inject, Injectable, signal} from '@angular/core';
import {HttpClient, HttpHeaders, provideHttpClient} from '@angular/common/http';
import {Profile} from '../Interfaces/profile.interface';
import {Pageable} from '../Interfaces/pageable.interface';
import {catchError, first, map, Observable, of, tap, throwError, switchMap} from 'rxjs';
import {CookieService} from 'ngx-cookie-service';



@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  http : HttpClient = inject(HttpClient)
  cookieService = inject(CookieService)

  baseApiUrl = `http://localhost:8081/api/users/`
  imageApiUrl = `http://localhost:8081/images/`

  private _isLoaded = false; // Флаг загрузки

  me = signal <Profile | null>(null)
  
  getTestAccounts(){
  return this.http.get<Profile[]>(`${this.baseApiUrl}all`)
  }

  getMe(force = false): Observable<Profile> {
    if (force || !this._isLoaded) {
      return this.http.get<Profile>(`${this.baseApiUrl}me`).pipe(
        tap(res => {
          this.me.set(res);
          this._isLoaded = true;
        })
      );
    }
    return of(this.me()!); // Возвращаем кэш
  }

  getAccount(id:string){
    return this.http.get<Profile>(`${this.baseApiUrl}${id}/get`)
  }

  getSubscribedShortList(subAmount = 3){
    return this.http.get<Pageable<Profile>>(`${this.baseApiUrl}account/subscribers/`)
      .pipe(
        map(res => res.items.slice(0, subAmount))
      )
  }

  isMyProfile(profileId: string | number): Observable<boolean> {
    return this.getMe().pipe(
      map(me => me?.id === profileId || profileId === 'me'),
      catchError(() => of(false))
    );
  }

  loadMe(force: boolean = false) {
    if (force || !this.me()) {
      this.getMe().subscribe();
    }
  }

  private sanitizeProfileData(data: Partial<Profile>): Partial<Profile> {
    const allowedFields: (keyof Profile)[] = [
      'name',
      'surname',
      'email',
      'phoneNumber',
      'avatarUrl',
    ];

    const result: Partial<Profile> = {};

    for (const key in data) {
      if (allowedFields.includes(key as keyof Profile) && data[key as keyof Profile] !== undefined) {
        result[key as keyof Profile] = data[key as keyof Profile] as never;
      }
    }

    return result;
  }

  patchProfile(profileData: Partial<Profile>): Observable<Profile> {

    return this.http.put<Profile>(
      `${this.baseApiUrl}update-my-account`,
      profileData
    ).pipe(
      catchError(error => {
        console.error('Ошибка при обновлении профиля:', error);
        return throwError(() => this.handleError(error));
      })
    );
  }

  private getAuthToken(): string {
    return this.cookieService.get('token') || '';
  }

  uploadAvatar(file: File) {
    const fd = new FormData();
    fd.append('file', file);
    console.log('Отправляем файл:', file.name);
    
    return this.http.post<Profile>(
      `${this.imageApiUrl}upload`,
      fd
    ).pipe(
      tap(profile => {
        console.log('Получен ответ от сервера:', profile);
        console.log('Текущий профиль:', this.me());
        
        // Сохраняем только URL аватара, не трогая остальные поля
        const currentProfile = this.me();
        if (currentProfile) {
          const updatedProfile = {
            ...currentProfile,
            avatarUrl: profile.avatarUrl
          };
          console.log('Обновленный профиль:', updatedProfile);
          this.me.set(updatedProfile);
        }
        this._isLoaded = true;
      })
    );
  }

  deleteAvatar() {
    return this.http.delete<Profile>(
      `${this.imageApiUrl}delete-my-profile-photo`
    ).pipe(
      tap(profile => {
        this.me.set(profile);
        this._isLoaded = true;
      })
    );
  }

  private handleError(error: any): Error {
    if (error.status === 401) {
      return new Error('Требуется авторизация');
    } else if (error.status === 403) {
      return new Error('Нет прав для редактирования');
    } else if (error.status === 404) {
      return new Error('Пользователь не найден');
    } else if (error.error?.message) {
      return new Error(error.error.message);
    }
    return new Error('Ошибка при обновлении профиля');
  }



}

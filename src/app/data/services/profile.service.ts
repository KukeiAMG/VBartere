import {inject, Injectable, signal} from '@angular/core';
import {HttpClient, HttpHeaders, provideHttpClient} from '@angular/common/http';
import {Profile} from '../Interfaces/profile.interface';
import {Pageable} from '../Interfaces/pageable.interface';
import {catchError, first, map, Observable, of, tap} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  http : HttpClient = inject(HttpClient)

  baseApiUrl = `http://localhost:8081/api/users/`

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

  patchProfile(profile: Partial<Profile>) {
    console.log(profile);
    return this.http.patch<Profile>(
      `${this.baseApiUrl}account/me`,
      profile
    ).pipe(first())
  }

  uploadAvatar(file: File){
    const fd = new FormData();
    fd.append('image', file);
    return this.http.post<Profile>(
      `${this.baseApiUrl}account/upload_image`
      , fd)
  }

}

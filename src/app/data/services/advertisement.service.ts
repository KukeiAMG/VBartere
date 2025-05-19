import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Advertisement, AdvertisementDTO } from '../Interfaces/advertisement.interface';
import { Observable, catchError, map, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdvertisementService {
  baseApiUrl = `http://localhost:8090/api/advertisements/`

  constructor(private http: HttpClient) {}

  // Получить все объявления
  getAllAdvertisements(): Observable<Advertisement[]> {
    return this.http.get<Advertisement[]>(`${this.baseApiUrl}all`);
  }

  // Получить объявление по ID
  getAdvertisementById(id: number): Observable<Advertisement> {
    return this.http.get<Advertisement>(`${this.baseApiUrl}${id}`);
  }

  // Создать новое объявление
  createAdvertisement(
    advertisement: AdvertisementDTO,
    files: File[],
    userId: number
  ): Observable<any> {
    const formData = new FormData();
  
    // Добавляем advertisement как JSON-строку с правильным content-type
    const advertisementBlob = new Blob(
      [JSON.stringify(advertisement)], 
      { type: 'application/json' }
    );
    formData.append('advertisement', advertisementBlob);
  

    files.forEach(file => {
      formData.append('files', file, file.name);
    });
  

    const headers = new HttpHeaders({
      'user-ID': userId
    });
  
    return this.http.post(
      `${this.baseApiUrl}create`,
      formData,
      { 
        headers,
        responseType: 'json' 
      }
    ).pipe(
      catchError(error => {
        console.error('Error creating advertisement:', error);
        return throwError(() => error);
      })
    );
  }

  // Обновить объявление
  updateAdvertisement(id: number, advertisement: AdvertisementDTO, files?: File[]): Observable<any> {
    const formData = new FormData();
  
    // Добавляем advertisement как JSON-строку с правильным content-type
    const advertisementBlob = new Blob(
      [JSON.stringify(advertisement)], 
      { type: 'application/json' }
    );
    formData.append('advertisement', advertisementBlob);

    // Добавляем файлы только если они есть
    if (files && files.length > 0) {
      files.forEach(file => {
        formData.append('files', file, file.name);
      });
    } else {
      formData.append('files', new Blob());
    }
  
    return this.http.put(
      `${this.baseApiUrl}${id}/update`,
      formData,
      { 
        responseType: 'json' 
      }
    ).pipe(
      catchError(error => {
        console.error('Error updating advertisement:', error);
        return throwError(() => error);
      })
    );
  }

  // Удалить объявление
  deleteAdvertisement(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseApiUrl}${id}`);
  }

  // Получить объявления пользователя
  getUserAdvertisements(userId: number): Observable<Advertisement[]> {
    return this.http.get<Advertisement[]>(`${this.baseApiUrl}all`).pipe(
      map(advertisements => advertisements.filter(ad => ad.ownerId === userId))
    );
  }

  // Поиск объявлений
  searchAdvertisements(query: string): Observable<Advertisement[]> {
    return this.http.get<Advertisement[]>(`${this.baseApiUrl}search`, {
      params: { query }
    });
  }

  // Добавить объявление в избранное
  addToFavorites(advertisementId: number): Observable<void> {
    return this.http.post<void>(`${this.baseApiUrl}${advertisementId}favorite`, {});
  }

  // Удалить объявление из избранного
  removeFromFavorites(advertisementId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseApiUrl}${advertisementId}favorite`);
  }

  // Получить избранные объявления
  getFavoriteAdvertisements(): Observable<Advertisement[]> {
    return this.http.get<Advertisement[]>(`${this.baseApiUrl}favorites`);
  }
} 
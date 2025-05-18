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
  
    // 1. Добавляем advertisement как JSON-строку с правильным content-type
    const advertisementBlob = new Blob(
      [JSON.stringify(advertisement)], 
      { type: 'application/json' }
    );
    formData.append('advertisement', advertisementBlob);
  
    // 2. Добавляем файлы с ключом "files"
    files.forEach(file => {
      formData.append('files', file, file.name);
    });
  
    // 3. Устанавливаем заголовки
    const headers = new HttpHeaders({
      'user-ID': userId.toString()
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
  updateAdvertisement(id: number, advertisement: Partial<Advertisement>): Observable<Advertisement> {
    return this.http.put<Advertisement>(`${this.baseApiUrl}${id}`, advertisement);
  }

  // Удалить объявление
  deleteAdvertisement(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseApiUrl}${id}`);
  }

  // Получить объявления пользователя
  getUserAdvertisements(userId: number): Observable<Advertisement[]> {
    return this.http.get<Advertisement[]>(`${this.baseApiUrl}user/${userId}`);
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
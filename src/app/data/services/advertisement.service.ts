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
    files: File[]
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
    return this.http.delete<void>(`${this.baseApiUrl}${id}/delete`);
  }

  // Фильтрация объявлений
  filterAdvertisements(filters: {
    title?: string;
    description?: string;
    subCategoryId?: number;
    sortBy?: 'А-я' | 'Я-а' ;
  }): Observable<Advertisement[]> {
    return this.getAllAdvertisements().pipe(
      map(advertisements => {
        let filtered = [...advertisements];

        // Фильтрация по названию
        if (filters.title) {
          filtered = filtered.filter(ad => 
            ad.title.toLowerCase().includes(filters.title!.toLowerCase())
          );
        }

        // Фильтрация по описанию
        if (filters.description) {
          filtered = filtered.filter(ad => 
            ad.description.toLowerCase().includes(filters.description!.toLowerCase())
          );
        }

        // Фильтрация по категории
        if (filters.subCategoryId) {
          filtered = filtered.filter(ad => ad.subCategoryId === filters.subCategoryId);
        }

        // Сортировка
        if (filters.sortBy) {
          switch (filters.sortBy) {
            case 'А-я':
              filtered.sort((a, b) => a.title.localeCompare(b.title));
              break;
            case 'Я-а':
              filtered.sort((a, b) => b.title.localeCompare(a.title));
              break;
          }
        }

        return filtered;
      })
    );
  }

  updateAdvertisementFields(id: number, advertisementDTO: AdvertisementDTO): Observable<AdvertisementDTO> {
    return this.http.put<AdvertisementDTO>(`${this.baseApiUrl}${id}/update-fields`, advertisementDTO);
  }

  updateAdvertisementImages(id: number, files: File[]): Observable<AdvertisementDTO> {
    const formData = new FormData();
    files.forEach(file => {
      formData.append('files', file);
    });
    return this.http.put<AdvertisementDTO>(`${this.baseApiUrl}${id}/update-images`, formData);
  }

  // Получить объявления по массиву ID
  getAdvertisementsByIds(ids: number[]): Observable<Advertisement[]> {
    const queryParams = ids.map(id => `ids=${id}`).join('&');
    return this.http.get<Advertisement[]>(`${this.baseApiUrl}by-ids?${queryParams}`).pipe(
      catchError(error => {
        console.error('Error getting advertisements by ids:', error);
        return throwError(() => error);
      })
    );
  }
} 
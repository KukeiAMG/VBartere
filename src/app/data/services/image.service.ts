import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, map, Observable, of, switchMap, tap } from 'rxjs';

interface ImageMetadata {
  id: number;
  name: string;
  originalFileName: string;
  contentType: string;
  size: number;
  previewImage: boolean;
  url: string;
}

@Injectable({
  providedIn: 'root'
})
export class ImageService {
  private basePictureUrl = 'http://localhost:8090/images/';
  private profilePictureUrl = 'http://localhost:8081/images/';

  constructor(private http: HttpClient) {}

  getImage(id: number): any  {
    return `${this.basePictureUrl}${id}`;
  }

  getProfileImage(userId: number): Observable<string> {
    return this.http.get<ImageMetadata>(`${this.profilePictureUrl}${userId}/metadata`).pipe(
      tap(response => {
        console.log('Raw metadata response:', response);
      }),
      map(metadata => {
        if (!metadata?.id || !metadata?.name) {
          console.log('Metadata validation failed:', {
            hasId: !!metadata?.id,
            hasName: !!metadata?.name,
            metadata
          });
          return '/assets/images/avatar_placeholder.png';
        }
        console.log('Metadata validation passed:', metadata);
        return `${this.profilePictureUrl}${metadata.id}`;
      }),
      catchError(error => {
        console.error('Error fetching metadata:', error);
        return of('/assets/images/avatar_placeholder.png');
      })
    );
  }
} 
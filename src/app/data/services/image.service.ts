import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ImageService {
  private basePictureUrl = 'http://localhost:8090/images/';

  constructor(private http: HttpClient) {}

  getImage(id: number): any  {
    return `${this.basePictureUrl}${id}`;
  }

} 
import { Injectable, signal } from '@angular/core';
import { Advertisement } from '../Interfaces/advertisement.interface';

@Injectable({
  providedIn: 'root'
})
export class FilterService {
  private filteredAdvertisements = signal<Advertisement[]>([]);

  setFilteredAdvertisements(advertisements: Advertisement[]) {
    this.filteredAdvertisements.set(advertisements);
  }

  getFilteredAdvertisements() {
    return this.filteredAdvertisements;
  }
} 
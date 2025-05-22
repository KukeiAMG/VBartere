import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { AdvertisementService } from '../../../data/services/advertisement.service';
import { debounceTime, switchMap, tap, map } from 'rxjs/operators';
import { FilterService } from '../../../data/services/filter.service';

@Component({
  selector: 'app-advertisements-filters',
  imports: [
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './advertisements-filters.component.html',
  styleUrl: './advertisements-filters.component.scss'
})
export class AdvertisementsFiltersComponent {
  fb = inject(FormBuilder);
  advertisementService = inject(AdvertisementService);
  filterService = inject(FilterService);
  isLoading = false;

  searchForm = this.fb.group({
    title: [''],
    subCategoryId: [null as number | null],
    description: [''],
    sortBy: ['']
  });

  constructor() {
    this.searchForm.valueChanges.pipe(
      tap(() => this.isLoading = true),
      debounceTime(500),
      map(formValue => ({
        ...formValue,
        subCategoryId: formValue.subCategoryId ? Number(formValue.subCategoryId) : null
      })),
      switchMap(formValue => this.advertisementService.filterAdvertisements(formValue as any)),
      tap(() => this.isLoading = false)
    ).subscribe(filteredAds => {
      this.filterService.setFilteredAdvertisements(filteredAds);
    });
  }
}

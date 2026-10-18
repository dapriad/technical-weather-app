import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';

import { Municipality } from '../models/municipality.model';
import { normalizeMunicipalityName } from '../utils/municipality-name.util';
import { WeatherApiService } from './weather-api.service';

@Injectable({ providedIn: 'root' })
export class MunicipalitySearchStore {
  private readonly api = inject(WeatherApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly remoteSearch$ = new Subject<string>();
  private readonly lastQuery = signal('');

  readonly catalog = signal<Municipality[] | null>(null);
  readonly catalogLoading = signal(false);
  readonly suggestions = signal<Municipality[]>([]);
  readonly hasCatalog = computed(() => this.catalog() !== null);

  constructor() {
    this.remoteSearch$
      .pipe(
        debounceTime(400),
        distinctUntilChanged(),
        switchMap((query) => {
          if (this.catalog() || query.trim().length < 3) {
            return of<Municipality[]>([]);
          }

          return this.api.searchMunicipalities(query.trim());
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((suggestions) => {
        if (!this.catalog()) {
          this.suggestions.set(suggestions);
        }
      });
  }

  loadCatalog(): void {
    if (this.catalogLoading()) {
      return;
    }

    this.catalogLoading.set(true);

    this.api
      .getMunicipalities()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (catalog) => {
          this.catalog.set(catalog);
          this.catalogLoading.set(false);
          this.applyLocalSearch(this.lastQuery());
        },
        error: () => {
          this.catalogLoading.set(false);
        },
      });
  }

  search(term: string): void {
    const query = term.trim();
    this.lastQuery.set(query);

    if (this.catalog()) {
      this.applyLocalSearch(query);
      return;
    }

    this.remoteSearch$.next(query);
  }

  clear(): void {
    this.lastQuery.set('');
    this.suggestions.set([]);
  }

  private applyLocalSearch(query: string): void {
    const catalog = this.catalog();

    if (!catalog) {
      return;
    }

    const normalizedQuery = normalizeMunicipalityName(query);

    if (!normalizedQuery) {
      this.suggestions.set([]);
      return;
    }

    this.suggestions.set(
      catalog.filter((municipality) => normalizeMunicipalityName(municipality.name).includes(normalizedQuery)),
    );
  }
}
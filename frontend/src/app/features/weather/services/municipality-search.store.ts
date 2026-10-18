import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  Subject,
  catchError,
  debounceTime,
  distinctUntilChanged,
  finalize,
  map,
  of,
  switchMap,
} from 'rxjs';

import { Municipality } from '../models/municipality.model';
import { normalizeMunicipalityName } from '../utils/municipality-name.util';
import { WeatherApiService } from './weather-api.service';

const MIN_QUERY_LENGTH = 3;
const MAX_SUGGESTIONS = 20;

function rankAndLimitSuggestions(municipalities: Municipality[], query: string): Municipality[] {
  const normalizedQuery = normalizeMunicipalityName(query);

  if (normalizedQuery.length < MIN_QUERY_LENGTH) {
    return [];
  }

  const prefixMatches: Municipality[] = [];
  const containsMatches: Municipality[] = [];

  for (const municipality of municipalities) {
    const normalizedName = normalizeMunicipalityName(municipality.name);

    if (normalizedName.startsWith(normalizedQuery)) {
      prefixMatches.push(municipality);
    } else if (normalizedName.includes(normalizedQuery)) {
      containsMatches.push(municipality);
    }
  }

  const matches = prefixMatches.length > 0 ? prefixMatches : containsMatches;

  return matches
    .sort((first, second) => first.name.localeCompare(second.name, 'es'))
    .slice(0, MAX_SUGGESTIONS);
}

@Injectable({ providedIn: 'root' })
export class MunicipalitySearchStore {
  private readonly api = inject(WeatherApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly remoteSearch$ = new Subject<string>();
  private readonly lastQuery = signal('');

  readonly catalog = signal<Municipality[] | null>(null);
  readonly catalogLoading = signal(false);
  readonly catalogError = signal(false);
  readonly searching = signal(false);
  readonly searchError = signal(false);
  readonly suggestions = signal<Municipality[]>([]);
  readonly hasCatalog = computed(() => this.catalog() !== null);

  constructor() {
    this.remoteSearch$
      .pipe(
        debounceTime(400),
        distinctUntilChanged(),
        switchMap((query) => {
          this.searchError.set(false);

          if (this.catalog() || query.trim().length < MIN_QUERY_LENGTH) {
            return of<Municipality[]>([]);
          }

          this.searching.set(true);

          return this.api.searchMunicipalities(query.trim()).pipe(
            map((suggestions) => rankAndLimitSuggestions(suggestions, query)),
            catchError(() => {
              this.searchError.set(true);
              return of<Municipality[]>([]);
            }),
            finalize(() => this.searching.set(false)),
          );
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

    this.catalogError.set(false);
    this.catalogLoading.set(true);

    this.api
      .getMunicipalities()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (catalog) => {
          this.catalog.set(catalog);
          this.catalogLoading.set(false);
          this.catalogError.set(false);
          this.applyLocalSearch(this.lastQuery());
        },
        error: () => {
          this.catalogLoading.set(false);
          this.catalogError.set(true);
        },
      });
  }

  search(term: string): void {
    const query = term.trim();
    this.lastQuery.set(query);
    this.searchError.set(false);

    if (this.catalog()) {
      this.applyLocalSearch(query);
      return;
    }

    this.remoteSearch$.next(query);
  }

  clear(): void {
    this.lastQuery.set('');
    this.searchError.set(false);
    this.suggestions.set([]);
  }

  private applyLocalSearch(query: string): void {
    const catalog = this.catalog();

    if (!catalog) {
      return;
    }

    this.suggestions.set(rankAndLimitSuggestions(catalog, query));
  }
}
import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, catchError, finalize, of, switchMap, tap } from 'rxjs';

import { Forecast } from '../models/forecast.model';
import { Municipality } from '../models/municipality.model';
import { TemperatureUnit } from '../models/temperature-unit.model';
import { mapForecastError } from '../utils/weather-error.util';
import { WeatherApiService } from './weather-api.service';

interface ForecastRequest {
  municipalityId: string;
  temperatureUnit: TemperatureUnit;
}

@Injectable({ providedIn: 'root' })
export class ForecastStore {
  private readonly api = inject(WeatherApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly request$ = new Subject<ForecastRequest | null>();

  readonly selectedMunicipality = signal<Municipality | null>(null);
  readonly temperatureUnit = signal<TemperatureUnit>('G_CEL');
  readonly forecast = signal<Forecast | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  constructor() {
    this.request$
      .pipe(
        switchMap((request) => {
          if (!request) {
            this.forecast.set(null);
            this.error.set(null);
            this.loading.set(false);
            return of(null);
          }

          this.loading.set(true);
          this.error.set(null);

          return this.api
            .getForecast(request.municipalityId, request.temperatureUnit)
            .pipe(
              tap((forecast) => this.forecast.set(forecast)),
              catchError((error: unknown) => {
                this.forecast.set(null);
                this.error.set(this.mapError(error));
                return of(null);
              }),
              finalize(() => this.loading.set(false)),
            );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe();
  }

  selectMunicipality(municipality: Municipality): void {
    this.selectedMunicipality.set(municipality);
    this.loadForecast();
  }

  changeUnit(unit: TemperatureUnit): void {
    this.temperatureUnit.set(unit);
    this.loadForecast();
  }

  clearMunicipality(): void {
    this.selectedMunicipality.set(null);
    this.request$.next(null);
  }

  private loadForecast(): void {
    const municipality = this.selectedMunicipality();

    if (!municipality) {
      this.request$.next(null);
      return;
    }

    this.request$.next({
      municipalityId: municipality.id,
      temperatureUnit: this.temperatureUnit(),
    });
  }

  private mapError(error: unknown): string {
    return mapForecastError(error);
  }
}
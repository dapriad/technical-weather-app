import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Forecast } from '../models/forecast.model';
import { Municipality } from '../models/municipality.model';
import { TemperatureUnit } from '../models/temperature-unit.model';

@Injectable({ providedIn: 'root' })
export class WeatherApiService {
  private readonly http = inject(HttpClient);

  getMunicipalities(): Observable<Municipality[]> {
    return this.http.get<Municipality[]>('/api/municipalities');
  }

  searchMunicipalities(name: string): Observable<Municipality[]> {
    const params = new HttpParams().set('name', name);
    return this.http.get<Municipality[]>('/api/municipalities', { params });
  }

  getForecast(municipalityId: string, temperatureUnit?: TemperatureUnit): Observable<Forecast> {
    let params = new HttpParams().set('municipalityId', municipalityId);

    if (temperatureUnit) {
      params = params.set('temperatureUnit', temperatureUnit);
    }

    return this.http.get<Forecast>('/api/forecast', { params });
  }
}
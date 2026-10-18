import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CardModule } from 'primeng/card';
import { MessageModule } from 'primeng/message';
import { SkeletonModule } from 'primeng/skeleton';

import { ForecastCard } from '../../components/forecast-card/forecast-card';
import { MunicipalityAutocomplete } from '../../components/municipality-autocomplete/municipality-autocomplete';
import { TemperatureUnitSelect } from '../../components/temperature-unit-select/temperature-unit-select';
import { Municipality } from '../../models/municipality.model';
import { TemperatureUnit } from '../../models/temperature-unit.model';
import { ForecastStore } from '../../services/forecast.store';
import { MunicipalitySearchStore } from '../../services/municipality-search.store';

@Component({
  imports: [
    CardModule,
    ForecastCard,
    MessageModule,
    MunicipalityAutocomplete,
    SkeletonModule,
    TemperatureUnitSelect,
  ],
  selector: 'app-weather-page',
  styleUrl: './weather-page.scss',
  templateUrl: './weather-page.html',
})
export class WeatherPage implements OnInit {
  protected readonly searchStore = inject(MunicipalitySearchStore);
  protected readonly forecastStore = inject(ForecastStore);
  protected readonly searchTerm = signal<string | null>(null);

  private readonly dateFormatter = new Intl.DateTimeFormat('es-ES', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    timeZone: 'Europe/Madrid',
  });

  protected readonly forecastDate = computed(() => {
    const tomorrow = new Date(Date.now() + 24 * 60 * 60 * 1000);
    const formatted = this.dateFormatter.format(tomorrow);
    return formatted.charAt(0).toUpperCase() + formatted.slice(1);
  });

  ngOnInit(): void {
    this.searchStore.loadCatalog();
  }

  protected onSearch(term: string): void {
    this.searchStore.search(term);
  }

  protected onSelect(municipality: Municipality): void {
    this.searchTerm.set(municipality.name);
    this.searchStore.clear();
    this.forecastStore.selectMunicipality(municipality);
  }

  protected onTyping(): void {
    this.forecastStore.clearMunicipality();
  }

  protected onCleared(): void {
    this.searchTerm.set(null);
    this.searchStore.clear();
    this.forecastStore.clearMunicipality();
  }

  protected onUnitChange(unit: TemperatureUnit | null): void {
    this.forecastStore.changeUnit(unit);
  }
}
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { Forecast } from '../../models/forecast.model';
import { Municipality } from '../../models/municipality.model';
import { TemperatureUnit } from '../../models/temperature-unit.model';
import { ForecastStore } from '../../services/forecast.store';
import { MunicipalitySearchStore } from '../../services/municipality-search.store';
import { WeatherPage } from './weather-page';

describe('WeatherPage', () => {
  let searchStore: {
    suggestions: ReturnType<typeof signal<Municipality[]>>;
    loadCatalog: ReturnType<typeof vi.fn>;
    search: ReturnType<typeof vi.fn>;
    clear: ReturnType<typeof vi.fn>;
  };

  let forecastStore: {
    selectedMunicipality: ReturnType<typeof signal<Municipality | null>>;
    temperatureUnit: ReturnType<typeof signal<TemperatureUnit>>;
    forecast: ReturnType<typeof signal<Forecast | null>>;
    loading: ReturnType<typeof signal<boolean>>;
    error: ReturnType<typeof signal<string | null>>;
    selectMunicipality: ReturnType<typeof vi.fn>;
    changeUnit: ReturnType<typeof vi.fn>;
    clearMunicipality: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    searchStore = {
      suggestions: signal<Municipality[]>([]),
      loadCatalog: vi.fn(),
      search: vi.fn(),
      clear: vi.fn(),
    };

    forecastStore = {
      selectedMunicipality: signal<Municipality | null>(null),
      temperatureUnit: signal<TemperatureUnit>('G_CEL'),
      forecast: signal<Forecast | null>(null),
      loading: signal(false),
      error: signal<string | null>(null),
      selectMunicipality: vi.fn(),
      changeUnit: vi.fn(),
      clearMunicipality: vi.fn(),
    };

    TestBed.configureTestingModule({
      imports: [WeatherPage],
      providers: [
        { provide: MunicipalitySearchStore, useValue: searchStore },
        { provide: ForecastStore, useValue: forecastStore },
      ],
    });
  });

  it('creates the page and loads the municipality catalog', () => {
    const fixture = TestBed.createComponent(WeatherPage);
    fixture.detectChanges();

    expect(fixture.componentInstance).toBeTruthy();
    expect(searchStore.loadCatalog).toHaveBeenCalled();
  });

  it('selects a municipality and clears suggestions', () => {
    const component = TestBed.createComponent(WeatherPage).componentInstance;
    const municipality: Municipality = { id: '28079', name: 'Madrid' };

    (component as any).onSelect(municipality);

    expect(searchStore.clear).toHaveBeenCalled();
    expect(forecastStore.selectMunicipality).toHaveBeenCalledWith(municipality);
  });

  it('changes the temperature unit', () => {
    const component = TestBed.createComponent(WeatherPage).componentInstance;

    (component as any).onUnitChange('G_FAH');

    expect(forecastStore.changeUnit).toHaveBeenCalledWith('G_FAH');
  });

  it('clears forecast when the user types after a selection', () => {
    const component = TestBed.createComponent(WeatherPage).componentInstance;

    (component as any).onTyping();

    expect(forecastStore.clearMunicipality).toHaveBeenCalled();
  });
});
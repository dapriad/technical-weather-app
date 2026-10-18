import { provideHttpClient, withFetch } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { ForecastStore } from './forecast.store';

describe('ForecastStore', () => {
  let store: ForecastStore;
  let httpMock: HttpTestingController;

  const forecast = {
    averageTemperature: 22.8,
    temperatureUnit: 'G_CEL',
    precipitationProbability: [{ probability: 5, period: '00-06' }],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withFetch()),
        provideHttpClientTesting(),
      ],
    });

    store = TestBed.inject(ForecastStore);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('defaults to Celsius', () => {
    expect(store.temperatureUnit()).toBe('G_CEL');
  });

  it('loads forecast when a municipality is selected', () => {
    store.selectMunicipality({ id: '28079', name: 'Madrid' });

    httpMock
      .expectOne((req) => req.url === '/api/forecast' && req.params.get('temperatureUnit') === 'G_CEL')
      .flush(forecast);

    expect(store.forecast()).toEqual(forecast);
    expect(store.loading()).toBe(false);
  });

  it('reloads forecast when the unit changes', () => {
    store.selectMunicipality({ id: '28079', name: 'Madrid' });
    httpMock
      .expectOne((req) => req.url === '/api/forecast' && req.params.get('temperatureUnit') === 'G_CEL')
      .flush(forecast);

    store.changeUnit('G_FAH');

    httpMock
      .expectOne((req) => req.url === '/api/forecast' && req.params.get('temperatureUnit') === 'G_FAH')
      .flush({ ...forecast, averageTemperature: 73.0, temperatureUnit: 'G_FAH' });

    expect(store.forecast()).toMatchObject({ averageTemperature: 73.0, temperatureUnit: 'G_FAH' });
  });

  it('clears forecast when municipality is cleared', () => {
    store.selectMunicipality({ id: '28079', name: 'Madrid' });
    httpMock
      .expectOne((req) => req.url === '/api/forecast' && req.params.get('temperatureUnit') === 'G_CEL')
      .flush(forecast);

    store.clearMunicipality();

    expect(store.selectedMunicipality()).toBeNull();
    expect(store.forecast()).toBeNull();
    expect(store.loading()).toBe(false);
  });

  it('maps ProblemDetail errors', () => {
    store.selectMunicipality({ id: '28079', name: 'Madrid' });

    httpMock
      .expectOne((req) => req.url === '/api/forecast')
      .flush(
        {
          code: 'RATE_LIMITED',
          detail: 'AEMET rate limit exceeded. Please try again later.',
        },
        { status: 503, statusText: 'Service Unavailable' },
      );

    expect(store.error()).toBe(
      'AEMET ha limitado las peticiones. Espera unos segundos y vuelve a intentarlo.',
    );
    expect(store.forecast()).toBeNull();
    expect(store.loading()).toBe(false);
  });
});
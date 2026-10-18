import { provideHttpClient, withFetch } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';

import { WeatherApiService } from './weather-api.service';

describe('WeatherApiService', () => {
  let service: WeatherApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withFetch()),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(WeatherApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('gets all municipalities', async () => {
    const promise = firstValueFrom(service.getMunicipalities());

    httpMock.expectOne('/api/municipalities').flush([{ id: '28079', name: 'Madrid' }]);

    await expect(promise).resolves.toEqual([{ id: '28079', name: 'Madrid' }]);
  });

  it('searches municipalities by name', async () => {
    const promise = firstValueFrom(service.searchMunicipalities('Madrid'));

    httpMock
      .expectOne((req) => req.url === '/api/municipalities' && req.params.get('name') === 'Madrid')
      .flush([{ id: '28079', name: 'Madrid' }]);

    await expect(promise).resolves.toEqual([{ id: '28079', name: 'Madrid' }]);
  });

  it('gets forecast without temperature unit', async () => {
    const promise = firstValueFrom(service.getForecast('28079'));

    httpMock
      .expectOne((req) => req.url === '/api/forecast' && !req.params.has('temperatureUnit'))
      .flush({ averageTemperature: 22.8, temperatureUnit: 'G_CEL', precipitationProbability: [] });

    await expect(promise).resolves.toMatchObject({ averageTemperature: 22.8 });
  });

  it('gets forecast with Fahrenheit', async () => {
    const promise = firstValueFrom(service.getForecast('28079', 'G_FAH'));

    httpMock
      .expectOne((req) => req.url === '/api/forecast' && req.params.get('temperatureUnit') === 'G_FAH')
      .flush({ averageTemperature: 73.0, temperatureUnit: 'G_FAH', precipitationProbability: [] });

    await expect(promise).resolves.toMatchObject({ averageTemperature: 73.0 });
  });
});
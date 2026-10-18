import { provideHttpClient, withFetch } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { MunicipalitySearchStore } from './municipality-search.store';

function wait(milliseconds: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, milliseconds));
}

describe('MunicipalitySearchStore', () => {
  let store: MunicipalitySearchStore;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withFetch()),
        provideHttpClientTesting(),
      ],
    });

    store = TestBed.inject(MunicipalitySearchStore);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads the municipality catalog in background', () => {
    store.loadCatalog();

    httpMock.expectOne('/api/municipalities').flush([
      { id: '28079', name: 'Madrid' },
      { id: '08019', name: 'Barcelona' },
    ]);

    expect(store.catalog()).toEqual([
      { id: '28079', name: 'Madrid' },
      { id: '08019', name: 'Barcelona' },
    ]);
    expect(store.catalogLoading()).toBe(false);
  });

  it('searches remotely with debounce before the catalog is loaded', async () => {
    store.search('mad');
    await wait(450);

    httpMock
      .expectOne((req) => req.url === '/api/municipalities' && req.params.get('name') === 'mad')
      .flush([{ id: '28079', name: 'Madrid' }]);

    await wait(0);

    expect(store.suggestions()).toEqual([{ id: '28079', name: 'Madrid' }]);
  });

  it('does not search remotely below three characters before the catalog is loaded', async () => {
    store.search('ma');
    await wait(450);

    httpMock.expectNone('/api/municipalities');
  });

  it('does not filter locally below three characters once the catalog is loaded', () => {
    store.loadCatalog();
    httpMock.expectOne('/api/municipalities').flush([
      { id: '27002', name: 'Lugo' },
      { id: '28079', name: 'Madrid' },
    ]);

    store.search('lu');

    expect(store.suggestions()).toEqual([]);
  });

  it('filters locally once the catalog is loaded', () => {
    store.loadCatalog();
    httpMock.expectOne('/api/municipalities').flush([
      { id: '29067', name: 'Málaga' },
      { id: '28079', name: 'Madrid' },
    ]);

    store.search('malaga');

    expect(store.suggestions()).toEqual([{ id: '29067', name: 'Málaga' }]);
  });

  it('ignores stale remote results once the catalog is loaded', async () => {
    store.search('mad');
    await wait(450);

    const remoteRequest = httpMock.expectOne(
      (req) => req.url === '/api/municipalities' && req.params.has('name'),
    );

    store.loadCatalog();
    httpMock.expectOne('/api/municipalities').flush([{ id: '08019', name: 'Barcelona' }]);

    remoteRequest.flush([{ id: '28079', name: 'Madrid' }]);
    await wait(0);

    expect(store.suggestions()).toEqual([]);
  });

  it('clears suggestions', () => {
    store.loadCatalog();
    httpMock.expectOne('/api/municipalities').flush([{ id: '28079', name: 'Madrid' }]);

    store.search('mad');
    store.clear();

    expect(store.suggestions()).toEqual([]);
  });
});
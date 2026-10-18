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

    expect(store.searching()).toBe(true);

    httpMock
      .expectOne((req) => req.url === '/api/municipalities' && req.params.get('name') === 'mad')
      .flush([{ id: '28079', name: 'Madrid' }]);

    await wait(0);

    expect(store.searching()).toBe(false);
    expect(store.suggestions()).toEqual([{ id: '28079', name: 'Madrid' }]);
  });

  it('reports remote search errors and keeps searching', async () => {
    store.search('mad');
    await wait(450);

    httpMock
      .expectOne((req) => req.url === '/api/municipalities' && req.params.get('name') === 'mad')
      .flush(null, { status: 500, statusText: 'Server Error' });

    await wait(0);

    expect(store.searchError()).toBe(true);
    expect(store.searching()).toBe(false);
    expect(store.suggestions()).toEqual([]);

    store.search('madr');
    await wait(450);

    httpMock
      .expectOne((req) => req.url === '/api/municipalities' && req.params.get('name') === 'madr')
      .flush([{ id: '28079', name: 'Madrid' }]);

    await wait(0);

    expect(store.searchError()).toBe(false);
    expect(store.suggestions()).toEqual([{ id: '28079', name: 'Madrid' }]);
  });

  it('reports catalog loading errors', () => {
    store.loadCatalog();

    httpMock
      .expectOne('/api/municipalities')
      .flush(null, { status: 503, statusText: 'Service Unavailable' });

    expect(store.catalogLoading()).toBe(false);
    expect(store.catalogError()).toBe(true);
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

  it('prioritizes prefix matches over substring matches', () => {
    store.loadCatalog();
    httpMock.expectOne('/api/municipalities').flush([
      { id: '18001', name: 'Fuente Alcántara' },
      { id: '02001', name: 'Alcóntar' },
      { id: '28079', name: 'Madrid' },
    ]);

    store.search('alc');

    expect(store.suggestions()).toEqual([{ id: '02001', name: 'Alcóntar' }]);
  });

  it('falls back to substring matches when there are no prefix matches', () => {
    store.loadCatalog();
    httpMock.expectOne('/api/municipalities').flush([
      { id: '18001', name: 'Fuente Alcántara' },
      { id: '28079', name: 'Madrid' },
    ]);

    store.search('alc');

    expect(store.suggestions()).toEqual([{ id: '18001', name: 'Fuente Alcántara' }]);
  });

  it('limits local suggestions to twenty results', () => {
    const catalog = Array.from({ length: 25 }, (_, index) => ({
      id: String(index).padStart(5, '0'),
      name: `Alcalá ${String(index).padStart(2, '0')}`,
    }));

    store.loadCatalog();
    httpMock.expectOne('/api/municipalities').flush(catalog);

    store.search('alc');

    expect(store.suggestions()).toHaveLength(20);
    expect(store.suggestions()[0].name).toBe('Alcalá 00');
    expect(store.suggestions()[19].name).toBe('Alcalá 19');
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
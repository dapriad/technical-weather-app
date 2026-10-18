import { provideHttpClient, withFetch } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { App } from './app';

describe('App', () => {
  let fixture: ComponentFixture<App>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(withFetch()), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(App);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    TestBed.resetTestingModule();
  });

  function flushApiResponses(): void {
    httpMock.expectOne('/api/health').flush({ status: 'UP' });
    httpMock.expectOne('/api/hello').flush({ message: 'Hello from Spring Boot' });
  }

  it('should create the app', () => {
    flushApiResponses();

    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should load backend health and hello', () => {
    const app = fixture.componentInstance;

    flushApiResponses();

    expect(app.health()).toBe('UP');
    expect(app.message()).toBe('Hello from Spring Boot');
  });
});

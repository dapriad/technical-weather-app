import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';

interface HealthResponse {
  status: string;
}

interface HelloResponse {
  message: string;
}

@Component({
  imports: [],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  readonly title = signal('Scaffolding Spring Boot + Angular');
  readonly health = signal('pending');
  readonly message = signal('Loading backend response...');
  readonly error = signal('');

  private readonly http = inject(HttpClient);

  constructor() {
    this.load();
  }

  load(): void {
    forkJoin({
      health: this.http.get<HealthResponse>('/api/health'),
      hello: this.http.get<HelloResponse>('/api/hello'),
    }).subscribe({
      next: ({ health, hello }) => {
        this.health.set(health.status);
        this.message.set(hello.message);
        this.error.set('');
      },
      error: () => {
        this.health.set('down');
        this.message.set('No response from backend');
        this.error.set('Start the Spring Boot backend and reload.');
      },
    });
  }
}

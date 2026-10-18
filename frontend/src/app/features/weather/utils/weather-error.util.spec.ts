import { HttpErrorResponse } from '@angular/common/http';

import { mapForecastError } from './weather-error.util';

describe('mapForecastError', () => {
  it('maps known backend problem codes to Spanish messages', () => {
    const error = new HttpErrorResponse({
      status: 503,
      error: { code: 'RATE_LIMITED' },
    });

    expect(mapForecastError(error)).toBe(
      'AEMET ha limitado las peticiones. Espera unos segundos y vuelve a intentarlo.',
    );
  });

  it('maps missing API key', () => {
    const error = new HttpErrorResponse({
      status: 503,
      error: { code: 'NOT_CONFIGURED' },
    });

    expect(mapForecastError(error)).toBe('Falta configurar la API key de AEMET.');
  });

  it('maps municipality not found', () => {
    const error = new HttpErrorResponse({
      status: 404,
      error: { code: 'NOT_FOUND' },
    });

    expect(mapForecastError(error)).toBe(
      'No hay predicción disponible para mañana para este municipio.',
    );
  });

  it('uses a Spanish fallback for unknown codes', () => {
    const error = new HttpErrorResponse({
      status: 500,
      error: { code: 'UNKNOWN' },
    });

    expect(mapForecastError(error)).toBe('No se pudo cargar la predicción. Vuelve a intentarlo.');
  });

  it('uses a Spanish fallback for non-http errors', () => {
    expect(mapForecastError(new Error('network'))).toBe(
      'No se pudo cargar la predicción. Vuelve a intentarlo.',
    );
  });
});
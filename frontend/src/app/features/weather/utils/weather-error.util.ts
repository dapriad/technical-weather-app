import { HttpErrorResponse } from '@angular/common/http';

interface ProblemDetail {
  code?: string;
}

const FORECAST_ERROR_MESSAGES: Record<string, string> = {
  NOT_CONFIGURED: 'Falta configurar la API key de AEMET.',
  UNAUTHORIZED: 'AEMET ha rechazado la API key configurada.',
  RATE_LIMITED: 'AEMET ha limitado las peticiones. Espera unos segundos y vuelve a intentarlo.',
  NOT_FOUND: 'No hay predicción disponible para mañana para este municipio.',
  UNAVAILABLE: 'El servicio de AEMET no está disponible en este momento.',
  TIMEOUT: 'AEMET ha tardado demasiado en responder. Vuelve a intentarlo.',
  INVALID_RESPONSE: 'AEMET ha devuelto una respuesta no válida.',
  INVALID_DATA_URL: 'AEMET ha devuelto una URL de datos no válida.',
  INVALID_MUNICIPALITY_ID: 'El código de municipio no es válido.',
  INVALID_TEMPERATURE_UNIT: 'La unidad de temperatura no es válida.',
};

export function mapForecastError(error: unknown): string {
  const fallback = 'No se pudo cargar la predicción. Vuelve a intentarlo.';

  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }

  const code = (error.error as ProblemDetail | null)?.code;

  return code ? FORECAST_ERROR_MESSAGES[code] ?? fallback : fallback;
}
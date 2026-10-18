# API Documentation

The Spring Boot API uses [Springdoc OpenAPI](https://springdoc.org/) to generate its own OpenAPI 3 documentation automatically.

The most useful URLs are:

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

## Current endpoints

- `GET /api/health` — Health check
- `GET /api/hello` — Hello endpoint
- `GET /api/municipalities?name=Madrid` — Municipality search (AEMET integration)
- `GET /api/forecast?municipalityId=28079&temperatureUnit=G_CEL` — Tomorrow forecast (AEMET integration)

## Municipality endpoint

### Request

```
GET /api/municipalities?name=Madrid
```

| Parameter | Type   | Required | Description                          |
|---|---|---|---|
| `name`    | string | No       | Municipality name (case & accent insensitive) |

### Response

```json
[
  {"id": "28079", "name": "Madrid"}
]
```

| Field  | Type   | Description                                      |
|---|---|---|
| `id`   | string | Municipality INE code (leading `id` prefix removed) |
| `name` | string | Municipality name in Spanish                     |

## Forecast endpoint

### Request

```
GET /api/forecast?municipalityId=28079&temperatureUnit=G_CEL
```

| Parameter | Type | Required | Default | Description |
|---|---|---|---|---|
| `municipalityId` | string | Yes | — | INE code. Accepts `28079` and `id28079`. |
| `temperatureUnit` | string | No | `G_CEL` | `G_CEL` or `G_FAH`. |

### Response

```json
{
  "averageTemperature": 22.8,
  "temperatureUnit": "G_CEL",
  "precipitationProbability": [
    { "probability": 0, "period": "00-06" },
    { "probability": 0, "period": "06-12" },
    { "probability": 0, "period": "12-18" },
    { "probability": 0, "period": "18-24" }
  ]
}
```

| Field | Type | Description |
|---|---|---|
| `averageTemperature` | number | Average daily temperature rounded to one decimal. |
| `temperatureUnit` | string | Unit used in the response: `G_CEL` or `G_FAH`. |
| `precipitationProbability` | array | Six-hour precipitation probability breakdown. |

### Forecast calculation

- The endpoint uses AEMET's daily municipality forecast.
- It selects the day after today in `Europe/Madrid`.
- The average temperature is calculated from the available `temperatura.dato` values.
- If no `dato` values are available, it falls back to `(maxima + minima) / 2`.
- Fahrenheit conversion is applied after calculating the Celsius average.
- Only the periods `00-06`, `06-12`, `12-18` and `18-24` are returned.

### Error responses

The API returns `ProblemDetail` responses with a stable `code` property:

| HTTP status | code | When |
|---|---|---|
| 400 | `INVALID_MUNICIPALITY_ID` | The municipality ID is not a five-digit INE code. |
| 400 | `INVALID_TEMPERATURE_UNIT` | The temperature unit is not `G_CEL` or `G_FAH`. |
| 404 | `NOT_FOUND` | AEMET did not return a forecast for tomorrow. |
| 503 | `NOT_CONFIGURED` | Missing `AEMET_API_KEY`. |
| 503 | `RATE_LIMITED` | AEMET 429 response. |
| 503 | `UNAVAILABLE` | AEMET 5xx, connection failure or unavailable service. |
| 504 | `TIMEOUT` | AEMET request timed out. |
| 502 | `INVALID_RESPONSE` | AEMET returned an invalid or empty payload. |
| 502 | `INVALID_DATA_URL` | AEMET returned an unexpected data URL. |
| 502 | `UNAUTHORIZED` | AEMET rejected credentials. |

### How the backend works

1. The AEMET client calls `GET /api/maestro/municipios` or `GET /api/prediccion/especifica/municipio/diaria/{id}`.
2. AEMET returns an envelope with a `datos` URL.
3. The client fetches the data URL and parses the payload.
4. The adapter maps AEMET fields to the normalized domain model.
5. The application service filters, selects tomorrow, converts units and returns the result.

No caching is performed in this iteration. Each search or forecast request calls AEMET twice. See [docs/IMPROVEMENTS.md](../IMPROVEMENTS.md) for the caching plan.

## Adding richer documentation for new endpoints

- Use `@Operation` for endpoint summaries and descriptions.
- Use `@Parameter` for query, path and header parameters.
- Use `@Schema` on DTOs and records for field descriptions.
- Use `@Tag` to group related endpoints.
- Use `@ApiResponses` to document error responses.

## Notes for this project

- The generated spec reflects the Spring Boot controllers directly.
- Response field names are normalized to English (`averageTemperature`, `temperatureUnit`, `precipitationProbability`) even when the original technical spec shows Spanish examples.
- Swagger UI is available at the backend port only. The frontend proxy does not expose `/swagger-ui.html`.
- Keep the configuration simple for now, and enrich it once actual weather data features are implemented.
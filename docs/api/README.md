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
  {"id": "28079", "name": "Madrid"},
  {"id": "28001", "name": "Ajalvir"}
]
```

| Field  | Type   | Description                                      |
|---|---|---|
| `id`   | string | Municipality code (preserves leading zeros)      |
| `name` | string | Municipality name (Spanish)                      |

### Error responses

The API returns `ProblemDetail` (RFC 7807) responses. A stable `code` property is included:

| HTTP status | code                        | When                                           |
|---|---|---|
| 503         | `AEMET_NOT_CONFIGURED`      | Missing `AEMET_API_KEY`                        |
| 503         | `AEMET_RATE_LIMITED`        | AEMET 429 response                             |
| 503         | `AEMET_UNAVAILABLE`         | AEMET 5xx / timeout / invalid response         |
| 504         | `AEMET_TIMEOUT`             | Network or read timeout                        |
| 502         | `AEMET_UNAUTHORIZED`        | AEMET 401 / 403 response                       |

### How the backend works

1. The AEMET client calls `GET /api/maestro/municipios?api_key=...` to get a two-step response.
2. The response contains a `datos` URL pointing to the full municipality list.
3. The client fetches the municipality JSON and maps each record to a normalized domain model.
4. The use case filters municipalities by name (case and accent insensitive).
5. The result is returned as a JSON array.

No caching is performed in this iteration. Every search calls AEMET twice. See [docs/IMPROVEMENTS.md](../IMPROVEMENTS.md) for the caching plan.

## Adding richer documentation for new endpoints

- Use `@Operation` for endpoint summaries and descriptions.
- Use `@Parameter` for query, path and header parameters.
- Use `@Schema` on DTOs and records for field descriptions.
- Use `@Tag` to group related endpoints.

## Notes for this project

- The generated spec reflects the Spring Boot controllers directly.
- Swagger UI is available at the backend port only. The frontend proxy does not expose `/swagger-ui.html`.
- Keep the configuration simple for now, and enrich it once actual weather data features are implemented.
# API Documentation

The Spring Boot API uses [Springdoc OpenAPI](https://springdoc.org/) to generate its own OpenAPI 3 documentation automatically.

The most useful URLs are:

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

## Current endpoints

- `GET /api/health`
- `GET /api/hello`

## Adding richer documentation for new endpoints

When real endpoints exist, document them where the code is written:

- `@Operation` for endpoint summaries and descriptions.
- `@ApiResponse` for expected status codes and response formats.
- `@Parameter` for path and query parameters.
- `@Schema` for DTO descriptions.
- `@Tag` to group related endpoints.

## Notes for this project

- The generated spec reflects the Spring Boot controllers directly.
- The frontend container only exposes `/api` through its proxy today. If Swagger must be reachable from the frontend origin as well, update `frontend/nginx.conf`.
- Keep the configuration simple for now, and enrich it once actual weather data features are implemented.

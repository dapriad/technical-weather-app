# Technical Weather App

[![Docker images](https://github.com/dapriad/technical-weather-app/actions/workflows/docker-publish.yml/badge.svg)](https://github.com/dapriad/technical-weather-app/actions/workflows/docker-publish.yml)

Technical monorepo for a technical exercise based on Spring Boot and Angular. The repository contains an API with AEMET OpenData integration, an Angular frontend, Docker configurations, and a GitHub Actions workflow that publishes public container images to GitHub Container Registry.

## Current status

This is a runnable application with AEMET municipality search integration:

- Angular application served through Nginx.
- Spring Boot REST endpoints including AEMET municipality search.
- Development proxy from Angular to Spring Boot.
- Local Docker Compose environment.
- Public GHCR images for `linux/amd64` and `linux/arm64`.
- Automatic OpenAPI documentation using Springdoc.

There is no database or authentication in this milestone. AEMET municipality forecasts are planned for a future iteration.

## Stack

| Component | Technology | Version |
|---|---|---|
| Backend | Java | 21 |
| Backend | Spring Boot | 4.1.1 |
| API documentation | Springdoc OpenAPI | 3.1.1 |
| API documentation | Swagger UI | 5.32.14 |
| Frontend | Angular | 22.2 |
| Frontend | TypeScript | 6.0.x |
| Frontend build image | Node.js | 24 Alpine |
| Frontend runtime image | Nginx | 1.27 Alpine |
| Container orchestration | Docker Compose V2 | current |
| AEMET client | RestClient | JDK HttpClient (Java 21) |

## Quick start with published images

Start a compatible Docker runtime, then run:

```bash
git clone https://github.com/dapriad/technical-weather-app.git
cd technical-weather-app

# Copy the environment example and add your AEMET key
# https://opendata.aemet.es/centrodedescargas/inicio
cp .env.example .env
# Edit .env and replace AEMET_API_KEY with your real key

docker compose up -d
```

Open:

```text
http://localhost:4200
http://localhost:8080/swagger-ui.html
```

Validate the API:

```bash
curl -fsS http://localhost:8080/api/health
curl -fsS http://localhost:8080/api/hello
curl -fsS http://localhost:4200/api/health
curl -fsS http://localhost:4200/api/hello
curl --get --data-urlencode 'name=Madrid' http://localhost:8080/api/municipalities
```

Stop the environment:

```bash
docker compose down
```

For a step-by-step reviewer guide, use the [onboarding documentation](docs/ONBOARDING.md).

## Current endpoints

| Method | Path | Response |
|---|---|---|
| GET | `/api/health` | `{"status":"UP"}` |
| GET | `/api/hello` | `{"message":"Hello from Spring Boot"}` |
| GET | `/api/municipalities?name=Madrid` | `[{"id":"28079","name":"Madrid"}]` |
| GET | `/swagger-ui.html` | Swagger UI for the Spring Boot API |
| GET | `/v3/api-docs` | Generated OpenAPI definition |

The same `/api/health`, `/api/hello` and `/api/municipalities` paths are available through the frontend proxy.

## Repository layout

```text
.
├── backend/      # Spring Boot API
├── frontend/     # Angular application
├── docs/         # Onboarding and API documentation notes
├── docker-compose.yml
└── .github/workflows/docker-publish.yml
```

## Local development

Install Java 21, Node.js and npm compatible with these versions:

- Java 21.
- Node.js `22.22.3+`, `24.15.0+` or `26+`.
- npm bundled with Node.js.

Start the backend:

```bash
cd backend
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

In another terminal, start the frontend:

```bash
cd frontend
npm ci
npm start
```

The application uses `proxy.conf.json` to send `/api` requests to `http://localhost:8080`.

Full local workflow and troubleshooting are documented in [docs/ONBOARDING.md](docs/ONBOARDING.md).

## Tests

Backend:

```bash
cd backend
./mvnw test
```

AEMET municipality search:

| Test | Description |
|---|---|
| Case-insensitive search | `Madrid` matches `madrid` |
| Accent-insensitive search | `malaga` matches `Málaga` |
| Substring search | `palma` matches `Palma de Mallorca` |
| Empty result | `searchByName("NotExisting")` returns `[]` |
| Missing key | Returns `503 Service Unavailable` |

Frontend:

```bash
cd frontend
npm test -- --watch=false
npm run build
```

## Container images

The published images are:

```text
ghcr.io/dapriad/technical-weather-app-backend:latest
ghcr.io/dapriad/technical-weather-app-frontend:latest
```

Package pages:

- [Backend package](https://github.com/users/dapriad/packages/container/package/technical-weather-app-backend)
- [Frontend package](https://github.com/users/dapriad/packages/container/package/technical-weather-app-frontend)

The GitHub Actions workflow publishes both images for `linux/amd64` and `linux/arm64`. The workflow runs on pushes to `main`, on version tags, and can also be executed manually.

## AEMET integration notes

- The municipality search endpoint calls AEMET's `GET /api/maestro/municipios` API.
- AEMET returns a two-step response: first an envelope with a data URL, then the municipalities JSON from that URL.
- The municipality list is requested on every search (no caching in this iteration). Caching is planned as a performance improvement.
- Search is case and accent insensitive.
- No database is used; all data lives in memory for the duration of each request.

## Documentation

- [Onboarding guide](docs/ONBOARDING.md)
- [API documentation guide](docs/api/README.md)
- [Planned improvements](docs/IMPROVEMENTS.md)

## Author

Daniel Pribeagu - [@dapriad](https://github.com/dapriad)
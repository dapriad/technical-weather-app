# Technical Weather App

[![Docker images](https://github.com/dapriad/technical-weather-app/actions/workflows/docker-publish.yml/badge.svg)](https://github.com/dapriad/technical-weather-app/actions/workflows/docker-publish.yml)

Technical monorepo for a technical exercise based on Spring Boot and Angular. The repository contains an API with AEMET OpenData integration, an Angular frontend, Docker configurations, and a GitHub Actions workflow that publishes public container images to GitHub Container Registry.

## Current status

This is a runnable application with AEMET OpenData integration:

- Angular application served through Nginx.
- Weather feature UI with PrimeNG Aura, municipality autocomplete and temperature unit selector.
- Spring Boot REST endpoints including AEMET municipality search and tomorrow forecast.
- Development proxy from Angular to Spring Boot.
- Local Docker Compose environment.
- Public GHCR images for `linux/amd64` and `linux/arm64`.
- Automatic OpenAPI documentation using Springdoc.

There is no database or authentication in this milestone. The autocomplete loads the municipality catalog once in the background and filters locally when it is available.

## Stack

| Component | Technology | Version |
|---|---|---|
| Backend | Java | 21 |
| Backend | Spring Boot | 4.1.1 |
| API documentation | Springdoc OpenAPI | 3.1.1 |
| API documentation | Swagger UI | 5.32.14 |
| Frontend | Angular | 22.2 |
| Frontend | PrimeNG | 22.1 |
| Frontend theme | @primeuix/themes Aura | 3.0 |
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

In the frontend, search for a municipality, select it and optionally change the temperature unit. The forecast card refreshes when the selected municipality or temperature unit changes.

Validate the API:

```bash
curl -fsS http://localhost:8080/api/health
curl -fsS http://localhost:4200/api/health
curl --get --data-urlencode 'name=Madrid' http://localhost:8080/api/municipalities
curl --get --data-urlencode 'municipalityId=28079' http://localhost:8080/api/forecast
curl --get --data-urlencode 'municipalityId=28079' --data-urlencode 'temperatureUnit=G_FAH' http://localhost:8080/api/forecast
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
| GET | `/api/municipalities?name=Madrid` | `[{"id":"28079","name":"Madrid"}]` |
| GET | `/api/forecast?municipalityId=28079&temperatureUnit=G_CEL` | Tomorrow average temperature and precipitation breakdown |
| GET | `/swagger-ui.html` | Swagger UI for the Spring Boot API |
| GET | `/v3/api-docs` | Generated OpenAPI definition |

The same `/api/health`, `/api/municipalities` and `/api/forecast` paths are available through the frontend proxy.

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

The application uses `proxy.conf.json` to send `/api` requests to `http://localhost:8080`. The frontend feature uses Angular Router, PrimeNG Aura, a municipality autocomplete and a temperature unit selector.

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

AEMET forecast:

| Test | Description |
|---|---|
| Default unit | `temperatureUnit` defaults to `G_CEL` |
| Fahrenheit conversion | `G_FAH` converts the Celsius average |
| Tomorrow selection | Uses `Europe/Madrid` to select the next day |
| Precipitation filtering | Returns only `00-06`, `06-12`, `12-18` and `18-24` |
| ID compatibility | Accepts `28079` and `id28079` |
| Invalid unit | Returns `400 INVALID_TEMPERATURE_UNIT` |

Frontend:

```bash
cd frontend
npm test -- --watch=false
npm run build
```

Weather feature:

| Area | Tests |
|---|---|
| API service | Endpoint URLs and forecast query parameters |
| Municipality search store | Background catalog load, local filtering, remote fallback and stale response handling |
| Forecast store | Forecast load on selection, unit changes, clearing and error mapping |
| UI components | Autocomplete selection, unit selection and forecast rendering |

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
- The forecast endpoint calls AEMET's `GET /api/prediccion/especifica/municipio/diaria/{id}` API.
- AEMET returns a two-step response: first an envelope with a data URL, then the JSON payload from that URL.
- The AEMET data URL host is validated against `opendata.aemet.es` before the second request.
- The backend does not cache AEMET responses. The frontend loads the municipality catalog once in memory and filters locally when available; forecast requests still call AEMET on every refresh.
- Municipality IDs are normalized to the five-digit INE code. The forecast endpoint also accepts the AEMET maestro `id` prefix.
- Forecast average temperature is calculated from AEMET's available `temperatura.dato` values, with a fallback to `(maxima + minima) / 2`.
- Search is case and accent insensitive.
- No database is used; backend data lives only in memory for the duration of each request.

## Documentation

- [Onboarding guide](docs/ONBOARDING.md)
- [API documentation guide](docs/api/README.md)
- [Planned improvements](docs/IMPROVEMENTS.md)

## Author

Daniel Pribeagu - [@dapriad](https://github.com/dapriad)
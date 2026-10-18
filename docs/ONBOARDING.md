# Project Onboarding

This guide is intended for reviewers who want to run, test and inspect the Technical Weather App, and for developers who need a complete local setup.

## What this project is

Technical Weather App is a technical exercise monorepo composed of:

- A Spring Boot REST API with AEMET OpenData municipality search and daily forecast integration.
- An Angular single-page application.
- Docker images for each component, published to GitHub Container Registry.
- A GitHub Actions workflow that builds and publishes those images automatically.

## What is not included yet

This milestone does not include:

- Authentication or authorization.
- Database integration.
- Caching for AEMET municipality or forecast requests.
- Frontend routing or advanced state management.

## Reviewer quick start

### Prerequisites

To run the project using containers, install:

- Docker Engine and Docker Compose V2.
- Git.
- curl or another HTTP client.

For local code development, also install:

- Java 21 (Eclipse Temurin recommended).
- Node.js `22.22.3+`, `24.15.0+` or `26+`.
- npm bundled with Node.js.

### Get an AEMET API key

The municipality search and forecast endpoints require an AEMET OpenData API key.

1. Visit https://opendata.aemet.es/centrodedescargas/inicio
2. Register with your email.
3. Wait for the confirmation email (usually a few minutes).
4. Use your email as the API key.

**Important:** Each request calls AEMET twice (envelope + data URL). Be aware of AEMET's rate limits or use caching in production.

### Start the published stack

From the repository root:

```bash
git clone https://github.com/dapriad/technical-weather-app.git
cd technical-weather-app

# Copy .env.example and set your AEMET key
cp .env.example .env
# Edit .env and replace the AEMET_API_KEY value
# Do NOT commit .env — it is gitignored

docker compose pull
docker compose up -d
docker compose ps
```

The default published container images are:

```text
ghcr.io/dapriad/technical-weather-app-backend:latest
ghcr.io/dapriad/technical-weather-app-frontend:latest
```

Expected exposed services:

| URL | Component |
|---|---|
| `http://localhost:4200` | Angular frontend served by nginx. |
| `http://localhost:4200/api/health` | Health endpoint through the frontend proxy. |
| `http://localhost:4200/api/hello` | Hello endpoint through the frontend proxy. |
| `http://localhost:4200/api/municipalities?name=Madrid` | Municipality search through the frontend proxy. |
| `http://localhost:4200/api/forecast?municipalityId=28079` | Tomorrow forecast through the frontend proxy. |
| `http://localhost:8080/api/health` | Backend health endpoint. |
| `http://localhost:8080/api/hello` | Backend hello endpoint. |
| `http://localhost:8080/api/municipalities?name=Madrid` | Backend municipality search. |
| `http://localhost:8080/api/forecast?municipalityId=28079&temperatureUnit=G_FAH` | Backend tomorrow forecast in Fahrenheit. |
| `http://localhost:8080/swagger-ui.html` | Swagger UI from the backend. |
| `http://localhost:8080/v3/api-docs` | OpenAPI definition. |

Validate the endpoints:

```bash
curl -fsS http://localhost:8080/api/health
curl -fsS http://localhost:8080/api/hello
curl --get --data-urlencode 'name=Madrid' http://localhost:8080/api/municipalities
curl --get --data-urlencode 'name=malaga' http://localhost:8080/api/municipalities
curl -fsS http://localhost:4200/api/municipalities?name=Madrid
curl --get --data-urlencode 'municipalityId=28079' http://localhost:8080/api/forecast
curl --get --data-urlencode 'municipalityId=28079' --data-urlencode 'temperatureUnit=G_FAH' http://localhost:8080/api/forecast
```

Stop the containers:

```bash
docker compose down
```

If you want to remove containers and networks more aggressively, run:

```bash
docker compose down --remove-orphans
```

## Local development workflow

### Start the backend

```bash
cd backend
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

### Start the frontend

```bash
cd ../frontend
npm ci
npm start
```

The frontend runs on `http://localhost:4200` with a dev proxy forwarding `/api` requests to `http://localhost:8080`.

### Test commands

```bash
# Backend unit tests
cd backend
./mvnw test

# Frontend unit tests
cd frontend
npm test -- --watch=false

# Frontend production build
cd frontend
npm run build
```

### Using a local AEMET key without Docker

Copy `.env.example` to `.env` in the repository root and set your `AEMET_API_KEY`. Spring Boot will automatically import the file via `spring.config.import` and the key will be available to the AEMET client.

```bash
# From backend/ directory
./mvnw spring-boot:run

# In another terminal
curl --get --data-urlencode 'name=Madrid' http://localhost:8080/api/municipalities
curl --get --data-urlencode 'municipalityId=28079' http://localhost:8080/api/forecast
```

## Building the container images from source

If you prefer to build the images locally instead of pulling from GHCR:

```bash
docker compose build
docker compose up -d
```

If you want to rebuild everything from scratch:

```bash
docker compose down
docker compose build --pull
docker compose up -d
```

## Published container registry layout

The project publishes two images:

```text
ghcr.io/dapriad/technical-weather-app-backend:latest
ghcr.io/dapriad/technical-weather-app-frontend:latest
```

Each image is published for these CPU architectures:

- `linux/amd64`
- `linux/arm64`

Useful inspection commands:

```bash
docker pull ghcr.io/dapriad/technical-weather-app-backend:latest
docker pull ghcr.io/dapriad/technical-weather-app-frontend:latest

docker buildx imagetools inspect ghcr.io/dapriad/technical-weather-app-backend:latest
docker buildx imagetools inspect ghcr.io/dapriad/technical-weather-app-frontend:latest
```

## GitHub Actions workflow

The workflow lives at `.github/workflows/docker-publish.yml`.

It automatically:

- Builds both backend and frontend images.
- Logs in to GitHub Container Registry using the workflow `GITHUB_TOKEN`.
- Pushes images to `ghcr.io/<owner>/<repo>-backend` and `ghcr.io/<owner>/<repo>-frontend`.
- Tags the latest build on the default branch.
- Adds a `linux/amd64` and `linux/arm64` manifest list.
- Publishes on:
  - any push to `main`
  - any tag starting with `v`
  - manual workflow dispatch

The workflow does not depend on any stored secrets besides `GITHUB_TOKEN`.

## Repository notes

### Backend

The backend uses a lightweight hexagonal structure:

- `backend/src/main/java/com/dapriad/weather/domain/model/`
  - Domain models and value objects (`Municipality`, `DailyForecast`, `MunicipalityForecast`, `TemperatureUnit`).
- `backend/src/main/java/com/dapriad/weather/domain/port/`
  - Output ports (`MunicipalityProvider`, `ForecastProvider`).
- `backend/src/main/java/com/dapriad/weather/application/`
  - Input ports, application services, validation and provider exceptions.
- `backend/src/main/java/com/dapriad/weather/infrastructure/adapter/in/rest/`
  - Inbound REST controllers and response DTOs.
- `backend/src/main/java/com/dapriad/weather/infrastructure/adapter/out/aemet/`
  - Outbound AEMET clients, adapters and DTOs.
- `backend/src/main/java/com/dapriad/weather/infrastructure/config/`
  - Spring configuration, `Clock`, OpenAPI and AEMET client wiring.
- `backend/src/main/java/com/dapriad/weather/shared/api/`
  - Global error handler.

### Frontend

The Angular application is a basic standalone app:

- `frontend/src/app/app.ts`
- `frontend/src/app/app.html`
- `frontend/src/app/app.scss`

The root service configuration is in:

- `frontend/src/app/app.config.ts`

## Troubleshooting

### Port already in use

If you see an error for port `8080` or `4200`, stop the process currently using that port.

On macOS/Linux:

```bash
lsof -i :8080
lsof -i :4200
```

Then stop the offending process or change the exposed port in `docker-compose.yml` or `proxy.conf.json`.

### Frontend cannot reach the backend

Make sure the backend is running and listening on port `8080`.

If using local development servers:

```bash
cd backend
./mvnw spring-boot:run
```

If using Docker, the nginx container proxies `/api` to `http://backend:8080`.

```bash
docker compose logs -f frontend backend
```

### Docker image pulls fail

Confirm the images are public and use the exact GHCR names in this file. If the project later becomes non-public or the image names change, the workflow and the compose default image references need to be updated together.

### Java version mismatch

Run `java -version` and `mvn -version` (or `./mvnw -version`) to verify the expected JDK is in use. On macOS, it is very common to see a different Java home. You can override the JDK per terminal with:

```bash
export JAVA_HOME=/path/to/java-21
export PATH="$JAVA_HOME/bin:$PATH"
```

### AEMET rate limit

The municipality and forecast endpoints call AEMET twice per request and do not cache yet. If you receive `503` with code `RATE_LIMITED`, wait and retry. You can inspect the backend logs:

```bash
docker compose logs -f backend
```

### Forecast not available

If the forecast endpoint returns `404` with code `NOT_FOUND`, AEMET did not return a forecast for tomorrow. This can happen when AEMET is updating its dataset or when the municipality code is not valid.

```bash
curl --get --data-urlencode 'municipalityId=28079' http://localhost:8080/api/forecast
```
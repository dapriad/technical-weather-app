# Technical Weather App

[![Docker images](https://github.com/dapriad/technical-weather-app/actions/workflows/docker-publish.yml/badge.svg)](https://github.com/dapriad/technical-weather-app/actions/workflows/docker-publish.yml)

Technical monorepo for a technical exercise based on Spring Boot and Angular. The repository contains an API, a frontend application, both Docker configurations and a GitHub Actions workflow that publishes public container images to GitHub Container Registry.

## Current status

This is a runnable scaffold. It currently provides:

- Angular application served through Nginx.
- Spring Boot REST endpoints exposed through `/api`.
- Development proxy from Angular to Spring Boot.
- Local Docker Compose environment.
- Public GHCR images for `linux/amd64` and `linux/arm64`.
- Automatic OpenAPI documentation using Springdoc.

There is no database, authentication or external weather integration in this milestone.

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

## Quick start with published images

Start a compatible Docker runtime, then run:

```bash
git clone https://github.com/dapriad/technical-weather-app.git
cd technical-weather-app

docker compose pull
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
| GET | `/swagger-ui.html` | Swagger UI for the Spring Boot API |
| GET | `/v3/api-docs` | Generated OpenAPI definition |

The same `/api/health` and `/api/hello` paths are available through the frontend when it proxies or forwards requests to `http://localhost:8080`.

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

## Documentation

- [Onboarding guide](docs/ONBOARDING.md)
- [API documentation guide](docs/api/README.md)

## Author

Daniel Pribeagu - [@dapriad](https://github.com/dapriad)

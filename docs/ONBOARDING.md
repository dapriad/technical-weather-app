# Project Onboarding

This guide is intended for reviewers who want to run, test and inspect the Technical Weather App without using Docker as their primary development environment, and also for people who only need to execute the published containers.

## What this project is

Technical Weather App is a technical exercise monorepo composed of:

- A Spring Boot REST API.
- An Angular single-page application.
- Docker images for each component.
- A GitHub Actions workflow that builds and publishes those images to GitHub Container Registry.

The current milestone is intentionally small. It is meant to prove that the frontend, backend, proxy, containers and deployment documentation work together, then build real weather functionality on top of that foundation.

## Current behavior

The application exposes two scaffold endpoints:

| Path | Example response | Notes |
|---|---|---|
| `GET /api/health` | `{"status":"UP"}` | Simple backend health check. |
| `GET /api/hello` | `{"message":"Hello from Spring Boot"}` | Demonstrates frontend-backend communication. |

OpenAPI documentation is generated through Springdoc and available at:

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

The frontend proxy currently forwards `/api` to `http://localhost:8080`. If you want to expose Swagger through the frontend container in the future, add a location rule in `frontend/nginx.conf`.

## What is not included yet

This scaffold does not currently include:

- Authentication or authorization.
- Database integration.
- Weather API consumption.
- Weather domain business logic.
- Frontend routing or advanced state management.

## Reviewer quick start

### Prerequisites

To run the project using containers, install:

- Docker Engine and Docker Compose V2.
- Git.
- curl or another HTTP client.

No API keys are needed for the current scaffold.

### Start the published stack

From the repository root:

```bash
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
| `http://localhost:8080/api/health` | Backend health endpoint. |
| `http://localhost:8080/api/hello` | Backend hello endpoint. |
| `http://localhost:8080/swagger-ui.html` | Swagger UI from the backend. |
| `http://localhost:8080/v3/api-docs` | OpenAPI definition. |

Validate the main endpoints:

```bash
curl -fsS http://localhost:8080/api/health
curl -fsS http://localhost:8080/api/hello
curl -fsS http://localhost:4200/api/health
curl -fsS http://localhost:4200/api/hello
```

Stop the containers:

```bash
docker compose down
```

If you want to remove containers and networks more aggressively, run:

```bash
docker compose down --remove-orphans
```

## Manual local development

### Prerequisites

For local code development, install:

- Java 21.
- Node.js `22.22.3+`, `24.15.0+` or `26+`.
- npm bundled with Node.js.

Use the exact command below only if you do not want to install Java globally.

```bash
export JAVA_HOME=/Users/niles/tools/jdk-21.0.12.1+1/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```

If your machine already has Java 21 on the `PATH`, you can skip those lines.

### Start the backend

```bash
cd backend
./mvnw spring-boot:run
```

Windows:

```cmd
cd backend
mvnw.cmd spring-boot:run
```

Keep this terminal open while working.

### Start the frontend

```bash
cd ../frontend
npm ci
npm start
```

The frontend is served by Angular Dev Server at `http://localhost:4200`. It uses `proxy.conf.json` to forward any request to `/api` to `http://localhost:8080`.

### Useful development commands

```bash
# Backend tests
cd backend
./mvnw test

# Frontend unit tests
cd frontend
npm test -- --watch=false

# Frontend production build
cd frontend
npm run build
```

If you want to watch logs while the stack is running with Docker Compose:

```bash
docker compose logs -f backend
docker compose logs -f frontend
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
ghcr.io/dapriad/technical-weather-app-backend
ghcr.io/dapriad/technical-weather-app-frontend
```

Each image is currently published for these CPU architectures:

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

The backend package is organized in a very flat way on purpose:

- `backend/src/main/java/com/dapriad/weather/api`
  - REST controllers.
- `backend/src/main/java/com/dapriad/weather/config`
  - Spring beans.
- `backend/src/main/java/com/dapriad/weather`
  - Application entrypoint.

### Frontend

The Angular application is a basic standalone app:

- `frontend/src/app/app.ts`
- `frontend/src/app/app.html`
- `frontend/src/app/app.scss`

The root service configuration is in:

- `frontend/src/app/app.config.ts`

### API documentation

- Generated OpenAPI definition: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

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

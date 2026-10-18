# Onboarding

This guide is for reviewers who want to run the published application and validate it without
building the source code first. For local development, tests and project structure, use the
[root README](../README.md).

## What you will run

The project is a two-container stack:

- **Frontend**: Angular SPA served by Nginx on `http://localhost:4200`.
- **Backend**: Spring Boot API on `http://localhost:8080`.

Nginx serves the Angular application and proxies `/api` requests to the backend. The backend calls
AEMET OpenData for municipality data and forecasts.

## Prerequisites

- Docker Engine.
- Docker Compose V2.
- Git.
- `curl` or another HTTP client.

## Get an AEMET API key

The backend requires an AEMET OpenData API key:

1. Open <https://opendata.aemet.es/centrodedescargas/inicio>.
2. Register with your email address.
3. Wait for the confirmation email, which usually arrives within a few minutes.
4. Use the API key included in that email.

AEMET uses a two-step response: the first request returns an envelope with a `datos` URL, and the
second request downloads the actual JSON payload. The backend does not cache these calls yet. The
frontend loads the municipality catalog once in memory, but forecast requests still call AEMET on
every municipality or unit change.

## Start the published stack

From the repository root:

```bash
git clone https://github.com/dapriad/technical-weather-app.git
cd technical-weather-app

cp .env.example .env
# Edit .env and replace AEMET_API_KEY with your real key
# Do not commit .env; it is gitignored

docker compose pull
docker compose up -d
docker compose ps
```

The default images are:

```text
ghcr.io/dapriad/technical-weather-app-backend:latest
ghcr.io/dapriad/technical-weather-app-frontend:latest
```

## Expected services

| URL | Component |
|---|---|
| `http://localhost:4200` | Weather UI served by Nginx. |
| `http://localhost:4200/api/health` | Health endpoint through the frontend proxy. |
| `http://localhost:8080/api/health` | Backend health endpoint. |
| `http://localhost:8080/api/municipalities?name=Madrid` | Municipality search. |
| `http://localhost:8080/api/forecast?municipalityId=28079` | Tomorrow forecast. |
| `http://localhost:8080/swagger-ui.html` | Swagger UI. |
| `http://localhost:8080/v3/api-docs` | Generated OpenAPI definition. |

## Validate the API

```bash
curl -fsS http://localhost:8080/api/health
curl -fsS http://localhost:4200/api/health
curl --get --data-urlencode 'name=Madrid' http://localhost:8080/api/municipalities
curl --get --data-urlencode 'municipalityId=28079' http://localhost:8080/api/forecast
curl --get --data-urlencode 'municipalityId=28079' \
  --data-urlencode 'temperatureUnit=G_FAH' http://localhost:8080/api/forecast
```

The interactive API contract is available in Swagger UI at `http://localhost:8080/swagger-ui.html`.

## Manual UI walkthrough

1. Open `http://localhost:4200`.
2. Type `Madrid` in the municipality autocomplete.
3. Select `Madrid` from the suggestions.
4. Confirm that the forecast card shows tomorrow's average temperature and the four precipitation
   periods.
5. Change the unit to `Fahrenheit (°F)` and confirm that the forecast refreshes.
6. Clear the municipality and confirm that the forecast disappears.

## Stop the stack

```bash
docker compose down
```

To remove containers, networks and orphaned resources more aggressively:

```bash
docker compose down --remove-orphans
```

## Build the images from source

If you prefer not to pull the published images:

```bash
docker compose build
docker compose up -d
```

To rebuild everything from scratch:

```bash
docker compose down
docker compose build --pull
docker compose up -d
```

## Troubleshooting

### Port already in use

Find and stop the process using port `8080` or `4200`:

```bash
lsof -i :8080
lsof -i :4200
```

Alternatively, change the exposed port in `docker-compose.yml`.

### Frontend cannot reach the backend

Check the container status and logs:

```bash
docker compose ps
docker compose logs -f frontend backend
```

The Nginx container proxies `/api` to `http://backend:8080`. In local development, Angular uses
`proxy.conf.json` to reach `http://localhost:8080` instead.

### Docker image pulls fail

Confirm that the GHCR packages are public and that the image names in `docker-compose.yml` are
exact. If the package visibility or repository name changes, update the compose file and the GitHub
Actions workflow together.

### AEMET rate limit

AEMET calls are not cached in the backend. If the API returns `503` with code `RATE_LIMITED`, wait
and retry. Inspect the logs with:

```bash
docker compose logs -f backend
```

### Forecast not available

A `404` with code `NOT_FOUND` means AEMET did not return a forecast for tomorrow, or the
municipality code is invalid. Retry later or verify the municipality ID.

## Next steps

- [Architecture guide](ARCHITECTURE.md) - how the application works internally.
- [Planned improvements](IMPROVEMENTS.md) - backlog and completed work.
- [Questions and answers](QUESTIONS.md) - open technical questions and notes.

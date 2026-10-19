# Demo deployment

Live demo: <https://technical-weather-app-niles-niles-lab.apps.nan.builders/>

This guide describes the single-container demo deployment used to publish the application on a
small container host. The local Docker Compose setup is unchanged: it still runs the frontend and
backend as two containers.

## Why a single container

The demo image serves the Angular build from Spring Boot and exposes the API on the same origin:

```text
https://demo.example.com/              -> Angular
https://demo.example.com/api/*         -> Spring Boot
https://demo.example.com/swagger-ui/*  -> Spring Boot
```

Because everything runs on one origin, the demo does not need CORS or a separate frontend host.
The Angular application keeps using relative `/api` URLs.

## Branch workflow

1. Create the deployment branch from `main`:

   ```bash
   git switch -c feat/demo-deployment main
   ```

2. Commit the changes and push the branch.
3. Open a draft pull request against `main`. The CI workflow runs on pull requests; a branch push
   without a pull request does not run `ci.yml`.
4. Deploy the branch to the container host and verify the demo.
5. Merge the pull request into `main`.
6. Change the demo app to deploy from `main`, or create the production app from `main` and remove
   the branch deployment. The published demo now follows `main`.

The `docker-publish.yml` workflow only publishes images from `main` and version tags, so branch
deployments do not overwrite the `latest` images.

## Container host configuration

Create a dedicated app:

| Setting | Value |
|---|---|
| App name | `technical-weather-app-demo` |
| Branch | `main` |
| Dockerfile path | `Dockerfile.demo` |
| Build context | `.` |
| Expose over HTTP | enabled |
| Container port | `8080` |
| Replicas | `1` |
| CPU | `1000m` |
| Memory | `1Gi` |
| Persistent storage | disabled |
| Auto-deploy on push | enabled once the branch is stable |

The single replica is intentional: the cache, rate limits and AEMET call budget are held in memory,
so every instance has its own counters. Do not enable additional replicas without moving those
counters to a shared store.

## Environment variables

Set these in the **Runtime** environment, never in the build environment:

| Variable | Required | Description |
|---|---|---|
| `AEMET_API_KEY` | yes | Dedicated AEMET OpenData key for the demo. |
| `JAVA_TOOL_OPTIONS` | recommended | `-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC` |
| `APP_RATE_LIMIT_API_REQUESTS_PER_MINUTE` | no | Per-IP API limit. Default `20`. |
| `APP_RATE_LIMIT_API_BURST` | no | Per-IP API burst. Default `5`. |
| `APP_RATE_LIMIT_GLOBAL_REQUESTS_PER_MINUTE` | no | Global API limit. Default `60`. |
| `APP_RATE_LIMIT_SWAGGER_REQUESTS_PER_MINUTE` | no | Per-IP Swagger limit. Default `10`. |
| `APP_AEMET_BUDGET_MAX_CALLS_PER_DAY` | no | Daily outbound AEMET call budget. Default `300`. |
| `APP_AEMET_BUDGET_MAX_CALLS_PER_MINUTE` | no | Outbound AEMET calls per minute. Default `10`. |
| `APP_CACHE_MUNICIPALITIES_TTL` | no | Municipality cache TTL. Default `24h`. |
| `APP_CACHE_FORECASTS_TTL` | no | Forecast cache TTL. Default `30m`. |

The API key must not be set as a build argument or committed to the repository. The demo image
reads it at runtime.

## Protection layers

### Cache

- Municipality catalog: 24 hours.
- Forecasts: 30 minutes per municipality.
- Cached data is stored after the AEMET response has been downloaded and parsed.
- Cache hits do not consume the outbound AEMET call budget.
- Empty responses and errors are not cached.

### Inbound rate limit

- `/api/**`: 20 requests per minute per IP, burst 5.
- `/api/**`: 60 requests per minute globally.
- Swagger and OpenAPI: 10 requests per minute per IP, burst 3.
- Swagger and OpenAPI: 20 requests per minute globally.
- `/api/health`, `OPTIONS` requests and Angular static assets are not limited.
- Rejected requests return `429 Too Many Requests` with a `Retry-After` header.
- `X-Forwarded-For` is used when present, with `X-Real-IP` and the remote address as fallbacks.

### Outbound AEMET budget

- 300 calls per day.
- 10 calls per minute, burst 5.
- The daily counter resets at midnight in `Europe/Madrid`.
- Each uncached API request normally consumes two calls: the envelope and the data URL.
- When the budget is exhausted, uncached requests return `503 Service Unavailable` with code
  `BUDGET_EXHAUSTED`. Cached data remains available.

## Security notes

- The repository and the demo image contain no API keys.
- `.env` files are ignored by Git and excluded from the Docker build context.
- The application does not expose Actuator or environment endpoints.
- Error responses do not include stack traces or internal messages.
- The frontend bundle does not contain the AEMET key.
- The public configuration (cache TTLs, rate limits, ports) is not secret. Enforcement happens on
  the server, so knowing the values does not bypass them.
- Use a dedicated AEMET key for the demo and rotate it if it is abused.
- Do not enable DEBUG logging for the HTTP client: the AEMET key is sent as a query parameter to
  AEMET and could appear in request logs.
- The container host dashboard and logs should remain private.

## Verification

Build the image locally:

```bash
docker build -f Dockerfile.demo -t technical-weather-demo .
```

Run it with a demo key:

```bash
docker run --rm -p 8080:8080 \
  -e AEMET_API_KEY=replace-with-demo-key \
  -e JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC" \
  technical-weather-demo
```

Check the application:

```bash
curl -fsS http://localhost:8080/api/health
curl --get --data-urlencode 'name=Madrid' http://localhost:8080/api/municipalities
curl --get --data-urlencode 'municipalityId=28079' http://localhost:8080/api/forecast
curl --get --data-urlencode 'municipalityId=28079' \
  --data-urlencode 'temperatureUnit=G_FAH' http://localhost:8080/api/forecast
```

Open `http://localhost:8080/` for the UI and `http://localhost:8080/swagger-ui.html` for Swagger.

Test the inbound rate limit without calling AEMET by using an invalid municipality ID:

```bash
for i in $(seq 1 25); do
  curl -s -o /dev/null -w '%{http_code}\n' \
    'http://localhost:8080/api/forecast?municipalityId=123'
done
```

The first requests return `400`; after the per-IP limit, they return `429`.

To test the outbound budget, start the container with a low daily limit and request several
different municipalities without cache entries:

```bash
docker run --rm -p 8080:8080 \
  -e AEMET_API_KEY=replace-with-demo-key \
  -e APP_AEMET_BUDGET_MAX_CALLS_PER_DAY=2 \
  technical-weather-demo
```

The third uncached forecast request returns `503` with code `BUDGET_EXHAUSTED`.

## Troubleshooting

| Symptom | Meaning |
|---|---|
| `429` and `Retry-After` | Inbound rate limit reached. Wait or tune the limits. |
| `503` with `BUDGET_EXHAUSTED` | AEMET call budget exhausted. Wait until midnight in Madrid or raise the limit. |
| `503` with `NOT_CONFIGURED` | `AEMET_API_KEY` is missing from the runtime environment. |
| `502` with `UNAUTHORIZED` | AEMET rejected the key. Replace or rotate it. |
| `504` with `TIMEOUT` | AEMET did not respond in time. Retry later. |
| Container restarts or is OOM-killed | Increase memory above `1Gi` or check that the JVM options are applied. |
| First request after deploy is slow | The JVM, cache and container are cold. Subsequent requests are faster. |

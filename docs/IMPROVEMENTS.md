# Possible Improvements

This file tracks planned enhancements for the Technical Weather App. Items are listed in priority order for the MVP-to-production migration path.

## Backlog

- [ ] **Persist frontend municipality catalog**: Store the loaded municipality catalog in `sessionStorage` or `localStorage` with a TTL to avoid calling AEMET on every page reload.
- [ ] **Pagination / result limits**: The `GET /api/municipalities` endpoint currently returns up to ~8,000 municipalities. Add pagination or a `?limit=` parameter for production use.
- [ ] **Automatic retries with backoff**: The AEMET client does not retry on transient failures. Add a retry policy with exponential backoff and honor the `Retry-After` header on 429 responses.
- [ ] **Metrics and structured logging**: Introduce Spring Boot Actuator metrics (latency, error rate, AEMET call count) and structured JSON logs for observability.
- [ ] **API key rotation / secrets manager**: Replace the plain `.env` file with a proper secrets solution (e.g., HashiCorp Vault, AWS Secrets Manager, or GitHub repository secrets with CI injection).
- [ ] **Integration tests against the real AEMET API**: Add a test profile that hits the real AEMET API (with a test key) before each PR. Requires rate limit handling.
- [ ] **HTTP conditional requests**: Use `Etag` and `If-None-Match` headers to reduce AEMET payloads for repeated requests.

## Done

- [x] **Municipality search endpoint**: AEMET-based `GET /api/municipalities?name=...`.
- [x] **Tomorrow forecast endpoint**: AEMET-based `GET /api/forecast?municipalityId=...&temperatureUnit=...`.
- [x] **SSRF protection**: The AEMET `datos` URL host is validated against `opendata.aemet.es` before the second request.
- [x] **Weather feature UI**: Municipality autocomplete, temperature unit selector and forecast card.
- [x] **Frontend municipality catalog cache**: The frontend loads the municipality catalog once in memory and filters locally when available.
- [x] **Backend municipality cache**: The catalog is cached with Caffeine for 24 hours.
- [x] **Backend forecast cache**: Forecasts are cached with Caffeine for 30 minutes per municipality.
- [x] **Inbound rate limiting**: Per-IP and global limits protect `/api/**` and Swagger UI.
- [x] **Outbound AEMET budget**: Per-minute and daily limits protect the AEMET provider quota.
- [x] **Single-container demo image**: `Dockerfile.demo` serves the Angular build and the API from one container.

## Related documentation

- [Architecture guide](ARCHITECTURE.md)
- [Onboarding guide](ONBOARDING.md)
- [Demo deployment guide](DEMO_DEPLOYMENT.md)

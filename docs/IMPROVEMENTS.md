# Possible Improvements

This file tracks planned enhancements for the Technical Weather App. Items are listed in priority order for the MVP-to-production migration path.

## Backlog

- [ ] **Cache AEMET municipality list**: Store the full municipality list in memory (Caffeine or Spring Cache) with a TTL (e.g. 24h). This avoids calling AEMET twice on every API request.
- [ ] **Frontend cache + client-side filtering**: Once the frontend consumes the full list, cache it and filter client-side. This provides instant autocomplete and no network latency.
- [ ] **Pagination / result limits**: The `GET /api/municipalities` endpoint currently returns up to ~8,000 municipalities. Add pagination or a `?limit=` parameter for production use.
- [ ] **Automatic retries with backoff**: The AEMET client does not retry on transient failures. Add a retry policy with exponential backoff and honor the `Retry-After` header on 429 responses.
- [ ] **Metrics and structured logging**: Introduce Spring Boot Actuator metrics (latency, error rate, AEMET call count) and structured JSON logs for observability.
- [ ] **API key rotation / secrets manager**: Replace the plain `.env` file with a proper secrets solution (e.g., HashiCorp Vault, AWS Secrets Manager, or GitHub repository secrets with CI injection).
- [ ] **Integration tests against the real AEMET API**: Add a test profile that hits the real AEMET API (with a test key) before each PR. Requires rate limit handling.
- [ ] **Municipality forecast endpoint**: Implement `GET /api/municipalities/{id}/forecast` using AEMET's `/api/prediccion/especifica/municipio/diaria/{municipio}` endpoint.
- [ ] **SSRF protection**: Validate the AEMET `datos` URL host against a strict allowlist before following redirects.
- [ ] **HTTP conditional requests**: Use `Etag` and `If-None-Match` headers to reduce AEMET payloads for repeated requests.
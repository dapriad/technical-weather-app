# Technical Weather App Frontend

Angular 22 single-page application for the technical weather app repository.

## Stack

- Angular standalone components.
- Angular Router.
- PrimeNG 22 with Aura styled theme.
- Development proxy to the Spring Boot backend.

## Local development

```bash
npm ci
npm start
```

The frontend runs on `http://localhost:4200`. It uses `proxy.conf.json` to forward `/api` requests to the Spring Boot backend on `http://localhost:8080`.

## Weather feature

The UI is organized as a feature-based vertical slice:

```text
src/app/features/weather/
├── components/
│   ├── municipality-autocomplete/
│   ├── temperature-unit-select/
│   └── forecast-card/
├── models/
├── pages/weather-page/
└── services/
    ├── weather-api.service.ts
    ├── municipality-search.store.ts
    └── forecast.store.ts
```

Behavior:

- The municipality autocomplete loads the full catalog once in the background.
- While the catalog is loading, the autocomplete can still search through the backend API.
- Once the catalog is loaded, filtering happens locally.
- Selecting a municipality loads tomorrow's forecast.
- Changing the temperature unit refreshes the forecast.
- Clearing the municipality clears the forecast.

## Commands

| Command | Purpose |
|---|---|
| `npm start` | Run the Angular development server with the API proxy. |
| `npm test -- --watch=false` | Run unit tests once. |
| `npm run build` | Create the production bundle. |

## Documentation

- Root README: `../README.md`
- Onboarding guide: `../docs/ONBOARDING.md`
- API documentation notes: `../docs/api/README.md`

## Author

Daniel Pribeagu - [@dapriad](https://github.com/dapriad)
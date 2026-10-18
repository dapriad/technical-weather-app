# Technical Weather App Frontend

Angular 22 single-page application for the technical weather app repository.

## Local development

```bash
npm ci
npm start
```

The frontend runs on `http://localhost:4200`. It uses `proxy.conf.json` to forward `/api` requests to the Spring Boot backend on `http://localhost:8080`.

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
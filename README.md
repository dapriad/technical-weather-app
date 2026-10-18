# Technical Weather App

Monorepo básico para una prueba técnica con:

- **Backend:** Spring Boot 4.1.1 + Java 21 + Maven Wrapper.
- **Frontend:** Angular 22 + Nginx en Docker.
- **Contenedores:** Docker Compose local.
- **Imágenes:** GitHub Container Registry mediante GitHub Actions.

## Estructura

```text
.
├── backend/      # API Spring Boot
├── frontend/     # SPA Angular
├── docker-compose.yml
└── .github/workflows/docker-publish.yml
```

Cada carpeta mantiene su configuración independiente.

## Endpoints

Backend directo:

- `GET http://localhost:8080/api/health`
  - `{"status":"UP"}`
- `GET http://localhost:8080/api/hello`
  - `{"message":"Hello from Spring Boot"}`

Frontend proxy:

- `GET http://localhost:4200/api/health`
- `GET http://localhost:4200/api/hello`

## Requisitos para ejecutar en local

- JDK 21, por ejemplo Eclipse Temurin 21.
- Node.js compatible con Angular 22: `22.22.3+`, `24.15.0+` o `26+`.
- npm.
- Docker con Docker Compose v2.

## Ejecución en local sin Docker

### Backend

```bash
cd backend
./mvnw test
./mvnw spring-boot:run
```

El backend levanta en `http://localhost:8080`.

### Frontend

En otra terminal:

```bash
cd frontend
npm install
npm start
```

El proxy de desarrollo está configurado en `proxy.conf.json` y envía las llamadas `/api` al backend en `http://localhost:8080`.

Abrir: `http://localhost:4200`.

Si el backend no está levantado, la aplicación mostrará un estado de error del frontend y el botón `Reload API` volverá a intentar la consulta.

## Ejecución con Docker Compose

### Primero, si usas Colima en macOS

```bash
colima start
```

### Construir y ejecutar el stack local

```bash
docker compose up --build -d
docker compose logs -f backend frontend
```

Una vez construido, abre `http://localhost:4200` y prueba:

```bash
curl http://localhost:8080/api/health
curl http://localhost:4200/api/health
curl http://localhost:4200/api/hello
```

Detener el stack:

```bash
docker compose down
```

## Imágenes publicadas en GHCR

Tras subir el repositorio a GitHub, el workflow publicará:

```text
ghcr.io/dapriad/technical-weather-app-backend:latest
ghcr.io/dapriad/technical-weather-app-frontend:latest
```

Para lanzar las imágenes publicadas:

```bash
BACKEND_IMAGE=ghcr.io/dapriad/technical-weather-app-backend:latest \
FRONTEND_IMAGE=ghcr.io/dapriad/technical-weather-app-frontend:latest \
docker compose pull

BACKEND_IMAGE=ghcr.io/dapriad/technical-weather-app-backend:latest \
FRONTEND_IMAGE=ghcr.io/dapriad/technical-weather-app-frontend:latest \
docker compose up -d
```

## CI/CD de imágenes

El workflow está en `.github/workflows/docker-publish.yml` y se ejecuta con:

- `push` a `main`;
- `push` de tags `v*`;
- ejecución manual desde la pestaña Actions.

Publica dos imágenes GHCR a la vez que construye `backend/` y `frontend/`. Cuando el paquete GitHub Container Registry se complete la primeira vez, revisa la visibilidad de ambos paquetes en GitHub y márcalos como públicos en package settings.

## Git local

El proyecto está preparado para versionar con:

```bash
git status

git init -b main
git add .
git commit -m "chore: scaffold Spring Boot 4 y Angular 22"
```

Para crear el repositorio público en GitHub, con el CLI `gh` autenticado:

```bash
gh repo create technical-weather-app --public --source=. --remote=origin --push
```

## Notas

- No hay base de datos, autenticación ni integración con APIs de clima externas.
- El objetivo funcional del scaffold es demostrar frontend, backend, proxy, contenedores y publicación.

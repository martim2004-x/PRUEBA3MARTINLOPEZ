# Evidencias — Evaluación Parcial 4

## 1. Microservicios Spring Boot (código en `backend/`)

| Servicio | Puerto | Base de datos | Endpoints |
|---|---|---|---|
| `catalog-service` | 8081 | H2 (`data/catalogdb`) | `/api/health`, `/api/products` (GET, POST, PUT, DELETE), `/api/products/{id}`, `/api/products/active`, `/api/products/code/{code}`, `/api/fairs` |
| `request-service` | 8082 | H2 (`data/requestdb`) | `/api/health`, `/api/requests` (GET, POST, PUT, DELETE), `/api/requests/{id}`, `/api/requests/user/{userId}`, `/api/requests/status/{status}`, `PATCH /api/requests/{id}/status` |

Ambos arrancan con `mvn spring-boot:run`, crean su carpeta `data/` y cargan datos semilla la primera vez.
Las consolas H2 están habilitadas en `/h2-console`.

## 2. Integración app ↔ microservicios

- Clientes Retrofit: `app/src/main/java/com/louaraya/macrame/data/remote/`.
- Sincronización al iniciar y CRUD desde la UI: `MacrameRepository.kt` y `BackendScreen.kt`.
- URLs configurables en `ApiConfig.kt` (`10.0.2.2` para emulador).
- Room actúa como caché: si el servicio no responde, la app sigue operando con datos locales.

## 3. API externa

Open-Meteo se consume con Retrofit (`WeatherApi.kt`) y se muestra en la tarjeta *API externa · Open-Meteo* de la
pestaña **APIs**. Endpoint: `GET https://api.open-meteo.com/v1/forecast?latitude=-33.4489&longitude=-70.6693&current=temperature_2m,weather_code&timezone=America/Santiago`.

## 4. Pruebas unitarias y cobertura

Comando ejecutado:

```
gradlew :app:testDebugUnitTest
```

Resultado: **60 pruebas, 0 fallos**, tarea `jacocoTestReport` completada.

Cobertura de líneas de la lógica sometida a pruebas (ViewModels, `MacrameRepository`, mapeadores, `Validators` y utilidades):

| Capa | Cobertura de líneas |
|---|---|
| Conjunto ViewModel + Repository + dominio + utilidades | **87,4 %** (654/748) |
| `MacrameRepository` | 89,6 % |
| `BackendViewModel` | 90,2 % |
| `RegisterViewModel` / `RequestFormViewModel` / `CatalogViewModel` / `MapViewModel` | 92 % – 100 % |
| `Validators` | 98 % |


## 5. APK firmado y llave `.jks`

Archivos incluidos en el repositorio:

| Archivo | Descripción |
|---|---|
| `apk/macrame-release-signed.apk` | APK de release firmado (36 171 141 bytes) |
| `app/keystore/macrame-release.jks` | Llave RSA 2048, alias `macrame`, validez 10000 días |
| `keystore.properties` | Ruta y credenciales de la firma |
| `app/build.gradle.kts` | `signingConfigs { release { ... } }` y `signingConfig` en `buildTypes.release` |

Capturas incluidas: `docs/evidencias/configuracion_firma_apk.png` (configuración de la firma) y
`docs/evidencias/apk_y_jks.png` (APK firmado + llave `.jks` con sus rutas, tamaños y verificación).

Salida real de `apksigner verify --print-certs`:

```
Signer #1 certificate DN: CN=Lou Araya Macrame, OU=DSY1105, O=Lou Araya, L=Santiago, ST=Metropolitana, C=CL
Signer #1 certificate SHA-256 digest: 47ddeaeb69f88531e063c8724293933e054f1c3bca99dd586330c8a0b96718ea
Signer #1 certificate SHA-1 digest: 9057676d3013ae1e959fdf731d148ceabe88aa95
Signer #1 certificate MD5 digest: b54caf241d4c47ec6b278e4ede1d645c
```

Comando para regenerarlo:

```
gradlew :app:assembleRelease
```

## 6. GitHub y Trello

- Repositorio GitHub público: https://github.com/martim2004-x/PRUEBA3MARTINLOPEZ.git (remoto `origin`, rama `main`).
- Los commits de la app y de los microservicios se realizan en el mismo repositorio, con mensajes
  técnicos por funcionalidad (ver ejemplos en `COLABORACION.md`).
- El tablero Trello usa `Backlog → En progreso → En revisión → Hecho`, con una tarjeta por funcionalidad y checklist;
- Foto del tablero: `docs/evidencias/trello.png` 

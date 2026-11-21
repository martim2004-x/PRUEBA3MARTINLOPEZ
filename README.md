# Lou Araya Macramé — App móvil (DSY1105, Evaluación Parcial 2)

Aplicación Android nativa (Kotlin + Jetpack Compose) para el emprendimiento de macramé artesanal **Lou Araya**.
Permite a clientes explorar el catálogo, guardar favoritos, escanear códigos QR de las piezas, ver ferias en un mapa
y solicitar cotizaciones; y a la administradora gestionar productos, campañas y solicitudes.

## Integrantes
- Martín Ignacio López Bustos — RUT: 217040957

## Funcionalidades
- **Dos perfiles** (Cliente / Administrador) con navegación inferior distinta y datos separados por usuario.
- **Registro e inicio de sesión** con validación por campo en vivo, íconos de estado y mensajes de error.
- **Catálogo** con búsqueda, categorías, detalle, favoritos y solicitud de cotización.
- **Solicitudes** con estados (Pendiente → Confirmado → Entregado / Rechazada) y **notificaciones** internas.
- **Administración**: CRUD de productos (con foto desde la cámara), campañas con fases y publicación.
- **Recursos nativos**: 📷 cámara (escáner QR con CameraX + ML Kit y foto de producto) y 📍 ubicación (mapa OpenStreetMap con osmdroid). Permisos en tiempo de ejecución con explicación y acceso a ajustes.
- **Animaciones**: bienvenida escalonada, chips, corazón de favorito, check de éxito, tarjetas expandibles, contadores del panel, línea de escaneo, transiciones de navegación.

## Arquitectura
```
com.louaraya.macrame
├── data/      Room (entidades, DAOs), repositorio, sesión, datos semilla
├── domain/    Modelos y Validators (reglas puras, sin Android)
├── ui/
│   ├── theme/       Paleta Verde Salvia y Ceniza, tipografía, formas
│   ├── components/  Campos de formulario, tarjetas, barra flotante, permisos
│   ├── viewmodel/   Estado de cada pantalla (StateFlow) + validación
│   ├── screens/     Pantallas Compose
│   └── navigation/  Rutas y NavHost
└── util/      Hash de contraseña, formateadores
```
MVVM + repositorio, inyección manual (`AppContainer`) y persistencia local con Room (SQLite).
Las contraseñas se guardan con SHA-256 + sal.

## Cómo ejecutar
1. Abrir la carpeta con **Android Studio** (Koala o superior, JDK 17).
2. Esperar la sincronización de Gradle (se regenera el wrapper si falta).
3. Ejecutar en un emulador o dispositivo con API 26+. El mapa necesita internet; el escáner funciona mejor en un dispositivo real (en emulador se puede escribir el código a mano, ej. `LAM-0001`).
4. Pruebas: `./gradlew test`.

## Credenciales de demostración
| Perfil | Correo | Contraseña |
|---|---|---|
| Administrador | admin@louaraya.cl | Admin123! |
| Cliente | valentina@demo.cl | Demo1234 |

## Cobertura de la rúbrica
| Criterio | Dónde verlo |
|---|---|
| Interfaz y navegación | `ui/navigation/AppRoot.kt`, `ui/screens/` |
| Formularios y validación visual | `ui/components/FormComponents.kt`, pantallas de registro, solicitud y admin |
| Validación desacoplada | `domain/Validators.kt` + `ValidatorsTest.kt` |
| Animaciones | `LayoutComponents.kt`, `ScannerScreen.kt`, `RequestScreens.kt` |
| Arquitectura y persistencia | `data/`, `ui/viewmodel/` |
| Recursos nativos | `ScannerScreen.kt`, `MapScreen.kt`, `AdminScreens.kt` (foto) |
| GitHub y Trello | `docs/COLABORACION.md` |

> Los datos y productos son ficticios. Backend remoto (Retrofit/Spring Boot) queda para la evaluación final; la capa de repositorio ya está preparada.

> **Nota agregada en la Parcial 4:** ese backend remoto ya está implementado y se documenta más abajo, sin haber eliminado ni reescrito nada de lo anterior.

---

# Agregado en la Evaluación Parcial 4

Integrante: Martín Ignacio López Bustos — RUT: **217040957**.

Repositorio GitHub: https://github.com/martim2004-x/PRUEBA3MARTINLOPEZ

## Microservicios propios (Spring Boot + H2)
Código fuente en `backend/`. Empaquetado en `entregables/Microservicios_Spring_Boot_Lou_Araya_Macrame.zip`.

| Servicio | Puerto | Base de datos | Inicialización |
|---|---|---|---|
| `catalog-service` | 8081 | H2 en archivo `./data/catalogdb` | `DataInitializer.java` (`CommandLineRunner`) crea 4 productos y 2 ferias si la tabla está vacía |
| `request-service` | 8082 | H2 en archivo `./data/requestdb` | `DataInitializer.java` (`CommandLineRunner`) crea 1 solicitud de demostración si la tabla está vacía |

## Pantallas desarrolladas (18)
`WelcomeScreen`, `LoginScreen`, `RegisterScreen`, `HomeScreen`, `CatalogScreen`, `ProductDetailScreen`,
`FavoritesScreen`, `MapScreen`, `ScannerScreen`, `RequestsScreen`, `RequestFormScreen`, `NotificationsScreen`,
`ProfileScreen`, `AdminProductsScreen`, `ProductFormScreen`, `AdminCampaignsScreen`, `CampaignFormScreen`,
`BackendScreen` (nueva en esta entrega).

## Endpoints utilizados

### Microservicios propios
**catalog-service — `http://<host>:8081`**

| Método | Endpoint | Uso en la app |
|---|---|---|
| GET | `/api/health` | Estado del servicio en la pestaña APIs |
| GET | `/api/products` | Listado y sincronización inicial |
| GET | `/api/products/active` | Productos activos |
| GET | `/api/products/code/{code}` | Búsqueda por código QR |
| GET | `/api/products/{id}` | Detalle |
| POST | `/api/products` | Crear producto (CRUD) |
| PUT | `/api/products/{id}` | Actualizar producto (CRUD) |
| DELETE | `/api/products/{id}` | Eliminar producto (CRUD) |
| GET | `/api/fairs` | Ferias del mapa |

**request-service — `http://<host>:8082`**

| Método | Endpoint | Uso en la app |
|---|---|---|
| GET | `/api/health` | Estado del servicio en la pestaña APIs |
| GET | `/api/requests` | Listado y sincronización inicial |
| GET | `/api/requests/user/{userId}` | Solicitudes por cliente |
| GET | `/api/requests/status/{status}` | Solicitudes por estado |
| GET | `/api/requests/{id}` | Detalle |
| POST | `/api/requests` | Crear solicitud (CRUD) |
| PUT | `/api/requests/{id}` | Actualizar solicitud (CRUD) |
| PATCH | `/api/requests/{id}/status` | Avanzar estado |
| DELETE | `/api/requests/{id}` | Eliminar solicitud (CRUD) |

### API externa (Open-Meteo, vía Retrofit)
```
GET https://api.open-meteo.com/v1/forecast?latitude=-33.4489&longitude=-70.6693&current=temperature_2m,weather_code&timezone=America/Santiago
```
Se muestra como clima actual de Santiago en la tarjeta *API externa · Open-Meteo* de la pestaña APIs.
Detalle completo en [`backend/README.md`](backend/README.md) y [`docs/EVIDENCIAS.md`](docs/EVIDENCIAS.md).

## Pasos para ejecutar el backend
Requiere Java 17+ y Maven (`mvn`):

```bash
cd backend/catalog-service
mvn spring-boot:run
```
```bash
cd backend/request-service
mvn spring-boot:run
```

Consolas H2: `http://localhost:8081/h2-console` y `http://localhost:8082/h2-console`
(JDBC `jdbc:h2:file:./data/catalogdb` / `jdbc:h2:file:./data/requestdb`, usuario `sa`, sin contraseña).

## Pasos para ejecutar el frontend (app Android)
1. Abrir la carpeta con **Android Studio** (Koala o superior, JDK 17).
2. Sincronizar Gradle; en `local.properties` debe estar `sdk.dir` apuntando al Android SDK.
3. Ejecutar en emulador (API 26+) o dispositivo. El emulador usa `10.0.2.2` como host; en físico cambia las IPs en `data/remote/ApiConfig.kt`.
4. Si los microservicios están apagados, la app sigue funcionando con Room y la pestaña APIs muestra *sin conexión*.
5. Pruebas: `:app:testDebugUnitTest`. Cobertura: `:app:jacocoTestReport`.

## Pruebas y APK firmado
- **60 pruebas, 0 fallos** (JUnit 4 + MockK + coroutines-test). Cobertura de líneas de la lógica testeable: **87,4 %**.
- APK firmado: [`apk/macrame-release-signed.apk`](apk/macrame-release-signed.apk).
- Llave: [`app/keystore/macrame-release.jks`](app/keystore/macrame-release.jks) + `keystore.properties` + `signingConfigs` en `app/build.gradle.kts`.
- Captura de la configuración de firma: `docs/evidencias/configuracion_firma_apk.png`.
- Captura del APK firmado y la llave `.jks`: `docs/evidencias/apk_y_jks.png`.
- Foto del tablero Trello: `docs/evidencias/trello.png`.



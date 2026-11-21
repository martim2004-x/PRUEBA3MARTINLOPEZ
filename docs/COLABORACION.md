# Plan de colaboración (GitHub + Trello)

## Tablero Trello (columnas)
`Backlog` → `En progreso` → `En revisión` → `Hecho`. Una tarjeta por funcionalidad, con responsable y checklist.

| Tarjeta | Responsable |
|---|---|
| Configuración del proyecto, Gradle y tema 
| Room: entidades, DAOs, repositorio, semilla 
| Validators + pruebas unitarias 
| Autenticación (bienvenida, login, registro)
| Catálogo, detalle y favoritos 
| Solicitudes y notificaciones 
| Escáner QR (cámara) 
| Mapa (ubicación) 
| Panel de administración 
| catalog-service: entidad, H2 y endpoints CRUD 
| request-service: entidad, H2 y endpoints CRUD 
| Cliente Retrofit y sincronización offline-first 
| Pantalla APIs + API externa Open-Meteo 
| Pruebas unitarias ViewModel/Repository + JaCoCo 
| APK firmado (.jks) y documentación final
| README y documentación 

## Flujo Git
- `main` estable; ramas `feature/<tema>`; Pull Request 
- Commits pequeños, en infinitivo y técnicos. Ejemplos:
  - `feat(data): agregar entidades y DAOs de Room`
  - `feat(domain): centralizar validaciones en Validators`
  - `feat(ui): campo de texto con ícono de error y validación en vivo`
  - `feat(camera): escáner QR con CameraX y ML Kit`
  - `feat(map): mostrar ferias en osmdroid con permiso de ubicación`
  - `test(domain): cubrir validaciones de correo y contraseña`
  - `docs: README con pasos de ejecución`
   - `feat(backend): agregar catalog-service con H2 y CRUD de productos`
   - `feat(backend): agregar request-service con H2 y cambio de estado`
   - `feat(data): integrar Retrofit y sincronizar microservicios en el repositorio`
   - `feat(ui): pantalla APIs con estado de servicios y clima de Open-Meteo`
   - `test(viewmodel): cubrir ViewModels y repositorio con MockK`
   - `build: configurar firma release con keystore y generar APK`
- Un único integrante: Martín Ignacio López Bustos (RUT 217040957). Todo el trabajo y los commits son suyos.
- Detalle de endpoints, comandos de verificación del APK y cobertura: [`EVIDENCIAS.md`](EVIDENCIAS.md).

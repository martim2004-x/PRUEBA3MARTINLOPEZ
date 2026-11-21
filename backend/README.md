# Microservicios Spring Boot — Lou Araya Macramé

Dos microservicios independientes con base de datos H2 activa y endpoints REST funcionales.
Se ejecutan con Java 17 y Maven (`mvn spring-boot:run`). Cada servicio crea su carpeta `data/`
y conserva la información entre reinicios.

## 1. catalog-service (puerto 8081)

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/health` | Estado del servicio y cantidad de productos |
| GET | `/api/products` | Listar productos (CRUD: read) |
| GET | `/api/products/active` | Productos activos |
| GET | `/api/products/code/{code}` | Buscar por código QR (`LAM-0001`) |
| GET | `/api/products/{id}` | Obtener producto |
| POST | `/api/products` | Crear producto (CRUD: create) |
| PUT | `/api/products/{id}` | Actualizar producto (CRUD: update) |
| DELETE | `/api/products/{id}` | Eliminar producto (CRUD: delete) |
| GET | `/api/fairs` | Ferias y showrooms |

Ejecutar:

```bash
cd backend/catalog-service
mvn spring-boot:run
```

Consola H2: `http://localhost:8081/h2-console` (JDBC URL `jdbc:h2:file:./data/catalogdb`, usuario `sa`, sin contraseña).

## 2. request-service (puerto 8082)

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/health` | Estado del servicio y cantidad de solicitudes |
| GET | `/api/requests` | Listar solicitudes |
| GET | `/api/requests/user/{userId}` | Solicitudes de un cliente |
| GET | `/api/requests/status/{status}` | Solicitudes por estado |
| GET | `/api/requests/{id}` | Obtener solicitud |
| POST | `/api/requests` | Crear solicitud |
| PUT | `/api/requests/{id}` | Actualizar solicitud |
| PATCH | `/api/requests/{id}/status` | Cambiar estado (`{"status":"Confirmado"}`) |
| DELETE | `/api/requests/{id}` | Eliminar solicitud |

Ejecutar:

```bash
cd backend/request-service
mvn spring-boot:run
```

Consola H2: `http://localhost:8082/h2-console` (JDBC URL `jdbc:h2:file:./data/requestdb`, usuario `sa`, sin contraseña).

## Conexión con la app Android

La app usa Retrofit y apunta por defecto a `http://10.0.2.2:8081/` y `http://10.0.2.2:8082/`
(dirección del host desde el emulador). En un dispositivo físico cambia `10.0.2.2` por la IP
de tu computador en `app/src/main/java/com/louaraya/macrame/data/remote/ApiConfig.kt`.

Si los microservicios no están encendidos, la app sigue funcionando con Room (modo offline) y
muestra el estado "sin conexión" en la pantalla **API** del panel de administración.

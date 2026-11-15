# API de Gestión de Estacionamiento — Grupo 10

![CI](../../actions/workflows/ci.yml/badge.svg)

Proyecto Grupal Unidad 1 · **Programación Avanzada**
Autor: **Isaac Betun**

API REST empresarial construida con **Spring Boot 3.5 / Java 21** para administrar usuarios, vehículos,
espacios de estacionamiento y registros de entrada/salida. Calcula el tiempo de permanencia y el monto a
cobrar, y determina en todo momento la disponibilidad de espacios.

## Tecnologías

| Componente | Tecnología |
|---|---|
| Framework | Spring Boot 3.5.7 (Web, Data JPA, Validation, AOP) |
| Lenguaje | Java 21 |
| Base de datos | PostgreSQL 16 (H2 en memoria solo para pruebas) |
| Pruebas | JUnit 5, Mockito, MockMvc |
| CI | GitHub Actions |

## Arquitectura N capas

```
controller  → recibe HTTP, valida DTOs y delega (sin lógica de negocio)
service     → reglas de negocio y transacciones (@Transactional)
repository  → acceso a datos con Spring Data JPA (consultas derivadas y JPQL)
entity      → modelo persistente (JPA)
dto         → contratos de entrada/salida (records + Bean Validation)
exception   → excepciones personalizadas + @RestControllerAdvice
config      → interceptores, pool asíncrono, reloj, datos iniciales
interceptor → middleware HTTP (trazabilidad y control de acceso)
aspect      → AOP (rendimiento y auditoría)
```

## Ejecución local

1. Crear la base de datos:
   ```sql
   CREATE USER parqueadero WITH PASSWORD 'parqueadero123';
   CREATE DATABASE parqueadero_db OWNER parqueadero;
   ```
2. Ejecutar:
   ```bash
   mvn spring-boot:run
   ```
   Variables opcionales: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `API_KEY` (por defecto `PARQ-2026-G10`).
3. Al iniciar se crean 9 espacios (A-01..A-06 autos $1.00/h, M-01..M-03 motos $0.50/h).

Pruebas: `mvn test`

## Seguridad básica

Las operaciones **POST, PUT, PATCH y DELETE** requieren la cabecera `X-API-KEY: PARQ-2026-G10`.
Las consultas GET son públicas. Cada respuesta incluye `X-Request-Id` y `X-Response-Time`.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/usuarios` | Listar usuarios |
| GET | `/api/usuarios/{id}` | Obtener usuario |
| GET | `/api/usuarios/{id}/vehiculos` | Vehículos de un usuario |
| POST | `/api/usuarios` | Registrar usuario |
| PUT | `/api/usuarios/{id}` | Actualizar usuario |
| PATCH | `/api/usuarios/{id}/estado` | Activar / desactivar usuario |
| DELETE | `/api/usuarios/{id}` | Eliminar usuario (sin vehículos) |
| GET | `/api/vehiculos` | Listar vehículos |
| GET | `/api/vehiculos/{id}` | Obtener vehículo |
| GET | `/api/vehiculos/placa/{placa}` | Buscar por placa |
| GET | `/api/vehiculos/placa/{placa}/historial` | Historial de estacionamiento |
| POST | `/api/vehiculos` | Registrar vehículo |
| PUT | `/api/vehiculos/{id}` | Actualizar vehículo |
| DELETE | `/api/vehiculos/{id}` | Eliminar vehículo (sin historial) |
| GET | `/api/espacios?estado=` | Listar espacios |
| GET | `/api/espacios/{id}` | Obtener espacio |
| GET | `/api/espacios/disponibles?tipo=AUTO` | Espacios libres por tipo |
| GET | `/api/espacios/disponibilidad` | Resumen de ocupación |
| POST | `/api/espacios` | Registrar espacio |
| PUT | `/api/espacios/{id}` | Actualizar espacio |
| PATCH | `/api/espacios/{id}/estado` | DISPONIBLE / MANTENIMIENTO |
| DELETE | `/api/espacios/{id}` | Eliminar espacio (libre y sin historial) |
| POST | `/api/registros/ingreso` | Controlar ingreso (asignación automática si no se envía espacio) |
| PATCH | `/api/registros/{id}/salida?notificacion=async\|sync` | Controlar salida, calcular tiempo y monto |
| GET | `/api/registros/activos` | Vehículos dentro del parqueadero |
| GET | `/api/registros/{id}` | Obtener registro |
| GET | `/api/reportes/ingresos?desde=&hasta=` | Reporte de recaudación (bloqueante) |
| GET | `/api/reportes/ingresos/async?desde=&hasta=` | Reporte de recaudación (asíncrono) |
| GET | `/api/auditoria?resultado=EXITO\|ERROR` | Trazas generadas por el aspecto |

Ejemplos listos para ejecutar en [`docs/requests.http`](docs/requests.http).

## Reglas de negocio

| Código | Regla |
|---|---|
| RN-01 | Placa, cédula, email y código de espacio son únicos (409). |
| RN-02 | Un vehículo no puede tener dos ingresos activos (422 `INGRESO_DUPLICADO`). |
| RN-03 | Solo se asignan espacios DISPONIBLES y del mismo tipo que el vehículo (409 / 422). |
| RN-04 | Un usuario inactivo no puede registrar ingresos (422 `USUARIO_INACTIVO`). |
| RN-05 | 10 min de tolerancia sin costo; luego cada fracción de hora se cobra como hora completa. |
| RN-06 | No se eliminan espacios ocupados/con historial, vehículos con historial ni usuarios con vehículos. |
| RN-07 | El estado OCUPADO solo lo asigna el sistema; un espacio ocupado no pasa a MANTENIMIENTO. |

## Programación bloqueante vs. no bloqueante

El envío del comprobante al registrar la salida simula una llamada a un servidor de correo (2 s).
- `?notificacion=sync` → el hilo HTTP espera el envío (≈2 s de respuesta).
- `?notificacion=async` (por defecto) → el envío se delega al pool `parq-async-*` con `@Async` y la respuesta es inmediata.

El reporte de recaudación tiene versión bloqueante y versión con `CompletableFuture`.

## CI/CD

`.github/workflows/ci.yml`: checkout → JDK 21 → compilar → pruebas → empaquetar → publicar reportes y JAR → resumen de éxito/fallo.

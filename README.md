# MANA FOOD API

API REST para restaurante **MANA FOOD** — Kotlin + Spring Boot 4.1 + PostgreSQL

**Puerto:** 8080 | **Swagger:** http://localhost:8080/swagger-ui.html

---

## Endpoints

| Módulo | Endpoints | Acceso |
|--------|-----------|--------|
| Auth | `POST /auth/register`, `POST /auth/login` | Público |
| Menú | `GET /menu`, `GET /categories` | Público |
| Usuarios | `GET/PUT /users/me`, `POST /users/me/change-password` | Auth |
| Carrito | `GET/POST /cart`, `PATCH/DELETE /cart/{id}`, `DELETE /cart` | Auth |
| Pedidos | `POST /orders`, `GET /orders/my-orders` | Auth |
| Admin | CRUD categorías, productos, pedidos, usuarios, estadísticas, auditoría, config | ADMIN |

## Seguridad

- **JWT** (HMAC-SHA256) en header `Authorization: Bearer <token>`
- **BCrypt** para contraseñas
- Roles: `ADMIN` y `CUSTOMER`

## Tech Stack

- Kotlin, Spring Boot, Spring Data JPA, Spring Security
- PostgreSQL, Swagger/OpenAPI, Gradle

## Credenciales iniciales

- Admin: `daniel@gmail.com` / `Daniel123.` (se crea automáticamente al iniciar)

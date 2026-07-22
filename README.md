# MANA FOOD API - Documentación Técnica Completa

API REST para gestión de menú, pedidos, carrito de compras y usuarios del restaurante **MANA FOOD**.

**Stack:** Kotlin + Spring Boot 4.1 + PostgreSQL  
**Puerto:** 8080  
**Swagger:** http://localhost:8080/swagger-ui.html

---

## INDICE

1. [Estructura General](#1-estructura-general)
2. [Paquete model (Entidades y Enums)](#2-paquete-model)
3. [Paquete repository (Repositorios JPA)](#3-paquete-repository)
4. [Paquete dto (Data Transfer Objects)](#4-paquete-dto)
5. [Paquete exception (Manejo de Errores)](#5-paquete-exception)
6. [Paquete service (Lógica de Negocio)](#6-paquete-service)
7. [Paquete controller (Endpoints REST)](#7-paquete-controller)
8. [Paquete config (Configuración)](#8-paquete-config)
9. [Archivo Principal](#9-archivo-principal)
10. [Reglas de Negocio](#10-reglas-de-negocio)
11. [Seguridad](#11-seguridad)

---

## 1. Estructura General

```
src/main/kotlin/co/edu/iub/manaproject/
│
├── ManaProjectApplication.kt       ← Punto de entrada
│
├── model/                           ← Entidades JPA + Enums
│   ├── User.kt
│   ├── UserRole.kt
│   ├── Category.kt
│   ├── Product.kt
│   ├── Order.kt
│   ├── OrderItem.kt
│   ├── OrderStatus.kt
│   ├── CartItem.kt
│   ├── PaymentType.kt
│   ├── AuditLog.kt
│   └── SystemConfig.kt
│
├── repository/                      ← Acceso a base de datos
│   ├── UserRepository.kt
│   ├── CategoryRepository.kt
│   ├── ProductRepository.kt
│   ├── OrderRepository.kt
│   ├── OrderItemRepository.kt
│   ├── CartItemRepository.kt
│   ├── AuditLogRepository.kt
│   └── SystemConfigRepository.kt
│
├── dto/                             ← Objetos de transferencia
│   ├── MessageResponse.kt
│   ├── auth/ (LoginRequest, TokenResponse)
│   ├── user/ (5 DTOs)
│   ├── category/ (4 DTOs)
│   ├── product/ (4 DTOs)
│   ├── menu/ (1 DTO)
│   ├── order/ (4 DTOs)
│   ├── cart/ (3 DTOs)
│   ├── stats/ (2 DTOs)
│   ├── audit/ (1 DTO)
│   └── config/ (2 DTOs)
│
├── exception/                       ← Manejo global de errores
│   ├── ApiError.kt
│   ├── ApiException.kt
│   └── GlobalExceptionHandler.kt
│
├── service/                         ← Lógica de negocio
│   ├── AuthService.kt
│   ├── UserService.kt
│   ├── JwtService.kt
│   ├── CategoryService.kt
│   ├── ProductService.kt
│   ├── MenuService.kt
│   ├── CartService.kt
│   ├── OrderService.kt
│   ├── StatsService.kt
│   ├── AuditService.kt
│   └── ConfigService.kt
│
├── controller/                      ← Endpoints REST
│   ├── HealthController.kt
│   ├── AuthController.kt
│   ├── UserController.kt
│   ├── CategoryController.kt
│   ├── ProductController.kt
│   ├── MenuController.kt
│   ├── CartController.kt
│   ├── OrderController.kt
│   ├── StatsController.kt
│   ├── AuditController.kt
│   └── ConfigController.kt
│
└── config/                          ← Configuración del sistema
    ├── SecurityConfig.kt
    ├── JwtAuthenticationFilter.kt
    ├── OpenApiConfig.kt
    └── DataInitializer.kt
```

---

## 2. Paquete model

Contiene las **entidades JPA** (mapean a tablas de la base de datos) y los **enums** (constantes con valores fijos).

---

### 2.1 `UserRole.kt` — Enum de roles

```kotlin
enum class UserRole {
    ADMIN,      // Administrador: acceso total
    CUSTOMER    // Cliente: solo puede ver menú, gestionar su carrito y sus pedidos
}
```

**Ubicación:** `model/UserRole.kt`  
**Tabla:** No aplica (es un enum, se almacena como texto en la columna `role` de `users`)

---

### 2.2 `User.kt` — Entidad Usuario

```kotlin
@Entity
@Table(name = "users")
class User(
    var id: Long?,          // Clave primaria, autoincremental
    var firstName: String,  // Nombre (máx 80 caracteres)
    var lastName: String,   // Apellido (máx 80)
    var username: String,   // Nombre de usuario (único)
    var email: String,      // Correo electrónico (único)
    var passwordHash: String, // Contraseña cifrada con BCrypt
    var role: UserRole,     // Rol: ADMIN o CUSTOMER
    var active: Boolean,    // true = activo, false = desactivado
    var createdAt: LocalDateTime  // Fecha de creación (solo lectura)
)
```

**Tabla:** `users`  
**Validaciones:** `username` y `email` son únicos (unique).  
**Nota:** La contraseña NUNCA se almacena en texto plano, siempre con BCrypt.

---

### 2.3 `Category.kt` — Entidad Categoría

```kotlin
@Entity
@Table(name = "categories")
class Category(
    var id: Long?,
    var name: String,        // Nombre de la categoría (único, máx 100)
    var description: String?, // Descripción (opcional, máx 300)
    var active: Boolean,     // true = activa (visible en menú)
    var createdAt: LocalDateTime
)
```

**Tabla:** `categories`  
**Regla de negocio:** Si una categoría tiene productos asociados, no se puede eliminar.

---

### 2.4 `Product.kt` — Entidad Producto

```kotlin
@Entity
@Table(name = "products")
class Product(
    var id: Long?,
    var name: String,         // Nombre (máx 120)
    var description: String?, // Descripción (máx 500)
    var price: BigDecimal,    // Precio (12 dígitos, 2 decimales)
    var active: Boolean,      // true = disponible para venta
    var category: Category?,  // Relación ManyToOne con Category
    var createdAt: LocalDateTime
)
```

**Tabla:** `products`  
**Relaciones:** `ManyToOne` → `Category` (un producto pertenece a una categoría).  
**Regla:** El precio no puede ser negativo.

---

### 2.5 `OrderStatus.kt` — Enum de estados de pedido

```kotlin
enum class OrderStatus {
    PENDING,     // Pedido creado, esperando confirmación
    CONFIRMED,   // Pedido confirmado por administrador
    PREPARING,   // Pedido en preparación
    COMPLETED,   // Pedido entregado (estado terminal)
    CANCELLED    // Pedido cancelado (estado terminal)
}
```

**Transiciones válidas:**
```
PENDING    → CONFIRMED, CANCELLED
CONFIRMED  → PREPARING, CANCELLED
PREPARING  → COMPLETED, CANCELLED
COMPLETED  → (ninguna)
CANCELLED  → (ninguna)
```

---

### 2.6 `PaymentType.kt` — Enum de métodos de pago

```kotlin
enum class PaymentType {
    EFECTIVO,       // Pago en efectivo
    TRANSFERENCIA   // Pago por transferencia bancaria
}
```

Se almacena directamente en la columna `payment_type` de la tabla `orders`.

---

### 2.7 `Order.kt` — Entidad Pedido

```kotlin
@Entity
@Table(name = "orders")
class Order(
    var id: Long?,
    var orderNumber: String,    // Número único (formato: ORD-{timestamp})
    var status: OrderStatus,    // Estado del pedido
    var total: BigDecimal,      // Total calculado automáticamente
    var notes: String?,         // Notas del pedido
    var user: User?,            // Relación ManyToOne → User (quién hizo el pedido)
    var paymentType: PaymentType?, // EFECTIVO o TRANSFERENCIA
    var active: Boolean,        // Soft delete: false = eliminado
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
    val items: MutableList<OrderItem>  // Relación OneToMany → OrderItem
)
```

**Tabla:** `orders`  
**Relaciones:**
- `ManyToOne` → `User` (un pedido pertenece a un usuario)
- `OneToMany` → `OrderItem` (un pedido tiene muchos items)

**Cascade:** `ALL` y `orphanRemoval = true` — si se elimina el pedido, se eliminan sus items.

---

### 2.8 `OrderItem.kt` — Entidad Item del Pedido

```kotlin
@Entity
@Table(name = "order_items")
class OrderItem(
    var id: Long?,
    var order: Order?,         // Relación ManyToOne → Order
    var product: Product?,     // Relación ManyToOne → Product
    var quantity: Int,         // Cantidad (mínimo 1)
    var unitPrice: BigDecimal, // Precio unitario al momento de la compra
    var subtotal: BigDecimal,  // unitPrice × quantity (cálculo automático)
    var notes: String?         // Notas del item (ej: "Sin cebolla")
)
```

**Tabla:** `order_items`  
**Nota:** `unitPrice` se guarda al momento de crear el pedido, por si el precio del producto cambia después.

---

### 2.9 `CartItem.kt` — Entidad Item del Carrito

```kotlin
@Entity
@Table(name = "cart_items")
class CartItem(
    var id: Long?,
    var user: User?,          // Relación ManyToOne → User (dueño del carrito)
    var product: Product?,    // Relación ManyToOne → Product
    var quantity: Int,        // Cantidad
    var notes: String?,       // Notas del item
    var createdAt: LocalDateTime
)
```

**Tabla:** `cart_items`  
**Comportamiento:** Si el usuario agrega un producto que ya está en su carrito, se incrementa la cantidad (no se duplica).

---

### 2.10 `AuditLog.kt` — Entidad de Auditoría

```kotlin
@Entity
@Table(name = "audit_logs")
class AuditLog(
    var id: Long?,
    var action: String,       // Acción: CREATE, UPDATE_STATUS, DELETE, CLEAR_ALL
    var entityType: String,   // Tipo de entidad: "Order", "User", etc.
    var entityId: Long,       // ID de la entidad afectada
    var username: String,     // Usuario que realizó la acción
    var details: String?,     // Detalles legibles (ej: "Estado cambiado de PENDING a CONFIRMED")
    var createdAt: LocalDateTime
)
```

**Tabla:** `audit_logs`  
**Propósito:** Registrar todas las acciones administrativas importantes para trazabilidad.

---

### 2.11 `SystemConfig.kt` — Entidad de Configuración

```kotlin
@Entity
@Table(name = "system_config")
class SystemConfig(
    var id: Long?,
    var configKey: String,    // Clave (única): ej: "IVA", "HORARIO"
    var configValue: String,  // Valor: ej: "19", "9:00-22:00"
    var description: String?  // Descripción opcional
)
```

**Tabla:** `system_config`  
**Propósito:** Almacenar configuración dinámica del sistema en formato clave-valor.

---

## 3. Paquete repository

Cada repositorio extiende `JpaRepository<T, ID>` y proporciona métodos para acceder a la base de datos. Spring Data JPA implementa automáticamente los métodos según el nombre.

---

### 3.1 `UserRepository.kt`

```kotlin
interface UserRepository : JpaRepository<User, Long> {

    // ¿Existe un usuario con este username?
    fun existsByUsername(username: String): Boolean
    // Ejemplo SQL: SELECT COUNT(*) FROM users WHERE username = ?

    // ¿Existe un usuario con este email?
    fun existsByEmail(email: String): Boolean
    // Ejemplo: SELECT COUNT(*) FROM users WHERE email = ?

    // Buscar por username O email (para login)
    fun findByUsernameOrEmail(username: String, email: String): User?
    // Ejemplo: SELECT * FROM users WHERE username = ? OR email = ? LIMIT 1

    // Buscar por username exacto
    fun findByUsername(username: String): User?
    // Ejemplo: SELECT * FROM users WHERE username = ?

    // ¿Existe email excluyendo un ID específico (para actualizar perfil)?
    fun existsByEmailAndIdNot(email: String, id: Long): Boolean
    // Ejemplo: SELECT COUNT(*) FROM users WHERE email = ? AND id != ?
}
```

---

### 3.2 `CategoryRepository.kt`

```kotlin
interface CategoryRepository : JpaRepository<Category, Long> {

    // ¿Existe una categoría con este nombre (sin importar mayúsculas)?
    fun existsByNameIgnoreCase(name: String): Boolean
    // SELECT COUNT(*) FROM categories WHERE LOWER(name) = LOWER(?)

    // Obtener todas las categorías activas ordenadas por nombre
    fun findAllByActiveTrueOrderByNameAsc(): List<Category>
    // SELECT * FROM categories WHERE active = true ORDER BY name ASC
}
```

---

### 3.3 `ProductRepository.kt`

```kotlin
interface ProductRepository : JpaRepository<Product, Long> {

    // Todos los productos ordenados por nombre
    fun findAllByOrderByNameAsc(): List<Product>
    // SELECT * FROM products ORDER BY name ASC

    // Productos activos cuya categoría también está activa
    fun findAllByActiveTrueAndCategoryActiveTrueOrderByCategoryNameAscNameAsc(): List<Product>
    // SELECT * FROM products p JOIN categories c ON p.category_id = c.id
    // WHERE p.active = true AND c.active = true ORDER BY c.name, p.name

    // Productos activos de una categoría específica
    fun findAllByActiveTrueAndCategoryActiveTrueAndCategoryIdOrderByNameAsc(categoryId: Long): List<Product>
    // Similar al anterior pero filtrando por categoryId
}
```

---

### 3.4 `OrderRepository.kt`

```kotlin
interface OrderRepository : JpaRepository<Order, Long> {

    // Todos los pedidos activos, ordenados por fecha descendente
    fun findAllByActiveTrueOrderByCreatedAtDesc(): List<Order>

    // Pedidos activos de un usuario específico
    fun findAllByUserIdAndActiveTrueOrderByCreatedAtDesc(userId: Long): List<Order>

    // Buscar pedido por su número único
    fun findByOrderNumber(orderNumber: String): Order?

    // Contar pedidos en un rango de fechas con un estado específico
    fun countByCreatedAtBetweenAndStatus(start: LocalDateTime, end: LocalDateTime, status: OrderStatus): Long

    // Pedidos activos en un rango de fechas
    fun findAllByCreatedAtBetweenAndActiveTrue(start: LocalDateTime, end: LocalDateTime): List<Order>
}
```

---

### 3.5 `OrderItemRepository.kt`

```kotlin
interface OrderItemRepository : JpaRepository<OrderItem, Long> {

    // Obtener todos los items de un pedido
    fun findAllByOrderId(orderId: Long): List<OrderItem>
    // SELECT * FROM order_items WHERE order_id = ?
}
```

---

### 3.6 `CartItemRepository.kt`

```kotlin
interface CartItemRepository : JpaRepository<CartItem, Long> {

    // Items del carrito de un usuario
    fun findAllByUserIdOrderByCreatedAtAsc(userId: Long): List<CartItem>

    // Buscar si un producto ya está en el carrito del usuario
    fun findByUserIdAndProductId(userId: Long, productId: Long): CartItem?

    // Eliminar todos los items del carrito de un usuario
    fun deleteAllByUserId(userId: Long)

    // Contar cuántos items tiene un usuario en su carrito
    fun countByUserId(userId: Long): Long
}
```

---

### 3.7 `AuditLogRepository.kt`

```kotlin
interface AuditLogRepository : JpaRepository<AuditLog, Long> {

    // Todos los logs ordenados por fecha descendente
    fun findAllByOrderByCreatedAtDesc(): List<AuditLog>

    // Logs en un rango de fechas
    fun findAllByCreatedAtBetweenOrderByCreatedAtDesc(start: LocalDateTime, end: LocalDateTime): List<AuditLog>

    // Logs de un usuario específico
    fun findAllByUsernameOrderByCreatedAtDesc(username: String): List<AuditLog>
}
```

---

### 3.8 `SystemConfigRepository.kt`

```kotlin
interface SystemConfigRepository : JpaRepository<SystemConfig, Long> {

    // Buscar configuración por clave exacta
    fun findByConfigKey(configKey: String): SystemConfig?

    // ¿Existe una configuración con esta clave?
    fun existsByConfigKeyIgnoreCase(configKey: String): Boolean
}
```

---

## 4. Paquete dto

Los DTOs (Data Transfer Objects) son objetos que transportan datos entre el cliente y el servidor. Se dividen en **Requests** (lo que el cliente envía) y **Responses** (lo que el servidor devuelve).

---

### 4.1 `MessageResponse.kt` — Respuesta genérica

```kotlin
data class MessageResponse(val message: String)
```

DTO simple que devuelve un mensaje de texto. Usado en operaciones como `DELETE /cart`, `POST /users/me/change-password`.

---

### 4.2 DTOs de Autenticación (`dto/auth/`)

#### `LoginRequest.kt`

```kotlin
data class LoginRequest(
    val identifier: String,  // Username o email del usuario
    val password: String     // Contraseña
)
```

Validaciones: `@NotBlank` en ambos campos.

#### `TokenResponse.kt`

```kotlin
data class TokenResponse(
    val accessToken: String,   // Token JWT
    val tokenType: String,     // Siempre "Bearer"
    val expiresIn: Long        // Segundos hasta expiración
)
```

---

### 4.3 DTOs de Usuario (`dto/user/`)

#### `CreateUserRequest.kt` — Registro de nuevo usuario

```kotlin
data class CreateUserRequest(
    val firstName: String,   // @NotBlank, @Size(max=80)
    val lastName: String,    // @NotBlank, @Size(max=80)
    val username: String,    // @NotBlank, @Size(min=3, max=50)
    val email: String,       // @NotBlank, @Email
    val password: String     // @NotBlank, @Size(min=8, max=100)
)
```

#### `UserResponse.kt` — Respuesta de datos del usuario

```kotlin
data class UserResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val role: UserRole,
    val active: Boolean
)
```

No incluye `passwordHash` por seguridad.

#### `UpdateProfileRequest.kt` — Actualización de perfil

```kotlin
data class UpdateProfileRequest(
    val email: String?,      // Opcional, @Email si se proporciona
    val firstName: String?,   // Opcional, @Size(min=1, max=80)
    val lastName: String?     // Opcional, @Size(min=1, max=80)
)
```

Todos los campos son opcionales (nullable), solo se actualizan los que vienen.

#### `ChangePasswordRequest.kt` — Cambio de contraseña

```kotlin
data class ChangePasswordRequest(
    val currentPassword: String,  // @NotBlank - contraseña actual
    val newPassword: String       // @NotBlank, @Size(min=8)
)
```

#### `UpdateUserRoleRequest.kt` — Cambio de rol (ADMIN)

```kotlin
data class UpdateUserRoleRequest(
    val role: UserRole  // @NotNull - nuevo rol
)
```

---

### 4.4 DTOs de Categoría (`dto/category/`)

#### `CreateCategoryRequest.kt`

```kotlin
data class CreateCategoryRequest(
    val name: String,         // @NotBlank, @Size(max=100)
    val description: String?  // @Size(max=300), opcional
)
```

#### `UpdateCategoryRequest.kt`

Mismos campos que el Create.

#### `CategoryStatusRequest.kt`

```kotlin
data class CategoryStatusRequest(
    val active: Boolean  // true = activar, false = desactivar
)
```

#### `CategoryResponse.kt`

```kotlin
data class CategoryResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val active: Boolean
)
```

---

### 4.5 DTOs de Producto (`dto/product/`)

#### `CreateProductRequest.kt`

```kotlin
data class CreateProductRequest(
    val name: String,           // @NotBlank, @Size(max=120)
    val description: String?,   // @Size(max=500)
    val price: BigDecimal,      // @NotNull, @DecimalMin("0.01")
    val categoryId: Long        // @NotNull - ID de la categoría
)
```

#### `UpdateProductRequest.kt`

Mismos campos que CreateProductRequest.

#### `ProductStatusRequest.kt`

```kotlin
data class ProductStatusRequest(
    val active: Boolean
)
```

#### `ProductResponse.kt`

```kotlin
data class ProductResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val active: Boolean,
    val categoryId: Long,
    val categoryName: String
)
```

---

### 4.6 DTOs de Menú (`dto/menu/`)

#### `MenuProductResponse.kt`

```kotlin
data class MenuProductResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val categoryId: Long,
    val categoryName: String
)
```

Similar a `ProductResponse` pero solo incluye productos activos de categorías activas.

---

### 4.7 DTOs de Pedido (`dto/order/`)

#### `CreateOrderRequest.kt`

```kotlin
data class CreateOrderRequest(
    val notes: String?,                        // @Size(max=500)
    val paymentType: PaymentType?,              // EFECTIVO o TRANSFERENCIA
    val items: List<CreateOrderItemRequest>    // @NotEmpty, mínimo 1 item
)
```

**Regla de negocio:** `items` no puede estar vacío.

#### `CreateOrderItemRequest.kt`

```kotlin
data class CreateOrderItemRequest(
    val productId: Long,        // @NotNull
    val quantity: Int,          // @Min(1), default = 1
    val unitPrice: BigDecimal,  // @DecimalMin("0.01")
    val notes: String?          // @Size(max=300)
)
```

#### `OrderItemResponse.kt`

```kotlin
data class OrderItemResponse(
    val id: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal,
    val notes: String?
)
```

#### `OrderResponse.kt`

```kotlin
data class OrderResponse(
    val id: Long,
    val orderNumber: String,
    val status: OrderStatus,
    val total: BigDecimal,
    val notes: String?,
    val userName: String,
    val userId: Long,
    val paymentType: PaymentType?,
    val items: List<OrderItemResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
```

#### `OrderStatusRequest.kt`

```kotlin
data class OrderStatusRequest(
    val status: OrderStatus  // @NotNull
)
```

---

### 4.8 DTOs de Carrito (`dto/cart/`)

#### `AddCartItemRequest.kt`

```kotlin
data class AddCartItemRequest(
    val productId: Long,   // @NotNull
    val quantity: Int,     // @Min(1), default = 1
    val notes: String?     // @Size(max=300)
)
```

#### `UpdateCartItemRequest.kt`

```kotlin
data class UpdateCartItemRequest(
    val quantity: Int,    // @Min(1)
    val notes: String?    // @Size(max=300)
)
```

#### `CartItemResponse.kt`

```kotlin
data class CartItemResponse(
    val id: Long,
    val productId: Long,
    val productName: String,
    val productPrice: BigDecimal,
    val quantity: Int,
    val subtotal: BigDecimal,  // productPrice × quantity
    val notes: String?
)
```

---

### 4.9 DTOs de Estadísticas (`dto/stats/`)

#### `DailyStatsResponse.kt`

```kotlin
data class DailyStatsResponse(
    val date: String,
    val totalOrders: Long,        // Total de pedidos del día
    val totalRevenue: BigDecimal,  // Suma de totales de pedidos COMPLETADOS
    val totalProductsSold: Long,  // Cantidad total de productos vendidos
    val completedOrders: Long,    // Pedidos completados
    val cancelledOrders: Long     // Pedidos cancelados
)
```

#### `StatsRequest.kt`

```kotlin
data class StatsRequest(
    val startDate: String?,   // Fecha inicio (YYYY-MM-DD)
    val endDate: String?      // Fecha fin (YYYY-MM-DD)
)
```

---

### 4.10 DTOs de Auditoría (`dto/audit/`)

#### `AuditLogResponse.kt`

```kotlin
data class AuditLogResponse(
    val id: Long,
    val action: String,
    val entityType: String,
    val entityId: Long,
    val username: String,
    val details: String?,
    val createdAt: LocalDateTime
)
```

---

### 4.11 DTOs de Configuración (`dto/config/`)

#### `ConfigResponse.kt`

```kotlin
data class ConfigResponse(
    val id: Long,
    val configKey: String,
    val configValue: String,
    val description: String?
)
```

#### `UpdateConfigRequest.kt`

```kotlin
data class UpdateConfigRequest(
    val configValue: String,  // @NotBlank, @Size(max=500)
    val description: String?  // @Size(max=300)
)
```

---

## 5. Paquete exception

Manejo centralizado de errores. Todas las excepciones lanzadas por los servicios son capturadas por `GlobalExceptionHandler` y convertidas en respuestas JSON con el formato adecuado.

---

### 5.1 `ApiError.kt` — Formato de error

```kotlin
data class ApiError(
    val timestamp: LocalDateTime,  // Momento del error
    val status: Int,                // Código HTTP: 400, 401, 403, 404, 409, 500
    val error: String,              // Razón: "Bad Request", "Not Found", etc.
    val message: String,            // Mensaje descriptivo para el usuario
    val path: String                // Ruta del endpoint que generó el error
)
```

**Ejemplo de respuesta de error:**
```json
{
  "timestamp": "2026-07-20T15:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Producto no encontrado",
  "path": "/orders/999"
}
```

---

### 5.2 `ApiException.kt` — Excepciones personalizadas

Clase base y sus subtipos:

| Excepción | Código HTTP | Uso |
|-----------|-------------|-----|
| `ApiException` (base) | - | Clase abstracta base |
| `DuplicateResourceException` | 409 Conflict | Email duplicado, username duplicado, categoría ya existe |
| `ResourceNotFoundException` | 404 Not Found | Usuario no encontrado, producto no encontrado, etc. |
| `InvalidCredentialsException` | 401 Unauthorized | Contraseña incorrecta, usuario inactivo |
| `ForbiddenOperationException` | 403 Forbidden | Acción no permitida |
| `InvalidRequestException` | 400 Bad Request | Validaciones de negocio: precio negativo, transición de estado inválida |
| `InvalidStatusTransitionException` | 400 Bad Request | Cambio de estado de pedido no válido |

**Ejemplo de uso en servicio:**
```kotlin
if (!user.active) {
    throw InvalidCredentialsException("Credenciales invalidas")
}
```

---

### 5.3 `GlobalExceptionHandler.kt` — Manejador global

Clase anotada con `@RestControllerAdvice` que captura TODAS las excepciones y las convierte en `ApiError`.

**Métodos:**

#### `handleApiException(ApiException, HttpServletRequest)`
- **Propósito:** Captura todas las excepciones personalizadas (ApiException y sus subtipos)
- **Respuesta:** Usa el `status` y `message` definidos en la excepción

#### `handleValidation(MethodArgumentNotValidException, HttpServletRequest)`
- **Propósito:** Captura errores de validación de `@Valid`
- **Ejemplo:** Cuando se envía un `name` vacío en `CreateCategoryRequest`
- **Respuesta:** 400 Bad Request con mensaje: "name: El nombre es obligatorio; email: El correo no tiene un formato válido"

#### `handleInvalidJson(HttpServletRequest)`
- **Propósito:** Captura errores cuando el JSON enviado no se puede parsear
- **Ejemplo:** Enviar `{ "name": }` (JSON malformado)
- **Respuesta:** 400 Bad Request

#### `handleAuthentication(AuthenticationException, HttpServletRequest)`
- **Propósito:** Captura errores de autenticación
- **Respuesta:** 401 Unauthorized

#### `handleAccessDenied(AccessDeniedException, HttpServletRequest)`
- **Propósito:** Captura intentos de acceso a recursos sin permisos
- **Respuesta:** 403 Forbidden

#### `handleUnexpected(Exception, HttpServletRequest)`
- **Propósito:** Captura cualquier error no previsto
- **Respuesta:** 500 Internal Server Error (con log del error completo)

---

## 6. Paquete service

Contiene la **lógica de negocio** de la aplicación. Cada servicio es un `@Service` de Spring y contiene métodos transactional donde es necesario.

---

### 6.1 `AuthService.kt` — Servicio de Autenticación

**Dependencias:** `UserRepository`, `PasswordEncoder`, `JwtService`

#### `createUser(request: CreateUserRequest): UserResponse`

**Propósito:** Registrar un nuevo usuario.  
**Validaciones:**
1. Verifica que el `username` no exista → si existe, lanza `DuplicateResourceException`
2. Verifica que el `email` no exista → si existe, lanza `DuplicateResourceException`
3. Crea el `User` con `passwordHash = passwordEncoder.encode(password)`
4. Guarda y devuelve `UserResponse`

#### `login(request: LoginRequest): TokenResponse`

**Propósito:** Iniciar sesión y obtener token JWT.  
**Validaciones:**
1. Busca usuario por `username` O `email` con `findByUsernameOrEmail`
2. Si no existe → lanza `DuplicateResourceException` (con mensaje "Credenciales invalidas" por seguridad)
3. Si el usuario no está activo O la contraseña no coincide → lanza `InvalidCredentialsException`
4. Si todo ok → genera token JWT con `jwtService.generateToken(user)`
5. Devuelve `TokenResponse(accessToken, "Bearer", expiresIn)`

---

### 6.2 `UserService.kt` — Servicio de Usuarios

**Dependencias:** `UserRepository`, `PasswordEncoder`

#### `getProfile(currentUsername: String): UserResponse`

Obtiene el perfil del usuario autenticado.

#### `updateProfile(currentUsername: String, request: UpdateProfileRequest): UserResponse`

Actualiza nombre, apellido y/o email.  
**Validación:** Si se cambia el email, verifica que no esté en uso por otro usuario (`existsByEmailAndIdNot`).

#### `updateRole(userId: Long, request: UpdateUserRoleRequest): UserResponse`

Solo ADMIN. Cambia el rol de un usuario.

#### `getAllUsers(): List<UserResponse>`

Solo ADMIN. Lista todos los usuarios.

#### `changePassword(currentUsername: String, request: ChangePasswordRequest)`

Valida la contraseña actual con `passwordEncoder.matches`, luego guarda la nueva cifrada.

---

### 6.3 `JwtService.kt` — Servicio JWT

**Dependencias:** `secret` y `expirationMinutes` de `application.yml`

#### `generateToken(user: User): String`

Crea un token JWT con:
- **subject:** `user.username`
- **claims:** `role` = `user.role.name`
- **issuedAt:** ahora
- **expiration:** ahora + `expirationMinutes` minutos
- Firma HMAC-SHA256 con `signingKey`

#### `extractUsername(token: String): String`

Extrae el username del token.

#### `isTokenValid(token: String): Boolean`

Verifica que el token no esté expirado y sea válido.

---

### 6.4 `CategoryService.kt` — Servicio de Categorías

**Dependencias:** `CategoryRepository`, `ProductRepository`

#### `getActiveCategory(): List<CategoryResponse>`

Devuelve solo categorías activas ordenadas por nombre.

#### `createCategory(request: CreateCategoryRequest): CategoryResponse`

Crea categoría. Verifica que no exista otra con el mismo nombre.

#### `updateCategory(id: Long, request: UpdateCategoryRequest): CategoryResponse`

Actualiza nombre y descripción. Si cambió el nombre, verifica que no esté duplicado.

#### `changeStatus(id: Long, request: CategoryStatusRequest): CategoryResponse`

Activa o desactiva una categoría.

#### `deleteCategory(id: Long): CategoryResponse`

**Regla de negocio:** Verifica que la categoría no tenga productos asociados. Si tiene, lanza `InvalidRequestException`. Si no, la desactiva (soft delete).

---

### 6.5 `ProductService.kt` — Servicio de Productos

**Dependencias:** `ProductRepository`, `CategoryRepository`

#### `getProducts(): List<ProductResponse>`

Lista todos los productos (activos e inactivos).

#### `createProduct(request: CreateProductRequest): ProductResponse`

Crea producto. Busca la categoría por ID, si no existe lanza `ResourceNotFoundException`.

#### `updateProduct(id: Long, request: UpdateProductRequest): ProductResponse`

Actualiza producto. Busca producto y categoría.

#### `changeStatus(id: Long, active: Boolean): ProductResponse`

Activa o desactiva un producto.

#### `deleteProduct(id: Long): ProductResponse`

Desactiva el producto (soft delete).

---

### 6.6 `MenuService.kt` — Servicio de Menú

**Dependencias:** `ProductRepository`

#### `getMenu(): List<MenuProductResponse>`

Devuelve productos que cumplan:
- `product.active = true` (producto activo)
- `product.category.active = true` (categoría activa)

Ordenados por nombre de categoría y nombre de producto.

#### `getMenuByCategoryId(categoryId: Long): List<MenuProductResponse>`

Filtra el menú por categoría específica. Mismas condiciones de activo.

---

### 6.7 `CartService.kt` — Servicio de Carrito

**Dependencias:** `CartItemRepository`, `ProductRepository`, `UserRepository`

#### `getCart(username: String): List<CartItemResponse>`

Obtiene todos los items del carrito del usuario autenticado.

#### `addItem(username: String, request: AddCartItemRequest): CartItemResponse`

**Comportamiento:**
1. Busca el producto → si no existe o no está activo → `ResourceNotFoundException` / `InvalidRequestException`
2. Si el producto ya está en el carrito → **incrementa la cantidad** (no crea duplicado)
3. Si no está → crea un nuevo `CartItem`

**Cálculo del subtotal:** `product.price × cartItem.quantity`

#### `updateItem(id: Long, username: String, request: UpdateCartItemRequest): CartItemResponse`

Actualiza cantidad y notas de un item del carrito. Verifica que el item pertenezca al usuario.

#### `removeItem(id: Long, username: String)`

Elimina un item específico del carrito.

#### `clearCart(username: String)`

Elimina TODOS los items del carrito del usuario.

#### `getCartCount(username: String): Long`

Devuelve el número de items en el carrito.

---

### 6.8 `OrderService.kt` — Servicio de Pedidos

**Dependencias:** `OrderRepository`, `ProductRepository`, `UserRepository`, `AuditService`

#### `createOrder(username: String, request: CreateOrderRequest): OrderResponse`

**Flujo:**
1. Busca el usuario autenticado
2. Para cada item en `request.items`:
   - Valida que el producto exista y esté activo
   - Valida que la categoría del producto esté activa
   - Valida que el precio no sea negativo
   - Calcula subtotal = `unitPrice × quantity`
3. Calcula total = suma de todos los subtotales
4. Genera `orderNumber = "ORD-${System.currentTimeMillis()}"` (único)
5. Crea el `Order` con estado `PENDING`
6. Guarda el pedido con sus items
7. Registra en auditoría: `auditService.log("CREATE", "Order", id, username, ...)`

#### `getOrders(): List<OrderResponse>`

Todos los pedidos activos, ordenados por fecha descendente. Solo ADMIN.

#### `getUserOrders(username: String): List<OrderResponse>`

Pedidos del usuario autenticado.

#### `getOrderById(id: Long): OrderResponse`

Detalle de un pedido específico.

#### `updateStatus(id: Long, request: OrderStatusRequest): OrderResponse`

**Cambio de estado con validación de transiciones:**
```kotlin
val validTransitions = mapOf(
    PENDING   → [CONFIRMED, CANCELLED],
    CONFIRMED → [PREPARING, CANCELLED],
    PREPARING → [COMPLETED, CANCELLED],
    COMPLETED → [],
    CANCELLED → []
)
```

Si la transición no es válida, lanza `InvalidRequestException`.

#### `deleteOrder(id: Long, username: String): OrderResponse`

Soft delete: marca `active = false`. Registra en auditoría.

#### `clearAllOrders(username: String): Long`

Marca todos los pedidos como inactivos. Retorna el número de pedidos afectados. Registra en auditoría.

---

### 6.9 `StatsService.kt` — Servicio de Estadísticas

**Dependencias:** `OrderRepository`

#### `getDailyStats(date: String): DailyStatsResponse`

Calcula estadísticas para un día específico:
- `totalOrders`: pedidos en ese día
- `totalRevenue`: suma de `total` de pedidos COMPLETADOS
- `totalProductsSold`: suma de cantidades de items de pedidos COMPLETADOS
- `completedOrders` y `cancelledOrders`: conteo por estado

#### `getStatsBetween(startDate: String, endDate: String): List<DailyStatsResponse>

Agrupa pedidos por día en un rango de fechas y calcula estadísticas para cada día.

---

### 6.10 `AuditService.kt` — Servicio de Auditoría

**Dependencias:** `AuditLogRepository`

#### `log(action: String, entityType: String, entityId: Long, username: String, details: String?)`

Crea un registro de auditoría. Llamado por otros servicios después de acciones importantes.

#### `getAllLogs(): List<AuditLogResponse>`

Todos los logs ordenados por fecha descendente. Solo ADMIN.

#### `getLogsByUser(username: String): List<AuditLogResponse>`

Logs filtrados por usuario.

---

### 6.11 `ConfigService.kt` — Servicio de Configuración

**Dependencias:** `SystemConfigRepository`

#### `getAllConfigs(): List<ConfigResponse>`

Lista todas las configuraciones.

#### `getConfigByKey(key: String): ConfigResponse`

Busca por clave exacta.

#### `createConfig(key: String, value: String, description: String?): ConfigResponse`

Crea configuración. Verifica que la clave no exista.

#### `updateConfig(id: Long, request: UpdateConfigRequest): ConfigResponse`

Actualiza valor y descripción de una configuración existente.

#### `deleteConfig(id: Long)`

Elimina una configuración.

---

## 7. Paquete controller

Los controladores exponen los endpoints REST. Usan `@RestController` y `@RequestMapping`.

---

### 7.1 `HealthController.kt` — Health Check

```kotlin
@RestController
@RequestMapping("/")
```

| Método | Endpoint | Autenticación | Descripción |
|--------|----------|---------------|-------------|
| GET | `/` | Público | Verifica que la app y DB estén funcionando |

**Respuesta:** `{"status": "UP", "database": "CONNECTED"}`

---

### 7.2 `AuthController.kt` — Autenticación

```kotlin
@RestController
@RequestMapping("/auth")
```

| Método | Endpoint | Autenticación | Request | Response |
|--------|----------|---------------|---------|----------|
| POST | `/auth/register` | Público | `CreateUserRequest` | `201 + UserResponse` |
| POST | `/auth/login` | Público | `LoginRequest` | `200 + TokenResponse` |

---

### 7.3 `UserController.kt` — Usuarios

```kotlin
@RestController
@RequestMapping("/users")
```

| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| GET | `/users` | ADMIN | Listar usuarios |
| GET | `/users/me` | Authenticated | Ver mi perfil |
| PUT | `/users/me` | Authenticated | Actualizar mi perfil |
| PUT | `/users/{id}/role` | ADMIN | Cambiar rol de usuario |
| POST | `/users/me/change-password` | Authenticated | Cambiar contraseña |

**Nota:** El `authentication.name` proporciona el username del usuario autenticado desde el token JWT.

---

### 7.4 `CategoryController.kt` — Categorías

```kotlin
@RestController
@RequestMapping("/categories")
```

| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| GET | `/categories` | Público | Categorías activas |
| POST | `/categories` | ADMIN | Crear categoría |
| PUT | `/categories/{id}` | ADMIN | Actualizar categoría |
| PATCH | `/categories/{id}/status` | ADMIN | Activar/desactivar |
| DELETE | `/categories/{id}` | ADMIN | Eliminar (valida productos asociados) |

---

### 7.5 `ProductController.kt` — Productos

```kotlin
@RestController
@RequestMapping("/products")
```

| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| GET | `/products` | ADMIN | Listar productos |
| POST | `/products` | ADMIN | Crear producto |
| PUT | `/products/{id}` | ADMIN | Actualizar producto |
| PATCH | `/products/{id}/status` | ADMIN | Activar/desactivar |
| DELETE | `/products/{id}` | ADMIN | Eliminar producto |

---

### 7.6 `MenuController.kt` — Menú

```kotlin
@RestController
@RequestMapping("/menu")
```

| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| GET | `/menu` | Público | Menú completo |
| GET | `/menu?categoryId={id}` | Público | Menú filtrado por categoría |

---

### 7.7 `CartController.kt` — Carrito de Compras

```kotlin
@RestController
@RequestMapping("/cart")
```

| Método | Endpoint | Rol | Request | Response |
|--------|----------|-----|---------|----------|
| GET | `/cart` | Auth | - | `List<CartItemResponse>` |
| GET | `/cart/count` | Auth | - | `{"count": N}` |
| POST | `/cart` | Auth | `AddCartItemRequest` | `201 + CartItemResponse` |
| PATCH | `/cart/{id}` | Auth | `UpdateCartItemRequest` | `CartItemResponse` |
| DELETE | `/cart/{id}` | Auth | - | `MessageResponse` |
| DELETE | `/cart` | Auth | - | `MessageResponse` |

---

### 7.8 `OrderController.kt` — Pedidos

```kotlin
@RestController
@RequestMapping("/orders")
```

| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| POST | `/orders` | Auth | Crear pedido |
| GET | `/orders/my-orders` | Auth | Mis pedidos |
| GET | `/orders` | ADMIN | Todos los pedidos |
| GET | `/orders/{id}` | Auth | Detalle del pedido |
| PATCH | `/orders/{id}/status` | ADMIN | Cambiar estado |
| DELETE | `/orders/{id}` | ADMIN | Eliminar pedido |
| DELETE | `/orders` | ADMIN | Limpiar todos |

---

### 7.9 `StatsController.kt` — Estadísticas

```kotlin
@RestController
@RequestMapping("/stats")
@PreAuthorize("hasRole('ADMIN')")  // Todos los endpoints requieren ADMIN
```

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/stats/daily?date=2026-07-20` | Estadísticas del día |
| GET | `/stats/range?startDate=...&endDate=...` | Estadísticas por rango |

---

### 7.10 `AuditController.kt` — Auditoría

```kotlin
@RestController
@RequestMapping("/audit")
@PreAuthorize("hasRole('ADMIN')")
```

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/audit` | Todos los logs |
| GET | `/audit/user/{username}` | Logs de un usuario |

---

### 7.11 `ConfigController.kt` — Configuración

```kotlin
@RestController
@RequestMapping("/config")
@PreAuthorize("hasRole('ADMIN')")
```

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/config` | Listar configuraciones |
| GET | `/config/{key}` | Obtener por clave |
| POST | `/config?key=...&value=...` | Crear configuración |
| PUT | `/config/{id}` | Actualizar configuración |
| DELETE | `/config/{id}` | Eliminar configuración |

---

## 8. Paquete config

Configuración del sistema Spring Boot.

---

### 8.1 `SecurityConfig.kt` — Configuración de Seguridad

Anotaciones: `@Configuration`, `@EnableWebSecurity`, `@EnableMethodSecurity`

#### `passwordEncoder(): PasswordEncoder`

```kotlin
@Bean
fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
```

Devuelve un codificador BCrypt para cifrar contraseñas. BCrypt aplica automáticamente un salt aleatorio.

#### `securityFilterChain(http: HttpSecurity): SecurityFilterChain`

Configura la cadena de filtros de seguridad:

1. **CSRF desactivado** — La API usa tokens JWT, no formularios HTML
2. **Stateless** — No se usan sesiones HTTP
3. **Reglas de autorización:** (ver tabla)
4. **Filtro JWT** — Se ejecuta antes del filtro de autenticación estándar
5. **Form login y HTTP Basic** — Desactivados

**Reglas de autorización por ruta:**

| Ruta | Acceso |
|------|--------|
| `GET /` | Público |
| `POST /auth/register`, `/auth/login` | Público |
| `GET /menu/**` | Público |
| `GET /categories/**` | Público |
| `/error` | Público |
| `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**` | Público |
| `POST/PUT/PATCH/DELETE /categories/**` | ADMIN |
| `GET/POST/PUT/PATCH/DELETE /products/**` | ADMIN |
| `GET /users/**` | ADMIN |
| `/orders/my-orders/**` | Autenticado |
| `GET/PATCH/DELETE /orders/**` | ADMIN |
| `POST /orders/**` | Autenticado |
| `/cart/**` | Autenticado |
| `/stats/**` | ADMIN |
| `/audit/**` | ADMIN |
| `/config/**` | ADMIN |
| Cualquier otra | Autenticado |

---

### 8.2 `JwtAuthenticationFilter.kt` — Filtro JWT

```kotlin
@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userRepository: UserRepository
) : OncePerRequestFilter()
```

**Flujo de autenticación:**

1. Lee el header `Authorization`
2. Si no comienza con "Bearer " → continúa la cadena sin autenticar
3. Extrae el token (sin "Bearer ")
4. Extrae el username del token
5. Si el token es válido y no hay autenticación previa:
   a. Busca el usuario en la BD
   b. Si existe y está activo → crea `UsernamePasswordAuthenticationToken` con rol
   c. Lo guarda en `SecurityContextHolder`
6. Continúa la cadena de filtros

**Nota:** No usamos `UserDetailsService` de Spring Security estándar; manejamos la autenticación manualmente con el `UserRepository`.

---

### 8.3 `OpenApiConfig.kt` — Configuración Swagger

```kotlin
@Configuration
class OpenApiConfig
```

Configura Swagger/OpenAPI con:
- Título: "MANA FOOD API"
- Versión: "1.0.0"
- Descripción: "API REST para gestión de menú, pedidos y usuarios"
- Esquema de seguridad JWT (Bearer token)

---

### 8.4 `DataInitializer.kt` — Inicializador de datos

```kotlin
@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner
```

**Propósito:** Crear datos iniciales al arrancar la aplicación por primera vez.

**`run()`:**
- Verifica si el usuario admin (`daniel@gmail.com`) ya existe
- Si NO existe → lo crea con:
  - Nombre: "Daniel Henriquez"
  - Username: "Daniel"
  - Email: "daniel@gmail.com"
  - Contraseña: "Daniel123." (cifrada con BCrypt)
  - Rol: ADMIN

**Importante:** Solo se ejecuta si el usuario no existe en la BD.

---

## 9. Archivo Principal

### `ManaProjectApplication.kt`

```kotlin
package co.edu.iub.manaproject

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ManaProjectApplication

fun main(args: Array<String>) {
    runApplication<ManaProjectApplication>(*args)
}
```

**`@SpringBootApplication`** es una combinación de:
- `@Configuration` — Marca la clase como fuente de beans
- `@EnableAutoConfiguration` — Configura Spring Boot automáticamente
- `@ComponentScan` — Escanea el paquete en busca de componentes

**`main(args)`** — Punto de entrada. Llama a `runApplication` que inicia el contenedor Spring y arranca el servidor Tomcat embebido.

---

## 10. Reglas de Negocio

| # | Regla | Implementación | Archivo |
|---|-------|---------------|---------|
| 1 | Solo se venden productos de categorías activas | `findAllByActiveTrueAndCategoryActiveTrue...` | `MenuService.kt:27` |
| 2 | Todo pedido debe contener al menos un producto | `@NotEmpty` en `items` | `CreateOrderRequest.kt:12` |
| 3 | Número de pedido único | `ORD-${System.currentTimeMillis()}` | `OrderService.kt:60` |
| 4 | Precios no pueden ser negativos | `@DecimalMin("0.01")` | `CreateProductRequest.kt:17` |
| 5 | Solo admins autenticados pueden eliminar pedidos | `@PreAuthorize("hasRole('ADMIN')")` | `OrderController.kt:69` |
| 6 | Categorías con productos no se eliminan | Validación en `deleteCategory()` | `CategoryService.kt:73` |
| 7 | Transiciones de estado válidas | Mapa de transiciones en `updateStatus()` | `OrderService.kt:103` |

---

## 11. Seguridad

### JWT (JSON Web Token)
- **Generación:** Al hacer login exitoso, se genera un token firmado con HMAC-SHA256
- **Contenido:** `subject = username`, `claim = role`
- **Expiración:** Configurable en `.env` (`JWT_EXPIRATION_MINUTES`)
- **Validación:** En cada request, el filtro JWT verifica el token y establece la autenticación

### BCrypt
- **Propósito:** Cifrado de contraseñas
- **Características:** Salt automático, función lenta (resistente a ataques de fuerza bruta)
- **Uso:** `passwordEncoder.encode(password)` para guardar, `passwordEncoder.matches(password, hash)` para verificar

### Control de Acceso Basado en Roles
- **Roles:** `ADMIN` y `CUSTOMER`
- **Mecanismo:** Spring Security `hasRole('ADMIN')` en rutas y `@PreAuthorize` en controladores
- **Almacenamiento:** El rol se incluye como claim en el token JWT y se asigna en el filtro

### Prevención de SQL Injection
- **Spring Data JPA** utiliza consultas parametrizadas (PreparedStatement) automáticamente
- Nunca se concatenan strings para construir consultas SQL

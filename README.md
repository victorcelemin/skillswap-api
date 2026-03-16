# SkillSwap API

> La plataforma donde el conocimiento es la moneda.

SkillSwap es un backend REST para una plataforma de intercambio de habilidades. Los usuarios ensenan lo que saben y aprenden lo que quieren usando **creditos** en lugar de dinero.

---

## Stack tecnologico

| Tecnologia | Version | Proposito |
|---|---|---|
| Java | 21 (LTS) | Lenguaje principal |
| Spring Boot | 3.2.3 | Framework base |
| Spring Security | 6.2 | Autenticacion y autorizacion |
| Spring Data JPA | 3.2 | Persistencia ORM |
| Hibernate | 6.4 | Implementacion JPA |
| PostgreSQL | 16 | Base de datos |
| JJWT | 0.12.5 | JSON Web Tokens |
| WebSocket STOMP | 6.1 | Notificaciones en tiempo real |
| MapStruct | 1.5.5 | Mapeo de objetos (compile-time) |
| Lombok | 1.18.30 | Reduccion de boilerplate |
| SpringDoc OpenAPI | 2.3.0 | Documentacion Swagger |
| Maven | 3.9.6 | Gestion de dependencias |
| Docker | 24+ | Contenedor de PostgreSQL |

---

## Prerequisitos

- **Java 21+** — [Eclipse Temurin](https://adoptium.net/)
- **Maven 3.9+** — [Apache Maven](https://maven.apache.org/download.cgi)
- **Docker Desktop** — [Docker](https://www.docker.com/products/docker-desktop/) (para PostgreSQL)
- **Git** (opcional)

> Si ya tienes PostgreSQL instalado nativamente, ver la seccion [PostgreSQL nativo](#postgresql-nativo).

---

## Inicio rapido (5 minutos)

### 1. Clonar o descargar el proyecto

```bash
git clone <repo-url>
cd skillswap
```

### 2. Levantar PostgreSQL con Docker

```bash
# Levantar solo la BD (puerto 5433 para evitar conflictos)
docker compose up -d postgres

# Verificar que esta healthy
docker compose ps
# STATUS debe ser: Up (healthy)
```

### 3. Compilar

```bash
mvn clean package -DskipTests
```

### 4. Ejecutar

```bash
java -jar target/skillswap-1.0.0-SNAPSHOT.jar
```

La aplicacion arranca en **http://localhost:8081/api**

Al iniciar por primera vez, se cargan automaticamente **37 habilidades** en 9 categorias.

### 5. Verificar

```bash
# Health check basico
curl http://localhost:8081/api/skills/categories

# Swagger UI
# Abrir en el navegador: http://localhost:8081/api/swagger-ui.html
```

---

## Configuracion

### Variables de entorno

| Variable | Default | Descripcion |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5433/skillswap_db` | URL de conexion a PostgreSQL |
| `DB_USERNAME` | `skillswap_user` | Usuario de BD |
| `DB_PASSWORD` | `skillswap_pass` | Password de BD |
| `JWT_SECRET` | *(valor por defecto en yml)* | Secreto para firmar JWT (cambiar en produccion) |
| `SERVER_PORT` | `8081` | Puerto del servidor |

### Perfiles de Spring

```bash
# Desarrollo (default)
java -jar target/skillswap-1.0.0-SNAPSHOT.jar

# Con variables personalizadas
java -jar target/skillswap-1.0.0-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:postgresql://mi-host:5432/skillswap_db \
  --server.port=9090
```

### PostgreSQL nativo

Si tienes PostgreSQL instalado nativamente en el puerto 5432:

```sql
-- Ejecutar como superusuario
CREATE DATABASE skillswap_db;
CREATE USER skillswap_user WITH PASSWORD 'skillswap_pass';
GRANT ALL PRIVILEGES ON DATABASE skillswap_db TO skillswap_user;
```

Luego ajustar la URL en `application.yml`:
```yaml
spring.datasource.url: jdbc:postgresql://localhost:5432/skillswap_db
```

---

## API Reference

**Base URL:** `http://localhost:8081/api`

**Documentacion interactiva:** `http://localhost:8081/api/swagger-ui.html`

### Autenticacion

Todos los endpoints protegidos requieren el header:
```
Authorization: Bearer <jwt-token>
```

El token se obtiene del endpoint `/auth/login` o `/auth/register`.

---

### Auth

#### Registrar usuario

```http
POST /auth/register
Content-Type: application/json

{
  "username": "john_dev",
  "email": "john@example.com",
  "password": "SecurePass1",
  "fullName": "John Doe",
  "bio": "Desarrollador Python"
}
```

**Respuesta 201:**
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente. Bienvenido a SkillSwap!",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "username": "john_dev",
      "creditsBalance": 50
    }
  }
}
```

#### Login

```http
POST /auth/login
Content-Type: application/json

{
  "identifier": "john@example.com",
  "password": "SecurePass1"
}
```

> El campo `identifier` acepta email O username.

---

### Habilidades (publico)

```http
GET /skills                        # Listar con paginacion
GET /skills/categories             # Todas las categorias
GET /skills/category/TECHNOLOGY    # Por categoria
```

**Categorias disponibles:**
`TECHNOLOGY` | `LANGUAGES` | `MUSIC` | `ART_AND_DESIGN` | `BUSINESS` | `SCIENCE` | `SPORTS_AND_FITNESS` | `COOKING` | `PERSONAL_DEVELOPMENT` | `OTHER`

---

### Ofertas

```http
# Publico
GET /offers                        # Listar activas (paginado)
GET /offers?q=python               # Busqueda por texto libre
GET /offers?category=TECHNOLOGY    # Filtrar por categoria
GET /offers/{id}                   # Detalle + incrementa vistas

# Autenticado
POST /offers                       # Crear oferta
GET /offers/my                     # Mis ofertas como teacher
PATCH /offers/{id}/status?status=PAUSED   # Pausar/cerrar
```

**Crear oferta:**
```json
{
  "skillId": 1,
  "title": "Aprende Python desde cero con proyectos reales",
  "description": "Descripcion detallada de 30+ caracteres...",
  "creditsCostPerHour": 5,
  "durationMinutes": 60,
  "modality": "ONLINE",
  "tags": "python,backend,api",
  "maxStudentsPerSession": 1
}
```

---

### Sesiones

El sistema de sesiones gestiona el flujo de reservas y la transferencia de creditos.

**Flujo de estados:**
```
PENDING -> CONFIRMED -> COMPLETED
        -> CANCELLED (con reembolso)
        -> REJECTED
```

**Creditos:**
- Al reservar (`PENDING`): se deducen del estudiante
- Al completar (`COMPLETED`): se transfieren al teacher
- Al cancelar: reembolso automatico al estudiante

```http
POST /sessions                     # Reservar sesion (deduce creditos)
PATCH /sessions/{id}/confirm       # Teacher confirma
PATCH /sessions/{id}/complete      # Teacher completa (transfiere creditos)
PATCH /sessions/{id}/cancel        # Cancelar (reembolso automatico)
GET /sessions/my/student           # Mis sesiones como estudiante
GET /sessions/my/teacher           # Mis sesiones como teacher
```

**Reservar sesion:**
```json
{
  "offerId": 1,
  "scheduledAt": "2025-12-25T15:00:00",
  "studentNotes": "Soy principiante"
}
```

---

### Reviews

Solo se puede resenar una sesion `COMPLETED`, y solo el estudiante de esa sesion.

```http
POST /reviews                           # Crear review
GET /reviews/teacher/{teacherId}        # Reviews de un teacher (publico)
GET /reviews/my                         # Mis reviews como estudiante
```

**Crear review:**
```json
{
  "sessionId": 1,
  "rating": 5,
  "comment": "Excelente clase!",
  "isPublic": true
}
```

---

### Usuarios

```http
GET /users/me                          # Mi perfil completo (JWT)
GET /users/{id}                        # Perfil publico por ID
GET /users/username/{username}         # Perfil publico por username
```

---

## WebSocket (STOMP)

### Conexion

```javascript
const socket = new SockJS('http://localhost:8081/api/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({'Authorization': 'Bearer ' + token}, function(frame) {
    console.log('Conectado:', frame);

    // Notificaciones privadas
    stompClient.subscribe('/user/queue/notifications', function(msg) {
        const event = JSON.parse(msg.body);
        console.log('Notificacion:', event.type, event.message);
    });

    // Stats en tiempo real
    stompClient.subscribe('/topic/stats', function(msg) {
        console.log('Stats:', JSON.parse(msg.body));
    });
});
```

### Eventos de notificacion

| Evento | Receptor | Cuando ocurre |
|---|---|---|
| `SESSION_BOOKED` | Teacher | Estudiante reserva la sesion |
| `SESSION_CONFIRMED` | Estudiante | Teacher confirma la sesion |
| `SESSION_COMPLETED` | Teacher + Estudiante | Sesion completada y creditos transferidos |
| `SESSION_CANCELLED` | Ambos | Cualquiera cancela |

### Heartbeat / Ping

```javascript
stompClient.send('/app/ping', {}, JSON.stringify({}));
// Recibe en: /user/queue/pong
```

---

## Sistema de creditos

```
Registro               -> +50 creditos (configurable)
Reservar sesion        -> -CEIL(creditsPorHora x minutos / 60)
Completar sesion       -> Teacher recibe los creditos del estudiante
Cancelar sesion        -> Reembolso automatico al estudiante
```

**Ejemplo:**
- Oferta: 5 creditos/hora, duracion 60 min
- Costo real: CEIL(5 x 60 / 60) = 5 creditos

---

## Arquitectura del proyecto

```
src/main/java/com/skillswap/
├── SkillSwapApplication.java       # Entry point (@EnableJpaAuditing)
├── config/
│   ├── SecurityConfig.java         # Spring Security + CORS + JWT filter chain
│   ├── WebSocketConfig.java        # STOMP broker + endpoints
│   ├── SwaggerConfig.java          # OpenAPI 3 definition
│   └── DataInitializer.java        # Catalogo inicial de 37 habilidades
├── controller/
│   ├── AuthController.java         # POST /auth/register, /auth/login
│   ├── OfferController.java        # CRUD ofertas + paginacion + busqueda
│   ├── SessionController.java      # Ciclo de vida de sesiones
│   ├── ReviewController.java       # Reviews post-sesion
│   ├── SkillController.java        # Catalogo de habilidades
│   └── UserController.java         # Perfiles de usuario
├── service/
│   ├── AuthService.java            # Registro, login, JWT generation
│   ├── OfferService.java           # Logica de ofertas + busqueda
│   ├── SessionService.java         # Reservas + transferencia de creditos
│   ├── ReviewService.java          # Reviews + actualizacion de ratings
│   └── UserDetailsServiceImpl.java # Spring Security UserDetailsService
├── repository/
│   ├── UserRepository.java         # findByEmailOrUsername, findByIdWithSkills
│   ├── OfferRepository.java        # searchByText, findByCreditRange (JpaSpec)
│   ├── SessionRepository.java      # existsConflictingSession, findPending
│   ├── ReviewRepository.java       # calculateAverageRating
│   ├── SkillRepository.java
│   └── UserSkillRepository.java
├── entity/
│   ├── BaseEntity.java             # id + createdAt + updatedAt (auditoria)
│   ├── User.java                   # Implementa UserDetails + creditos
│   ├── Skill.java                  # Catalogo con color hex para UI
│   ├── UserSkill.java              # Relacion User<->Skill con nivel/tipo
│   ├── Offer.java                  # Oferta de ensenanza con tags CSV
│   ├── Session.java                # Reserva con estado y snapshot de creditos
│   └── Review.java                 # Valoracion 1-5 estrellas
├── dto/
│   ├── request/                    # RegisterRequest, LoginRequest, Create*
│   └── response/                   # ApiResponse<T>, *Response, AuthResponse
├── mapper/                         # MapStruct: generacion en compile-time
│   ├── UserMapper.java             # + UserSkillMapper
│   ├── OfferMapper.java            # Convierte tags CSV -> List<String>
│   ├── SessionMapper.java
│   ├── ReviewMapper.java
│   └── SkillMapper.java
├── security/
│   ├── jwt/JwtService.java         # HMAC-SHA512 signing/validation
│   └── filter/JwtAuthenticationFilter.java  # OncePerRequestFilter
├── exception/
│   ├── GlobalExceptionHandler.java # @RestControllerAdvice centralizado
│   ├── ResourceNotFoundException.java
│   ├── BusinessException.java
│   └── InsufficientCreditsException.java
└── websocket/
    ├── WebSocketController.java    # @MessageMapping handlers
    ├── NotificationService.java    # SimpMessagingTemplate wrapper
    └── NotificationEvent.java      # DTO de eventos push
```

---

## Decisiones de arquitectura

### 1. JWT Stateless
Sin sesiones HTTP → el servidor no guarda estado de sesion.
Permite escalar horizontalmente sin sticky sessions ni Redis.

### 2. MapStruct sobre ModelMapper
Generacion de codigo en compile-time via annotation processor.
Zero reflection en runtime = mayor performance y deteccion de errores en compilacion.

### 3. Transaccionalidad en creditos
Todo el flujo de creditos ocurre dentro de `@Transactional`.
Si algo falla (BD, OOM, etc.), el rollback es automatico y consistente.

### 4. WebSocket post-commit
Las notificaciones se envian despues de que la BD confirma el cambio.
Si el WebSocket falla, la operacion de negocio ya esta committed.

### 5. Snapshot en Session.creditsPaid
El precio se guarda en el momento de la reserva.
Cambios futuros en la oferta no afectan sesiones ya reservadas.

### 6. Colecciones LAZY + JOIN FETCH
Las relaciones `@OneToMany` son LAZY por defecto para evitar N+1.
Cuando se necesita el perfil completo, se usa `findByIdWithSkills()` con JOIN FETCH.

### 7. BaseEntity con JPA Auditing
`@EnableJpaAuditing` + `@EntityListeners(AuditingEntityListener)` en todas las entidades.
`createdAt` y `updatedAt` automaticos sin codigo repetido.

---

## Testing manual con curl

```bash
BASE=http://localhost:8081/api

# 1. Registro
curl -s -X POST $BASE/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"dev1","email":"dev1@test.com","password":"SecurePass1","fullName":"Dev One"}' | python3 -m json.tool

# 2. Login y guardar token
TOKEN=$(curl -s -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"dev1@test.com","password":"SecurePass1"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")

# 3. Ver skills disponibles
curl -s $BASE/skills | python3 -m json.tool

# 4. Crear oferta (usar un skillId del paso anterior)
curl -s -X POST $BASE/offers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"skillId":1,"title":"Aprende Python desde cero con proyectos","description":"Clases de Python para principiantes con proyectos reales y practicos","creditsCostPerHour":5,"durationMinutes":60,"modality":"ONLINE","tags":"python,backend"}' | python3 -m json.tool

# 5. Buscar ofertas
curl -s "$BASE/offers?q=python" | python3 -m json.tool
```

---

## Docker Compose

```yaml
# docker-compose.yml incluido en el proyecto
# Servicios disponibles:
#   - postgres: PostgreSQL 16 (siempre activo)
#   - pgadmin: Interfaz web de administracion (perfil "tools")

# Solo BD:
docker compose up -d postgres

# BD + pgAdmin (admin@skillswap.app / admin123):
docker compose --profile tools up -d

# Detener todo:
docker compose down

# Detener y borrar volumenes (reset completo):
docker compose down -v
```

---

## Monitoreo de logs

```bash
# Ver logs en tiempo real
tail -f skillswap/logs/app.log

# Solo errores
grep ERROR skillswap/logs/app.log

# Eventos de sesiones
grep "Sesion\|credito\|SESSION" skillswap/logs/app.log
```

---

## Notas de produccion

Antes de desplegar en produccion:

1. **Cambiar JWT_SECRET** — usar un secreto de 256+ bits generado aleatoriamente
2. **Cambiar ddl-auto** — de `update` a `validate` (usar Flyway/Liquibase para migraciones)
3. **Deshabilitar Swagger** en produccion o protegerlo con auth
4. **Configurar HTTPS** — obligatorio para JWT en produccion
5. **Configurar CORS** — especificar dominios exactos en lugar de wildcards
6. **Ajustar HikariCP** — pool size segun la carga esperada
7. **WebSocket broker** — reemplazar SimpleBroker por RabbitMQ/ActiveMQ para escalado horizontal

# Inventory Config Service

Microservicio en **Kotlin** con **Spring Boot** para administrar configuraciones internas de productos en inventario. Implementa seguridad con **AWS Cognito** y auditoría de cambios.

## 📋 Características

- ✅ Arquitectura por capas (Controller, Service, Repository)
- ✅ DTOs y Mappers para conversión de entidades
- ✅ Custom Exceptions con manejo centralizado
- ✅ Spring Security integrado con AWS Cognito (JWT)
- ✅ Auditoría de cambios (updatedBy desde JWT)
- ✅ Tres niveles de acceso: público, autenticado y admin
- ✅ PostgreSQL como base de datos
- ✅ Docker Compose para desarrollo local

## 🏗️ Estructura del Proyecto

```
src/main/kotlin/com/puce/inventory/
├── config/
│   └── SecurityConfig.kt          # Configuración de Spring Security
├── controller/
│   ├── PublicController.kt        # Endpoints públicos
│   └── InventoryRuleController.kt # Endpoints de reglas
├── dto/
│   ├── InventoryRuleRequest.kt    # DTO de entrada
│   ├── InventoryRuleResponse.kt   # DTO de salida
│   ├── HealthResponse.kt          # DTO de health check
│   └── ErrorResponse.kt           # DTO de errores
├── entity/
│   └── InventoryRule.kt           # Entidad JPA
├── exception/
│   ├── NotFoundException.kt       # 404 Not Found
│   ├── BadRequestException.kt     # 400 Bad Request
│   ├── UnauthorizedActionException.kt # 403 Forbidden
│   ├── UserIdNotFoundException.kt # User ID no encontrado en JWT
│   └── GlobalExceptionHandler.kt  # Handler centralizado
├── mapper/
│   └── InventoryRuleMapper.kt     # Conversión Entity ↔ DTO
├── repository/
│   └── InventoryRuleRepository.kt # Repositorio JPA
├── security/
│   ├── CognitoJwtAuthenticationConverter.kt # Converter de JWT
│   └── JwtUserExtractor.kt        # Extracción de userId del JWT
├── service/
│   └── InventoryRuleService.kt    # Lógica de negocio
└── InventoryApplication.kt        # Clase principal
```

## ⚙️ Variables de Entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `DATABASE_URL` | URL de conexión a PostgreSQL | `jdbc:postgresql://localhost:5432/inventory_db` |
| `DATABASE_USERNAME` | Usuario de base de datos | `postgres` |
| `DATABASE_PASSWORD` | Contraseña de base de datos | `postgres` |
| `COGNITO_ISSUER_URI` | URI del issuer de Cognito | `https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX` |
| `COGNITO_JWK_SET_URI` | URI del JWK Set de Cognito | `https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX/.well-known/jwks.json` |
| `USER_ID_CLAIM` | Claim del JWT para el ID de usuario | `sub` |
| `ROLES_CLAIM` | Claim del JWT para roles/grupos | `cognito:groups` |
| `ADMIN_ROLE` | Nombre del rol de administrador | `ADMIN` |
| `SERVER_PORT` | Puerto del servidor | `8080` |

## 🔐 Configuración de AWS Cognito

### 1. Crear User Pool en AWS Cognito

1. Ve a AWS Console → Cognito → User Pools
2. Crea un nuevo User Pool
3. Configura los atributos requeridos (email, etc.)
4. Crea un App Client (sin client secret para SPAs, con secret para apps server-side)

### 2. Crear grupo ADMIN

1. En tu User Pool, ve a "Groups"
2. Crea un grupo llamado `ADMIN`
3. Asigna usuarios que deben tener permisos de administrador a este grupo

### 3. Obtener URIs

Reemplaza `us-east-1_XXXXXXXXX` con tu Pool ID real:

```
COGNITO_ISSUER_URI=https://cognito-idp.{region}.amazonaws.com/{userPoolId}
COGNITO_JWK_SET_URI=https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json
```

### Claims utilizados

| Claim | Uso |
|-------|-----|
| `sub` | ID único del usuario (usado para `updatedBy` en auditoría) |
| `cognito:groups` | Lista de grupos del usuario (usado para verificar rol ADMIN) |

## 🚀 Cómo Ejecutar

### 1. Iniciar PostgreSQL con Docker

```bash
docker-compose up -d postgres
```

### 2. Configurar variables de entorno

Puedes usar un archivo `.env` o configurar las variables directamente:

**Windows PowerShell:**
```powershell
$env:COGNITO_ISSUER_URI="https://cognito-idp.us-east-1.amazonaws.com/us-east-1_TU_POOL_ID"
$env:COGNITO_JWK_SET_URI="https://cognito-idp.us-east-1.amazonaws.com/us-east-1_TU_POOL_ID/.well-known/jwks.json"
```

**Linux/Mac:**
```bash
export COGNITO_ISSUER_URI="https://cognito-idp.us-east-1.amazonaws.com/us-east-1_TU_POOL_ID"
export COGNITO_JWK_SET_URI="https://cognito-idp.us-east-1.amazonaws.com/us-east-1_TU_POOL_ID/.well-known/jwks.json"
```

### 3. Ejecutar la aplicación

```bash
./gradlew bootRun
```

O en Windows:
```powershell
.\gradlew.bat bootRun
```

La aplicación estará disponible en: `http://localhost:8080`

## 📝 Endpoints

### Público (sin autenticación)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/public/health` | Health check del servicio |

### Autenticado (cualquier usuario con JWT válido)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/rules` | Lista todas las reglas |
| GET | `/api/rules/{id}` | Obtiene una regla por ID |
| GET | `/api/rules/active` | Lista solo reglas activas |
| GET | `/api/rules/search?name=xxx` | Busca reglas por nombre |

### Solo Admin (requiere rol ADMIN)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/rules` | Crea una nueva regla |
| PUT | `/api/rules/{id}` | Actualiza una regla |
| PATCH | `/api/rules/{id}/toggle` | Activa/desactiva una regla |
| DELETE | `/api/rules/{id}` | Elimina una regla |

## 🧪 Probar los Endpoints

### Obtener token de Cognito

Para obtener un JWT válido desde Cognito, puedes usar:

1. **AWS CLI:**
```bash
aws cognito-idp initiate-auth \
  --client-id TU_CLIENT_ID \
  --auth-flow USER_PASSWORD_AUTH \
  --auth-parameters USERNAME=usuario@email.com,PASSWORD=contraseña
```

2. **Hosted UI de Cognito:** Configura la Hosted UI en tu App Client y usa el flujo OAuth2.

3. **Postman/Insomnia:** Configura OAuth2 con el Authorization URL y Token URL de Cognito.

### Ejemplos de requests

#### 1. Health Check (público)
```bash
curl http://localhost:8080/public/health
```

Respuesta:
```json
{
  "status": "OK",
  "service": "inventory-config-service",
  "timestamp": "2024-01-15T10:30:00"
}
```

#### 2. Listar reglas (autenticado)
```bash
curl -H "Authorization: Bearer TU_JWT_TOKEN" \
  http://localhost:8080/api/rules
```

#### 3. Crear regla (solo admin)
```bash
curl -X POST \
  -H "Authorization: Bearer TU_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Low Stock Alert", "description": "Alerta cuando stock bajo", "isActive": true}' \
  http://localhost:8080/api/rules
```

Respuesta:
```json
{
  "id": 1,
  "name": "Low Stock Alert",
  "description": "Alerta cuando stock bajo",
  "isActive": true,
  "updatedBy": "abc123-cognito-sub-id",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 4. Actualizar regla (solo admin)
```bash
curl -X PUT \
  -H "Authorization: Bearer TU_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Low Stock Alert Updated", "description": "Nueva descripción", "isActive": true}' \
  http://localhost:8080/api/rules/1
```

#### 5. Toggle estado (solo admin)
```bash
curl -X PATCH \
  -H "Authorization: Bearer TU_ADMIN_JWT_TOKEN" \
  http://localhost:8080/api/rules/1/toggle
```

### Archivo requests.http

También se incluye un archivo `requests.http` compatible con IntelliJ IDEA / VS Code REST Client con todos los ejemplos de requests.

## 📊 Auditoría

Todas las operaciones que modifican datos registran el ID del usuario de Cognito:

- **Campo `updatedBy`:** Se guarda el claim `sub` del JWT en cada operación CREATE, UPDATE, TOGGLE y DELETE.
- **Logs de auditoría:** Cada operación registra en logs quién realizó la acción.
- **DELETE:** Adicionalmente se registra la intención de eliminación en logs con nivel WARN.

Si el token no contiene el claim `sub`, la operación falla con un error `401 Unauthorized`.

## ⚠️ Errores Manejados

| Código | Excepción | Descripción |
|--------|-----------|-------------|
| 400 | `BadRequestException` | Datos de entrada inválidos |
| 401 | `UserIdNotFoundException` | Token sin userId |
| 403 | `AccessDeniedException` | Sin permisos suficientes |
| 404 | `NotFoundException` | Recurso no encontrado |
| 500 | `Exception` | Error interno del servidor |

## 🐳 Docker

### Solo base de datos
```bash
docker-compose up -d postgres
```

### Ver logs
```bash
docker-compose logs -f postgres
```

### Detener
```bash
docker-compose down
```

### Eliminar volúmenes
```bash
docker-compose down -v
```

## 📦 Tecnologías

- Kotlin 2.2
- Spring Boot 4.0
- Spring Security + OAuth2 Resource Server
- Spring Data JPA
- PostgreSQL 16
- Docker & Docker Compose
- Gradle Kotlin DSL

## 📄 Licencia

MIT License


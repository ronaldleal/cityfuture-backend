# CityFuture Backend

Sistema de gestión de construcciones para la ciudadela del futuro. Permite gestionar órdenes de construcción,
materiales, y programar construcciones secuenciales con validaciones automáticas.

## 🚀 Características

- ✅ **Gestión de Órdenes de Construcción**: CRUD completo con validaciones
- ✅ **Gestión de Materiales**: Control de inventario y stock
- ✅ **Programación Secuencial**: Las construcciones se programan una después de otra
- ✅ **Scheduler Automático**: Cambio automático de estados (Pendiente → En Progreso → Finalizado)
- ✅ **Validaciones**: Ubicaciones únicas, materiales suficientes, tipos de construcción válidos
- ✅ **Reportes**: Estadísticas completas del proyecto y construcciones
- ✅ **Logging**: Trazabilidad completa de operaciones
- ✅ **Autenticación**: Sistema de roles (ARQUITECTO, usuarios autenticados)

## 🛠️ Tecnologías

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Security** (JWT)
- **Spring Data JPA**
- **PostgreSQL**
- **MapStruct** (mapeo automático)
- **Lombok** (reducción de boilerplate)
- **SLF4J** (logging)

## 📋 Prerrequisitos

- Java 17 o superior
- Maven 3.6+
- PostgreSQL 12+
- Git

## 🗄️ Configuración de Base de Datos

### 1. Instalar PostgreSQL

**Windows:**

```bash
# Descargar desde https://www.postgresql.org/download/windows/
# O usar Chocolatey
choco install postgresql
```

**macOS:**

```bash
brew install postgresql
brew services start postgresql
```

**Linux (Ubuntu/Debian):**

```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

### 2. Crear Base de Datos

```sql
-- Conectarse como superusuario
psql
-U postgres

-- Crear usuario
CREATE
USER cityfuture_user WITH PASSWORD 'cityfuture_password';

-- Crear base de datos
CREATE
DATABASE cityfuture_db OWNER cityfuture_user;

-- Otorgar permisos
GRANT ALL PRIVILEGES ON DATABASE
cityfuture_db TO cityfuture_user;
```

### 3. Configurar application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cityfuture_db
    username: cityfuture_user
    password: cityfuture_password
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: true

  security:
    jwt:
      secret: your-super-secret-jwt-key-here
      expiration: 86400000 # 24 horas

logging:
  level:
    com.cityfuture: DEBUG
    org.springframework.security: DEBUG
```

## 🚀 Instalación y Ejecución

### 1. Clonar el Repositorio

```bash
git clone <repository-url>
cd cityfuture-backend
```

### 2. Compilar el Proyecto

```bash
mvn clean install
```

### 3. Ejecutar la Aplicación

```bash
mvn spring-boot:run
```

O ejecutar el JAR:

```bash
java -jar target/cityfuture-backend-1.0.0.jar
```

La aplicación estará disponible en: `http://localhost:8084`

## 📡 Endpoints API

### 🔐 Autenticación

| Método | Endpoint             | Descripción       | Rol Requerido |
|--------|----------------------|-------------------|---------------|
| POST   | `/api/auth/login`    | Iniciar sesión    | Público       |
| POST   | `/api/auth/register` | Registrar usuario | Público       |

**Ejemplo Login:**

```bash
curl -X POST http://localhost:8084/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "arquitecto1",
    "password": "password123"
  }'
```

### 🏗️ Construcciones

| Método | Endpoint                      | Descripción                 | Rol Requerido |
|--------|-------------------------------|-----------------------------|---------------|
| POST   | `/api/constructions`          | Crear orden de construcción | ARQUITECTO    |
| GET    | `/api/constructions`          | Listar todas las órdenes    | Autenticado   |
| GET    | `/api/constructions/{id}`     | Obtener orden por ID        | Autenticado   |
| PUT    | `/api/constructions/{id}`     | Actualizar orden            | ARQUITECTO    |
| DELETE | `/api/constructions/{id}`     | Eliminar orden              | ARQUITECTO    |
| POST   | `/api/constructions/validate` | Validar antes de crear      | ARQUITECTO    |

**Crear Orden de Construcción:**

```bash
curl -X POST http://localhost:8084/api/constructions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "projectName": "Casa del Futuro",
    "location": {
      "latitude": 6.2442,
      "longitude": -75.5812
    },
    "typeConstruction": "Casa"
  }'
```

**Tipos de Construcción Disponibles:**

- `Casa` (5 días)
- `Edificio` (15 días)
- `Piscina` (3 días)
- `Lago` (4 días)

### 🧱 Materiales

| Método | Endpoint              | Descripción             | Rol Requerido |
|--------|-----------------------|-------------------------|---------------|
| POST   | `/api/materials`      | Crear material          | ARQUITECTO    |
| GET    | `/api/materials`      | Listar materiales       | Público       |
| GET    | `/api/materials/{id}` | Obtener material por ID | Público       |
| PUT    | `/api/materials/{id}` | Actualizar material     | ARQUITECTO    |
| DELETE | `/api/materials/{id}` | Eliminar material       | ARQUITECTO    |

**Crear Material:**

```bash
curl -X POST http://localhost:8084/api/materials \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "materialName": "Cemento",
    "description": "Cemento de alta calidad",
    "code": "CEM001"
  }'
```

### 📊 Reportes

| Método | Endpoint                        | Descripción                        | Rol Requerido |
|--------|---------------------------------|------------------------------------|---------------|
| GET    | `/api/reports/constructions`    | Reporte completo de construcciones | Autenticado   |
| GET    | `/api/reports/project-summary`  | Resumen del proyecto               | Autenticado   |
| GET    | `/api/reports/project-end-date` | Fecha de finalización del proyecto | Autenticado   |

**Obtener Reporte de Construcciones:**

```bash
curl -X GET http://localhost:8084/api/reports/constructions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Respuesta del Reporte:**

```json
{
  "reportDate": "2024-12-18",
  "totalOrders": 5,
  "pendingOrders": 2,
  "inProgressOrders": 1,
  "finishedOrders": 2,
  "pendingByType": {
    "Casa": 1,
    "Edificio": 1
  },
  "inProgressByType": {
    "Lago": 1
  },
  "finishedByType": {
    "Casa": 1,
    "Piscina": 1
  },
  "projectSummary": {
    "totalConstructionDays": 25,
    "projectStartDate": "2024-12-01",
    "projectEndDate": "2024-12-30",
    "estimatedDeliveryDate": "2024-12-30",
    "totalOrders": 5,
    "status": "En progreso"
  }
}
```

### 🧪 Testing/Debug

| Método | Endpoint                              | Descripción                    |
|--------|---------------------------------------|--------------------------------|
| POST   | `/api/constructions/test-scheduler`   | Ejecutar scheduler manualmente |
| POST   | `/api/constructions/process-overdue`  | Procesar órdenes atrasadas     |
| GET    | `/api/constructions/debug-order/{id}` | Debug información de orden     |
| POST   | `/api/test/scheduler/execute-today`   | Ejecutar scheduler para hoy    |
| GET    | `/api/test/scheduler/simulate/{date}` | Simular scheduler para fecha   |

## 🤖 Scheduler Automático

El sistema incluye un scheduler que se ejecuta automáticamente:

- **8:00 AM**: Cambia órdenes de "Pendiente" a "En progreso"
- **11:00 PM**: Cambia órdenes de "En progreso" a "Finalizado"

### Lógica de Fechas

1. **Primera construcción**: Inicia al día siguiente de la solicitud
2. **Construcciones siguientes**: Inician al día siguiente de terminar la anterior
3. **Ejemplo**:
    - Casa solicitada 01/01/2024 (5 días) → Inicia 02/01/2024, termina 06/01/2024
    - Edificio solicitado 02/01/2024 (15 días) → Inicia 07/01/2024, termina 21/01/2024

## 🔧 Configuración Adicional

### Variables de Entorno

```bash
# Base de datos
DB_HOST=localhost
DB_PORT=5432
DB_NAME=cityfuture_db
DB_USER=cityfuture_user
DB_PASSWORD=cityfuture_password

# JWT
JWT_SECRET=your-super-secret-jwt-key-here
JWT_EXPIRATION=86400000

# Logging
LOG_LEVEL=INFO
```

## 📝 Validaciones del Sistema

### Construcciones

- ✅ Solo un tipo de construcción por ubicación (coordenadas únicas)
- ✅ Materiales suficientes antes de crear la orden
- ✅ Tipos de construcción válidos
- ✅ Programación secuencial automática

### Materiales

- ✅ Nombres únicos de materiales
- ✅ Códigos únicos
- ✅ Stock mínimo controlado

## 🐛 Troubleshooting

### Error de Conexión a Base de Datos

```bash
# Verificar que PostgreSQL esté ejecutándose
sudo systemctl status postgresql

# Verificar conexión
psql -h localhost -U cityfuture_user -d cityfuture_db
```

### Error de Puerto Ocupado

```bash
# Cambiar puerto en application.yml
server:
  port: 8085
```

### Error de JWT

```bash
# Verificar que JWT_SECRET esté configurado
# Debe tener al menos 256 bits (32 caracteres)
```

## 📊 Logs

Los logs se encuentran en:

- Consola: Nivel INFO
- Archivo: `logs/cityfuture.log`

**Habilitar logs de DEBUG:**

```yaml
logging:
  level:
    com.cityfuture: DEBUG
```

## 🤝 Contribución

1. Fork el proyecto
2. Crear feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push al branch (`git push origin feature/AmazingFeature`)
5. Crear Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE.md](LICENSE.md) para detalles.
# 📚 Documentación API con Swagger

## 🌐 **Acceso a Swagger UI**

Una vez que el servidor esté ejecutándose, puedes acceder a la documentación interactiva de la API en:

- **Swagger UI**: http://localhost:8084/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8084/api-docs
- **OpenAPI YAML**: http://localhost:8084/api-docs.yaml

## 🔐 **Autenticación en Swagger**

### Paso 1: Obtener Token JWT
1. Ve al endpoint `POST /api/auth/login` en Swagger UI
2. Usa las credenciales de prueba:
   ```json
   {
     "username": "arquitecto",
     "password": "password"
   }
   ```
3. Copia el token JWT de la respuesta

### Paso 2: Configurar Autorización
1. Haz clic en el botón **"Authorize"** (🔒) en la parte superior de Swagger UI
2. En el campo "Value", ingresa: `Bearer [tu-token-jwt]`
   - Ejemplo: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
3. Haz clic en **"Authorize"**
4. Ahora puedes probar endpoints protegidos

## 📋 **Endpoints Disponibles**

### 🏗️ **Construcciones** (`/api/constructions`)
- `POST` - Crear nueva orden de construcción
- `GET` - Obtener todas las órdenes (con filtro opcional por estado)
- `GET /{id}` - Obtener orden específica por ID
- `PUT /{id}` - Actualizar orden existente
- `DELETE /{id}` - Eliminar orden
- `POST /validate` - Validar orden antes de crear

### 🧱 **Materiales** (`/api/materials`)
- `POST` - Crear nuevo material
- `GET` - Obtener todos los materiales
- `GET /{id}` - Obtener material específico por ID
- `PUT /{id}` - Actualizar material existente
- `DELETE /{id}` - Eliminar material

### 📊 **Reportes** (`/api/reports`)
- `GET /constructions` - Reporte completo de construcciones
- `GET /project-summary` - Resumen del proyecto
- `GET /project-end-date` - Fecha estimada de finalización

### 🔐 **Autenticación** (`/api/auth`)
- `POST /login` - Iniciar sesión y obtener token JWT
- `POST /logout` - Cerrar sesión
- `GET /validate` - Validar token actual

## 🎯 **Ejemplos de Uso**

### Crear Orden de Construcción
```json
{
  "projectName": "Casa Modelo A",
  "location": {
    "latitude": 4.60971,
    "longitude": -74.08175
  },
  "typeConstruction": "Casa",
  "estado": "Pendiente",
  "estimatedDays": 30,
  "entregaDate": "2025-12-31"
}
```

### Crear Material
```json
{
  "materialName": "Cemento Portland",
  "code": "CEM-001",
  "quantity": 1000
}
```

## 🛠️ **Configuración**

### Personalización de Swagger
La configuración se encuentra en `OpenApiConfig.java`:
- Información del API (título, descripción, versión)
- Configuración de servidores
- Esquema de seguridad JWT
- Contacto y licencia

### Propiedades de SpringDoc
En `application.properties`:
```properties
# Rutas de Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html

# Configuración de UI
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.try-it-out-enabled=true
springdoc.swagger-ui.filter=true

# Agrupación de APIs
springdoc.group-configs[0].group=cityfuture-api
springdoc.group-configs[0].paths-to-match=/api/**
```

## 🔍 **Características Implementadas**

✅ **Documentación Automática**: Generada desde anotaciones del código
✅ **Interfaz Interactiva**: Probar endpoints directamente desde el navegador
✅ **Autenticación JWT**: Soporte completo para tokens Bearer
✅ **Validaciones**: Documentación de todas las validaciones de entrada
✅ **Ejemplos**: Ejemplos de requests y responses
✅ **Agrupación**: Endpoints organizados por funcionalidad
✅ **CORS**: Configurado para desarrollo y pruebas
✅ **Esquemas**: Definición completa de modelos de datos

## 🚀 **Cómo Usar**

1. **Inicia el servidor**: `./gradlew bootRun`
2. **Abre Swagger UI**: http://localhost:8084/swagger-ui.html
3. **Auténticate**: Usa el endpoint `/api/auth/login`
4. **Explora**: Navega por las diferentes secciones de la API
5. **Prueba**: Ejecuta requests directamente desde la interfaz

## 📝 **Notas**

- Los endpoints marcados con 🔒 requieren autenticación JWT
- Las validaciones están documentadas en cada campo
- Los códigos de estado HTTP están claramente definidos
- Los ejemplos son funcionales y se pueden copiar directamente
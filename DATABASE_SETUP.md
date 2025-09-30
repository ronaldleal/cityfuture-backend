# 🐘 Configuración de PostgreSQL para CityFuture Backend

## Prerrequisitos

1. **Instalar PostgreSQL**
   - Descargar desde: https://www.postgresql.org/download/
   - Durante la instalación, recordar el password del usuario `ronald`

2. **Verificar instalación**
   ```bash
   psql --version
   ```

## Configuración de Base de Datos

### Opción 1: Usando pgAdmin (Interfaz Gráfica)
1. Abrir pgAdmin
2. Conectarse al servidor PostgreSQL
3. Crear nueva base de datos: `cityfuture_db`
4. Ejecutar el script `database/create_database.sql`

### Opción 2: Usando línea de comandos
```bash
# Conectarse como superusuario
psql -U postgres -h localhost

# Ejecutar el script
\i C:/Sura/cityfuture-backend/database/create_database.sql

# Salir
\q
```

## Configuración de Variables de Entorno

### Windows (PowerShell)
```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="cityfuturedb"
$env:DB_USERNAME="ronald"
$env:DB_PASSWORD="18566621"
$env:JWT_SECRET="tu_jwt_secret_seguro"
```

### Windows (CMD)
```cmd
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=cityfuturedb
set DB_USERNAME=ronald
set DB_PASSWORD=18566621
set JWT_SECRET=MiClaveSecretaSuperSeguraParaJWT2024!#$
```

### Linux/Mac
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=cityfuturedb
export DB_USERNAME=ronald
export DB_PASSWORD=18566621
export JWT_SECRET=MiClaveSecretaSuperSeguraParaJWT2024!#$
```

## Iniciar la aplicación

```bash
cd C:\Sura\cityfuture-backend
.\gradlew bootRun
```

## Verificación

1. **Aplicación corriendo**: http://localhost:8084
2. **Swagger UI**: http://localhost:8084/swagger-ui/index.html
3. **API Docs**: http://localhost:8084/api-docs

## Notas importantes

- ⚠️ Cambiar las contraseñas por defecto en producción
- 🔐 Usar variables de entorno para credenciales sensibles
- 📊 Las tablas se crearán automáticamente con `ddl-auto=update`
- 🔄 Los datos de usuarios de prueba se cargarán al iniciar la aplicación
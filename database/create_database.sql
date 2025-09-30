-- Script para crear la base de datos PostgreSQL
-- Ejecutar como superusuario (postgres)

-- 1. Crear la base de datos
CREATE DATABASE cityfuture_db;

-- 2. Crear un usuario específico para la aplicación (opcional pero recomendado)
CREATE USER cityfuture_user WITH PASSWORD 'secure_password_here';

-- 3. Otorgar permisos al usuario
GRANT ALL PRIVILEGES ON DATABASE cityfuture_db TO cityfuture_user;

-- 4. Conectarse a la base de datos
\c cityfuture_db;

-- 5. Otorgar permisos en el esquema público
GRANT ALL ON SCHEMA public TO cityfuture_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO cityfuture_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO cityfuture_user;

-- Configurar permisos por defecto para objetos futuros
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO cityfuture_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO cityfuture_user;
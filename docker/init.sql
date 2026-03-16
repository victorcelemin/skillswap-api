-- SkillSwap — Script de inicialización de PostgreSQL
-- Se ejecuta automáticamente al crear el contenedor por primera vez

-- Extensión para UUID (útil en futuras migraciones)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Extensión para búsqueda de texto (futura mejora de search)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- El schema public ya existe por defecto en PostgreSQL
-- Spring Boot con ddl-auto=update crea las tablas automáticamente

-- Verificar que la BD fue creada correctamente
SELECT 'SkillSwap DB inicializada correctamente' AS status;

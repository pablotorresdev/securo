-- Migración V2: Agregar jerarquía de usuarios y tracking de creador en movimientos
-- Fecha: 2025-01-XX
-- Descripción:
--   1. Agrega columna 'nivel' a tabla roles para jerarquía
--   2. Agrega columna 'fecha_expiracion' a tabla users para auditores temporales
--   3. Agrega columna 'creado_por_user_id' a tabla movimientos para tracking
--   4. Crea tabla auditoria_accesos para registro de accesos de auditores
--   5. Actualiza roles existentes con niveles correctos

-- ============================================
-- 1. AGREGAR COLUMNA NIVEL A ROLES
-- ============================================
ALTER TABLE roles ADD COLUMN nivel INTEGER;

-- Actualizar roles existentes (si existen) con niveles temporales
UPDATE roles SET nivel = 6 WHERE name = 'ADMIN';
UPDATE roles SET nivel = 1 WHERE name IN ('USER1', 'USER2');

-- Hacer columna nivel NOT NULL después de setear valores
ALTER TABLE roles ALTER COLUMN nivel SET NOT NULL;

-- ============================================
-- 2. AGREGAR FECHA EXPIRACION A USERS
-- ============================================
ALTER TABLE users ADD COLUMN fecha_expiracion DATE;

-- Comentario explicativo
COMMENT ON COLUMN users.fecha_expiracion IS 'Fecha de expiración para usuarios temporales (ej: auditores). NULL = sin expiración';

-- ============================================
-- 3. AGREGAR CREADO POR A MOVIMIENTOS
-- ============================================
ALTER TABLE movimientos ADD COLUMN creado_por_user_id BIGINT;

-- Agregar foreign key constraint
ALTER TABLE movimientos
ADD CONSTRAINT fk_movimiento_creado_por
FOREIGN KEY (creado_por_user_id)
REFERENCES users(id)
ON DELETE SET NULL;

-- Crear índice para mejorar performance en queries de autorización
CREATE INDEX idx_movimiento_creado_por ON movimientos(creado_por_user_id);

-- Comentario explicativo
COMMENT ON COLUMN movimientos.creado_por_user_id IS 'Usuario que creó el movimiento. NULL para movimientos legacy (anteriores a esta migración)';

-- ============================================
-- 4. CREAR TABLA AUDITORIA_ACCESOS
-- ============================================
CREATE TABLE auditoria_accesos (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    accion VARCHAR(255) NOT NULL,
    url VARCHAR(500),
    metodo_http VARCHAR(10),
    ip_address VARCHAR(45),
    user_agent TEXT,
    fecha_hora TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_auditoria_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE
);

-- Índices para queries comunes
CREATE INDEX idx_auditoria_user ON auditoria_accesos(user_id);
CREATE INDEX idx_auditoria_fecha ON auditoria_accesos(fecha_hora DESC);
CREATE INDEX idx_auditoria_username ON auditoria_accesos(username);
CREATE INDEX idx_auditoria_role ON auditoria_accesos(role_name);

-- Comentario explicativo
COMMENT ON TABLE auditoria_accesos IS 'Registro de accesos al sistema, especialmente para auditores externos';

-- ============================================
-- 5. INSERTAR/ACTUALIZAR ROLES DEFINITIVOS
-- ============================================
-- Nota: Los roles se insertarán/actualizarán desde CustomUserDetailsService
-- Este script solo prepara la estructura

-- Datos de ejemplo para desarrollo (comentar en producción)
-- INSERT INTO roles (name, nivel) VALUES ('ADMIN', 6) ON CONFLICT (name) DO UPDATE SET nivel = 6;
-- INSERT INTO roles (name, nivel) VALUES ('DT', 5) ON CONFLICT (name) DO UPDATE SET nivel = 5;
-- INSERT INTO roles (name, nivel) VALUES ('GERENTE_GARANTIA_CALIDAD', 4) ON CONFLICT (name) DO UPDATE SET nivel = 4;
-- INSERT INTO roles (name, nivel) VALUES ('GERENTE_CONTROL_CALIDAD', 3) ON CONFLICT (name) DO UPDATE SET nivel = 3;
-- INSERT INTO roles (name, nivel) VALUES ('SUPERVISOR_PLANTA', 3) ON CONFLICT (name) DO UPDATE SET nivel = 3;
-- INSERT INTO roles (name, nivel) VALUES ('ANALISTA_CONTROL_CALIDAD', 2) ON CONFLICT (name) DO UPDATE SET nivel = 2;
-- INSERT INTO roles (name, nivel) VALUES ('ANALISTA_PLANTA', 2) ON CONFLICT (name) DO UPDATE SET nivel = 2;
-- INSERT INTO roles (name, nivel) VALUES ('AUDITOR', 1) ON CONFLICT (name) DO UPDATE SET nivel = 1;

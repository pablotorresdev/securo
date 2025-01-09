-- Asegurarse de que la extensión pgcrypto esté habilitada (para generación UUID si se requiere)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Tabla roles
CREATE TABLE roles
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Tabla users
CREATE TABLE users
(
    id BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50) NOT NULL UNIQUE, -- Refleja la restricción de tamaño en @Size
    password   VARCHAR(255) NOT NULL,       -- Longitud máxima para passwords
    role_id    BIGINT NOT NULL,             -- Clave foránea al rol
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Trigger para actualizar el campo updated_at automáticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_updated_at
    BEFORE UPDATE
    ON users
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

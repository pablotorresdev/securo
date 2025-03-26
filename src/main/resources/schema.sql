DROP TABLE IF EXISTS analisis CASCADE;
DROP TABLE IF EXISTS movimientos CASCADE;

-- 2. Luego las tablas que dependen de otras
DROP TABLE IF EXISTS lotes CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 3. Eliminar las tablas restantes
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS productos CASCADE;
DROP TABLE IF EXISTS contactos CASCADE;
DROP TABLE IF EXISTS configuracion CASCADE;

DROP EXTENSION IF EXISTS "pgcrypto" CASCADE;


-- Asegurarse de que la extensión pgcrypto esté habilitada (para generación UUID si se requiere)
CREATE
    EXTENSION IF NOT EXISTS "pgcrypto";

-- Tabla roles
CREATE TABLE IF NOT EXISTS roles
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

-- Tabla users
CREATE TABLE IF NOT EXISTS users
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50)                         NOT NULL UNIQUE, -- Refleja la restricción de tamaño en @Size
    password   VARCHAR(255)                        NOT NULL,        -- Longitud máxima para passwords
    role_id    BIGINT                              NOT NULL,        -- Clave foránea al rol
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

--TABLAS DATOS MAESTROS--
CREATE TABLE productos
(
    id                  SERIAL PRIMARY KEY,
    nombre_generico     VARCHAR(255) NOT NULL,
    codigo_interno      VARCHAR(50)  NOT NULL UNIQUE, -- Ejemplo: '9-120'
    tipo_producto       VARCHAR(50)  NOT NULL,
    pais_origen         VARCHAR(100) NOT NULL,
    unidad_medida       VARCHAR(50)  NOT NULL,
    producto_destino_id INT,
    observaciones       TEXT,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_producto
        FOREIGN KEY (producto_destino_id) REFERENCES productos (id)
);

CREATE TABLE contactos
(
    id               SERIAL PRIMARY KEY,
    razon_social     VARCHAR(255) NOT NULL,
    cuit             VARCHAR(255) NOT NULL,
    direccion        VARCHAR(255) NOT NULL,
    ciudad           VARCHAR(100) NOT NULL,
    pais             VARCHAR(100) NOT NULL,
    telefono         VARCHAR(50),
    email            VARCHAR(100),
    persona_contacto VARCHAR(100),
    observaciones    TEXT,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE configuracion
(
    id     SERIAL PRIMARY KEY,
    clave  VARCHAR(255) NOT NULL,
    valor  VARCHAR(255) NOT NULL,
    activo BOOLEAN      NOT NULL DEFAULT TRUE
);

--TABLAS DATOS OPERATIVOS--
CREATE TABLE lotes
(
    id                   SERIAL PRIMARY KEY,
    id_lote              VARCHAR(50)    NOT NULL,
    producto_id          INT            NOT NULL,
    proveedor_id         INT            NOT NULL,
    estado_lote          TEXT           NOT NULL,
    dictamen             TEXT           NOT NULL,
    lote_origen_id       INT,
    fecha_ingreso        DATE           NOT NULL,
    nro_bulto            INT            NOT NULL,
    bultos_totales       INT            NOT NULL,
    cantidad_inicial     NUMERIC(12, 2) NOT NULL,
    cantidad_actual      NUMERIC(12, 2) NOT NULL,
    unidad_medida        VARCHAR(50)    NOT NULL,
    lote_proveedor       TEXT           NOT NULL,
    nro_remito           TEXT,
    fabricante_id        INT            NOT NULL,
    detalle_conservacion TEXT,
    fecha_vencimiento    DATE,
    fecha_reanalisis     DATE,
    nro_analisis_id      INT,
    titulo               NUMERIC(12, 2),
    observaciones        TEXT,
    activo               BOOLEAN        NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_producto
        FOREIGN KEY (producto_id) REFERENCES productos (id),
    CONSTRAINT fk_lote_origen
        FOREIGN KEY (lote_origen_id) REFERENCES lotes (id),
    CONSTRAINT fk_proveedor_lote
        FOREIGN KEY (proveedor_id) REFERENCES contactos (id),
    CONSTRAINT fk_fabricante_lote
        FOREIGN KEY (fabricante_id) REFERENCES contactos (id)
-- ,
--     CONSTRAINT fk_nro_analisis
--         FOREIGN KEY (nro_analisis_id) REFERENCES analisis (id)
);

CREATE TABLE movimientos
(
    id                   SERIAL PRIMARY KEY,
    fecha                DATE           NOT NULL,
    tipo_movimiento      TEXT           NOT NULL,
    motivo               TEXT           NOT NULL,
    lote_id              INT            NOT NULL,
    descripcion          TEXT,
    cantidad             NUMERIC(12, 2) NOT NULL,
    unidad_medida        TEXT           NOT NULL,
    nro_analisis         VARCHAR(50), -- Post QA
    orden_produccion     VARCHAR(50), -- Para movimientos de consumo prod
    dictamen_inicial     TEXT,
    dictamen_final       TEXT           NOT NULL,
    movimiento_origen_id INT,
    activo               BOOLEAN        NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_lote_id
        FOREIGN KEY (lote_id) REFERENCES lotes (id),
    CONSTRAINT fk_movimiento_origen
        FOREIGN KEY (movimiento_origen_id) REFERENCES movimientos (id)
);

CREATE TABLE analisis
(
    id             SERIAL PRIMARY KEY,
    lote_id        INT         NOT NULL,
    fecha_analisis DATE        NOT NULL,
    nro_analisis   VARCHAR(50) NOT NULL,
    dictamen       TEXT        NOT NULL,
    observaciones  TEXT,
    activo         BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_lote_analisis
        FOREIGN KEY (lote_id) REFERENCES lotes (id)
);

ALTER TABLE lotes
    ADD CONSTRAINT fk_nro_analisis
        FOREIGN KEY (nro_analisis_id) REFERENCES analisis (id);



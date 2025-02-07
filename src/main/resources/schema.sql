DROP TABLE IF EXISTS movimientos CASCADE;
DROP TABLE IF EXISTS lote_destino CASCADE;

-- 2. Luego las tablas que dependen de otras
DROP TABLE IF EXISTS lotes CASCADE;
DROP TABLE IF EXISTS especificacion_productos CASCADE;
DROP TABLE IF EXISTS productos CASCADE;
DROP TABLE IF EXISTS users CASCADE;  -- Depende de roles

-- 3. Eliminar las tablas restantes
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS tipo_producto CASCADE;
DROP TABLE IF EXISTS clase CASCADE;
DROP TABLE IF EXISTS terceros CASCADE;
DROP TABLE IF EXISTS dictamen CASCADE;
DROP TABLE IF EXISTS motivo CASCADE;
DROP TABLE IF EXISTS estado CASCADE;
DROP TABLE IF EXISTS tipo_movimiento CASCADE;
DROP TABLE IF EXISTS unidad_medida CASCADE;

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

CREATE TABLE IF NOT EXISTS dictamen
(
    id     SERIAL PRIMARY KEY,
    estado VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS motivo
(
    id          SERIAL PRIMARY KEY,
    descripcion VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS estado
(
    id          SERIAL PRIMARY KEY,
    descripcion VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS tipo_movimiento
(
    id   SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL UNIQUE
);


CREATE TABLE IF NOT EXISTS unidad_medida
(
    id                SERIAL PRIMARY KEY,
    nombre              VARCHAR(255)     NOT NULL UNIQUE,
    tipo              VARCHAR(50)      NOT NULL,
    simbolo            VARCHAR(3)       NOT NULL,
    factor_conversion DOUBLE PRECISION NOT NULL
);


CREATE TABLE terceros
(
    id        SERIAL PRIMARY KEY,
    direccion VARCHAR(255) NOT NULL,
    ciudad    VARCHAR(100) NOT NULL,
    pais      VARCHAR(100) NOT NULL,
    telefono  VARCHAR(50),
    fax       VARCHAR(50),
    email     VARCHAR(100),
    contacto  VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS clase
(
    id   SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE tipo_producto
(
    id     SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE productos
(
    id               SERIAL PRIMARY KEY,
    nombre_generico  VARCHAR(255) NOT NULL,
    codigo_interno   VARCHAR(50)  NOT NULL UNIQUE, -- Ejemplo: '9-120'
    tipo_producto_id INT          NOT NULL,
    descripcion      TEXT,
    coa              TEXT,
    clase_id         INT          NOT NULL,
    CONSTRAINT fk_tipo_producto
        FOREIGN KEY (tipo_producto_id) REFERENCES tipo_producto (id),
    CONSTRAINT fk_clase_producto
        FOREIGN KEY (clase_id) REFERENCES clase (id)
);

CREATE TABLE especificacion_productos
(
    id               SERIAL PRIMARY KEY,
    producto_id      INT            NOT NULL,
    codigo_version   VARCHAR(50)    NOT NULL, -- Ejemplo: '(9-120)/01'
    unidad_medida_id INT            NOT NULL,
    cantidad         NUMERIC(12, 2) NOT NULL,
    detalle          TEXT,
    CONSTRAINT fk_producto_especificacion
        FOREIGN KEY (producto_id) REFERENCES productos (id),
    CONSTRAINT fk_unidad_medida_especificacion
        FOREIGN KEY (unidad_medida_id) REFERENCES unidad_medida (id),
    CONSTRAINT uq_producto_version UNIQUE (producto_id, codigo_version)
);

CREATE TABLE lotes
(
    id                         SERIAL PRIMARY KEY,
    especificacion_producto_id INT            NOT NULL,
    cantidad                   NUMERIC(12, 2) NOT NULL,
    id_lote                    VARCHAR(50)    NOT NULL UNIQUE, -- Identificador del lote
    fecha_elaboracion          DATE           NOT NULL,
    fecha_caducidad            DATE           NOT NULL,
    unidad_medida_id           INT            NOT NULL,
    bultos_totales             INT            NOT NULL,
    nro_bulto                  INT            NOT NULL,
    proveedor_id               INT            NOT NULL,
    fabricante_id              INT            NOT NULL,
    conservacion               TEXT,
    pureza                     NUMERIC(5, 2),                  -- Porcentaje o valor numérico
    estado                     VARCHAR(10)    NOT NULL,        -- 'activo' o 'inactivo'
    observaciones              TEXT,
    id_analisis_qa             VARCHAR(50),
    fecha_reanalisis           DATE,
    dictamen                   TEXT,
    clase_id                   INT            NOT NULL,
    CONSTRAINT fk_especificacion_producto_lote
        FOREIGN KEY (especificacion_producto_id) REFERENCES especificacion_productos (id),
    CONSTRAINT fk_unidad_medida_lote
        FOREIGN KEY (unidad_medida_id) REFERENCES unidad_medida (id),
    CONSTRAINT fk_proveedor_lote
        FOREIGN KEY (proveedor_id) REFERENCES terceros (id),
    CONSTRAINT fk_fabricante_lote
        FOREIGN KEY (fabricante_id) REFERENCES terceros (id),
    CONSTRAINT fk_clase_lote
        FOREIGN KEY (clase_id) REFERENCES clase (id),
    CONSTRAINT chk_estado CHECK (estado IN ('activo', 'inactivo'))
);

CREATE TABLE lote_destino
(
    lote_id                    INT NOT NULL,
    especificacion_producto_id INT NOT NULL,
    PRIMARY KEY (lote_id, especificacion_producto_id),
    CONSTRAINT fk_lote_destino_lote
        FOREIGN KEY (lote_id) REFERENCES lotes (id),
    CONSTRAINT fk_lote_destino_especificacion
        FOREIGN KEY (especificacion_producto_id) REFERENCES especificacion_productos (id)
);

CREATE TABLE movimientos
(
    id               SERIAL PRIMARY KEY,
    motivo           TEXT           NOT NULL,
    orden_produccion VARCHAR(50),
    id_analisis_qa   VARCHAR(50),
    nro_analisis     VARCHAR(50),
    ref_lote_origen  INT            NOT NULL,
    ref_lote_destino INT            NOT NULL,
    cantidad         NUMERIC(12, 2) NOT NULL,
    unidad_medida_id INT            NOT NULL,
    tipo             VARCHAR(20)    NOT NULL, -- 'Ingreso', 'Egreso' o 'Transformacion'
    CONSTRAINT fk_lote_origen
        FOREIGN KEY (ref_lote_origen) REFERENCES lotes (id),
    CONSTRAINT fk_lote_destino
        FOREIGN KEY (ref_lote_destino) REFERENCES lotes (id),
    CONSTRAINT fk_unidad_medida_mov
        FOREIGN KEY (unidad_medida_id) REFERENCES unidad_medida (id),
    CONSTRAINT chk_tipo_movimiento CHECK (tipo IN ('Ingreso', 'Egreso', 'Transformacion'))
);

DROP TABLE IF EXISTS trazas_movimientos CASCADE;
DROP TABLE IF EXISTS trazas CASCADE;
DROP TABLE IF EXISTS lote_analisis CASCADE;
DROP TABLE IF EXISTS analisis CASCADE;
DROP TABLE IF EXISTS movimientos CASCADE;

-- 2. Luego las tablas que dependen de otras
DROP TABLE IF EXISTS lotes CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 3. Eliminar las tablas restantes
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS productos CASCADE;
DROP TABLE IF EXISTS proveedores CASCADE;
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
    id               SERIAL PRIMARY KEY,
    nombre_generico  VARCHAR(255) NOT NULL,
    codigo_interno   VARCHAR(50)  NOT NULL UNIQUE, -- Ejemplo: '9-120'
    tipo_producto    VARCHAR(50)  NOT NULL,
    unidad_medida    VARCHAR(50)  NOT NULL,
    producto_destino VARCHAR(50),
    observaciones    TEXT,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE proveedores
(
    id            SERIAL PRIMARY KEY,
    razon_social  VARCHAR(255) NOT NULL,
    cuit          VARCHAR(255) NOT NULL,
    direccion     VARCHAR(255) NOT NULL,
    ciudad        VARCHAR(100) NOT NULL,
    pais          VARCHAR(100) NOT NULL,
    telefono      VARCHAR(50),
    email         VARCHAR(100),
    contacto      VARCHAR(100),
    observaciones TEXT,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE
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
    fecha_creacion       TIMESTAMP      NOT NULL,
    codigo_interno       VARCHAR(50)    NOT NULL,
    producto_id          INT            NOT NULL,
    proveedor_id         INT            NOT NULL,
    fabricante_id        INT,
    pais_origen          TEXT           NOT NULL,

    fecha_ingreso        DATE           NOT NULL,
    nro_bulto            INT            NOT NULL,
    bultos_totales       INT            NOT NULL,
    cantidad_inicial     NUMERIC(12, 4) NOT NULL,
    cantidad_actual      NUMERIC(12, 4) NOT NULL,
    unidad_medida        VARCHAR(50)    NOT NULL,

    lote_proveedor       TEXT           NOT NULL,
    fecha_reanal_prov    DATE,
    fecha_vto_prov       DATE,
    estado               VARCHAR(30)    NOT NULL,
    dictamen             TEXT           NOT NULL,
    lote_origen_id       INT,
    nro_remito           TEXT,
    detalle_conservacion TEXT,
    observaciones        TEXT,
    activo               BOOLEAN        NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_producto_lote
        FOREIGN KEY (producto_id) REFERENCES productos (id),
    CONSTRAINT fk_lote_origen_lote
        FOREIGN KEY (lote_origen_id) REFERENCES lotes (id),
    CONSTRAINT fk_proveedor_lote
        FOREIGN KEY (proveedor_id) REFERENCES proveedores (id),
    CONSTRAINT fk_fabricante_lote
        FOREIGN KEY (fabricante_id) REFERENCES proveedores (id)
);


CREATE TABLE movimientos
(
    id                   SERIAL PRIMARY KEY,
    fecha_creacion       TIMESTAMP NOT NULL,
    fecha                DATE      NOT NULL,
    tipo_movimiento      TEXT      NOT NULL,
    motivo               TEXT      NOT NULL,
    lote_id              INT       NOT NULL,
    cantidad             NUMERIC(12, 4),
    unidad_medida        TEXT,
    nro_analisis         VARCHAR(30), -- Post QA
    orden_produccion     VARCHAR(30), -- Para movimientos de consumo prod
    dictamen_inicial     TEXT,
    dictamen_final       TEXT,
    movimiento_origen_id INT,
    observaciones        TEXT,
    activo               BOOLEAN   NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_lote_movimiento
        FOREIGN KEY (lote_id) REFERENCES lotes (id),
    CONSTRAINT fk_movimiento_origen_movimiento
        FOREIGN KEY (movimiento_origen_id) REFERENCES movimientos (id)
);


CREATE TABLE analisis
(
    id                SERIAL PRIMARY KEY,
    fecha_creacion    TIMESTAMP   NOT NULL,
    nro_analisis      VARCHAR(30) NOT NULL,
    fecha_realizado   DATE,
    fecha_reanalisis  DATE,
    fecha_vencimiento DATE,
    dictamen          TEXT,
    titulo            NUMERIC(12, 4),
    observaciones     TEXT,
    activo            BOOLEAN     NOT NULL DEFAULT TRUE
);


CREATE TABLE lote_analisis
(
    lote_id     INT NOT NULL,
    analisis_id INT NOT NULL,
    PRIMARY KEY (lote_id, analisis_id),
    CONSTRAINT fk_lote_analisis
        FOREIGN KEY (lote_id) REFERENCES lotes (id) ON DELETE CASCADE,
    CONSTRAINT fk_analisis_lote
        FOREIGN KEY (analisis_id) REFERENCES analisis (id) ON DELETE CASCADE
);

CREATE TABLE trazas
(
    id             SERIAL PRIMARY KEY,
    fecha_creacion TIMESTAMP   NOT NULL,
    lote_id        INT         NOT NULL,
    producto_id    INT         NOT NULL,
    nro_traza      BIGINT      NOT NULL,
    estado         VARCHAR(30) NOT NULL,
    observaciones  TEXT,
    activo         BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_trazas_lote
        FOREIGN KEY (lote_id) REFERENCES lotes (id) ON DELETE CASCADE,
    CONSTRAINT fk_trazas_producto
        FOREIGN KEY (producto_id) REFERENCES productos (id),
    CONSTRAINT uk_traza_producto_traza
        UNIQUE (producto_id, nro_traza)
);

CREATE TABLE trazas_movimientos
(
    traza_id     BIGINT NOT NULL,
    movimiento_id BIGINT NOT NULL,
    PRIMARY KEY (traza_id, movimiento_id),
    CONSTRAINT fk_traza_movimiento
        FOREIGN KEY (traza_id) REFERENCES trazas (id) ON DELETE CASCADE,
    CONSTRAINT fk_movimiento_traza
        FOREIGN KEY (movimiento_id) REFERENCES movimientos (id) ON DELETE CASCADE
);
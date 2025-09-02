-- -- Índices simples
-- DROP INDEX IF EXISTS idx_lotes_codigo_activo;
-- DROP INDEX IF EXISTS idx_lotes_fecha_ingreso;
-- DROP INDEX IF EXISTS idx_lotes_lote_origen_id;
-- DROP INDEX IF EXISTS idx_bultos_lote_id;
-- DROP INDEX IF EXISTS idx_bultos_lote_nro;
-- DROP INDEX IF EXISTS idx_movs_fecha;
-- DROP INDEX IF EXISTS idx_movs_codigo;
-- DROP INDEX IF EXISTS idx_movs_lote_id;
-- DROP INDEX IF EXISTS idx_movs_mov_origen_id;
-- DROP INDEX IF EXISTS idx_analisis_nro;
-- DROP INDEX IF EXISTS idx_analisis_lote_id;
-- DROP INDEX IF EXISTS idx_trazas_lote_id;
-- DROP INDEX IF EXISTS idx_trazas_bulto_nro;
-- DROP INDEX IF EXISTS idx_trazas_producto_id;
-- DROP INDEX IF EXISTS idx_detmov_mov_id;
-- DROP INDEX IF EXISTS idx_detmov_bulto_id;
--
-- -- Índices parciales
-- DROP INDEX IF EXISTS idx_lotes_codigo_activo_true;
-- DROP INDEX IF EXISTS idx_bultos_lote_activo_true;
-- DROP INDEX IF EXISTS idx_trazas_bulto_activo_true;
-- DROP INDEX IF EXISTS idx_movs_fecha_activo_true;
--
-- -- Índices únicos parciales
-- DROP INDEX IF EXISTS uk_lotes_codigo_lote_activo;
-- DROP INDEX IF EXISTS uk_traza_producto_traza_activo;
-- DROP INDEX IF EXISTS uk_bulto_por_lote_activo;

DROP TABLE IF EXISTS trazas_detalles CASCADE;
DROP TABLE IF EXISTS detalle_movimientos CASCADE;
DROP TABLE IF EXISTS trazas CASCADE;
DROP TABLE IF EXISTS analisis CASCADE;
DROP TABLE IF EXISTS movimientos CASCADE;

-- 2. Luego las tablas que dependen de otras
DROP TABLE IF EXISTS bultos CASCADE;
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
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

--TABLAS DATOS MAESTROS--
CREATE TABLE productos
(
    id               BIGSERIAL PRIMARY KEY,
    nombre_generico  VARCHAR(255) NOT NULL,
    codigo_producto  VARCHAR(50)  NOT NULL UNIQUE, -- Ejemplo: '9-120'
    tipo_producto    VARCHAR(50)  NOT NULL,
    unidad_medida    VARCHAR(50)  NOT NULL,
    producto_destino VARCHAR(255),
    observaciones    TEXT,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE proveedores
(
    id            BIGSERIAL PRIMARY KEY,
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
    id     BIGSERIAL PRIMARY KEY,
    clave  VARCHAR(255) NOT NULL,
    valor  VARCHAR(255) NOT NULL,
    activo BOOLEAN      NOT NULL DEFAULT TRUE
);


--TABLAS DATOS OPERATIVOS--
CREATE TABLE lotes
(
    id                      BIGSERIAL PRIMARY KEY,
    fecha_creacion          TIMESTAMPTZ NOT NULL,
    codigo_lote             VARCHAR(70) NOT NULL,
    producto_id             BIGINT      NOT NULL,
    proveedor_id            BIGINT      NOT NULL,
    fabricante_id           BIGINT,
    pais_origen             TEXT        NOT NULL,

    fecha_ingreso           DATE        NOT NULL,
    bultos_totales          INT         NOT NULL,
    cantidad_inicial        NUMERIC(12, 4),
    cantidad_actual         NUMERIC(12, 4),
    unidad_medida           VARCHAR(50) NOT NULL,

    orden_produccion_origen VARCHAR(30),
    lote_proveedor          TEXT        NOT NULL,
    fecha_reanal_prov       DATE,
    fecha_vto_prov          DATE,
    estado                  VARCHAR(30) NOT NULL,
    dictamen                TEXT        NOT NULL,
    lote_origen_id          BIGINT,
    nro_remito              TEXT,
    detalle_conservacion    TEXT,
    observaciones           TEXT,
    activo                  BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_producto_lote
        FOREIGN KEY (producto_id) REFERENCES productos (id),
    CONSTRAINT fk_lote_origen_lote
        FOREIGN KEY (lote_origen_id) REFERENCES lotes (id),
    CONSTRAINT fk_proveedor_lote
        FOREIGN KEY (proveedor_id) REFERENCES proveedores (id),
    CONSTRAINT fk_fabricante_lote
        FOREIGN KEY (fabricante_id) REFERENCES proveedores (id)
);

--N bultos por lote--
CREATE TABLE bultos
(
    id               BIGSERIAL PRIMARY KEY,
    lote_id          BIGINT         NOT NULL,

    nro_bulto        INT            NOT NULL,
    cantidad_inicial NUMERIC(12, 4) NOT NULL,
    cantidad_actual  NUMERIC(12, 4) NOT NULL,
    unidad_medida    VARCHAR(50)    NOT NULL,

    estado           VARCHAR(30)    NOT NULL,
    activo           BOOLEAN        NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_bultos_lote
        FOREIGN KEY (lote_id) REFERENCES lotes (id)
);

CREATE TABLE movimientos
(
    id                   BIGSERIAL PRIMARY KEY,
    codigo_movimiento    VARCHAR(100) NOT NULL,
    fecha_creacion       TIMESTAMPTZ  NOT NULL,
    fecha                DATE         NOT NULL,
    tipo_movimiento      TEXT         NOT NULL,
    motivo               TEXT         NOT NULL,
    lote_id              BIGINT       NOT NULL,
    cantidad             NUMERIC(12, 4),
    unidad_medida        TEXT,
    nro_analisis         VARCHAR(30), -- Post QA
    orden_produccion     VARCHAR(30), -- Para movimientos de consumo prod
    dictamen_inicial     TEXT,
    dictamen_final       TEXT,
    movimiento_origen_id BIGINT,
    observaciones        TEXT,
    activo               BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_lote_movimiento
        FOREIGN KEY (lote_id) REFERENCES lotes (id),
    CONSTRAINT fk_movimiento_origen_movimiento
        FOREIGN KEY (movimiento_origen_id) REFERENCES movimientos (id)
);

CREATE TABLE detalle_movimientos
(
    id            BIGSERIAL PRIMARY KEY,
    movimiento_id BIGINT         NOT NULL,
    bulto_id      BIGINT         NOT NULL,
    cantidad      NUMERIC(12, 4) NOT NULL,
    unidad_medida VARCHAR(50)    NOT NULL,

    CONSTRAINT uk_mov_bulto UNIQUE (movimiento_id, bulto_id),

    CONSTRAINT fk_mov_bulto_det_mov
        FOREIGN KEY (movimiento_id) REFERENCES movimientos (id),
    CONSTRAINT fk_mov_bulto_det_bul
        FOREIGN KEY (bulto_id) REFERENCES bultos (id)
);

CREATE TABLE analisis
(
    id                BIGSERIAL PRIMARY KEY,
    fecha_creacion    TIMESTAMPTZ NOT NULL,
    nro_analisis      VARCHAR(30) NOT NULL,
    lote_id           BIGINT      NOT NULL,
    fecha_realizado   DATE,
    fecha_reanalisis  DATE,
    fecha_vencimiento DATE,
    dictamen          TEXT,
    titulo            NUMERIC(12, 4),
    observaciones     TEXT,
    activo            BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_analisis_lote
        FOREIGN KEY (lote_id) REFERENCES lotes (id)
);

CREATE TABLE trazas
(
    id             BIGSERIAL PRIMARY KEY,
    fecha_creacion TIMESTAMPTZ NOT NULL,
    lote_id        BIGINT      NOT NULL,
    bulto_id       BIGINT      NOT NULL,
    producto_id    BIGINT      NOT NULL,
    nro_traza      BIGINT      NOT NULL,
    estado         VARCHAR(30) NOT NULL,
    observaciones  TEXT,
    activo         BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_trazas_lote
        FOREIGN KEY (lote_id) REFERENCES lotes (id),
    CONSTRAINT fk_trazas_bulto
        FOREIGN KEY (bulto_id) REFERENCES bultos (id),
    CONSTRAINT fk_trazas_producto
        FOREIGN KEY (producto_id) REFERENCES productos (id)
);

CREATE TABLE trazas_detalles
(
    traza_id   BIGINT NOT NULL,
    detalle_id BIGINT NOT NULL,
    PRIMARY KEY (traza_id, detalle_id),
    CONSTRAINT fk_traza_detalle
        FOREIGN KEY (traza_id) REFERENCES trazas (id),
    CONSTRAINT fk_movimiento_traza
        FOREIGN KEY (detalle_id) REFERENCES detalle_movimientos (id)
);

CREATE INDEX IF NOT EXISTS idx_lotes_codigo_activo ON lotes (codigo_lote, activo);
CREATE INDEX IF NOT EXISTS idx_lotes_fecha_ingreso ON lotes (fecha_ingreso);
CREATE INDEX IF NOT EXISTS idx_lotes_lote_origen_id ON lotes (lote_origen_id);
CREATE INDEX IF NOT EXISTS idx_bultos_lote_id ON bultos (lote_id);
CREATE INDEX IF NOT EXISTS idx_bultos_lote_nro ON bultos (lote_id, nro_bulto);
CREATE INDEX IF NOT EXISTS idx_movs_fecha ON movimientos (fecha);
CREATE INDEX IF NOT EXISTS idx_movs_codigo ON movimientos (codigo_movimiento);
CREATE INDEX IF NOT EXISTS idx_movs_lote_id ON movimientos (lote_id);
CREATE INDEX IF NOT EXISTS idx_movs_mov_origen_id ON movimientos (movimiento_origen_id);
CREATE INDEX IF NOT EXISTS idx_analisis_nro ON analisis (nro_analisis);
CREATE INDEX IF NOT EXISTS idx_analisis_lote_id ON analisis (lote_id);
CREATE INDEX IF NOT EXISTS idx_trazas_lote_id ON trazas (lote_id);
CREATE INDEX IF NOT EXISTS idx_trazas_bulto_nro ON trazas (bulto_id, nro_traza);
CREATE INDEX IF NOT EXISTS idx_trazas_producto_id ON trazas (producto_id);
CREATE INDEX IF NOT EXISTS idx_detmov_mov_id ON detalle_movimientos (movimiento_id);
CREATE INDEX IF NOT EXISTS idx_detmov_bulto_id ON detalle_movimientos (bulto_id);

CREATE INDEX IF NOT EXISTS idx_lotes_codigo_activo_true ON lotes (codigo_lote)
    WHERE activo = true;

CREATE INDEX IF NOT EXISTS idx_bultos_lote_activo_true ON bultos (lote_id, nro_bulto)
    WHERE activo = true;

CREATE INDEX IF NOT EXISTS idx_trazas_bulto_activo_true ON trazas (bulto_id, nro_traza)
    WHERE activo = true;

CREATE INDEX IF NOT EXISTS idx_movs_fecha_activo_true ON movimientos (fecha)
    WHERE activo = true;

CREATE UNIQUE INDEX uk_lotes_codigo_lote_activo
    ON lotes (codigo_lote)
    WHERE activo = true;

CREATE UNIQUE INDEX uk_traza_producto_traza_activo
    ON trazas (producto_id, nro_traza)
    WHERE activo = true;

CREATE UNIQUE INDEX uk_bulto_por_lote_activo
    ON bultos (lote_id, nro_bulto)
    WHERE activo = true;

ALTER TABLE lotes
    ADD CONSTRAINT chk_lotes_cant_inicial CHECK (cantidad_inicial >= 0),
    ADD CONSTRAINT chk_lotes_cant_actual CHECK (cantidad_actual >= 0);

ALTER TABLE lotes
    ADD CONSTRAINT chk_lotes_cant_coherente CHECK (cantidad_actual <= cantidad_inicial);

ALTER TABLE bultos
    ADD CONSTRAINT chk_bultos_cant_inicial CHECK (cantidad_inicial >= 0),
    ADD CONSTRAINT chk_bultos_cant_actual CHECK (cantidad_actual >= 0);

ALTER TABLE bultos
    ADD CONSTRAINT chk_bultos_cant_coherente CHECK (cantidad_actual <= cantidad_inicial);

ALTER TABLE movimientos
    ADD CONSTRAINT chk_movs_cantidad CHECK (cantidad IS NULL OR cantidad >= 0);


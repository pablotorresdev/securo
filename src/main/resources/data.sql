INSERT INTO tipo_producto (nombre)
VALUES ('Api'),
       ('Excipiente'),
       ('Capsula'),
       ('Semielaborado'),
       ('Acond. primario'),
       ('Acond. secundario'),
       ('Unidad venta');


INSERT INTO dictamen (estado)
VALUES ('Recibido'),
       ('Cuarentena'),
       ('Aprobado'),
       ('Rechazado'),
       ('Vencido'),
       ('Destruido'),
       ('Retiro mercado'),
       ('Consumido'),
       ('Depomax');


INSERT INTO motivo (descripcion)
VALUES ('Consumo Produccion'),
       ('Vencido'),
       ('Reanalizado'),
       ('Ajuste'),
       ('Rechazado'),
       ('Muestreo'),
       ('Compra'),
       ('Devolucion'),
       ('Saldo Inicial'),
       ('Desarrollo');

INSERT INTO estado (descripcion)
VALUES ('Activo'),
       ('Inactivo');

INSERT INTO tipo_movimiento (nombre)
VALUES ('Ingreso'),
       ('Egreso'),
       ('Transformacion');


/***************************unidad_medida******************/
INSERT INTO unidad_medida (nombre, tipo, simbolo, factor_conversion)
VALUES ('Unidad', 'Generica', 'U', 1.0),

-- Unidades de Masa
       ('Microgramo', 'Masa', 'µg', 0.000001),
       ('Miligramo', 'Masa', 'mg', 0.001),
       ('Gramo', 'Masa', 'g', 1.0),
       ('Kilogramo', 'Masa', 'kg', 1000.0),
       ('Tonelada', 'Masa', 't', 1000000.0),

-- Unidades de Volumen
       ('Microlitro', 'Volumen', 'µL', 0.000001),        -- 1 µL = 0.000001 L
       ('Mililitro', 'Volumen', 'mL', 0.001),            -- 1 mL = 0.001 L
       ('Centilitro', 'Volumen', 'cL', 0.01),            -- 1 cL = 0.01 L
       ('Decilitro', 'Volumen', 'dL', 0.1),              -- 1 dL = 0.1 L
       ('Litro', 'Volumen', 'L', 1.0),                   -- 1 L = 1 L

       ('Milimetro cubico', 'Volumen', 'mm3', 0.000001), -- 1 mm³ = 0.000001 L
       ('Centimetro cubico', 'Volumen', 'cm3', 0.001),   -- 1 cm³ = 0.001 L
       ('Metro cubico', 'Volumen', 'm3', 1000.0),        -- 1 m³ = 1000 L

-- Unidades de Superficie
       ('Milimetro cuadrado', 'Superficie', 'mm2', 0.000001),
       ('Centimetro cuadrado', 'Superficie', 'cm2', 0.0001),
       ('Metro cuadrado', 'Superficie', 'm2', 1.0),
       ('Kilometro cuadrado', 'Superficie', 'km2', 1000000.0),
       ('Hectarea', 'Superficie', 'ha', 10000.0),

-- Unidades de Longitud
       ('Micrometro', 'Longitud', 'µm', 0.000001),
       ('Milimetro', 'Longitud', 'mm', 0.001),
       ('Centimetro', 'Longitud', 'cm', 0.01),
       ('Metro', 'Longitud', 'm', 1.0),
       ('Kilometro', 'Longitud', 'km', 1000.0),

-- Unidades porcentuales
       ('Porcentaje', 'Porcentaje', '%', 0.01),
       ('Partes por millon', 'Porcentaje', 'ppm', 0.000001)
;










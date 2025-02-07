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
VALUES ('Consumo Producción'),
       ('Vencido'),
       ('Reanalizado'),
       ('Ajuste'),
       ('Rechazado'),
       ('Muestreo'),
       ('Compra'),
       ('Devolución'),
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

       ('Milímetro cúbico', 'Volumen', 'mm3', 0.000001), -- 1 mm³ = 0.000001 L
       ('Centímetro cúbico', 'Volumen', 'cm3', 0.001),   -- 1 cm³ = 0.001 L
       ('Metro cúbico', 'Volumen', 'm3', 1000.0),        -- 1 m³ = 1000 L

-- Unidades de Superficie
       ('Milímetro cuadrado', 'Superficie', 'mm2', 0.000001),
       ('Centímetro cuadrado', 'Superficie', 'cm2', 0.0001),
       ('Metro cuadrado', 'Superficie', 'm2', 1.0),
       ('Kilómetro cuadrado', 'Superficie', 'km2', 1000000.0),
       ('Hectárea', 'Superficie', 'ha', 10000.0),

-- Unidades de Longitud
       ('Micrometro', 'Longitud', 'µm', 0.000001),
       ('Milímetro', 'Longitud', 'mm', 0.001),
       ('Centímetro', 'Longitud', 'cm', 0.01),
       ('Metro', 'Longitud', 'm', 1.0),
       ('Kilómetro', 'Longitud', 'km', 1000.0),

-- Unidades porcentuales
       ('Porcentaje', 'Porcentaje', '%', 0.01),
       ('Partes por millon', 'Porcentaje', 'ppm', 0.000001)
;










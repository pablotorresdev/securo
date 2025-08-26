-- Proveedores
INSERT INTO proveedores (razon_social, cuit, direccion, ciudad, pais, telefono, email, contacto, activo)
VALUES ('Laboratorios BioPharma S.A.', '30-49439948-9', 'Av. Corrientes 1234', 'Buenos Aires', 'Argentina', '(011) 4321-5678', 'info@biopharma.com.ar', 'Juan Perez', true);

INSERT INTO proveedores (razon_social, cuit, direccion, ciudad, pais, telefono, email, contacto, activo)
VALUES ('Quimica del Plata S.R.L.', '33-96275551-3', 'Calle San Martin 987', 'Cordoba', 'Argentina', '(0351) 468-1122', 'ventas@quimicadelplata.com.ar', 'Maria Lopez', true);

INSERT INTO proveedores (razon_social, cuit, direccion, ciudad, pais, telefono, email, contacto, activo)
VALUES ('Distribuidora Medicorp S.A.', '30-51557422-7', 'Av. Pellegrini 752', 'Rosario', 'Argentina', '(0341) 223-3344', 'proveedor@medicorp.com', 'Carlos Gonzalez', true);

INSERT INTO proveedores (razon_social, cuit, direccion, ciudad, pais, telefono, email, contacto, activo)
VALUES ('Insumos Quimicos Mendoza S.R.L.', '33-38544412-0', 'Av. Las Heras 2300', 'Mendoza', 'Argentina', '(0261) 445-6677', 'administracion@insumosmendoza.com.ar', 'Lucia Martinez', true);

INSERT INTO proveedores (razon_social, cuit, direccion, ciudad, pais, telefono, email, contacto, activo)
VALUES ('Pharma Solutions S.A.', '34-05146058-9', 'Calle 12 456', 'La Plata', 'Argentina', '(0221) 412-8999', 'info@pharmasolutions.com', 'Pedro Fernandez', true);

INSERT INTO proveedores (razon_social, cuit, direccion, ciudad, pais, telefono, email, contacto, activo)
VALUES ('Conifarma S.A.', '34-11111111-9', 'Pringles 10', 'CABA', 'Argentina', '(0221) 412-8999', 'info@pharmasolutions.com', 'Silvita', true);

-- Productos
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, unidad_medida,  observaciones, activo)
VALUES ('Paracetamol', 'P-001', 'API', 'GRAMO', 'Analgesico y antipiretico de uso farmaceutico', true);

INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, unidad_medida, observaciones, activo)
VALUES ('Ibuprofeno', 'P-002', 'API', 'GRAMO', 'Antiinflamatorio no esteroideo para aliviar el dolor', true);

INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, unidad_medida, observaciones, activo)
VALUES ('H2O', 'P-022', 'EXCIPIENTE', 'LITRO', 'Esteril y purificada para uso farmaceutico', true);

INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, unidad_medida, observaciones, activo)
VALUES ('Lactosa Monohidrato', 'P-003', 'EXCIPIENTE', 'KILOGRAMO', 'Excipiente para formulaciones farmaceuticas de alta pureza', true);

INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, unidad_medida, observaciones, activo)
VALUES ('Capsula Vacio', 'P-004', 'UNIDAD_VENTA', 'UNIDAD', 'Capsula de gelatina vacia para encapsulamiento de medicamentos', true);

INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto,  producto_destino, unidad_medida, observaciones, activo)
VALUES ('Semielaborado X', 'P-005', 'SEMIELABORADO', 'GLUMIC 200 MG X 60 COMPRIMIDOS DISPERSABLES', 'MILIGRAMO', 'Producto semielaborado destinado a procesos de produccion', true);

INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto,  unidad_medida, observaciones, activo)
VALUES ('Acond. primario Y', 'P-006', 'ACOND_PRIMARIO', 'UNIDAD', 'Material para empaque primario en la industria farmaceutica', true);

INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ACIDO CARGLUMICO', '1-05700', 'API', 'Glumic 200 mg', 'GRAMO', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ACIDO TARTARICO', '1-05400', 'API', 'Cystam 50 y 150 mg', 'KILOGRAMO', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ACIDO TARTARICO V1', '1-10000', 'API', 'Cystam 50 y 150 mg', 'KILOGRAMO', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ALMIDON DE MAIZ', '1-03600', 'API', 'Nitine 5, 10 y 20 mg', 'GRAMO', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ALMIDON PARCIALMENTE PREGELATINIZADO V2', '1-04802', 'API', 'Cystam/Nitine', 'GRAMO', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ALMIDÓN PREGELATINIZADO', '1-03702', 'API', 'Cystam/Nitine', 'GRAMO', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('BOLSA DE SILICA GEL 0,5 GRAMOS', '2-00880', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CAPSULA N°00 AZUL/BLANCO', '1-06000', 'EXCIPIENTE', 'Cystam GR', 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CAPSULA N°00 BLANCO/BLANCO V2', '1-05201', 'EXCIPIENTE', 'Cystam 150 mg', 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CAPSULA N°3 BLANCA/AZUL V1', '1-04401', 'EXCIPIENTE', 'Nitine 5 mg', 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CAPSULA N°3 ESCARLATA/CREMA', '1-30000', 'EXCIPIENTE', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CAPSULA Nº0 AZUL/BLANCO', '1-20000', 'EXCIPIENTE', 'CystamGR', 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CAPSULA Nº00 AZUL/BLANCO', '1-06000', 'EXCIPIENTE', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CAPSULA Nº00 BLANCO/BLANCO', '1-05201', 'EXCIPIENTE', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CAPSULA Nº3 AZUL/BLANCO', '1-04401', 'EXCIPIENTE', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CAPSULA Nº3 BLANCO/BLANCO', '1-04900', 'EXCIPIENTE', 'Cystam/Nitine', 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CAPSULA Nº3 ROJO/BLANCO', '1-04001', 'EXCIPIENTE', 'Nitine 10 mg', 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CAPSULA Nº3 VERM/CREM', '1-04000', 'EXCIPIENTE', 'Nitine 10 mg', 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CELULOSA MICROCRISTALINA PH 101', '1-05500', 'API', 'Cystam 50 y 150 mg', 'GRAMO', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CELULOSA MICROCRISTALINA PH 102 V2', '1-05601', 'API', 'Glumic 200 mg', 'GRAMO', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CISTEAMINA BASE', '1-05800', 'API', 'Cystam 50 y 150 mg', 'KILOGRAMO', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CISTEAMINA BITARTRATO V3', '1-02702', 'API', 'Cystam 50 y 150 mg', 'GRAMO', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CITRATO DE TRIETILO', '1-08000', 'API', 'Cystam GR', 'GRAMO', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CROSCARMELOSA SODICA V2', '1-01103', 'API', 'Cystam/Nitine', 'GRAMO', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CYSTAM 150 MG (GRANEL CAPSULA)', '9-04200', 'GRANEL_CAPSULAS', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CYSTAM 150 MG (GRANEL FRASCO)', '9-04800', 'GRANEL_FRASCOS', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CYSTAM 150 MG (GRANEL MEZCLA)', '9-04100', 'GRANEL_MEZCLA_POLVO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CYSTAM 150 MG CAPSULAS', '9-02300', 'GRANEL_CAPSULAS', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CYSTAM 50 MG (GRANEL CAPSULA)', '9-04600', 'GRANEL_CAPSULAS', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CYSTAM 50 MG (GRANEL FRASCO)', '9-04700', 'GRANEL_FRASCOS', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CYSTAM 50 MG (GRANEL MEZCLA)', '9-04500', 'GRANEL_MEZCLA_POLVO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('CYSTAM 50 MG CAPSULAS', '9-03500', 'GRANEL_CAPSULAS', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('DIOXIDO DE SILICIO COLOIDAL V1', '1-05001', 'API', 'Cystam 50 y 150 mg', 'GRAMO', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ESTEARATO DE MAGNESIO V1', '1-01602', 'API', 'Cystam 50 y 150 mg', 'GRAMO', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ESTEARIL FUMARATO DE SODIO V2', '1-03102', 'API', 'Glumic 200 mg', 'GRAMO', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ESTUCHE CYSTAM 150 V3', '2-01101', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ESTUCHE CYSTAM 50 V3', '2-01201', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ESTUCHE GLUMIC 200 X 15 COMPRIMIDOS', '2-03100', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ESTUCHE GLUMIC 200 X 5 COMPRIMIDOS', '2-03000', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ESTUCHE GLUMIC 200 X 60 COMPRIMIDOS', '2-03200', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ESTUCHE NITINE 10 MG V3', '2-02400', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ESTUCHE NITINE 20 MG V3', '2-02500', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ESTUCHE NITINE 5 MG V3', '2-02300', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ETIQUETA AUTOADHESIVA CYSTAM 150 V5', '2-01500', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ETIQUETA AUTOADHESIVA CYSTAM 50 V4', '2-01400', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ETIQUETA AUTOADHESIVA DE SEGURIDAD', '2-00780', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ETIQUETA AUTOADHESIVA DE SEGURIDAD V2', '2-03800', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ETIQUETA AUTOADHESIVA GLUMIC 200 X 15 COMPRIMIDOS', '2-03400', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ETIQUETA AUTOADHESIVA GLUMIC 200 X 5 COMPRIMIDOS', '2-03300', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ETIQUETA AUTOADHESIVA GLUMIC 200 X 60 COMPRIMIDOS', '2-03500', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ETIQUETA AUTOADHESIVA NITINE 10 MG V6', '2-02000', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ETIQUETA AUTOADHESIVA NITINE 20 MG V3', '2-02100', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('ETIQUETA AUTOADHESIVA NITINE 5 MG V3', '2-01900', 'ACOND_SECUNDARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('EUDRAGIT L30 D-55', '1-09000', 'API', 'Cystam GR', 'GRAMO', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('FRASCO BLANCO NITINE', '2-00721', 'ACOND_PRIMARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('FRASCO DE POLIET BCO ALTA DENS ROSCADO 250ML V1', '2-00431', 'ACOND_PRIMARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('FRASCO DE POLIET BCO ALTA DENS ROSCADO 250ML V2', '2-04000', 'ACOND_PRIMARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('FRASCO LEVEPET 30 CC RP-28 BLANCO', '2-02600', 'ACOND_PRIMARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('FRASCO LEVEPET 50 CC RP-28 BLANCO', '2-02700', 'ACOND_PRIMARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('FRASCO PEAD BLANCO S/TAPA A ROSCA NITINE V1', '2-05000', 'ACOND_PRIMARIO', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('GLUMIC 200 MG  X 15 COMP. DISPERSABLES (GRANEL FRASCO)', '9-11000', 'UNIDAD_VENTA', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('GLUMIC 200 MG  X 5 COMP. DISPERSABLES (GRANEL FRASCO)', '9-10000', 'UNIDAD_VENTA', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('GLUMIC 200 MG  X 60 COMP. DISPERSABLES (GRANEL FRASCO)', '9-09000', 'UNIDAD_VENTA', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('GLUMIC 200 MG (GRANEL COMPRIMIDO DISPERSABLE)', '9-08000', 'UNIDAD_VENTA', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('GLUMIC 200 MG (GRANEL MEZCLA)', '9-07000', 'UNIDAD_VENTA', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('GLUMIC 200 MG X 15 COMPRIMIDOS DISPERSABLES', '9-13000', 'UNIDAD_VENTA', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('GLUMIC 200 MG X 5 COMPRIMIDOS DISPERSABLES', '9-12000', 'UNIDAD_VENTA', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
INSERT INTO productos (nombre_generico, codigo_producto, tipo_producto, producto_destino, unidad_medida, activo) VALUES ('GLUMIC 200 MG X 60 COMPRIMIDOS DISPERSABLES', '9-06000', 'UNIDAD_VENTA', null, 'UNIDAD', true) ON CONFLICT (codigo_producto) DO NOTHING;
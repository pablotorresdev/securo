INSERT INTO contactos (razon_social, cuit, direccion, ciudad, pais, telefono, fax, email, persona_contacto,activo) VALUES ('Laboratorios BioPharma S.A.', '30-49439948-9', 'Av. Corrientes 1234', 'Buenos Aires', 'Argentina', '(011) 4321-5678', '(011) 4321-5679', 'info@biopharma.com.ar', 'Juan Perez', true);
INSERT INTO contactos (razon_social, cuit, direccion, ciudad, pais, telefono, fax, email, persona_contacto,activo) VALUES ('Quimica del Plata S.R.L.', '33-96275551-3', 'Calle San Martin 987', 'Cordoba', 'Argentina', '(0351) 468-1122', '(0351) 468-1123', 'ventas@quimicadelplata.com.ar', 'Maria Lopez', true);
INSERT INTO contactos (razon_social, cuit, direccion, ciudad, pais, telefono, fax, email, persona_contacto,activo) VALUES ('Distribuidora Medicorp S.A.', '30-51557422-7', 'Av. Pellegrini 752', 'Rosario', 'Argentina', '(0341) 223-3344', NULL, 'contacto@medicorp.com', 'Carlos Gonzalez', true);
INSERT INTO contactos (razon_social, cuit, direccion, ciudad, pais, telefono, fax, email, persona_contacto,activo) VALUES ('Insumos Quimicos Mendoza S.R.L.', '33-38544412-0', 'Av. Las Heras 2300', 'Mendoza', 'Argentina', '(0261) 445-6677', NULL, 'administracion@insumosmendoza.com.ar', 'Lucia Martinez', true);
INSERT INTO contactos (razon_social, cuit, direccion, ciudad, pais, telefono, fax, email, persona_contacto,activo) VALUES ('Pharma Solutions S.A.', '34-05146058-9', 'Calle 12 456', 'La Plata', 'Argentina', '(0221) 412-8999', '(0221) 412-8998', 'info@pharmasolutions.com', 'Pedro Fernandez', true);
INSERT INTO contactos (razon_social, cuit, direccion, ciudad, pais, telefono, fax, email, persona_contacto,activo) VALUES ('Conifarma S.A.', '34-11111111-9', 'Pringles 10', 'CABA', 'Argentina', '(0221) 412-8999', '(0221) 412-8998', 'info@pharmasolutions.com', 'Silvita', true);

INSERT INTO productos (nombre_generico, codigo_interno, tipo_producto, unidad_medida, descripcion, coa, observaciones,activo) VALUES
    ('Paracetamol', 'P-001', 'API', 'GRAMO', 'Analgesico y antipiretico de uso farmaceutico', 'COA Paracetamol', 'Uso en tabletas', true);
INSERT INTO productos (nombre_generico, codigo_interno, tipo_producto, unidad_medida, descripcion, coa, observaciones,activo) VALUES
    ('Ibuprofeno', 'P-002', 'API', 'GRAMO', 'Antiinflamatorio no esteroideo para aliviar el dolor', 'COA Ibuprofeno', 'Uso en suspension oral', true);
INSERT INTO productos (nombre_generico, codigo_interno, tipo_producto, unidad_medida, descripcion, coa, observaciones,activo) VALUES
    ('Lactosa Monohidrato', 'P-003', 'EXCIPIENTE', 'KILOGRAMO', 'Excipiente para formulaciones farmaceuticas de alta pureza', 'COA Lactosa', 'Adecuado para tabletas', true);
INSERT INTO productos (nombre_generico, codigo_interno, tipo_producto, unidad_medida, descripcion, coa, observaciones,activo) VALUES
    ('Capsula Vacio', 'P-004', 'CAPSULA', 'UNIDAD', 'Capsula de gelatina vacia para encapsulamiento de medicamentos', 'COA Capsula', 'Estandar en la industria', true);
INSERT INTO productos (nombre_generico, codigo_interno, tipo_producto, unidad_medida, descripcion, coa, observaciones,activo) VALUES
    ('Semielaborado X', 'P-005', 'SEMIELABORADO', 'MILIGRAMO', 'Producto semielaborado destinado a procesos de produccion', 'COA Semielaborado', 'Requiere control adicional', true);
INSERT INTO productos (nombre_generico, codigo_interno, tipo_producto, unidad_medida, descripcion, coa, observaciones,activo) VALUES
    ('Acond. primario Y', 'P-006', 'ACOND_PRIMARIO', 'UNIDAD', 'Material para empaque primario en la industria farmaceutica', 'COA AcondPrimario', 'Control de calidad estricto', true);

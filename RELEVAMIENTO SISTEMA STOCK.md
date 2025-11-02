# RELEVAMIENTO SISTEMA STOCK

El sistema está diseñado para gestionar todas las operaciones relacionadas con la entrada y salida de materiales, desde la compra o producción interna, pasando por los estadios intermedios relacionados con el análisis de calidad, liberación para la venta u otros procesos asociados, hasta su consumo para producción o venta.

# Modelo de Casos de Uso

El modelo de casos de uso explicará los procesos de operación del sistema.

La presentación de cada uno se estructura de la siguiente manera:

1. Una descripción del motivo real que requiere realizar la acción del caso de uso.
2. Enumeración de las Pre-condiciones para que el mismo sea ejecutado.
3. Usuarios que pueden realizar dicho procedimiento.
4. Una tabla de los datos asociados a la acción del **Caso de Uso**. Estos datos se llenan por el usuario que está realizando la acción. Varios datos son fijos, están asociados al **Caso de Uso** (los mismos caracterizan al **CU** y no son editables y se completan automáticamente al seleccionar la operación a realizar).
5. Los posibles flujos alternativos de la operación.
6. Las postcondiciones del mismo.

Antes de detallar cada caso de uso, se presenta un diagrama básico del funcionamiento del stock en la cadena productiva y se describirán los distintos usuarios intervinientes en la misma.

## Diagrama de flujo del stock en la empresa

# A continuación se presenta un esquema simple sobre el flujo de los materiales dentro del alcance del sistema

Fase de Producto externo hasta el proceso productivo

Se realiza una compra, que implica el ingreso de producto externo  \[API, Excipiente, Cápsula, Acond 1º o 2º\]

1) **RECEPCIÓN:** Se recibe el material: se ejecuta **CU1** para hacer un alta de lote por **Compra** al **dictamen** de **Recibido**
   1) *QC (control de calidad)* le asigna un *Nro de Analisis* y  se pasa todo el lote al **dictamen** de **Cuarentena** mediante el **CU2**
   2) Se toma una muestra para *QC*: se ejecuta **CU3** para hacer un baja del lote por **Muestreo multi bulto**
   3) En caso de que el lote deba ser devuelto al proveedor, se ejecuta **CU4** para hacer un baja de lote por **Devolución Compra**
2) **ANÁLISIS:** Para el lote puesto en **Cuarentena** por **Análisis**, en caso de que *QC* apruebe el análisis, se ejecuta **CU5** para hacer cambiar el **dictamen** del lote a **Aprobado**
   1) En caso de que *QC* **no** apruebe el análisis, se ejecuta **CU6** para hacer cambiar el **dictamen** del lote a **Rechazado**
   2) En caso de que *QC* necesite nuevas muestras, se ejecuta **CU3** para hacer un baja del lote por **Muestreo**
   3) En caso de que *QC* necesite anular el análisis en curso, se ejecuta **CU11** para hacer un un cambio de dictamen de lote propio a su dictamen anterior y anular el análisis en curso asociado.
3) **PRODUCCIÓN:** Se inicia un proceso productivo con ese lote **Aprobado**, se ejecuta **CU7** para hacer un baja del lote por **Consumo Producción**
   1) En caso de que *QC* decida re-analizar el lote por próximo vencimiento, se asigna un nuevo análisis al producto **Aprobado** mediante el **CU8**
   2) En caso de se alcance la fecha de *reanálisis*, de manera automática se pasa todo el lote al **dictamen** de **Análisis expirado** mediante el **CU9**
   3) En caso de se alcance la fecha de *vencimiento*, de manera automática se pasa todo el lote al **dictamen** de **Vencido** mediante el **CU10**

Fase de Producto de producción interna hasta el proceso productivo o venta

Se realiza una producción interna, que implica el ingreso de producto propio \[Semielaborado, U de Vta\]

1) **INGRESO PRODUCCIÓN PROPIA  (Semielaborado):**  Se recibe el material: se ejecuta **CU20** para hacer un alta de lote propio a **Cuarentena**
   1) *QC (control de calidad)* le asigna un *Nro de Analisis* y  se pasa todo el lote al **dictamen** de **Cuarentena** mediante el **CU2**
   2) Se toma una muestra para *QC*: se ejecuta **CU3** para hacer un baja del lote por **Muestreo**

   **NOTA: a partir de este punto, el producto Semielaborado se trata igual que un producto externo, siguiendo el flujo a partir del punto 2\. ANALISIS de la fase anterior.**



2) **PRODUCTO PROPIO (U de Vta):** Se recibe el material: se ejecuta **CU20** para hacer un alta de lote propio a **Cuarentena**
   1) Se toma una muestra para QC: se ejecuta **CU3** para hacer un baja del lote propio a **Muestra**
3) **ANÁLISIS:** Para el lote propio en **Cuarentena**, en caso de que QC apruebe el análisis, se ejecuta **CU5** para hacer cambiar el **dictamen** del lote propio a **Aprobado**
   1) En caso de que QC **no** apruebe el análisis, se ejecuta **CU6** para hacer cambiar el **dictamen** del lote propio a **Rechazado**
   2) En caso de que QC necesite nuevas muestras, se ejecuta **CU3** para hacer un baja del lote propio por **Muestra**
4) **TRAZADO:** Se traza el lote, se ejecuta **CU27** para asignar un numero de traza a cada unidad de venta del lote.
5) **LIBERACIÓN:** Se aprueba la liberación del lote propio **Aprobado** para venta, se ejecuta **CU21** para hacer cambiar el **dictamen** del lote propio a **Liberado**
6) **VENTA:** Se realiza una venta del lote propio **Liberado**, se ejecuta **CU22** para hacer un baja del lote propio por **Venta**



Casos de contingencia

1) Se realiza la recepción de devolución por parte de un cliente, se ejecuta **CU23** para hacer un cambio de dictamen de lote propio a **Devolucion clientes** (**IMPLEMENTADO**: CU23 crea un nuevo lote con dictamen DEVOLUCION_CLIENTES a partir del movimiento de venta original)
2) Se realiza el retiro de un producto del mercado, se ejecuta **CU24** para hacer un cambio de dictamen de lote propio a **Retiro mercado** (**IMPLEMENTADO**: CU24 implementa sistema dual-lot: marca lote original como RECALL y crea nuevo lote con trazas devueltas con dictamen RETIRO_MERCADO)
3) Se realiza cualquier tipo de ajuste de cantidades de Stock, se ejecuta **CU25** para hacer una alta/baja del lote. (**IMPLEMENTADO**: CU28)
4) Reverso de movimiento, si es necesario deshacer el último movimiento realizado, se ejecuta **CU26** para revertir el movimiento y sus cambios al stock. (**IMPLEMENTADO**: CU29, CU30, CU31 según tipo de movimiento a reversar)



Casos no operativos

1) Se carga un nuevo proveedor, se ejecuta **CU30**
2) Se carga un nuevo producto, se ejecuta **CU31**
3) Se cambia la configuración del sistema, se ejecuta  **CU32**
4) Se hace un ABM de usuarios, se ejecuta  **CU33**

---

## ABM por fases

Fase de Producto externo hasta el proceso productivo

**ALTAS:**

* **CU1 \- Ingreso de Stock x compra**

**BAJAS:**

* **CU3 \- Retiro por Muestreo**
* **CU4 \- Devolución compra**
* **CU7 \- Consumo produccion**

**MODIFICACIONES**

* **CU2 \- Cambio de dictamen a Cuarentena x muestreo**
* **CU5 \- Analisis QC Aprobado**
* **CU6 \- Analisis QC Rechazado**
* **CU8 \- Reanálisis de Producto Aprobado**
* **CU9 \- Fecha reanalisis alcanzada**
* **CU10 \- Vencimiento de Stock**
* **CU11 \- Anulación de Análisis en Curso**

Fase de Producto de producción interna hasta el proceso productivo o venta

**ALTAS:**

* **CU20 Ingreso de stock por Producción Interna**
* **CU23 Devolucion Cliente** (crea nuevo lote de devolución)
* **CU24 Retiro de Mercado** (sistema dual-lot: MODIFICA lote original a RECALL + ALTA de nuevo lote con trazas devueltas)


**BAJAS:**

* **CU22 \- Venta de Producto propio**

**MODIFICACIONES:**

* **CU21 \- Liberación Unidad de Venta**
* **CU27 \- Trazado Unidad de Venta**

**ABM GENERAL:**

* **CU28** **\- Ajuste stock (A/B)** (implementado como CU28)
* **CU29-31 \- Reverso de movimiento** (CU29: reverso Alta, CU30: reverso Baja, CU31: reverso Modificación)
* **CU30 \- ABM Proveedor** (ABM maestro, no implementado en servicios CU)
* **CU31 \- ABM Producto** (ABM maestro, no implementado en servicios CU)

---

##

## Perfiles de Usuario del sistema

A continuación se listan los distintos usuarios del sistema y los Casos de Uso que puede ejecutar cada uno:

A \- Supervisor de planta (Matías Silva).

* CU7-Consumo de Producción
* CU20-Ingreso de stock por Producción Interna
* CU25-Ajuste de inventario

B \- Analista de planta (Emiliano Raciti).

* CU1-Ingreso de Stock por compra
* CU4-Devolución Compra
* CU22-Venta de Producto Propio
* CU25-Ajuste de Inventario
* CU30-Alta de Proveedor

C \- Gerente control de calidad. (Juan Martinez)

* CU5-Resultado QC: Aprobado
* CU6-Resultado QC: Rechazado
* CU8-Reanalisis de Producto Aprobado
* CU11-Anulación de Análisis en Curso
* CU31-Alta de Producto

D \- Analista control de calidad: \-\> (Noelia Pazo, Romina Gonzalez, ???)

* CU2-Pasaje a Cuarentena por QC
* CU3-Retiro por Muestreo

E \- Gerente garantía de calidad (Carolina Ponce)

* CU21-Liberación Unidad de venta
* CU27-Trazado Unidad de venta
* CU23-Devolución de cliente
* CU24-Retiro de mercado

F \- DT (Juan Torres)

* CU21-Liberación Unidad de venta
* CU27-Trazado Unidad de venta

G \- ADMIN / Usuario que realizo el movimiento

* CU26-Reverso de Ultimo movimiento

X \- PROCESO AUTOMÁTICO

* CU9-Fecha reanalisis alcanzada
* CU10-Vencimiento de Stock

---

## RECEPCION

### CU1. Ingreso de Stock por compra

Este caso de uso ocurre cuando ingresan nuevos materiales al stock desde un proveedor externo.

**Referencia:** Ver documentación detallada en [CU1_ALTA_INGRESO_COMPRA.md](./docs/cu/CU1_ALTA_INGRESO_COMPRA.md)

**Pre-condiciones**

1. **Producto** debe existir en el sistema con tipo: API, EXCIPIENTE, ACOND_PRIMARIO, ACOND_SECUNDARIO (NO puede ser SEMIELABORADO ni UNIDAD_VENTA)
2. **Proveedor** debe existir en el sistema y NO puede ser Conifarma (proveedor interno)
3. **Fabricante** es opcional (si no se especifica, se usa el proveedor)
4. Si no se cumplen estas condiciones, ejecutar el flujo alternativo correspondiente (ver más abajo)

**Flujo del proceso**

**Usuario:** B - Analista de planta

**Carga de Datos**

| CAMPO | OBLIGATORIO | VALOR | VALIDACIÓN e INFO | FORMATO |
| ----- | ----- | ----- | ----- | ----- |
| Tipo de movimiento |  | ALTA | Generado automáticamente |  |
| Motivo |  | COMPRA | Generado automáticamente |  |
| Dictamen |  | RECIBIDO | Generado automáticamente |  |
| Estado |  | NUEVO | Generado automáticamente |  |
| Fecha de ingreso | SI | Fecha | No superior a hoy | DD/MM/AAAA |
| ID Producto (Código) | SI | Cuadro de búsqueda | No SemiElab/U. D Vta | Referencia a tabla Producto |
| Proveedor | SI | Cuadro de búsqueda | No Conifarma | Ref -> Proveedores |
| Cantidad  | SI | Numérico | > 0, Entero si UNIDAD, >= bultos totales | Numérico, flotante (según unidad de Medida) |
| Unidad de medida | SI | Default del producto | Convertible a U. de Medida default del producto | Ref tabla Unidad medida |
| Lote proveedor | SI | Texto | Definido por proveedor | Texto |
| Detalle de conservación | NO | Texto |  | Texto |
| Numero de bulto | SI |  | [1..cant bultos totales] | Entero positivo, (Ej. 1) |
| Cantidad de bultos totales | SI |  | [1..n] | Entero positivo (TOTAL Ej. 3) |
| Cantidades por bulto | CONDICIONAL | Arreglo | Si bultos > 1: suma debe coincidir con cantidad total (tolerancia 6 decimales) | Numérico |
| Unidades por bulto | CONDICIONAL | Arreglo | Si bultos > 1: convertibles entre sí | Ref tabla Unidad medida |
| Fabricante | NO | Cuadro de búsqueda | No Conifarma | Ref -> Proveedores |
| País de origen | NO | Cuadro de búsqueda | Si vacío: DTO > Fabricante > Proveedor | Lista ISO países |
| Fecha de reanálisis | NO | Fecha | Si existe vencimiento: debe ser anterior a vencimiento | DD/MM/AAAA |
| Fecha de vencimiento | NO | Fecha | Si existe reanálisis: debe ser posterior a reanálisis | DD/MM/AAAA |
| Observaciones | NO | texto |  | Alfanumérico 300 caracteres |
| Número de remito/factura | NO | Texto | nroRemito campo en Lote | Alfanumérico, 30 caracteres |

**Validaciones Detalladas**

Ver tabla completa de 15 validaciones en [CU1_ALTA_INGRESO_COMPRA.md](./docs/cu/CU1_ALTA_INGRESO_COMPRA.md#validaciones)

1. **JSR-303 (12 validaciones):** @NotNull, @Positive, @Size, etc.
2. **Custom Business (3 validaciones):** cantidades, fechas, bultos
3. **Total:** 15 verificaciones ejecutadas antes de persistencia

**Generación Automática**

- **Código de lote:** `L-{codigoProducto}-{timestamp}` (formato: `yy.MM.dd_HH.mm.ss`)
- **Estado inicial:** NUEVO
- **Dictamen inicial:** RECIBIDO
- **Bultos:** Creados con numeración secuencial [1..n]
- **Movimiento:** ALTA/COMPRA asociado al lote

**Reglas de Negocio Implementadas**

1. **País de origen:** Prioridad DTO > Fabricante > Proveedor
2. **Bulto único:** Si bultosTotales = 1, cantidad se asigna al único bulto
3. **Múltiples bultos:** Suma convertida debe coincidir (tolerancia: 6 decimales)
4. **Unidad UNIDAD:** Debe ser entero y >= bultosTotales
5. **Conversiones:** Automáticas entre unidades compatibles (factor conversión)

**Flujos alternativos**

**Pre-condición Proveedor NO existe**

Escalar a **Supervisor/Analista** de planta para dar de alta el **Proveedor** (el aviso se dará fuera del sistema). Se deberá ejecutar el **CU30** previamente y luego el usuario vuelve a iniciar el Caso de Uso.

**Pre-condición Producto NO existe**

Escalar a **Gerencia Calidad** para dar de alta el **Producto** (el aviso se dará fuera del sistema). Se deberá ejecutar el **CU31** previamente y luego el usuario vuelve a iniciar el Caso de Uso.

**Post-condiciones**

1. **Lote creado:** Con código único, estado NUEVO, dictamen RECIBIDO
2. **Bultos creados:** De 1 a n bultos con numeración secuencial
3. **Movimiento creado:** Tipo ALTA, motivo COMPRA, activo=true
4. **DetalleMovimiento:** Uno por cada bulto, relacionado bidireccionalmente
5. **Inventario actualizado:** Cantidad inicial = cantidad actual
6. **Persistencia transaccional:** Lote, Bultos, Movimiento guardados en orden

**Ejemplo de Resultado**

```json
{
  "codigoLote": "L-API-001-25.01.15_10.30.45",
  "estado": "NUEVO",
  "dictamen": "RECIBIDO",
  "cantidadInicial": 100.0,
  "cantidadActual": 100.0,
  "unidadMedida": "KILOGRAMO",
  "bultosTotales": 3,
  "bultos": [
    {"nroBulto": 1, "cantidad": 50.0, "unidad": "KILOGRAMO"},
    {"nroBulto": 2, "cantidad": 30.0, "unidad": "KILOGRAMO"},
    {"nroBulto": 3, "cantidad": 20.0, "unidad": "KILOGRAMO"}
  ],
  "producto": {"id": 1, "codigo": "API-001", "nombre": "Paracetamol"},
  "proveedor": {"id": 5, "razonSocial": "Sigma-Aldrich", "pais": "Alemania"},
  "paisOrigen": "Alemania"
}
```

**Siguiente CU Permitido**

- CU2 (Cuarentena) - Flujo normal
- CU4 (Devolución Compra) - Si hay error
- CU28 (Ajuste) - Corrección
- CU29 (Reverso) - Anular ingreso

**Componentes Técnicos**

- **Service:** `AltaIngresoCompraService.java`
- **Controller:** `LoteController.java`
- **Utils:** `LoteEntityUtils.java`, `MovimientoAltaUtils.java`
- **Tests:** `AltaIngresoCompraServiceTest.java` (17 tests de integración)

**Deseable:** Se debe enviar mail con documentación escaneada para notificar a las partes interesadas, como el departamento de Calidad, ventas y el de producción, informándoles sobre la actualización del inventario. Estos avisos se manejan por fuera del sistema.

---

#

### CU2. Pasaje a Cuarentena

Este caso de uso se realiza cuando Control de Calidad le asigna un Nro de Analisis al Lote. También se puede iniciar automáticamente cuando Control de Calidad toma una muestra para iniciar un análisis.

**Pre-condiciónes**

El estado actual del producto debe ser con dictamen **Recibido, Aprobado, Analisis Expirado, Liberado, Devolucion Clientes** o **Retiro de Mercado**.

**Flujo del proceso**

**Usuario:** D \- Analista control de calidad

**Carga de datos**

| CAMPO | VALOR | VALIDACIÓN | FORMATO |
| :---- | :---- | :---- | :---- |
| Tipo de movimiento | MODIFICACION |  |  |
| Motivo | ANALISIS |  |  |
| Dictamen Inicial | Dictamen actual del lote | **IMPLEMENTADO** |  |
| Dictamen Final | Cuarentena | **IMPLEMENTADO** |  |
| Id\_Lote / Lote\_Proveedor /Codigo Interno/Nro Analisis | Todos los bultos del lote | OBLIGATORIO | Referencia a tabla Lote |
| N° de análisis | Nueva entrada a tabla Analisis | OBLIGATORIO | Alfanumérico, 30 caracteres |
| Fecha movimiento |  | OBLIGATORIO Default a la de hoy | DD/MM/AAAA |
| Observaciones |  |  | Alfanumérico 300 caracteres |

**Flujos alternativos**

Al ser una acción automática no se contemplan flujos alternativos.

**Post-condiciónes**

El inventario debe reflejar el ***cambio de dictamen*** de stock y debe existir el movimiento de **Modificación** asociado. **IMPLEMENTADO**: Se crea una nueva entrada en tabla Analisis con el nroAnalisis asignado.

**Deseable:** Se deben enviar por mail de forma externa al sistema notificaciones a las partes interesadas, informándoles del cambio en el dictamen.

---

###

### CU8. Reanalisis de Producto Aprobado

Este caso de uso se realiza cuando Control de Calidad le asigna un nuevo Nro de Análisis a un Lote aprobado ya que se acerca su fecha de reanálisis, pero no puede pasarlo a cuarentena debido a que se encuentra en uso.

**Pre-condiciónes**

El estado actual del producto debe ser con dictamen **Aprobado** y cuyo último **Análisis dictaminado** no contenga **fecha de vencimiento**.

**Flujo del proceso**

**Usuario:** C \- Gerente control de calidad

**Carga de datos**

| CAMPO | VALOR | VALIDACIÓN | FORMATO |
| :---- | :---- | :---- | :---- |
| Tipo de movimiento | MODIFICACION |  |  |
| Motivo | ANALISIS |  |  |
| Dictamen | Aprobado | **IMPLEMENTADO**: Sin cambio de dictamen |  |
| Id\_Lote / Lote\_Proveedor /Codigo Interno/Nro Analisis | Todos los bultos del lote | OBLIGATORIO | Referencia a tabla Lote |
| N° de análisis | Nueva entrada a tabla Analisis | OBLIGATORIO | Alfanumérico, 30 caracteres |
| Fecha movimiento |  | OBLIGATORIO Default a la de hoy | DD/MM/AAAA |
| Observaciones |  |  | Alfanumérico 300 caracteres |

**Pre-condición Lote Aprobado NO existe**

El sistema informa que no es posible realizar la acción requerida.

**Post-condiciónes**

El inventario debe reflejar el ***nuevo análisis en curso*** asociado al lote como último análisis activo. **IMPLEMENTADO**: Se crea nuevo registro en tabla Analisis sin cambiar el dictamen del lote.

---

### CU11. Anulación de Análisis en Curso

Este caso de uso se realiza cuando Control de Calidad necesita anular un análisis que está en curso, revirtiendo el lote a su dictamen anterior.

**Pre-condiciónes**

Debe existir un lote con un **Análisis en curso** (análisis activo con dictamen = null y fechaRealizado = null).

**Flujo del proceso**

**Usuario:** C \- Gerente control de calidad

**Carga de datos**

| CAMPO | VALOR | VALIDACIÓN | FORMATO |
| :---- | :---- | :---- | :---- |
| Tipo de movimiento | MODIFICACION |  |  |
| Motivo | ANULACION\_ANALISIS | **IMPLEMENTADO** |  |
| Dictamen Inicial | Cuarentena (dictamen actual) | **IMPLEMENTADO** |  |
| Dictamen Final | Dictamen anterior del lote | **IMPLEMENTADO**: recuperado del último movimiento |  |
| Id\_Lote / Nro Analisis | Lote con análisis en curso | OBLIGATORIO | Referencia a tabla Lote |
| Fecha movimiento |  | OBLIGATORIO Default a la de hoy | DD/MM/AAAA |
| Observaciones |  |  | Alfanumérico 300 caracteres |

**Post-condiciónes**

El inventario debe reflejar el cambio de dictamen al dictamen anterior. El Análisis en curso debe marcarse con dictamen = ANULADO y activo = false.

**NOTA IMPORTANTE: ANULADO vs CANCELADO**

El sistema distingue entre dos formas de cancelación de análisis en curso:

1. **ANULADO (CU11)**: Cancelación **manual por el usuario** mediante CU11
   - Usuario decide explícitamente cancelar el análisis
   - El lote revierte al dictamen anterior
   - Reversible mediante CU30

2. **CANCELADO (Automático)**: Cancelación **automática por el sistema** cuando otras operaciones hacen el análisis irrelevante
   - **CU4 (Devolución Compra)**: Lote devuelto completamente → análisis sin objeto
   - **CU7 (Consumo Producción)**: Stock = 0 → no hay material para analizar
   - **CU10 (Vencimiento)**: Producto vencido → análisis cancelado automáticamente
   - **CU22 (Venta)**: Stock = 0 → no hay material para analizar
   - **CU24 (Recall)**: Lote retirado del mercado → análisis sin sentido
   - **CU28 (Ajuste)**: Stock ajustado a 0 → no hay material para analizar
   - Reversible mediante reverso del CU origen (restaura análisis a estado "en curso")

Esta distinción permite rastrear si la cancelación fue por decisión del usuario o por imposibilidad del sistema de continuar con el análisis.

---

###

### CU3. Retiro por Muestreo

Este caso de uso ocurre cuando se toma una muestra para realizar un análisis de control de calidad por el departamento de QC.

**Pre-condiciónes**

Existencia de un lote en el inventario en estado **Distinto a Recibido** y con al menos **1 Nro de Analisis asociado.**

**Flujo del proceso**

**Usuario:** D \- Analista control de calidad

**Carga de datos**

| CAMPO | VALOR | VALIDACIÓN | FORMATO |
| :---- | :---- | :---- | :---- |
| Tipo de movimiento | BAJA |  |  |
| Motivo | MUESTREO |  |  |
| N° de análisis | Último asociado al bulto |  |  |
| Id Lote / Lote\_Proveedor+Nro\_bulto |  | OBLIGATORIO | Referencia a tabla Lote |
| Cantidad |  | OBLIGATORIO  \[1…Lote.cantActual\] \< a % de Lote\>cantActual | Numérico, flotante |
| Unidad de medida |  | OBLIGATORIO Default el del producto | Subunidades |
| Identificación del bulto |  |  | Lista de opciones |
| Nro traza / Rango traza | **IMPLEMENTADO**: Para productos trazados (Unidad de Venta) | Selección de trazas específicas | Campo trazaDTOs |
| Fecha movimiento |  | OBLIGATORIO Default a la de hoy | DD/MM/AAAA |
| Observaciones |  |  | Alfanumérico 300 caracteres |

**IMPLEMENTACIÓN REAL**:
- Para productos **NO trazados**: se descuenta cantidad del bulto y del lote
- Para productos **trazados** (Unidad de Venta):
  - Se descuenta cantidad del bulto específico (cantidadActual)
  - Se descuenta cantidad del lote (cantidadActual)
  - Se marca cada traza individual como CONSUMIDO (estado terminal)
  - **Sistema dual**: actualiza tanto cantidades como estados de trazas

**Flujos alternativos**

**Pre-condición Lote Recibido o en Cuarentena NO existe**

El sistema informa que no es posible realizar la acción requerida.

**Post-condiciónes**

El inventario debe reflejar la **baja** de stock y debe existir el movimiento de **Baja** asociado.

---

#

### CU4. Devolucion Compra

Este caso de uso ocurre cuando materiales externos son devueltos al proveedor.

**Pre-condiciónes**

Existencia de un lote en el inventario de productos del tipo **Api**, **Excipiente**, **Cápsula**, **Acond. Primario** o **Acond. Secundario.** Y con dictamen **Recibido**, **Cuarentena, Aprobado** o **Rechazado**.

**Flujo del proceso**

**Usuario:** B \- Analista de planta

**Carga de Datos**

| CAMPO | VALOR | VALIDACIÓN e INFO | FORMATO |
| ----- | ----- | ----- | ----- |
| Tipo de movimiento | BAJA |  |  |
| Motivo | DEVOLUCION COMPRA |  |  |
| Id Lote / Lote\_Proveedor+Nro\_bulto |  | OBLIGATORIO | Referencia a tabla Lote |
| Cantidad |  | OBLIGATORIO  \[1…Lote\>cantActual\] \< a % de Lote\>cantActual | Numérico, flotante |
| Unidad de medida |  | OBLIGATORIO Default el del producto | Medidas convertibles al default del producto |
| Identificación del bulto |  |  |  |
| Fecha movimiento |  | OBLIGATORIO Default a la de hoy | DD/MM/AAAA |
| Observaciones |  | OBLIGATORIO | Texto |

**Flujos alternativos**

**Pre-condición Lote NO existe**

El sistema informa que no es posible realizar la acción requerida.

**Post-condiciónes**

El inventario debe reflejar la **baja** de stock y debe existir el movimiento de **Baja** asociado.

**Tratamiento para bultos:** En el caso que existieran más de un bulto, el sistema solicitará ingresar las cantidades individuales para cada bulto, bultos completos o devolución completa. Esto afectará a cada bulto independientemente o a todo el lote, respectivamente.

**Deseable:** Se debe enviar mail con documentación escaneada para notificar a las partes interesadas, como el departamento Técnico, Administración, Producción, etc. Estos avisos se manejan por fuera del sistema.

---

##

## ANÁLISIS

### CU5. Resultado QC: Aprobado

Este caso de uso se realiza cuando Control de Calidad aprueba el análisis de la muestra tomada.

**Pre-condiciónes**

Existencia de un lote en el inventario con dictamen **Cuarentena** y con un **Análisis en Curso**.

**Validaciones de fecha**

1\) Ingreso sin Ambas fechas:

	Ambas fechas las defino yo via Analisis de Calidad \-\> reanálisis, y 1 de vto

2\) Ingreso con Fecha de Reanálisis del proveedor

La fecha de realizado mi 1er Análisis debe ser anterior a esta fecha, y la fecha de reanálisis del resultado tiene que ser una fecha mas proxima a esa.

		\- Resultado del 1er analisis \-\> Fecha reanálisis Conifarma \<= Fecha de reanalisis del Proveedor

La fecha de realizado mi 1er Análisis debe ser anterior a esta fecha, y la fecha de reanálisis del resultado tiene que ser una fecha más próxima a esa.

\- Resultado a partir del 2do análisis (o 1er reanálisis) \-\> Fecha de reanálisis Conifarma \<= Vencimiento / Independiente a la fecha del Proveedor

3\) Fecha de Vencimiento Proveedor

	Cuando QC define la fecha de vencimiento, esta va a ser menor o igual que la del proveedor.

**Flujo Principal**

**Usuario:** C \- Gerente control de calidad

**Carga de datos**

| CAMPO | VALOR | VALIDACIÓN | FORMATO |
| :---- | :---- | :---- | :---- |
| Tipo de movimiento | MODIFICACION |  |  |
| Motivo | RESULTADO\_ANALISIS | **IMPLEMENTADO** |  |
| Dictamen Inicial | Cuarentena | **IMPLEMENTADO** |  |
| Dictamen Final | APROBADO | **IMPLEMENTADO** |  |
| Id\_Lote /  Nº Analisis | Todos los bultos del lote | OBLIGATORIO | Referencia a tabla Lote |
| Fecha de reanalisis |    Al menos 1 debe estar completo | No menor a X tiempo  | DD/MM/AAAA |
| Fecha de vencimiento |  | No menor a X tiempo | DD/MM/AAAA |
| Fecha movimiento |  | OBLIGATORIO Default a la de hoy | DD/MM/AAAA |
| Titulo |  | OBLIGATORIO | % (decimal con max 100) |
| Observaciones |  |  | Alfanumérico 300 caracteres |

**Flujos alternativos**

**Pre-condición Lote NO existe**

El sistema informa que no es posible realizar la acción requerida.

**Post-condiciónes**

El inventario debe reflejar el ***cambio de dictamen*** de stock, el **Análisis** debe actualizarse con dictamen=APROBADO, fechaRealizado, fechaReanalisis, fechaVencimiento y titulo, y debe existir el movimiento de **Modificación** asociado.

**IMPLEMENTACIÓN**: El estado del lote cambia a DISPONIBLE cuando se aprueba el análisis.

---

#

### CU6. Resultado QC: Rechazado

Este caso de uso se realiza cuando Control de Calidad **NO aprueba** el análisis de la muestra tomada.

**Pre-condiciónes**

Existencia de un lote en el inventario con dictamen **Cuarentena** y con un **Análisis en Curso**.

**Flujo Principal**

**Usuario:** C \- Gerente control de calidad

**Carga de datos**

| CAMPO | VALOR | VALIDACIÓN | FORMATO |
| :---- | :---- | :---- | :---- |
| Tipo de movimiento | MODIFICACION |  |  |
| Motivo | RESULTADO\_ANALISIS | **IMPLEMENTADO** |  |
| Dictamen Inicial | Cuarentena | **IMPLEMENTADO** |  |
| Dictamen Final | RECHAZADO | **IMPLEMENTADO** |  |
| Id\_Lote / Nº Analisis | Todos los bultos del lote | OBLIGATORIO | Referencia a tabla Lote |
| Fecha movimiento |  | OBLIGATORIO Default a la de hoy | DD/MM/AAAA |
| Observaciones |  |  | Alfanumérico 300 caracteres |

**Flujos alternativos**

**Pre-condición Lote NO existe**

El sistema informa que no es posible realizar la acción requerida.

**Post-condiciónes**

El inventario debe reflejar el ***cambio de dictamen*** de stock, el **Análisis** debe actualizarse con dictamen=RECHAZADO y fechaRealizado, y debe existir el movimiento de **Modificación** asociado.

**Deseable:** Se deben enviar por mail de forma externa al sistema notificaciones a las partes interesadas, informándoles del cambio en el dictamen. El aviso por mail se dará fuera del sistema.

---

#

## PRODUCCION

### CU7. Consumo de Producción

Este caso de uso sucede cuando un material es retirado para su utilización en un proceso productivo.

**Pre-condiciónes:**

Existencia de un lote con **existencias** en el inventario con dictamen **Aprobado** y cuya fecha de Análisis/Reanálisis no sea menor a X.

**IMPLEMENTACIÓN**: El estado del lote debe ser DISPONIBLE.

**Flujo principal**

**Usuario:** A \- Jefe de planta

**Carga de Datos**

| CAMPO | VALOR | VALIDACIÓN e INFO | FORMATO |
| ----- | ----- | ----- | ----- |
| Tipo de movimiento | BAJA |  |  |
| Motivo | CONSUMO\_PRODUCCION | **IMPLEMENTADO** |  |
| Id Lote / Nro Analisis |  | OBLIGATORIO | Referencia a tabla Lote |
| Cantidad | No \> a lote.cantActual. En caso de bultos, el sistema pedirá especificar cantidades x bulto. | OBLIGATORIO  No negativo No cero No vacio | Numérico, flotante |
| Unidad de medida | Subunidades / bulto | OBLIGATORIO Default el del producto | Medidas convertibles al default del producto |
| Orden de elaboración | Definido por Conifarma | OBLIGATORIO | Alfanumérico, 50 caracteres |
| Fecha movimiento |  | OBLIGATORIO Default a la de hoy | DD/MM/AAAA |
| Observaciones |  | OBLIGATORIO | Texto |

**Flujos alternativos**

**Pre-condición Lote NO existe**

El sistema informa que no es posible realizar la acción requerida.

**Post-condiciónes**

El inventario debe reflejar la baja de stock y debe existir el movimiento de Baja asociado.

**IMPLEMENTACIÓN**:
- Cuando la cantidad del bulto llega a 0, el estado del bulto cambia a CONSUMIDO
- Cuando todos los bultos están CONSUMIDOS, el estado del lote cambia a CONSUMIDO

**Tratamiento para bultos:** En el caso que existieran más de un bulto, el sistema solicitará ingresar las cantidades individuales para cada bulto.Esto resultará en un movimiento independiente en el inventario por cada bulto seleccionado.

---

### CU9. Fecha Reanálisis Alcanzada (**Automatico**)

Esto sucede cuando se cumple la fecha de reanálisis.

**Pre-condiciónes**

**Acción automática:** Un proceso programado se ejecuta periódicamente (diariamente a las 5 AM, por ejemplo) para revisar las **Fecha de Reanálisis dictaminados** de todos los lotes.

Para todo lote en el inventario con dictamen **Aprobado,**  cuya última **Fecha de Reanálisis dictaminado** haya sido alcanzada y no sea SemiElaborado o Unidad de venta. O en su defecto se compara contra la fecha de reanálisis del proveedor. Si existiera un **Análisis en curso** asociado al lote, el mismo pasará directamente a **Cuarentena**.

**NOTA IMPLEMENTACIÓN**: Existe un error en la lógica de comparación de fechas en FechaValidatorService. La condición está invertida: `if (fechaRe.isBefore(hoy))` debería ser `if (!fechaRe.isBefore(hoy) && !fechaRe.isAfter(hoy))` o `if (fechaRe.isEqual(hoy) || fechaRe.isBefore(hoy))`.

**Flujo del proceso**

**Usuario:** Automatico

**Carga de datos**

| CAMPO | VALOR | VALIDACIÓN | FORMATO |
| :---- | :---- | :---- | :---- |
| Tipo de movimiento | MODIFICACION |  |  |
| Motivo | EXPIRACION\_ANALISIS | **IMPLEMENTADO** |  |
| Dictamen Inicial | APROBADO | **IMPLEMENTADO** |  |
| Dictamen Final | ANALISIS\_EXPIRADO | **IMPLEMENTADO** |  |
| Id\_Lote / Lote\_Proveedor | Todos los bultos del lote | OBLIGATORIO | Referencia a tabla Lote |
| Fecha movimiento |  | OBLIGATORIO Default a la de hoy | DD/MM/AAAA |

**Flujos alternativos**

Al ser una acción automática no se contemplan flujos alternativos.

**Post-condiciónes**

El inventario debe reflejar el ***cambio de dictamen*** de stock y debe existir el movimiento de **Modificación** asociado.

**IMPLEMENTADO - No Reversibilidad:**
Las operaciones de CU9 (Análisis Expirado) **NO SON REVERSIBLES** mediante CU29/CU30/CU31, ya que son operaciones automáticas controladas por fechas. El análisis debe rehacerse mediante CU2 → CU3 → CU5/6.

**Deseable:** Se deben enviar por mail de forma externa al sistema notificaciones a las partes interesadas, informándoles del cambio en el dictamen.**Umbral de Advertencia (Opcional):** Se puede establecer un período de tiempo previo a la fecha de expiración para generar alertas de advertencia. Por ejemplo, 30 días antes del vencimiento.

---

### CU10. Vencimiento de Stock

Esto sucede cuando se cumple la fecha de vencimiento.

**Pre-condiciónes**

**Acción automática:** Un proceso programado se ejecuta periódicamente (diariamente a las 5 AM, por ejemplo) para revisar las **Fecha de Vencimiento** de todos los lotes.

Para todo lote en el inventario cuya **Fecha de Vencimiento** haya sido alcanzada. O en su defecto la fecha de vencimiento del proveedor.

**NOTA IMPLEMENTACIÓN**: Existe el mismo error en la lógica de comparación de fechas que en CU9.

**Flujo del proceso**

**Usuario:** Automatico

**Carga de datos**

| CAMPO | VALOR | VALIDACIÓN | FORMATO |
| :---- | :---- | :---- | :---- |
| Tipo de movimiento | MODIFICACION |  |  |
| Motivo | VENCIMIENTO |  |  |
| Dictamen Inicial | Dictamen actual | **IMPLEMENTADO** |  |
| Dictamen Final | VENCIDO | **IMPLEMENTADO** |  |
| Id\_Lote / Lote\_Proveedor | Todos los bultos del lote | OBLIGATORIO | Referencia a tabla Lote |
| Fecha movimiento |  | OBLIGATORIO Default a la de hoy | DD/MM/AAAA |

**Flujos alternativos**

Al ser una acción automática no se contemplan flujos alternativos.

**Post-condiciónes**

El inventario debe reflejar el ***cambio de dictamen*** de stock y debe existir el movimiento de **Modificación** asociado.

**IMPLEMENTADO - Cancelación de Análisis en Curso:**
Si el lote tiene un análisis en curso (dictamen=null) al momento del vencimiento, el análisis se marca automáticamente como **CANCELADO** porque el producto vencido no puede ser analizado.

**IMPLEMENTADO - No Reversibilidad:**
Las operaciones de CU10 (Vencimiento) **NO SON REVERSIBLES** mediante CU29/CU30/CU31, ya que son operaciones automáticas controladas por fechas. Una vez que un producto está vencido, no tiene sentido revertir esta operación.

**Deseable:** Se deben enviar por mail de forma externa al sistema notificaciones a las partes interesadas, informándoles del cambio en el dictamen.**Umbral de Advertencia (Opcional):** Se puede establecer un período de tiempo previo a la fecha de expiración para generar alertas de advertencia. Por ejemplo, 30 días antes del vencimiento.

---

##

## PRODUCTO PROPIO

### CU20. Ingreso de stock por Producción Interna

Este caso de uso ocurre cuando se ingresan materiales cuyo origen es un proceso productivo de Conifarma.

**Pre-condiciónes**

Los productos que se ingresarán deben estar configurados en el sistema con toda la información necesaria (códigos, descripciones, unidad de medida, etc.).

Si no se cumple, ejecutar el flujo alternativo correspondiente (ver más abajo).

**Flujo del proceso**

**Usuario:** A \- Jefe de planta

**Carga de Datos**

| CAMPO | OBLIGATORIO | VALOR | VALIDACIÓN e INFO | FORMATO |
| ----- | ----- | ----- | ----- | ----- |
| Tipo de movimiento |  | ALTA |  |  |
| Motivo |  | PRODUCCION\_PROPIA | **IMPLEMENTADO** |  |
| Dictamen |  | CUARENTENA |  |  |
| Estado |  | NUEVO | **IMPLEMENTADO** |  |
| Proveedor | SI |  | Conifarma | Ref \-\> Proveedores |
| Fecha de ingreso | SI | Fecha | *No superior a hoy*  | DD/MM/AAAA |
| ID Producto (Código) | SI | Cuadro de búsqueda | *SemiElab/U. D Vta* | Referencia a tabla Producto |
| Cantidad  | SI |  | No negativo No cero No vacio | Numérico, flotante (según unidad de Medida) |
| Unidad de medida | SI | Default el del producto | Convertible a U. de Medida default del producto | Ref tabla Unidad medida |
| Lote proveedor | SI |  | Definido por Conifarma | Texto |
| Orden de elaboración |  |  | Definido por Conifarma | Texto |
| Detalle de conservación |  |  |  | Texto |
| Nro de traza inicial  |  | N: rango final \-\> \[n, cant unidades\]  | No solapado producto | Text/Nro |
| Numero de bulto | SI |  | \[1..cant bultos totales\] | Entero positivo |
| Cantidad de bultos totales | SI |  | \[1..n\] | Entero positivo |
| Observaciones |  | texto |  | Alfanumérico 300 caracteres |

**IMPLEMENTACIÓN REAL**:
- El sistema genera el código de lote con formato: `L-{productoCode}-{timestamp}`
- Para productos trazados (Unidad de Venta): se crean registros en tabla Traza con nroTraza secuencial
- Campo `trazado` en Lote determina si el producto requiere trazabilidad unitaria

**Flujos alternativos**

**Pre-condición Producto NO existe**

Escalar a **Gerencia Calidad** para dar de alta el **Producto** (el aviso se dará fuera del sistema). Se deberá ejecutar el **CU31** previamente y luego el usuario vuelve a iniciar el Caso de Uso.

**Post-condiciónes**

El inventario debe reflejar el **alta** de stock y debe existir el movimiento de **Alta** asociado.

**Tratamiento para bultos:** En el caso que existieran más de un bulto, el sistema solicitará ingresar las cantidades individuales para cada bulto. Esto resultará en tantas entradas independientes en el inventario como número de bultos. Estas entradas pertenecen a una misma partida y son iguales en todos sus datos menos en **Nro de bultos**. Es decir, el alta generará tantos lotes como número de bultos se ingresen.

**IMPLEMENTACIÓN REAL**: Como en CU1, se crea UN SOLO LOTE con múltiples BULTOS (no múltiples lotes).

**Deseable:** Se debe enviar mail con documentación escaneada para notificar a las partes interesadas, como el departamento de Calidad, ventas y el de producción, informándoles sobre la actualización del inventario. Estos avisos se manejan por fuera del sistema.

---

#

## LIBERACION

### CU21. Liberación Unidad de venta

En este caso de uso se aprueba la liberación de un producto para su venta.

**Pre-condiciónes**

Existencia de un lote en el inventario de Unidad de Venta con dictamen **Aprobado** y estado **DISPONIBLE**.

**Flujo Principal**

**Usuario::** E \- Gerente garantía de calidad; F \- DT

**Carga de datos**

| CAMPO | VALOR | VALIDACIÓN | FORMATO |
| :---- | :---- | :---- | :---- |
| Tipo de movimiento | MODIFICACION |  |  |
| Motivo | LIBERACION | **IMPLEMENTADO** |  |
| Dictamen Inicial | APROBADO | **IMPLEMENTADO** |  |
| Dictamen Final | LIBERADO | **IMPLEMENTADO** |  |
| Id\_Lote / Lote\_Proveedor / Nº Analisis | Todos los bultos del lote | OBLIGATORIO | Referencia a tabla Lote |
| Fecha movimiento |  | OBLIGATORIO Default a la de hoy | DD/MM/AAAA |
| Observaciones |  |  | Alfanumérico 300 caracteres |

**Flujos alternativos**

**Pre-condición Lote NO existe**

El sistema informa que no es posible realizar la acción requerida.

**Post-condiciónes**

El inventario debe reflejar el ***cambio de dictamen*** de stock y debe existir el movimiento de **Modificación** asociado.

**Deseable:** Se deben enviar por mail de forma externa al sistema notificaciones a las partes interesadas, informándoles del cambio en el dictamen. El aviso por mail se dará fuera del sistema.

---

#

## VENTA

### CU22. Venta de Producto Propio

Este caso sucede cuando se registra un egreso de producto terminado por venta.

**Pre-condiciónes:**

Existencia de un lote en el inventario con dictamen **Liberado** y cuya fecha de Vencimiento no sea menor a X.

**IMPLEMENTACIÓN**: El estado del lote debe ser DISPONIBLE.

**Flujo principal**

**Usuario:** B \- Analista de planta

**Carga de Datos**

| CAMPO | VALOR | VALIDACIÓN e INFO | FORMATO |
| ----- | ----- | ----- | ----- |
| Tipo de movimiento | BAJA |  |  |
| Motivo | VENTA |  |  |
| Id Lote / Lote\_Proveedor / Nro Analisis |  | OBLIGATORIO | Referencia a tabla Lote |
| Cantidad | No \> a NroAnalisis.cantActual. En caso de bultos, el sistema pedirá especificar cantidades x bulto. | OBLIGATORIO  No negativo No cero No vacio | Numérico, flotante |
| Nro traza / Rango traza | **IMPLEMENTADO**: Para Unidad de Venta trazada | Selección de trazas específicas | Campo trazaDTOs |
| Unidad de medida |  | OBLIGATORIO Default el del producto | Medidas convertibles al default del producto |
| Orden de compra |  | OBLIGATORIO Definido por Conifarma | Alfanumérico, 50 caracteres |
| Fecha movimiento |  | OBLIGATORIO Default a la de hoy | DD/MM/AAAA |
| Observaciones |  | OBLIGATORIO | Texto |

**IMPLEMENTACIÓN REAL**:
- Para productos **NO trazados**: se descuenta cantidad del bulto y del lote
- Para productos **trazados** (Unidad de Venta):
  - Se descuenta cantidad del bulto específico (cantidadActual)
  - Se descuenta cantidad del lote (cantidadActual)
  - Cada traza vendida cambia estado a VENDIDO
  - **Sistema dual**: actualiza tanto cantidades como estados de trazas
- Cuando cantidadActual de un bulto llega a cero, el bulto cambia estado a CONSUMIDO
- Cuando todos los bultos están CONSUMIDOS, el lote cambia estado a CONSUMIDO

**Flujos alternativos**

**Pre-condición Lote NO existe**

El sistema informa que no es posible realizar la acción requerida.

**Post-condiciónes**

El inventario debe reflejar la baja de stock y debe existir el movimiento de Baja asociado.

**Tratamiento para bultos:** En el caso que existieran más de un bulto, el sistema intentará definir si es venta total, caso contrario solicitará ingresar las cantidades individuales para cada bulto.Esto resultará en un movimiento independiente en el inventario por cada bulto seleccionado.

---

#

## CONTINGENCIAS

### CU23. Devolución de cliente

Este caso de uso ocurre cuando un cliente realiza una devolución de una venta de Conifarma.

**Pre-condiciónes**

Debe existir el movimiento de Baja por Venta asociado y el lote de origen.

**IMPLEMENTACIÓN**: El sistema busca el movimiento de venta por codigoMovimientoOrigen.

**Flujo del proceso**

**Usuario:** E \- Gerente garantía de calidad

**Carga de Datos**

| CAMPO | OBLIGATORIO | VALOR | VALIDACIÓN e INFO | FORMATO |
| ----- | ----- | ----- | ----- | ----- |
| Tipo de movimiento |  | ALTA |  |  |
| Motivo |  | DEVOLUCION\_VENTA | **IMPLEMENTADO** |  |
| Dictamen |  | DEVOLUCION\_CLIENTES | **IMPLEMENTADO** |  |
| Estado |  | DEVUELTO | **IMPLEMENTADO** |  |
| Lote Origen |  | Ref Movimiento seleccionado |  |  |
| Fecha de ingreso | SI | Fecha | *No superior a hoy*  | DD/MM/AAAA |
| Id\_Lote / Lote\_Proveedor / ID Movimiento relacionado | SI | Cuadro de búsqueda | *Movimiento de Vta* | Referencia a tabla Movimientos |
| Cantidad  | SI |  | No negativo No cero No vacio | Numérico, flotante (según unidad de Medida) |
| Nro traza / Rango traza | **IMPLEMENTADO**: Para Unidad de Venta trazada | Trazas devueltas | Campo trazaDTOs |
| Unidad de medida | SI | Default el del producto | Convertible a U. de Medida default del producto | Ref tabla Unidad medida |
| Numero de bulto | SI |  | \[1..cant bultos totales\] | Entero positivo |
| Observaciones |  | texto |  | Alfanumérico 300 caracteres |

**IMPLEMENTACIÓN REAL**:
- Se crea un **NUEVO LOTE** con dictamen DEVOLUCION_CLIENTES y estado DEVUELTO
- El nuevo lote referencia al lote original mediante campo loteOrigen
- El código del nuevo lote tiene formato: `{codigoLoteOriginal}_D_{secuencia}`
- Para productos trazados: las trazas devueltas se reasignan al nuevo lote con estado DEVUELTO

**Flujos alternativos**

**Pre-condición Movimiento NO existe**

El sistema informa que no es posible realizar la acción requerida.

**Post-condiciónes**

El inventario debe reflejar el **alta** de stock y debe existir el movimiento de **Alta** asociado.

**Tratamiento para bultos:** En el caso que existieran más de un bulto, el sistema solicitará ingresar las cantidades individuales para cada bulto. Esto resultará en tantas entradas independientes en el inventario como número de bultos. Estas entradas pertenecen a una misma partida y son iguales en todos sus datos menos en **Nro de bultos**. Es decir, el alta generará tantos lotes como número de bultos se ingresen.

**Deseable:** Se envían notificaciones de forma externa al sistema al cliente y a los departamentos correspondientes sobre el estado de la devolución. Estos avisos se manejan por fuera del sistema.

---

#

### CU24. Retiro de mercado

Este caso de uso ocurre cuando se realiza un retiro de un lote de producción propia del mercado (market recall).

**IMPLEMENTACIÓN**: Sistema dual-lot para cumplir requerimientos regulatorios (FDA/ANVISA):
1. **Modifica** el lote original marcándolo como RECALL
2. **Crea** un nuevo lote para las unidades devueltas con dictamen RETIRO_MERCADO

**JUSTIFICACIÓN DEL SISTEMA DUAL-LOT**:

El sistema implementa la creación de lotes independientes para cada grupo de unidades devueltas (en lugar de reintegrarlas al lote original) debido a:

1. **Asignación de número de análisis independiente**: Cada grupo de unidades puede ingresar físicamente al depósito en momentos distintos, lo que puede requerir un nuevo número de análisis específico para ese subgrupo. El sistema regulatorio (FDA/ANVISA) exige trazabilidad analítica independiente.

2. **Marcación de inventario en depósito como RECALL**: Cuando se inicia un retiro de mercado, todas las unidades que aún estaban en depósito disponibles para venta deben marcarse como RECALL, ya que no están disponibles para su distribución debido a la acción de retiro de mercado.

3. **Doble impacto del recall**:
   - Productos vendidos que son devueltos → se reciben en nuevo lote con dictamen RETIRO_MERCADO
   - Productos en depósito no vendidos → se marcan como RECALL (inhabilitados para distribución)

**Pre-condiciónes**

Debe existir al menos un movimiento de Baja por Venta asociado a un lote de Unidad de Venta.

**Flujo del proceso**

**Usuario:** E \- Gerente garantía de calidad

**Carga de Datos**

| CAMPO | OBLIGATORIO | VALOR | VALIDACIÓN e INFO | FORMATO |
| ----- | ----- | ----- | ----- | ----- |
| Tipo de movimiento |  | ALTA + MODIFICACION | **IMPLEMENTADO**: Operación dual |  |
| Motivo |  | RETIRO\_MERCADO | **IMPLEMENTADO** |  |
| Dictamen |  | RETIRO\_MERCADO | **IMPLEMENTADO** |  |
| Estado |  | RECALL | **IMPLEMENTADO** |  |
| Lote Origen |  | Ref Movimiento seleccionado |  |  |
| Fecha de ingreso | SI | Fecha | *No superior a hoy*  | DD/MM/AAAA |
| Id\_Lote / Lote\_Proveedor / ID Movimiento relacionado  | SI | Todos los bultos del lote | *Existe al menos 1 Movimiento de Vta* | Referencia a tabla Movimientos/ Lotes |
| Nro traza / Rango traza | **IMPLEMENTADO**: Para Unidad de Venta trazada | Trazas devueltas/afectadas | Campo trazaDTOs |
| Cantidad  | **IMPLEMENTADO**: Para no trazados | Cantidad devuelta | Campo detalleMovimientoDTOs |
| Observaciones |  | texto |  | Alfanumérico 300 caracteres |

**IMPLEMENTACIÓN REAL - Sistema Dual-Lot**:

**Fase 1 - ALTA (nuevo lote recall):**
- Se crea **NUEVO LOTE** con código: `{codigoLoteOriginal}_R_{secuencia}`
- Dictamen: RETIRO_MERCADO
- Estado: RECALL
- Para **trazados**: se crean bultos y se reasignan trazas devueltas con estado RECALL
- Para **no trazados**: se crean bultos con cantidades devueltas según detalleMovimientoDTOs
- El lote referencia al original mediante loteOrigen

**Fase 2 - MODIFICACION (lote original):**
- Si el lote original NO estaba en RECALL:
  - Para **trazados**: marca como RECALL todas las trazas DISPONIBLES del lote original
  - Para **no trazados**: marca bultos como RECALL si tienen cantidadActual > 0
  - Cambia dictamen del lote original a RECALL
  - Cambia estado del lote original a RECALL
  - Si el ultimoAnalisis no tiene dictamen, lo marca como ANULADO
- Si el lote ya estaba en RECALL: no se modifica (solo se procesa el alta)

**Flujos alternativos**

**Pre-condición Movimiento NO existe**

El sistema informa que no es posible realizar la acción requerida.

**Post-condiciónes**

El inventario debe reflejar:
1. **ALTA** de nuevo lote con unidades devueltas (dictamen RETIRO_MERCADO)
2. **MODIFICACION** del lote original a estado RECALL (si no lo estaba ya)
3. Dos movimientos asociados: uno de ALTA para el nuevo lote y uno de MODIFICACION para el original

**Tratamiento para bultos:**
- Para **trazados**: El sistema agrupa las trazas por bulto y crea un bulto en el nuevo lote por cada grupo
- Para **no trazados**: El sistema crea bultos según las cantidades especificadas en detalleMovimientoDTOs

**NOTA IMPORTANTE**: No es posible revertir (reverso) una operación de CU24 debido a la complejidad del sistema dual-lot.

**Deseable:** Se envían notificaciones de forma externa al sistema al cliente y a los departamentos correspondientes sobre el estado de la devolución. Estos avisos se manejan por fuera del sistema.

---

#

### CU28. Ajuste de Inventario

Este caso de uso ocurre cuando se necesita reflejar un cambio en el inventario físico. El mismo se puede dar por errores en el ingreso de información al sistema, pérdidas, daños o bajas físicas de otra índole.

**NOTA**: El documento original especificaba CU25, pero en la implementación real este es **CU28**.

**Pre-condiciónes**

Existencia de un lote en el inventario.

**Flujo del proceso**

**Usuario:** A \- Jefe de planta; B \- Analista de planta**;**

**Carga de datos**

| CAMPO | VALOR | VALIDACIÓN | FORMATO |
| :---- | :---- | :---- | :---- |
| Motivo | AJUSTE |  |  |
| Tipo de movimiento | ALTA / BAJA |  |  |
| Id Lote / Lote\_Proveedor+Nro\_bulto |  | OBLIGATORIO | Referencia a tabla Lote |
| Cantidad |  | OBLIGATORIO  \<= cant.Original (salvo Recibido/Prod Propia)\>= cant.Actual | Numérico, flotante |
| Unidad de medida |  | OBLIGATORIO Default el del producto | Medidas convertibles al default del producto |
| Identificación del bulto |  |  | Lista de opciones |
| Fecha movimiento |  | OBLIGATORIO Default a la de hoy | DD/MM/AAAA |
| Observaciones |  |  | Alfanumérico 300 caracteres |

**Flujos alternativos**

**Pre-condición Lote NO existe**

El sistema informa que no es posible realizar la acción requerida.

**Post-condiciónes**

El inventario debe reflejar la **alta**/**baja** de stock y debe existir el movimiento de **Alta** o **Baja** asociado.

La validaciones de cantidades máximas y mínimas serán distintas para productos en dictamen Recibido o Producto propio en Cuarentena que para el resto de los productos.

**En caso de necesitar agregar bultos. El procedimiento es hacer un Ajuste de Baja total y un nuevo ingreso de Stock.**

**Deseable:** En algunos casos, se envían de forma externa alertas o notificaciones a los responsables para informar sobre los ajustes realizados.

---

### CU29-31. Reverso de movimiento

Se necesita anular el último movimiento por errores detectados en la carga de datos.

**IMPLEMENTACIÓN**: El sistema implementa tres CUs separados por tipo de movimiento:
- **CU29**: Reverso de movimientos tipo ALTA
- **CU30**: Reverso de movimientos tipo BAJA
- **CU31**: Reverso de movimientos tipo MODIFICACION

**Usuario: Pre-condiciónes**

Debe existir un movimiento anulable, eso es que el movimiento sea el último realizado sobre un lote. No es posible anular movimientos anteriores o intermedio en el flujo de stock.

**RESTRICCIONES IMPLEMENTADAS**:
- No se puede reversar CU24 (Retiro de Mercado) debido a la complejidad del sistema dual-lot
- No se puede reversar movimientos que no sean el último del lote
- El movimiento a reversar debe estar activo=true

**Flujo del proceso**

**Usuario:** A \- Jefe de planta; B \- Analista de planta

**Carga de Datos**

**Selección desde Lista:**

Solo se podrá seleccionar el movimiento a anular desde una lista filtrada de opciones previamente cargadas, la cual mostrará la mayor cantidad de información relevante (por ejemplo: id\_lote, fecha de ingreso, producto, proveedor, cantidad, etc.).

Se confirma la anulación.

**IMPLEMENTACIÓN - Lógica por Motivo**:

El sistema utiliza un switch por MotivoEnum para aplicar la lógica de reverso específica:

**CU29 - Reverso ALTA**:
- COMPRA: marca lote como inactivo
- PRODUCCION_PROPIA: marca lote como inactivo
- DEVOLUCION_VENTA: marca lote como inactivo
- AJUSTE: revierte ajuste de alta

**CU30 - Reverso BAJA**:
- MUESTREO: restaura cantidad/trazas al bulto
- VENTA: restaura cantidad/trazas, cambia estado de trazas de VENDIDO a DISPONIBLE
- CONSUMO_PRODUCCION: restaura cantidad, cambia estado bultos de CONSUMIDO a EN_USO
- DEVOLUCION_COMPRA: restaura cantidad
- AJUSTE: revierte ajuste de baja

**CU31 - Reverso MODIFICACION**:
- ANALISIS: revierte dictamen a dictamenInicial del movimiento
- RESULTADO_ANALISIS: revierte análisis y dictamen del lote
- ANULACION_ANALISIS: revierte anulación, restaura análisis
- LIBERACION: revierte de LIBERADO a APROBADO
- EXPIRACION_ANALISIS: revierte de ANALISIS_EXPIRADO a APROBADO
- VENCIMIENTO: revierte de VENCIDO al dictamen anterior

**Flujos alternativos**

**Pre-condición Movimiento NO existe**

El sistema informa que no es posible realizar la acción requerida.

**Post-condiciónes**

El inventario debe quedar en el estado previo al movimiento anulado. Al movimiento se le hara un borrado logico (activo=false). Si el movimiento era de ingreso de stock (ALTA), el lote tambien quedara en estado inactivo.

**IMPLEMENTACIÓN**: Se crea un nuevo movimiento con motivo=REVERSO que documenta la operación de reverso para auditoría.

### **Tratamiento para bultos:** En el caso que existieran más de un bulto, el sistema aplicará la anulación a todos los bultos del lote y todos los movimientos respectivos a los mismos

---

###

### CU30. ABM de Proveedor

Se agrega un nuevo proveedor

**Flujo del proceso**

**Usuario:** B \- Analista de planta

**NOTA**: Este es un ABM de tabla maestra, no implementado como servicio CU. Se gestiona mediante controllers REST estándar.

---

### CU31. ABM de material o producto

Se agrega un nuevo ítem a la lista de productos.

**Flujo del proceso**

**Usuario:** C \- Gerente control de calidad

**NOTA**: Este es un ABM de tabla maestra, no implementado como servicio CU. Se gestiona mediante controllers REST estándar.

---

### CU32. Configuración del sistema

Se modifican datos de configuracion del sistema (A DEFINIR).

**Flujo del proceso**

**Usuario:** F \- DT

**NOTA**: Por implementar.

---

### CU33. ABM de usuarios

Se modifican datos de usuarios/roles del sistema (A DEFINIR).

**Flujo del proceso**

**Usuario:** F \- DT

**NOTA**: Por implementar.

---

###

## Checklist casos de uso

|  | DICTAMEN DEL LOTE: |  |  |  |  |  |  |  |  |
| ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- |
| TIPOS DE PRODUCTO: | Recibido | Cuarentena | Aprobado | Liberado | Rechazado | Analisis Expirado | Vencido | Devolución clientes | Retiro mercado |
| Api | CU1 B | CU2 D | CU5 C | N/A | CU6 C | CU9 X | CU10 X | N/A | N/A |
| Excipiente | CU1 B | CU2 D | CU5 C | N/A | CU6 C | CU9 X | CU10 X | N/A | N/A |
| Capsula | CU1 B | CU2 D | CU5 C | N/A | CU6 C | CU9 X | CU10 X | N/A | N/A |
| Semielaborado | N/A | CU20 A, CU2 D | CU5 C | N/A | CU6 C | N/A | CU10 X | N/A | N/A |
| Acond. primario | CU1 B | CU2 D | CU5 C | N/A | CU6 C | CU9 X | CU10 X | N/A | N/A |
| Acond. secundario | CU1 B | CU2 D | CU5 C | N/A | CU6 C | CU9 X | CU10 X | N/A | N/A |
| Unidad venta | N/A | CU20 A, CU2 D | CU5 C | CU21 EF | CU6 C | N/A | CU10 X | CU23 E | CU24 E |

|  | MOTIVO DEL MOVIMIENTO |  |  |  |  |  |  |  |  |  |  |
| ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- |
| TIPOS PROD. | Compra | Muestreo | Devolución compra | Análisis | Consumo Prod. | Prod. Propia | Liberación | Venta | Expiracion analisis | Vencimiento | Devol. venta |
| Api | CU1 B | CU3 D | CU4 B | CU5,CU6 C | CU7 A | N/A | N/A | N/A | CU9 X | CU10 X | N/A |
| Excipiente | CU1 B | CU3 D | CU4 B | CU5,CU6 C | CU7 A | N/A | N/A | N/A | CU9 X | CU10 X | N/A |
| Capsula | CU1 B | CU3 D | CU4 B | CU5,CU6 C | CU7 A | N/A | N/A | N/A | CU9 X | CU10 X | N/A |
| Semielaborado | N/A | CU3 D | N/A | CU5,CU6 C | CU7 A | CU20 A | N/A | N/A | N/A | CU10 X | N/A |
| Acond. primario | CU1 B | CU3 D | CU4 B | CU5,CU6 C | CU7 A | N/A | N/A | N/A | CU9 X | CU10 X | N/A |
| Acond. secundario | CU1 B | CU3 D | CU4 B | CU5,CU6 C | CU7 A | N/A | N/A | N/A | CU9 X | CU10 X | N/A |
| Unidad venta | N/A | CU3 D | N/A | CU5,CU6 C | N/A | CU20 A | CU21 EF | CU22 B | N/A | CU10 X | CU23 E |

|  |  | MOTIVO DEL MOVIMIENTO |  |  |  |  |  |  |  |  |  |
| ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- | ----- |
| DICTAMEN DEL LOTE: | Compra | Muestreo | Devol. compra | Análisis | Consumo Prod. | Prod. Propia | Liberación | Venta | Expiracion analisis | Vencimiento | Devolución Venta |
| Recibido | CU1 B | N/A | CU4 B | N/A | N/A | N/A | N/A | N/A | CU9 X | CU10 X | N/A |
| Cuarentena | N/A | CU3 D | CU4 B | CU2 D | N/A | CU20 A | N/A | N/A | CU9 X | CU10 X | N/A |
| Aprobado | N/A | CU3 D | CU4 B | CU5 C | CU7 A | N/A | N/A | N/A | CU9 X | CU10 X | N/A |
| Liberado | N/A | CU3 D | N/A | N/A | N/A | N/A | CU21 EF | CU22 B | N/A | CU10 X | N/A |
| Rechazado | N/A | CU3 D | CU4 B | CU6 C | N/A | N/A | N/A | N/A | N/A | CU10 X | N/A |
| Analisis Exp. | N/A | N/A | CU4 B | CU2 D | N/A | N/A | N/A | N/A | N/A | CU10 X | N/A |
| Vencido | N/A | CU3 D | CU4 B | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A |
| Devolución  clientes | N/A | CU3 D | N/A | N/A | N/A | N/A | N/A | N/A | N/A | CU10 X | CU23 E |
| Retiro mercado | N/A | CU3 D | N/A | N/A | N/A | N/A | N/A | N/A | N/A | CU10 X | CU24 E |

## Requerimientos adicionales

Se deben generar registros de auditoría para cada operación confirmada (incluyendo información sobre el usuario, fecha, material, cantidad, etc.).

El sistema solo muestra las operaciones habilitadas para el usuario de la sesión, las opciones para las cuales no posee permisos, se verán deshabilitadas o no se mostrarán en absoluto, según el caso.

Cuando se finaliza el total de un lote, el mismo debe quedar en estado inactivo u otro estado similar ya que su saldo es 0 (NO SE DEBERÍA PODER SELECCIONAR PARA PRODUCIR, POR EJEMPLO).

**IMPLEMENTACIÓN**: El sistema usa estados de enum EstadoEnum (NUEVO, DISPONIBLE, EN_USO, CONSUMIDO, VENDIDO, DEVUELTO, RECALL, DESCARTADO) para controlar el ciclo de vida de lotes, bultos y trazas.

#

# Modelo de datos

## Tipos de tablas

1. **Diccionarios**
   Contienen datos estructurales del negocio (por ejemplo, tipos de unidades, dictámenes, motivos) que sólo se modifican si cambia la lógica del sistema.

2. **Datos maestros**
   Incluyen información estable como proveedores o productos. Cambian muy poco, son cargados por usuarios autorizados y provienen de otros sectores fuera del control del sistema de stock.

3. **Datos operativos**
   Reflejan la actividad diaria del sistema (ingresos, egresos, movimientos, existencias). Se actualizan constantemente y representan el funcionamiento del stock en tiempo real. También el sistema de auditoría de uso.

## ENUMS DICCIONARIOS

**DICTAMEN (Lotes)**
```java
public enum DictamenEnum {
    RECIBIDO,
    CUARENTENA,
    APROBADO,
    RECHAZADO,
    ANULADO,          // **IMPLEMENTADO**: Para análisis anulados
    CANCELADO,          // **IMPLEMENTADO**: Para análisis cancelados
    ANALISIS_EXPIRADO,
    VENCIDO,
    LIBERADO,
    DEVOLUCION_CLIENTES,
    RETIRO_MERCADO
}
```

**ESTADO (Lote/Bulto/Traza)**
```java
public enum EstadoEnum {
    NUEVO(0),         // **IMPLEMENTADO**: Lote recién creado
    DISPONIBLE(0),    // **IMPLEMENTADO**: Disponible para uso
    EN_USO(1),        // **IMPLEMENTADO**: En proceso de consumo
    CONSUMIDO(2),     // **IMPLEMENTADO**: Terminal - consumido en producción
    VENDIDO(2),       // **IMPLEMENTADO**: Terminal - vendido
    DEVUELTO(2),      // **IMPLEMENTADO**: Terminal - devuelto por cliente
    RECALL(2),        // **IMPLEMENTADO**: Terminal - retiro de mercado
    DESCARTADO(2)     // **IMPLEMENTADO**: Terminal - descartado (muestreo)
}
```
**NOTA**: El número entre paréntesis indica el orden de magnitud del estado.

**MOTIVO (Movimientos)**
```java
public enum MotivoEnum {
    COMPRA,
    MUESTREO,
    DEVOLUCION_COMPRA,
    ANALISIS,
    RESULTADO_ANALISIS,        // **IMPLEMENTADO**: Para CU5/CU6
    ANULACION_ANALISIS,        // **IMPLEMENTADO**: Para CU11
    CONSUMO_PRODUCCION,
    PRODUCCION_PROPIA,
    TRAZADO,                   // **IMPLEMENTADO**: Operaciones con trazas
    LIBERACION,                // **IMPLEMENTADO**: Para CU21
    VENTA,
    EXPIRACION_ANALISIS,       // **IMPLEMENTADO**: Para CU9
    VENCIMIENTO,               // **IMPLEMENTADO**: Para CU10
    DEVOLUCION_VENTA,          // **IMPLEMENTADO**: Para CU23
    RETIRO_MERCADO,            // **IMPLEMENTADO**: Para CU24
    AJUSTE,
    REVERSO                    // **IMPLEMENTADO**: Para CU29-31
}
```

**TIPO\_MOVIMIENTO (Movimientos)**
```java
public enum TipoMovimientoEnum {
    ALTA,
    BAJA,
    MODIFICACION
}
```

**TIPO\_PRODUCTO (Productos)**
```java
public enum TipoProductoEnum {
    API,
    EXCIPIENTE,
    CAPSULA,
    ACONDICIONAMIENTO_PRIMARIO,
    ACONDICIONAMIENTO_SECUNDARIO,
    SEMIELABORADO,
    UNIDAD_VENTA
}
```

**UNIDAD\_MEDIDA (Productos/Movimientos)**
```java
public enum UnidadMedidaEnum {
    // Unidad genérica
    UNIDAD("Generica", "U", BigDecimal.ONE),

    // Unidades de Masa
    MICROGRAMO("Masa", "µg", new BigDecimal("0.000001")),
    MILIGRAMO("Masa", "mg", new BigDecimal("0.001")),
    GRAMO("Masa", "g", BigDecimal.ONE),
    KILOGRAMO("Masa", "kg", new BigDecimal("1000")),
    TONELADA("Masa", "t", new BigDecimal("1000000")),

    // Unidades de Volumen
    MICROLITRO("Volumen", "µL", new BigDecimal("0.000001")),
    MILILITRO("Volumen", "mL", new BigDecimal("0.001")),
    CENTILITRO("Volumen", "cL", new BigDecimal("0.01")),
    DECILITRO("Volumen", "dL", new BigDecimal("0.1")),
    LITRO("Volumen", "L", BigDecimal.ONE),
    MILIMETRO_CUBICO("Volumen", "mm³", new BigDecimal("0.000001")),
    CENTIMETRO_CUBICO("Volumen", "cm³", new BigDecimal("0.001")),
    METRO_CUBICO("Volumen", "m³", new BigDecimal("1000")),

    // Unidades de Superficie
    MILIMETRO_CUADRADO("Superficie", "mm²", new BigDecimal("0.000001")),
    CENTIMETRO_CUADRADO("Superficie", "cm²", new BigDecimal("0.0001")),
    METRO_CUADRADO("Superficie", "m²", BigDecimal.ONE),
    KILOMETRO_CUADRADO("Superficie", "km²", new BigDecimal("1000000")),
    HECTAREA("Superficie", "ha", new BigDecimal("10000")),

    // Unidades de Longitud
    MICROMETRO("Longitud", "µm", new BigDecimal("0.000001")),
    MILIMETRO("Longitud", "mm", new BigDecimal("0.001")),
    CENTIMETRO("Longitud", "cm", new BigDecimal("0.01")),
    METRO("Longitud", "m", BigDecimal.ONE),
    KILOMETRO("Longitud", "km", new BigDecimal("1000")),

    // Unidades porcentuales
    PORCENTAJE("Porcentaje", "%", new BigDecimal("0.01")),
    PARTES_POR_MILLON("Porcentaje", "ppm", new BigDecimal("0.000001"))
}
```

## TABLAS DATOS MAESTROS

**PRODUCTOS**
```java
@Entity
@Table(name = "productos")
public class Producto {
    @Id @GeneratedValue
    private Long id;

    @Column(name = "nombre_generico", nullable = false)
    private String nombreGenerico;

    @Column(name = "codigo_interno", nullable = false, unique = true)
    private String codigoInterno;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_producto", nullable = false)
    private TipoProductoEnum tipoProducto;

    @Column(name = "pais_origen")
    private String paisOrigen;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false)
    private UnidadMedidaEnum unidadMedida;

    @ManyToOne
    @JoinColumn(name = "producto_destino_id")
    private Producto productoDestino;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo;
}
```

**PROVEEDOR**
```java
@Entity
@Table(name = "proveedores")
public class Proveedor {
    @Id @GeneratedValue
    private Long id;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(nullable = false, unique = true)
    private String cuit;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private String ciudad;

    @Column(nullable = false)
    private String pais;

    private String telefono;
    private String email;
    private String contacto;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo;
}
```

## TABLAS DATOS OPERATIVOS

**LOTES** (TABLA PRINCIPAL DE ESTADOS DEL STOCK)
```java
@Entity
@Table(name = "lotes")
public class Lote {
    @Id @GeneratedValue
    private Long id;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaYHoraCreacion;

    @Column(name = "codigo_lote", length = 50, nullable = false)
    private String codigoLote;

    @ManyToOne @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @ManyToOne @JoinColumn(name = "fabricante_id")
    private Proveedor fabricante;

    @Column(name = "pais_origen")
    private String paisOrigen;

    @Column(name = "orden_produccion_origen", length = 50)
    private String ordenProduccionOrigen;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "bultos_totales", nullable = false)
    private Integer bultosTotales;

    @Column(name = "cantidad_inicial", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadInicial;

    @Column(name = "cantidad_actual", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadActual;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false)
    private UnidadMedidaEnum unidadMedida;

    @Column(name = "lote_proveedor", nullable = false)
    private String loteProveedor;

    @Column(name = "fecha_reanal_prov")
    private LocalDate fechaReanalisisProveedor;

    @Column(name = "fecha_vto_prov")
    private LocalDate fechaVencimientoProveedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEnum estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DictamenEnum dictamen;

    @ManyToOne @JoinColumn(name = "lote_origen_id")
    private Lote loteOrigen;

    @Column(name = "nro_remito")
    private String nroRemito;

    @Column(name = "detalle_conservacion", columnDefinition = "TEXT")
    private String detalleConservacion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private Boolean trazado; // **IMPLEMENTADO**: indica si requiere trazabilidad unitaria

    @Column(nullable = false)
    private Boolean activo;

    // Relaciones
    @OneToMany(mappedBy = "lote", cascade = {PERSIST, MERGE}, orphanRemoval = true)
    private Set<Bulto> bultos = new HashSet<>();

    @OneToMany(mappedBy = "lote", cascade = {PERSIST, MERGE}, orphanRemoval = true)
    private Set<Movimiento> movimientos = new HashSet<>();

    @OneToMany(mappedBy = "lote", cascade = {PERSIST, MERGE}, orphanRemoval = true)
    private List<Analisis> analisisList = new ArrayList<>();

    @OneToMany(mappedBy = "lote", cascade = {PERSIST, MERGE}, orphanRemoval = true)
    private Set<Traza> trazas = new HashSet<>();
}
```

**BULTOS** (**IMPLEMENTADO**: Subdivisión física del lote)
```java
@Entity
@Table(name = "bultos")
public class Bulto {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(name = "nro_bulto", nullable = false)
    private Integer nroBulto;

    @Column(name = "cantidad_inicial", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadInicial;

    @Column(name = "cantidad_actual", nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadActual;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false)
    private UnidadMedidaEnum unidadMedida;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEnum estado;

    @Column(nullable = false)
    private Boolean activo;

    @OneToMany(mappedBy = "bulto", cascade = {PERSIST, MERGE}, orphanRemoval = true)
    private Set<DetalleMovimiento> detalles = new HashSet<>();

    @OneToMany(mappedBy = "bulto", cascade = {PERSIST, MERGE}, orphanRemoval = true)
    private Set<Traza> trazas = new HashSet<>();
}
```

**MOVIMIENTOS** (TABLA PRINCIPAL DE MOVIMIENTOS/ESTADOS/PROCESOS)
```java
@Entity
@Table(name = "movimientos")
public class Movimiento {
    @Id @GeneratedValue
    private Long id;

    @Column(name = "codigo_movimiento", length = 100, nullable = false)
    private String codigoMovimiento;

    @ManyToOne @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @OneToMany(mappedBy = "movimiento", cascade = {PERSIST, MERGE}, orphanRemoval = true)
    private Set<DetalleMovimiento> detalles = new HashSet<>();

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaYHoraCreacion;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false)
    private TipoMovimientoEnum tipoMovimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false)
    private MotivoEnum motivo;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(precision = 12, scale = 4)
    private BigDecimal cantidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida")
    private UnidadMedidaEnum unidadMedida;

    @Column(name = "nro_analisis", length = 50)
    private String nroAnalisis;

    @Column(name = "orden_produccion", length = 50)
    private String ordenProduccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "dictamen_inicial", nullable = false)
    private DictamenEnum dictamenInicial;

    @Enumerated(EnumType.STRING)
    @Column(name = "dictamen_final", nullable = false)
    private DictamenEnum dictamenFinal;

    @ManyToOne @JoinColumn(name = "movimiento_origen_id")
    private Movimiento movimientoOrigen;

    @Column(nullable = false)
    private Boolean activo;
}
```

**DETALLE_MOVIMIENTO** (**IMPLEMENTADO**: Detalle de movimiento por bulto)
```java
@Entity
@Table(name = "detalle_movimientos")
public class DetalleMovimiento {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne @JoinColumn(name = "movimiento_id", nullable = false)
    private Movimiento movimiento;

    @ManyToOne @JoinColumn(name = "bulto_id", nullable = false)
    private Bulto bulto;

    @Column(precision = 12, scale = 4, nullable = false)
    private BigDecimal cantidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false)
    private UnidadMedidaEnum unidadMedida;

    @Column(nullable = false)
    private Boolean activo;

    @ManyToMany
    @JoinTable(
        name = "detalle_traza",
        joinColumns = @JoinColumn(name = "detalle_movimiento_id"),
        inverseJoinColumns = @JoinColumn(name = "traza_id")
    )
    private Set<Traza> trazas = new HashSet<>();
}
```

**ANALISIS**(TABLA PRINCIPAL DE ANALISIS)
```java
@Entity
@Table(name = "analisis")
public class Analisis {
    @Id @GeneratedValue
    private Long id;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaYHoraCreacion;

    @Column(name = "nro_analisis", length = 30, nullable = false)
    private String nroAnalisis;

    @ManyToOne @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(name = "fecha_realizado")
    private LocalDate fechaRealizado;

    @Column(name = "fecha_reanalisis")
    private LocalDate fechaReanalisis;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    private DictamenEnum dictamen;

    @Column(name = "titulo", precision = 12, scale = 4)
    private BigDecimal titulo; // Porcentaje de pureza (0-100)

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private Boolean activo;
}
```

**TRAZA** (**IMPLEMENTADO**: TABLA PRINCIPAL DE TRAZA POR UNIDAD)
```java
@Entity
@Table(name = "trazas")
public class Traza {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @ManyToOne @JoinColumn(name = "bulto_id", nullable = false)
    private Bulto bulto;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaYHoraCreacion;

    @ManyToOne @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "nro_traza", nullable = false)
    private Long nroTraza;

    @ManyToMany(mappedBy = "trazas")
    private List<DetalleMovimiento> detalles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEnum estado;

    @Column(nullable = false)
    private Boolean activo;
}
```

**NOTA:** Todas las tablas implementan borrado lógico mediante el campo `activo` (Boolean). El borrado físico NO se realiza; en su lugar, se marca `activo = false`.

---

# Modelo de Casos de Uso de Consultas

### **1\. A COMPLETAR**

---

# Plan de Testing del SW

### **1\. A COMPLETAR**

---

# Cambios solicitados 1era revision

Filtros/autocompletar/seleccion en pasos de la informacion de carga

---

# RESUMEN DE DISCREPANCIAS ENCONTRADAS (Diseño vs Implementación)

## Discrepancias Críticas

1. **Estructura de Lote/Bulto**
   - **Diseñado**: Un lote por bulto (múltiples lotes para múltiples bultos)
   - **Implementado**: Un lote con múltiples bultos (arquitectura correcta)

2. **CU24 - Retiro de Mercado**
   - **Diseñado**: Simple cambio de dictamen
   - **Implementado**: Sistema dual-lot complejo (MODIF lote original + ALTA nuevo lote) para cumplir regulaciones FDA/ANVISA

3. **CU3 - Muestreo para productos trazados**
   - **Diseñado**: Reducción de cantidad
   - **Implementado**: Marca trazas como DESCARTADO (no reduce cantidad del bulto)

4. **Numeración de CUs**
   - **Diseñado**: CU25 (Ajuste), CU26 (Reverso)
   - **Implementado**: CU28 (Ajuste), CU29-31 (Reverso por tipo de movimiento)

5. **Enum EstadoEnum**
   - **Diseñado**: Solo estados básicos
   - **Implementado**: Estados extendidos con orden de magnitud (NUEVO, DISPONIBLE, EN_USO, CONSUMIDO, VENDIDO, DEVUELTO, RECALL, DESCARTADO)

6. **Enum DictamenEnum**
   - **Diseñado**: Sin ANULADO
   - **Implementado**: Incluye ANULADO para análisis anulados

7. **Enum MotivoEnum**
   - **Diseñado**: Lista básica
   - **Implementado**: Incluye RESULTADO_ANALISIS, ANULACION_ANALISIS, TRAZADO, LIBERACION, REVERSO

8. **Bug en FechaValidatorService (CU9/CU10)**
   - Lógica de comparación de fechas invertida: `if (fechaRe.isBefore(hoy))` no captura fecha = hoy

9. **Tabla Bultos**
   - **Diseñado**: No documentada explícitamente
   - **Implementado**: Tabla completa con estado independiente por bulto

10. **Tabla DetalleMovimiento**
    - **Diseñado**: No documentada
    - **Implementado**: Tabla de relación N:M entre Movimiento y Bulto con trazas asociadas

11. **CU23 - Devolución Cliente**
    - **Diseñado**: Simple cambio de dictamen
    - **Implementado**: Crea nuevo lote con código `{original}_D_{seq}`

12. **Campo `trazado` en Lote**
    - **Diseñado**: No documentado
    - **Implementado**: Campo Boolean crítico que determina el comportamiento de trazabilidad unitaria

package com.mb.conitrack.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import static org.junit.jupiter.api.Assertions.*;

class LoteEntityUtilsTest {

    // ================ Constructor Test ================

    @Test
    @DisplayName("Constructor lanza UnsupportedOperationException")
    void constructor_lanzaExcepcion() throws Exception {
        Constructor<LoteEntityUtils> constructor = LoteEntityUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
        assertEquals("Utility class cannot be instantiated", exception.getCause().getMessage());
    }

    // ================ createLoteIngreso Tests ================

    @Test
    @DisplayName("createLoteIngreso crea lote con campos base correctos")
    void createLoteIngreso_creaConCamposBase() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC);
        LoteDTO dto = new LoteDTO();
        dto.setFechaYHoraCreacion(timestamp);
        dto.setPaisOrigen("Argentina");
        dto.setFechaIngreso(timestamp.toLocalDate());
        dto.setBultosTotales(5);
        dto.setLoteProveedor("LOT-PROV-001");
        dto.setOrdenProduccion("OP-2025-001");
        dto.setObservaciones("Test observations");

        // when
        Lote lote = LoteEntityUtils.createLoteIngreso(dto);

        // then
        assertNotNull(lote);
        assertEquals(timestamp, lote.getFechaYHoraCreacion());
        assertEquals(EstadoEnum.NUEVO, lote.getEstado());
        assertEquals(DictamenEnum.RECIBIDO, lote.getDictamen());
        assertTrue(lote.getActivo());
        assertEquals("Argentina", lote.getPaisOrigen());
        assertEquals(timestamp.toLocalDate(), lote.getFechaIngreso());
        assertEquals(5, lote.getBultosTotales());
        assertEquals("LOT-PROV-001", lote.getLoteProveedor());
        assertEquals("OP-2025-001", lote.getOrdenProduccionOrigen());
        assertEquals("Test observations", lote.getObservaciones());
    }

    @Test
    @DisplayName("createLoteIngreso lanza NullPointerException si loteDTO es null")
    void createLoteIngreso_loteDTONull_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () ->
            LoteEntityUtils.createLoteIngreso(null)
        );
    }

    // ================ getAnalisisEnCurso Tests ================

    @Test
    @DisplayName("getAnalisisEnCurso devuelve analisis en curso")
    void getAnalisisEnCurso_devuelveAnalisisEnCurso() {
        // given
        Analisis enCurso = new Analisis();
        enCurso.setActivo(true);
        enCurso.setDictamen(null);
        enCurso.setFechaRealizado(null);

        Analisis completado = new Analisis();
        completado.setActivo(true);
        completado.setDictamen(DictamenEnum.APROBADO);

        List<Analisis> analisisList = List.of(enCurso, completado);

        // when
        Optional<Analisis> result = LoteEntityUtils.getAnalisisEnCurso(analisisList);

        // then
        assertTrue(result.isPresent());
        assertSame(enCurso, result.get());
    }

    @Test
    @DisplayName("getAnalisisEnCurso devuelve empty si no hay analisis en curso")
    void getAnalisisEnCurso_sinAnalisisEnCurso_devuelveEmpty() {
        // given
        Analisis completado = new Analisis();
        completado.setActivo(true);
        completado.setDictamen(DictamenEnum.APROBADO);

        List<Analisis> analisisList = List.of(completado);

        // when
        Optional<Analisis> result = LoteEntityUtils.getAnalisisEnCurso(analisisList);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAnalisisEnCurso lanza excepcion si hay mas de uno en curso")
    void getAnalisisEnCurso_masDeUno_lanzaExcepcion() {
        // given
        Analisis enCurso1 = new Analisis();
        enCurso1.setActivo(true);
        enCurso1.setDictamen(null);
        enCurso1.setFechaRealizado(null);

        Analisis enCurso2 = new Analisis();
        enCurso2.setActivo(true);
        enCurso2.setDictamen(null);
        enCurso2.setFechaRealizado(null);

        List<Analisis> analisisList = List.of(enCurso1, enCurso2);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            LoteEntityUtils.getAnalisisEnCurso(analisisList)
        );
        assertEquals("El lote tiene más de un análisis en curso", exception.getMessage());
    }

    @Test
    @DisplayName("getAnalisisEnCurso lanza NullPointerException si analisisList es null")
    void getAnalisisEnCurso_listNull_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () ->
            LoteEntityUtils.getAnalisisEnCurso(null)
        );
    }

    // ================ populateLoteAltaProduccionPropia Tests ================

    @Test
    @DisplayName("populateLoteAltaProduccionPropia configura lote correctamente")
    void populateLoteAltaProduccionPropia_configuraTodoCorrectamente() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 2, 10, 14, 30, 0, 0, ZoneOffset.UTC);
        LoteDTO dto = new LoteDTO();
        dto.setFechaYHoraCreacion(timestamp);
        dto.setBultosTotales(1);
        dto.setCantidadInicial(BigDecimal.valueOf(100));
        dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);

        Lote lote = new Lote();

        Producto producto = new Producto();
        producto.setCodigoProducto("PROD-001");

        Proveedor conifarma = new Proveedor();
        conifarma.setPais("Argentina");

        // when
        LoteEntityUtils.populateLoteAltaProduccionPropia(lote, dto, producto, conifarma);

        // then
        assertEquals("L-PROD-001-25.02.10_14.30.00", lote.getCodigoLote());
        assertSame(producto, lote.getProducto());
        assertSame(conifarma, lote.getProveedor());
        assertSame(conifarma, lote.getFabricante());
        assertEquals("Argentina", lote.getPaisOrigen());
        assertEquals(1, lote.getBultosTotales());
        assertEquals(1, lote.getBultos().size());
        assertEquals(BigDecimal.valueOf(100), lote.getCantidadInicial());
        assertEquals(BigDecimal.valueOf(100), lote.getCantidadActual());
        assertEquals(UnidadMedidaEnum.KILOGRAMO, lote.getUnidadMedida());
    }

    @Test
    @DisplayName("populateLoteAltaProduccionPropia lanza NullPointerException si lote es null")
    void populateLoteAltaProduccionPropia_loteNull_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () ->
            LoteEntityUtils.populateLoteAltaProduccionPropia(null, new LoteDTO(), new Producto(), new Proveedor())
        );
    }

    // ================ populateLoteAltaStockCompra Tests ================

    @Test
    @DisplayName("populateLoteAltaStockCompra configura lote con fabricante")
    void populateLoteAltaStockCompra_conFabricante() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 3, 15, 9, 0, 0, 0, ZoneOffset.UTC);
        LoteDTO dto = new LoteDTO();
        dto.setFechaYHoraCreacion(timestamp);
        dto.setPaisOrigen("");  // Empty to test fallback logic
        dto.setBultosTotales(2);
        dto.setCantidadInicial(BigDecimal.valueOf(50));
        dto.setUnidadMedida(UnidadMedidaEnum.GRAMO);
        dto.setCantidadesBultos(List.of(BigDecimal.valueOf(30), BigDecimal.valueOf(20)));
        dto.setUnidadMedidaBultos(List.of(UnidadMedidaEnum.GRAMO, UnidadMedidaEnum.GRAMO));

        Lote lote = new Lote();

        Producto producto = new Producto();
        producto.setCodigoProducto("MED-002");

        Proveedor proveedor = new Proveedor();
        proveedor.setPais("Argentina");

        Proveedor fabricante = new Proveedor();
        fabricante.setPais("China");

        // when
        LoteEntityUtils.populateLoteAltaStockCompra(lote, dto, producto, proveedor, fabricante);

        // then
        assertEquals("L-MED-002-25.03.15_09.00.00", lote.getCodigoLote());
        assertSame(producto, lote.getProducto());
        assertSame(proveedor, lote.getProveedor());
        assertSame(fabricante, lote.getFabricante());
        assertEquals("China", lote.getPaisOrigen());  // From fabricante
        assertEquals(2, lote.getBultosTotales());
        assertEquals(2, lote.getBultos().size());
    }

    @Test
    @DisplayName("populateLoteAltaStockCompra usa pais proveedor si fabricante es null")
    void populateLoteAltaStockCompra_sinFabricante_usaPaisProveedor() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.now();
        LoteDTO dto = new LoteDTO();
        dto.setFechaYHoraCreacion(timestamp);
        dto.setPaisOrigen("");
        dto.setBultosTotales(1);
        dto.setCantidadInicial(BigDecimal.TEN);
        dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        Lote lote = new Lote();
        Producto producto = new Producto();
        producto.setCodigoProducto("PROD");

        Proveedor proveedor = new Proveedor();
        proveedor.setPais("Argentina");

        // when
        LoteEntityUtils.populateLoteAltaStockCompra(lote, dto, producto, proveedor, null);

        // then
        assertEquals("Argentina", lote.getPaisOrigen());  // From proveedor since fabricante is null
    }

    @Test
    @DisplayName("populateLoteAltaStockCompra lanza NullPointerException si proveedor es null")
    void populateLoteAltaStockCompra_proveedorNull_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () ->
            LoteEntityUtils.populateLoteAltaStockCompra(new Lote(), new LoteDTO(), new Producto(), null, new Proveedor())
        );
    }

    // ================ addTrazasToLote Tests ================

    @Test
    @DisplayName("addTrazasToLote crea y distribuye trazas correctamente")
    void addTrazasToLote_creaYDistribuye() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.now();
        MovimientoDTO movDto = new MovimientoDTO();
        movDto.setTrazaInicial(1000L);  // Long
        movDto.setFechaYHoraCreacion(timestamp);

        Producto producto = new Producto();
        producto.setCodigoProducto("MED-001");
        producto.setNombreGenerico("Medicamento Test");

        Lote lote = new Lote();
        lote.setProducto(producto);
        lote.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        lote.setCantidadActual(BigDecimal.valueOf(5));

        Bulto bulto1 = new Bulto();
        bulto1.setCantidadActual(BigDecimal.valueOf(3));
        lote.getBultos().add(bulto1);

        Bulto bulto2 = new Bulto();
        bulto2.setCantidadActual(BigDecimal.valueOf(2));
        lote.getBultos().add(bulto2);

        // when
        LoteEntityUtils.addTrazasToLote(lote, movDto);

        // then
        assertTrue(lote.getTrazado());
        assertEquals(5, lote.getTrazas().size());
        assertEquals(3, bulto1.getTrazas().size());
        assertEquals(2, bulto2.getTrazas().size());

        // Verify trace numbers are in expected range
        List<Long> traceNumbers = lote.getTrazas().stream()
            .map(com.mb.conitrack.entity.Traza::getNroTraza)
            .sorted()
            .toList();
        assertEquals(1000L, traceNumbers.get(0));
        assertEquals(1004L, traceNumbers.get(4));
    }

    @Test
    @DisplayName("addTrazasToLote lanza IllegalStateException si unidad no es UNIDAD")
    void addTrazasToLote_unidadIncorrecta_lanzaExcepcion() {
        // given
        Lote lote = new Lote();
        lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);  // Not UNIDAD
        lote.setCantidadActual(BigDecimal.TEN);
        lote.setProducto(new Producto());

        MovimientoDTO movDto = new MovimientoDTO();

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            LoteEntityUtils.addTrazasToLote(lote, movDto)
        );
        assertEquals("La traza solo es aplicable a UNIDADES", exception.getMessage());
    }

    @Test
    @DisplayName("addTrazasToLote lanza IllegalStateException si cantidad no es entera")
    void addTrazasToLote_cantidadDecimal_lanzaExcepcion() {
        // given
        Lote lote = new Lote();
        lote.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        lote.setCantidadActual(new BigDecimal("5.5"));  // Decimal
        lote.setProducto(new Producto());

        MovimientoDTO movDto = new MovimientoDTO();

        // when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
            LoteEntityUtils.addTrazasToLote(lote, movDto)
        );
        assertEquals("La cantidad de Unidades debe ser entero", exception.getMessage());
    }

    @Test
    @DisplayName("addTrazasToLote lanza NullPointerException si lote es null")
    void addTrazasToLote_loteNull_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () ->
            LoteEntityUtils.addTrazasToLote(null, new MovimientoDTO())
        );
    }

    @Test
    @DisplayName("addTrazasToLote lanza NullPointerException si movimientoDto es null")
    void addTrazasToLote_movimientoDtoNull_lanzaExcepcion() {
        assertThrows(NullPointerException.class, () ->
            LoteEntityUtils.addTrazasToLote(new Lote(), null)
        );
    }

    @Test
    @DisplayName("populateLoteAltaStockCompra: usa paisOrigen del DTO cuando está explícitamente establecido")
    void populateLoteAltaStockCompra_paisOrigenDelDTO() {
        // given
        Lote lote = new Lote();
        LoteDTO loteDTO = new LoteDTO();
        loteDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        loteDTO.setCantidadInicial(new BigDecimal("100"));
        loteDTO.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        loteDTO.setBultosTotales(1);
        loteDTO.setCantidadesBultos(List.of(new BigDecimal("100")));
        loteDTO.setUnidadMedidaBultos(List.of(UnidadMedidaEnum.KILOGRAMO));
        loteDTO.setPaisOrigen("España");  // Explicitly set paisOrigen

        Producto producto = new Producto();
        producto.setCodigoProducto("PROD-001");

        Proveedor proveedor = new Proveedor();
        proveedor.setPais("Argentina");

        Proveedor fabricante = new Proveedor();
        fabricante.setPais("Brasil");

        // when
        LoteEntityUtils.populateLoteAltaStockCompra(lote, loteDTO, producto, proveedor, fabricante);

        // then
        assertEquals("España", lote.getPaisOrigen());  // Should use DTO value, not fabricante or proveedor
    }

    @Test
    @DisplayName("getAnalisisEnCurso: excluye análisis con fechaRealizado != null")
    void getAnalisisEnCurso_excluyeAnalisisRealizados() {
        // given
        Analisis analisisEnCurso = new Analisis();
        analisisEnCurso.setActivo(true);
        analisisEnCurso.setDictamen(null);
        analisisEnCurso.setFechaRealizado(null);  // En curso

        Analisis analisisRealizado = new Analisis();
        analisisRealizado.setActivo(true);
        analisisRealizado.setDictamen(null);
        analisisRealizado.setFechaRealizado(LocalDate.now());  // Ya realizado

        List<Analisis> analisisList = List.of(analisisEnCurso, analisisRealizado);

        // when
        Optional<Analisis> result = LoteEntityUtils.getAnalisisEnCurso(analisisList);

        // then
        assertTrue(result.isPresent());
        assertSame(analisisEnCurso, result.get());  // Should return only the one in progress
    }

}

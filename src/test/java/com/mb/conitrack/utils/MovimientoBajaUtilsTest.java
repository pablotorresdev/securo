package com.mb.conitrack.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovimientoBajaUtilsTest {

    private User testUser;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setId(1L);
        testUser = new User("testuser", "password", adminRole);
        testUser.setId(1L);
    }

    @Test
    @DisplayName("Constructor lanza UnsupportedOperationException")
    void constructor_lanzaExcepcion() throws Exception {
        Constructor<MovimientoBajaUtils> constructor = MovimientoBajaUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
        assertEquals("Utility class cannot be instantiated", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("createMovimientoDevolucionCompra crea movimiento BAJA con motivo DEVOLUCION_COMPRA")
    void createMovimientoDevolucionCompra_creaTodosLosCampos() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 1, 10, 8, 0, 0, 0, ZoneOffset.UTC);
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote("LOT-001");
        dto.setFechaYHoraCreacion(timestamp);
        dto.setFechaMovimiento(timestamp.toLocalDate());
        dto.setObservaciones("Devolución a proveedor");

        // when
        Movimiento movimiento = MovimientoBajaUtils.createMovimientoDevolucionCompra(dto, testUser);

        // then
        assertNotNull(movimiento);
        assertEquals(TipoMovimientoEnum.BAJA, movimiento.getTipoMovimiento());
        assertEquals(MotivoEnum.DEVOLUCION_COMPRA, movimiento.getMotivo());
        assertEquals(timestamp, movimiento.getFechaYHoraCreacion());
        assertEquals("LOT-001-25.01.10_08.00.00", movimiento.getCodigoMovimiento());
        assertEquals(DictamenEnum.RECHAZADO, movimiento.getDictamenFinal());
        assertEquals(timestamp.toLocalDate(), movimiento.getFecha());
        assertTrue(movimiento.getActivo());
        assertEquals("_CU4_\nDevolución a proveedor", movimiento.getObservaciones());
    }

    @Test
    @DisplayName("createMovimientoBajaProduccion crea movimiento con motivo CONSUMO_PRODUCCION")
    void createMovimientoBajaProduccion_creaConMotivoConsumo() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 2, 15, 10, 30, 0, 0, ZoneOffset.UTC);
        LocalDate fechaEgreso = LocalDate.of(2025, 2, 15);
        LoteDTO dto = new LoteDTO();
        dto.setFechaYHoraCreacion(timestamp);
        dto.setFechaEgreso(fechaEgreso);
        dto.setObservaciones("Consumido en producción");

        Lote lote = new Lote();
        lote.setCodigoLote("LOT-PROD-01");

        // when
        Movimiento movimiento = MovimientoBajaUtils.createMovimientoBajaProduccion(dto, lote, testUser);

        // then
        assertNotNull(movimiento);
        assertEquals(TipoMovimientoEnum.BAJA, movimiento.getTipoMovimiento());
        assertEquals(MotivoEnum.CONSUMO_PRODUCCION, movimiento.getMotivo());
        assertEquals("LOT-PROD-01-25.02.15_10.30.00", movimiento.getCodigoMovimiento());
        assertEquals(fechaEgreso, movimiento.getFecha());
        assertSame(lote, movimiento.getLote());
        assertTrue(movimiento.getActivo());
        assertEquals("_CU7_\nConsumido en producción", movimiento.getObservaciones());
    }

    @Test
    @DisplayName("createMovimientoBajaVenta crea movimiento con motivo VENTA")
    void createMovimientoBajaVenta_creaConMotivoVenta() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 3, 20, 14, 0, 0, 0, ZoneOffset.UTC);
        LocalDate fechaEgreso = LocalDate.of(2025, 3, 20);
        LoteDTO dto = new LoteDTO();
        dto.setFechaYHoraCreacion(timestamp);
        dto.setFechaEgreso(fechaEgreso);
        dto.setObservaciones("Venta a cliente ABC");

        Lote lote = new Lote();
        lote.setCodigoLote("LOT-VENTA-01");

        // when
        Movimiento movimiento = MovimientoBajaUtils.createMovimientoBajaVenta(dto, lote, testUser);

        // then
        assertNotNull(movimiento);
        assertEquals(TipoMovimientoEnum.BAJA, movimiento.getTipoMovimiento());
        assertEquals(MotivoEnum.VENTA, movimiento.getMotivo());
        assertEquals("_CU22_\nVenta a cliente ABC", movimiento.getObservaciones());
    }

    @Test
    @DisplayName("createMovimientoAjusteStock crea movimiento con código incluyendo bulto")
    void createMovimientoAjusteStock_incluyeNroBultoEnCodigo() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 4, 5, 11, 15, 0, 0, ZoneOffset.UTC);
        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaYHoraCreacion(timestamp);
        dto.setFechaMovimiento(timestamp.toLocalDate());
        dto.setCantidad(BigDecimal.valueOf(2));
        dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        dto.setNroAnalisis("A-001");
        dto.setObservaciones("Ajuste por diferencia física");

        Lote lote = new Lote();
        lote.setCodigoLote("LOT-AJUSTE-01");

        Bulto bulto = new Bulto();
        bulto.setNroBulto(3);
        bulto.setLote(lote);
        bulto.setCantidadActual(BigDecimal.valueOf(10));
        bulto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);

        // when
        Movimiento movimiento = MovimientoBajaUtils.createMovimientoAjusteStock(dto, bulto, testUser);

        // then
        assertNotNull(movimiento);
        assertEquals(TipoMovimientoEnum.BAJA, movimiento.getTipoMovimiento());
        assertEquals(MotivoEnum.AJUSTE, movimiento.getMotivo());
        assertEquals("LOT-AJUSTE-01-B_3-25.04.05_11.15.00", movimiento.getCodigoMovimiento());
        assertEquals(BigDecimal.valueOf(2), movimiento.getCantidad());
        assertEquals(UnidadMedidaEnum.KILOGRAMO, movimiento.getUnidadMedida());
        assertEquals("A-001", movimiento.getNroAnalisis());
        assertSame(lote, movimiento.getLote());
        assertEquals(1, movimiento.getDetalles().size());
        assertEquals("_CU25_\nAjuste por diferencia física", movimiento.getObservaciones());
    }

    @Test
    @DisplayName("createMovimientoMuestreoConAnalisis crea movimiento con nro análisis")
    void createMovimientoMuestreoConAnalisis_incluyeAnalisis() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 5, 10, 9, 0, 0, 0, ZoneOffset.UTC);
        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaYHoraCreacion(timestamp);
        dto.setFechaMovimiento(timestamp.toLocalDate());
        dto.setCantidad(BigDecimal.ONE);
        dto.setUnidadMedida(UnidadMedidaEnum.GRAMO);
        dto.setObservaciones("Muestra para análisis microbiológico");

        Lote lote = new Lote();
        lote.setCodigoLote("LOT-MUESTREO-01");

        Bulto bulto = new Bulto();
        bulto.setNroBulto(5);
        bulto.setLote(lote);
        bulto.setCantidadActual(BigDecimal.valueOf(50));
        bulto.setUnidadMedida(UnidadMedidaEnum.GRAMO);

        Analisis analisis = new Analisis();
        analisis.setNroAnalisis("AN-2025-001");

        // when
        Movimiento movimiento = MovimientoBajaUtils.createMovimientoMuestreoConAnalisis(dto, bulto, analisis, testUser);

        // then
        assertNotNull(movimiento);
        assertEquals(TipoMovimientoEnum.BAJA, movimiento.getTipoMovimiento());
        assertEquals(MotivoEnum.MUESTREO, movimiento.getMotivo());
        assertEquals("LOT-MUESTREO-01-B_5-25.05.10_09.00.00", movimiento.getCodigoMovimiento());
        assertEquals("AN-2025-001", movimiento.getNroAnalisis());
        assertEquals(BigDecimal.ONE, movimiento.getCantidad());
        assertSame(lote, movimiento.getLote());
        assertEquals(1, movimiento.getDetalles().size());
        assertEquals("_CU3_\nMuestra para análisis microbiológico", movimiento.getObservaciones());
    }

    @Test
    @DisplayName("createMovimientoPorMuestreoMultiBulto crea movimiento sin nro bulto en código")
    void createMovimientoPorMuestreoMultiBulto_sinNroBultoEnCodigo() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 6, 15, 13, 45, 0, 0, ZoneOffset.UTC);
        LocalDate fechaEgreso = LocalDate.of(2025, 6, 15);
        LoteDTO dto = new LoteDTO();
        dto.setFechaYHoraCreacion(timestamp);
        dto.setFechaEgreso(fechaEgreso);
        dto.setObservaciones("Muestreo de múltiples bultos");

        Lote lote = new Lote();
        lote.setCodigoLote("LOT-MULTI-01");

        // when
        Movimiento movimiento = MovimientoBajaUtils.createMovimientoPorMuestreoMultiBulto(dto, lote, testUser);

        // then
        assertNotNull(movimiento);
        assertEquals(TipoMovimientoEnum.BAJA, movimiento.getTipoMovimiento());
        assertEquals(MotivoEnum.MUESTREO, movimiento.getMotivo());
        assertEquals("LOT-MULTI-01-25.06.15_13.45.00", movimiento.getCodigoMovimiento());
        assertEquals(fechaEgreso, movimiento.getFecha());
        assertSame(lote, movimiento.getLote());
        assertTrue(movimiento.getActivo());
        assertEquals("_CU3_\nMuestreo de múltiples bultos", movimiento.getObservaciones());
    }

}

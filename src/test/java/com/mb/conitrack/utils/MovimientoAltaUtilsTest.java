package com.mb.conitrack.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mb.conitrack.dto.MovimientoDTO;
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

class MovimientoAltaUtilsTest {

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
        Constructor<MovimientoAltaUtils> constructor = MovimientoAltaUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
        assertEquals("Utility class cannot be instantiated", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("createMovimientoAltaIngresoCompra crea movimiento con todos los campos correctos")
    void createMovimientoAltaIngresoCompra_creaTodosLosCampos() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 1, 15, 10, 30, 0, 0, ZoneOffset.UTC);
        Lote lote = new Lote();
        lote.setCodigoLote("LOT-001");
        lote.setFechaYHoraCreacion(timestamp);
        lote.setCantidadInicial(BigDecimal.TEN);
        lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        lote.setDictamen(DictamenEnum.RECIBIDO);
        lote.setObservaciones("Lote de prueba");

        // when
        Movimiento movimiento = MovimientoAltaUtils.createMovimientoAltaIngresoCompra(lote, testUser);

        // then
        assertNotNull(movimiento);
        assertEquals(TipoMovimientoEnum.ALTA, movimiento.getTipoMovimiento());
        assertEquals(MotivoEnum.COMPRA, movimiento.getMotivo());
        assertEquals(timestamp, movimiento.getFechaYHoraCreacion());
        assertEquals(timestamp.toLocalDate(), movimiento.getFecha());
        assertEquals("LOT-001-25.01.15_10.30.00", movimiento.getCodigoMovimiento());
        assertEquals(BigDecimal.TEN, movimiento.getCantidad());
        assertEquals(UnidadMedidaEnum.KILOGRAMO, movimiento.getUnidadMedida());
        assertEquals(DictamenEnum.RECIBIDO, movimiento.getDictamenFinal());
        assertSame(lote, movimiento.getLote());
        assertTrue(movimiento.getActivo());
        assertEquals("_CU1_\nLote de prueba", movimiento.getObservaciones());
    }

    @Test
    @DisplayName("createMovimientoAltaIngresoCompra lanza NullPointerException si lote es null")
    void createMovimientoAltaIngresoCompra_loteNull_lanzaExcepcion() {
        // when & then
        assertThrows(NullPointerException.class, () ->
            MovimientoAltaUtils.createMovimientoAltaIngresoCompra(null, testUser)
        );
    }

    @Test
    @DisplayName("createMovimientoAltaIngresoProduccion crea movimiento con motivo PRODUCCION_PROPIA")
    void createMovimientoAltaIngresoProduccion_creaConMotivoProduccion() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 2, 10, 14, 0, 0, 0, ZoneOffset.UTC);
        Lote lote = new Lote();
        lote.setCodigoLote("LOT-PROD-01");
        lote.setFechaYHoraCreacion(timestamp);
        lote.setCantidadInicial(BigDecimal.valueOf(100));
        lote.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        lote.setDictamen(DictamenEnum.CUARENTENA);
        lote.setObservaciones("Producción interna");

        // when
        Movimiento movimiento = MovimientoAltaUtils.createMovimientoAltaIngresoProduccion(lote, testUser);

        // then
        assertNotNull(movimiento);
        assertEquals(TipoMovimientoEnum.ALTA, movimiento.getTipoMovimiento());
        assertEquals(MotivoEnum.PRODUCCION_PROPIA, movimiento.getMotivo());
        assertEquals("_CU20_\nProducción interna", movimiento.getObservaciones());
    }

    @Test
    @DisplayName("createMovimientoAltaIngresoProduccion lanza NullPointerException si lote es null")
    void createMovimientoAltaIngresoProduccion_loteNull_lanzaExcepcion() {
        // when & then
        assertThrows(NullPointerException.class, () ->
            MovimientoAltaUtils.createMovimientoAltaIngresoProduccion(null, testUser)
        );
    }

    @Test
    @DisplayName("createMovimientoAltaDevolucion crea movimiento con motivo DEVOLUCION_VENTA")
    void createMovimientoAltaDevolucion_creaConMotivoDevolucion() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 3, 5, 9, 15, 0, 0, ZoneOffset.UTC);
        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaYHoraCreacion(timestamp);
        dto.setFechaMovimiento(timestamp.toLocalDate());
        dto.setCantidad(BigDecimal.valueOf(5));
        dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        dto.setDictamenInicial(DictamenEnum.LIBERADO);
        dto.setDictamenFinal(DictamenEnum.RECIBIDO);
        dto.setObservaciones("Devolución de cliente");

        Lote lote = new Lote();
        lote.setCodigoLote("LOT-DEV-01");

        // when
        Movimiento movimiento = MovimientoAltaUtils.createMovimientoAltaDevolucion(dto, lote, testUser);

        // then
        assertNotNull(movimiento);
        assertEquals(TipoMovimientoEnum.ALTA, movimiento.getTipoMovimiento());
        assertEquals(MotivoEnum.DEVOLUCION_VENTA, movimiento.getMotivo());
        assertEquals("LOT-DEV-01-25.03.05_09.15.00", movimiento.getCodigoMovimiento());
        assertEquals(BigDecimal.valueOf(5), movimiento.getCantidad());
        assertEquals("_CU23_\nDevolución de cliente", movimiento.getObservaciones());
        assertEquals(DictamenEnum.LIBERADO, movimiento.getDictamenInicial());
        assertEquals(DictamenEnum.RECIBIDO, movimiento.getDictamenFinal());
    }

    @Test
    @DisplayName("createMovimientoAltaDevolucion lanza NullPointerException si dto es null")
    void createMovimientoAltaDevolucion_dtoNull_lanzaExcepcion() {
        // given
        Lote lote = new Lote();

        // when & then
        assertThrows(NullPointerException.class, () ->
            MovimientoAltaUtils.createMovimientoAltaDevolucion(null, lote, testUser)
        );
    }

    @Test
    @DisplayName("createMovimientoAltaDevolucion lanza NullPointerException si lote es null")
    void createMovimientoAltaDevolucion_loteNull_lanzaExcepcion() {
        // given
        MovimientoDTO dto = new MovimientoDTO();

        // when & then
        assertThrows(NullPointerException.class, () ->
            MovimientoAltaUtils.createMovimientoAltaDevolucion(dto, null, testUser)
        );
    }

    @Test
    @DisplayName("createMovimientoAltaRecall crea movimiento con motivo RETIRO_MERCADO")
    void createMovimientoAltaRecall_creaConMotivoRecall() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 4, 20, 16, 45, 0, 0, ZoneOffset.UTC);
        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaYHoraCreacion(timestamp);
        dto.setFechaMovimiento(timestamp.toLocalDate());
        dto.setCantidad(BigDecimal.valueOf(50));
        dto.setUnidadMedida(UnidadMedidaEnum.GRAMO);
        dto.setObservaciones("Recall por lote defectuoso");

        Lote lote = new Lote();
        lote.setCodigoLote("LOT-RECALL-01");

        // when
        Movimiento movimiento = MovimientoAltaUtils.createMovimientoAltaRecall(dto, lote, testUser);

        // then
        assertNotNull(movimiento);
        assertEquals(TipoMovimientoEnum.ALTA, movimiento.getTipoMovimiento());
        assertEquals(MotivoEnum.RETIRO_MERCADO, movimiento.getMotivo());
        assertEquals("_CU24_\nRecall por lote defectuoso", movimiento.getObservaciones());
    }

    @Test
    @DisplayName("createMovimientoAltaRecall lanza NullPointerException si dto es null")
    void createMovimientoAltaRecall_dtoNull_lanzaExcepcion() {
        // given
        Lote lote = new Lote();

        // when & then
        assertThrows(NullPointerException.class, () ->
            MovimientoAltaUtils.createMovimientoAltaRecall(null, lote, testUser)
        );
    }

    @Test
    @DisplayName("createMovimientoAltaRecall lanza NullPointerException si lote es null")
    void createMovimientoAltaRecall_loteNull_lanzaExcepcion() {
        // given
        MovimientoDTO dto = new MovimientoDTO();

        // when & then
        assertThrows(NullPointerException.class, () ->
            MovimientoAltaUtils.createMovimientoAltaRecall(dto, null, testUser)
        );
    }

    @Test
    @DisplayName("addLoteInfoToMovimientoAlta completa el movimiento y agrega detalles por bulto")
    void addLoteInfoToMovimientoAlta_completaMovimientoYDetalles() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 5, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        Lote lote = new Lote();
        lote.setCodigoLote("LOT-INFO-01");
        lote.setFechaYHoraCreacion(timestamp);

        Bulto bulto1 = new Bulto();
        bulto1.setNroBulto(1);
        bulto1.setCantidadInicial(BigDecimal.TEN);
        bulto1.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        lote.getBultos().add(bulto1);

        Bulto bulto2 = new Bulto();
        bulto2.setNroBulto(2);
        bulto2.setCantidadInicial(BigDecimal.valueOf(20));
        bulto2.setUnidadMedida(UnidadMedidaEnum.GRAMO);
        lote.getBultos().add(bulto2);

        Movimiento movimiento = new Movimiento();

        // when
        MovimientoAltaUtils.addLoteInfoToMovimientoAlta(lote, movimiento);

        // then
        assertEquals("LOT-INFO-01-25.05.01_12.00.00", movimiento.getCodigoMovimiento());
        assertSame(lote, movimiento.getLote());
        assertEquals(2, movimiento.getDetalles().size());
    }

    @Test
    @DisplayName("addLoteInfoToMovimientoAlta lanza NullPointerException si lote es null")
    void addLoteInfoToMovimientoAlta_loteNull_lanzaExcepcion() {
        // given
        Movimiento movimiento = new Movimiento();

        // when & then
        assertThrows(NullPointerException.class, () ->
            MovimientoAltaUtils.addLoteInfoToMovimientoAlta(null, movimiento)
        );
    }

    @Test
    @DisplayName("addLoteInfoToMovimientoAlta lanza NullPointerException si movimiento es null")
    void addLoteInfoToMovimientoAlta_movimientoNull_lanzaExcepcion() {
        // given
        Lote lote = new Lote();

        // when & then
        assertThrows(NullPointerException.class, () ->
            MovimientoAltaUtils.addLoteInfoToMovimientoAlta(lote, null)
        );
    }

}

package com.mb.conitrack.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mb.conitrack.dto.MovimientoDTO;
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

class MovimientoModificacionUtilsTest {

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
        Constructor<MovimientoModificacionUtils> constructor = MovimientoModificacionUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
        assertEquals("Utility class cannot be instantiated", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("createMovimientoModifRecall crea movimiento MODIFICACION con motivo RETIRO_MERCADO")
    void createMovimientoModifRecall_creaTodosLosCampos() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 1, 20, 10, 0, 0, 0, ZoneOffset.UTC);
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote("LOT-RECALL-01");
        dto.setFechaYHoraCreacion(timestamp);
        dto.setFechaMovimiento(timestamp.toLocalDate());
        dto.setObservaciones("Recall por defecto de calidad");

        // when
        Movimiento movimiento = MovimientoModificacionUtils.createMovimientoModifRecall(dto, testUser);

        // then
        assertNotNull(movimiento);
        assertEquals(TipoMovimientoEnum.MODIFICACION, movimiento.getTipoMovimiento());
        assertEquals(MotivoEnum.RETIRO_MERCADO, movimiento.getMotivo());
        assertEquals(timestamp, movimiento.getFechaYHoraCreacion());
        assertEquals("LOT-RECALL-01-25.01.20_10.00.00", movimiento.getCodigoMovimiento());
        assertEquals(DictamenEnum.RETIRO_MERCADO, movimiento.getDictamenFinal());
        assertEquals(timestamp.toLocalDate(), movimiento.getFecha());
        assertTrue(movimiento.getActivo());
        assertEquals("_CU24_\nRecall por defecto de calidad", movimiento.getObservaciones());
    }

    @Test
    @DisplayName("createMovimientoModificacion crea movimiento MODIFICACION genérico")
    void createMovimientoModificacion_creaMovimientoGenerico() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 2, 10, 14, 30, 0, 0, ZoneOffset.UTC);
        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaYHoraCreacion(timestamp);
        dto.setFechaMovimiento(timestamp.toLocalDate());
        dto.setObservaciones("Modificación general");

        Lote lote = new Lote();
        lote.setCodigoLote("LOT-MODIF-01");

        // when
        Movimiento movimiento = MovimientoModificacionUtils.createMovimientoModificacion(dto, lote, testUser);

        // then
        assertNotNull(movimiento);
        assertEquals(TipoMovimientoEnum.MODIFICACION, movimiento.getTipoMovimiento());
        assertEquals(timestamp, movimiento.getFechaYHoraCreacion());
        assertEquals(timestamp.toLocalDate(), movimiento.getFecha());
        assertEquals("LOT-MODIF-01-25.02.10_14.30.00", movimiento.getCodigoMovimiento());
        assertEquals("Modificación general", movimiento.getObservaciones());
        assertSame(lote, movimiento.getLote());
        assertTrue(movimiento.getActivo());
    }

    @Test
    @DisplayName("createMovimientoReverso crea movimiento con motivo REVERSO y vincula movimiento origen")
    void createMovimientoReverso_vinculaMovimientoOrigen() {
        // given
        OffsetDateTime timestampOrigen = OffsetDateTime.of(2025, 3, 1, 9, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime timestampReverso = OffsetDateTime.of(2025, 3, 5, 11, 0, 0, 0, ZoneOffset.UTC);

        Lote lote = new Lote();
        lote.setCodigoLote("LOT-REV-01");
        lote.setCantidadActual(BigDecimal.valueOf(100));
        lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);

        Movimiento movimientoOrigen = new Movimiento();
        movimientoOrigen.setCodigoMovimiento("LOT-REV-01-25.03.01_09.00.00");
        movimientoOrigen.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        movimientoOrigen.setMotivo(MotivoEnum.VENTA);
        movimientoOrigen.setLote(lote);
        movimientoOrigen.setCantidad(BigDecimal.TEN);
        movimientoOrigen.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);

        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaYHoraCreacion(timestampReverso);
        dto.setFechaMovimiento(timestampReverso.toLocalDate());
        dto.setObservaciones("Reverso por error en venta");

        // when
        Movimiento movimientoReverso = MovimientoModificacionUtils.createMovimientoReverso(dto, movimientoOrigen, testUser);

        // then
        assertNotNull(movimientoReverso);
        assertEquals(TipoMovimientoEnum.MODIFICACION, movimientoReverso.getTipoMovimiento());
        assertEquals(MotivoEnum.REVERSO, movimientoReverso.getMotivo());
        assertEquals(timestampReverso, movimientoReverso.getFechaYHoraCreacion());
        assertEquals(timestampReverso.toLocalDate(), movimientoReverso.getFecha());
        assertEquals("LOT-REV-01-25.03.05_11.00.00", movimientoReverso.getCodigoMovimiento());
        assertSame(movimientoOrigen, movimientoReverso.getMovimientoOrigen());
        assertSame(lote, movimientoReverso.getLote());
        assertTrue(movimientoReverso.getActivo());
        assertEquals("Reverso por error en venta", movimientoReverso.getObservaciones());
    }

    @Test
    @DisplayName("createMovimientoModificacion permite configurar motivo y dictamen posteriormente")
    void createMovimientoModificacion_permiteConfiguracionPosterior() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 4, 15, 16, 0, 0, 0, ZoneOffset.UTC);
        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaYHoraCreacion(timestamp);
        dto.setFechaMovimiento(timestamp.toLocalDate());
        dto.setObservaciones("Resultado de análisis");

        Lote lote = new Lote();
        lote.setCodigoLote("LOT-ANALISIS-01");
        lote.setDictamen(DictamenEnum.CUARENTENA);

        // when
        Movimiento movimiento = MovimientoModificacionUtils.createMovimientoModificacion(dto, lote, testUser);
        // Configuración posterior (como lo haría el servicio)
        movimiento.setMotivo(MotivoEnum.ANALISIS);
        movimiento.setDictamenInicial(DictamenEnum.CUARENTENA);
        movimiento.setDictamenFinal(DictamenEnum.APROBADO);

        // then
        assertNotNull(movimiento);
        assertEquals(MotivoEnum.ANALISIS, movimiento.getMotivo());
        assertEquals(DictamenEnum.CUARENTENA, movimiento.getDictamenInicial());
        assertEquals(DictamenEnum.APROBADO, movimiento.getDictamenFinal());
    }

    @Test
    @DisplayName("createMovimientoModifRecall maneja observaciones null correctamente")
    void createMovimientoModifRecall_observacionesNull_noCausaError() {
        // given
        OffsetDateTime timestamp = OffsetDateTime.of(2025, 5, 1, 8, 0, 0, 0, ZoneOffset.UTC);
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoLote("LOT-RECALL-02");
        dto.setFechaYHoraCreacion(timestamp);
        dto.setFechaMovimiento(timestamp.toLocalDate());
        dto.setObservaciones(null);

        // when
        Movimiento movimiento = MovimientoModificacionUtils.createMovimientoModifRecall(dto, testUser);

        // then
        assertNotNull(movimiento);
        assertEquals("_CU24_\n", movimiento.getObservaciones());
    }

}

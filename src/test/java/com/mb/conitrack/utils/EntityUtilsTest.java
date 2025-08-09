package com.mb.conitrack.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityUtilsTest {

    @Test
    void createBultoIngreso() {
    }

    @Test
    @DisplayName("Mapea todos los campos y setea defaults (NUEVO/RECIBIDO/activo)")
    void createLoteIngreso_ok() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 1, 10, 11, 22, 33);
        LocalDate fechaIngreso = LocalDate.of(2025, 1, 11);
        LocalDate reanalisis = LocalDate.of(2026, 2, 1);
        LocalDate vencimiento = LocalDate.of(2027, 3, 1);

        LoteDTO dto = new LoteDTO();
        dto.setFechaYHoraCreacion(now);
        dto.setPaisOrigen("AR");
        dto.setFechaIngreso(fechaIngreso);
        dto.setBultosTotales(3);
        dto.setLoteProveedor("LP-777");
        dto.setFechaReanalisisProveedor(reanalisis);
        dto.setFechaVencimientoProveedor(vencimiento);
        dto.setNroRemito("REM-123");
        dto.setDetalleConservacion("Frío (2-8°C)");
        dto.setObservaciones("Observación de prueba");

        // when
        Lote lote = EntityUtils.getInstance().createLoteIngreso(dto);

        // then (defaults)
        assertNotNull(lote);
        assertEquals(now, lote.getFechaYHoraCreacion());
        assertEquals(EstadoEnum.NUEVO, lote.getEstado());
        assertEquals(DictamenEnum.RECIBIDO, lote.getDictamen());
        assertEquals(Boolean.TRUE, lote.getActivo());

        // then (mapeos 1:1)
        assertEquals("AR", lote.getPaisOrigen());
        assertEquals(fechaIngreso, lote.getFechaIngreso());
        assertEquals(3, lote.getBultosTotales());
        assertEquals("LP-777", lote.getLoteProveedor());
        assertEquals(reanalisis, lote.getFechaReanalisisProveedor());
        assertEquals(vencimiento, lote.getFechaVencimientoProveedor());
        assertEquals("REM-123", lote.getNroRemito());
        assertEquals("Frío (2-8°C)", lote.getDetalleConservacion());
        assertEquals("Observación de prueba", lote.getObservaciones());
    }

    @Test
    @DisplayName("Tolera opcionales nulos (se copian como null)")
    void createLoteIngreso_opcionalesNull() {
        // given
        LoteDTO dto = new LoteDTO();
        dto.setFechaYHoraCreacion(LocalDateTime.of(2025, 1, 1, 0, 0));
        dto.setPaisOrigen(null);
        dto.setFechaIngreso(LocalDate.of(2025, 1, 2));
        dto.setBultosTotales(1);
        dto.setLoteProveedor(null);
        dto.setFechaReanalisisProveedor(null);
        dto.setFechaVencimientoProveedor(null);
        dto.setNroRemito(null);
        dto.setDetalleConservacion(null);
        dto.setObservaciones(null);

        // when
        Lote lote = EntityUtils.getInstance().createLoteIngreso(dto);

        // then
        assertNull(lote.getPaisOrigen());
        assertNull(lote.getLoteProveedor());
        assertNull(lote.getFechaReanalisisProveedor());
        assertNull(lote.getFechaVencimientoProveedor());
        assertNull(lote.getNroRemito());
        assertNull(lote.getDetalleConservacion());
        assertNull(lote.getObservaciones());
        // y defaults siguen correctos
        assertEquals(EstadoEnum.NUEVO, lote.getEstado());
        assertEquals(DictamenEnum.RECIBIDO, lote.getDictamen());
        assertEquals(Boolean.TRUE, lote.getActivo());
    }

    @Test
    void createMovimientoAltaDevolucionVenta() {
    }

    @Test
    void createMovimientoAltaIngresoCompra() {
    }

    @Test
    @DisplayName("createMovimientoAltaIngresoCompra copia y calcula todos los campos correctamente")
    void createMovimientoAltaIngresoCompra_ok() {
        // given (un lote completo y determinista)
        LocalDateTime fch = LocalDateTime.of(2025, 1, 2, 3, 4, 5);
        String timestamp = fch.format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));

        Lote lote = new Lote();
        lote.setFechaYHoraCreacion(fch);
        lote.setCodigoInterno("L-API-XYZ");
        lote.setDictamen(DictamenEnum.RECIBIDO);
        lote.setObservaciones("Obs lote");

        // bultos en el lote para verificar que se copian
        Bulto b1 = new Bulto();
        b1.setCantidadInicial(BigDecimal.TEN);
        b1.setCantidadActual(BigDecimal.TEN);
        b1.setUnidadMedida(UnidadMedidaEnum.GRAMO);
        Bulto b2 = new Bulto();
        b2.setCantidadInicial(BigDecimal.ONE);
        b2.setCantidadActual(BigDecimal.ONE);
        b2.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        lote.getBultos().add(b1);
        lote.getBultos().add(b2);

        // when
        Movimiento mov = EntityUtils.createMovimientoAltaIngresoCompra(lote);

        // then
        assertNotNull(mov);

        // Tipo y motivo
        assertEquals(TipoMovimientoEnum.ALTA, mov.getTipoMovimiento());
        assertEquals(MotivoEnum.COMPRA, mov.getMotivo());

        // Fechas y código interno
        assertEquals(fch, mov.getFechaYHoraCreacion());
        assertEquals(fch.toLocalDate(), mov.getFecha());  // toLocalDate()
        String esperadoCodigo = lote.getCodigoInterno() + "-B_" + lote.getNroBulto() + "-" + timestamp;
        assertEquals(esperadoCodigo, mov.getCodigoInterno());

        // Cantidad, UM, dictamen final
        assertEquals(lote.getCantidadInicial(), mov.getCantidad());
        assertEquals(lote.getUnidadMedida(), mov.getUnidadMedida());
        assertEquals(lote.getDictamen(), mov.getDictamenFinal());

        // Lote y bultos copiados
        assertSame(lote, mov.getLote());
        assertEquals(2, mov.getBultos().size());
        assertTrue(mov.getBultos().contains(b1));
        assertTrue(mov.getBultos().contains(b2));

        // Activo y observaciones
        assertTrue(mov.getActivo());
        assertEquals("_CU1_\n" + lote.getObservaciones(), mov.getObservaciones());
    }

    @Test
    void createMovimientoAltaIngresoProduccion() {
    }

    @Test
    void createMovimientoModificacion() {
    }

    @Test
    void createMovimientoPorMuestreo() {
    }

    @Test
    void getAnalisisEnCurso() {
    }

}
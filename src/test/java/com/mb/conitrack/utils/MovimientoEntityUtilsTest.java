package com.mb.conitrack.utils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class MovimientoEntityUtilsTest {

    @Test
    @DisplayName("createMovimientoAltaIngresoCompra copia y calcula todos los campos correctamente")
    void createMovimientoAltaIngresoCompra_ok() {
        // given (un lote completo y determinista)
        OffsetDateTime fch = OffsetDateTime.of(2025, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC);
        String timestamp = fch.format(DateTimeFormatter.ofPattern("yy.MM.dd_HH.mm.ss"));

        Lote lote = new Lote();
        lote.setFechaYHoraCreacion(fch);
        lote.setCodigoLote("L-API-XYZ");
        lote.setDictamen(DictamenEnum.RECIBIDO);
        lote.setObservaciones("Obs lote");

        // bultos en el lote para verificar que se copian
        Bulto b1 = new Bulto();
        b1.setNroBulto(1);
        b1.setLote(lote);
        b1.setCantidadInicial(BigDecimal.TEN);
        b1.setCantidadActual(BigDecimal.TEN);
        b1.setUnidadMedida(UnidadMedidaEnum.GRAMO);

        Bulto b2 = new Bulto();
        b2.setNroBulto(2);
        b2.setLote(lote);
        b2.setCantidadInicial(BigDecimal.ONE);
        b2.setCantidadActual(BigDecimal.ONE);
        b2.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        lote.getBultos().add(b1);
        lote.getBultos().add(b2);

        // when
        Movimiento mov = MovimientoAltaUtils.createMovimientoAltaIngresoCompra(lote);

        // then
        assertNotNull(mov);

        // Tipo y motivo
        assertEquals(TipoMovimientoEnum.ALTA, mov.getTipoMovimiento());
        assertEquals(MotivoEnum.COMPRA, mov.getMotivo());

        // Fechas y código interno
        assertEquals(fch, mov.getFechaYHoraCreacion());
        assertEquals(fch.toLocalDate(), mov.getFecha());  // toLocalDate()
        String esperadoCodigo = lote.getCodigoLote() + "-" + timestamp;
        assertEquals(esperadoCodigo, mov.getCodigoMovimiento());

        // Cantidad, UM, dictamen final
        assertEquals(lote.getCantidadInicial(), mov.getCantidad());
        assertEquals(lote.getUnidadMedida(), mov.getUnidadMedida());
        assertEquals(lote.getDictamen(), mov.getDictamenFinal());

        // Lote y bultos copiados
        assertSame(lote, mov.getLote());
        assertTrue(mov.getDetalles().isEmpty());

        // Activo y observaciones
        assertTrue(mov.getActivo());
        assertEquals("_CU1_\n" + lote.getObservaciones(), mov.getObservaciones());
    }

}
package com.mb.conitrack.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
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
import static org.mockito.Mockito.when;

class LoteEntityUtilsTest {

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
        Lote lote = LoteEntityUtils.getInstance().createLoteIngreso(dto);

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
        Lote lote = LoteEntityUtils.getInstance().createLoteIngreso(dto);

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
    @DisplayName("fabricante presente -> pais de fabricante; bultosTotales > 1; codigo interno correcto; bultos linkeados")
    void fabricantePresentePaisFabricante_yLoopMultiBultos() throws Exception {
        // given
        LoteDTO dto = new LoteDTO();
        dto.setFabricanteId(99L);
        dto.setPaisOrigen(null); // dispara seteo de país
        dto.setBultosTotales(3);
        dto.setCantidadesBultos(java.util.List.of(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3")));
        dto.setUnidadMedidaBultos(java.util.List.of(
            UnidadMedidaEnum.GRAMO,
            UnidadMedidaEnum.GRAMO,
            UnidadMedidaEnum.GRAMO));
        dto.setFechaIngreso(LocalDate.now());
        dto.setFechaYHoraCreacion(LocalDateTime.of(2025, 1, 1, 12, 0, 0));

        Producto producto = new Producto();
        producto.setCodigoInterno("P-123");

        Proveedor proveedor = new Proveedor();
        proveedor.setPais("AR");

        Proveedor fabricante = new Proveedor();
        fabricante.setPais("BR");

        Lote lote = new Lote();

        LoteEntityUtils.getInstance().populateLoteAltaStockCompra(lote, dto, producto, proveedor, fabricante);

        // then
        assertEquals("L-P-123-25.01.01_12.00.00", lote.getCodigoInterno());
        assertSame(producto, lote.getProducto());
        assertSame(proveedor, lote.getProveedor());
        assertSame(fabricante, lote.getFabricante()); // fabricante seteado
        assertEquals("BR", lote.getPaisOrigen());     // país viene del fabricante

        assertEquals(3, lote.getBultos().size());
        for (int i = 0; i < 3; i++) {
            Bulto b = lote.getBultos().get(i);
            assertSame(lote, b.getLote());               // link lote<->bulto
            assertEquals(i + 1, b.getNroBulto());        // seteado en populateCantidadUdeMBulto
        }
    }

    @Test
    @DisplayName("fabricanteId presente pero findById vacío -> pais del proveedor; bultosTotales=0 -> 1 bulto")
    void fabricanteVacioPaisProveedor_yBultosMinimoUno() throws Exception {
        // given
        LoteDTO dto = new LoteDTO();
        dto.setFabricanteId(77L);
        dto.setPaisOrigen(null); // dispara seteo del país
        dto.setBultosTotales(0); // Math.max -> 1
        dto.setCantidadInicial(new BigDecimal("10"));
        dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        dto.setFechaIngreso(LocalDate.now());
        dto.setFechaYHoraCreacion(LocalDateTime.of(2025, 1, 1, 12, 0, 0));

        Producto producto = new Producto();
        producto.setCodigoInterno("P-XYZ");

        Proveedor proveedor = new Proveedor();
        proveedor.setPais("UY");

        Lote lote = new Lote();

        // when
        LoteEntityUtils.getInstance().populateLoteAltaStockCompra(lote, dto, producto, proveedor, null);
        // then
        assertEquals("L-P-XYZ-25.01.01_12.00.00", lote.getCodigoInterno());
        assertSame(producto, lote.getProducto());
        assertSame(proveedor, lote.getProveedor());
        assertNull(lote.getFabricante());         // no presente
        assertEquals("UY", lote.getPaisOrigen()); // país viene del proveedor

        assertEquals(1, lote.getBultos().size());
        Bulto b = lote.getBultos().get(0);
        assertSame(lote, b.getLote());
        assertEquals(1, b.getNroBulto());
    }

    @Test
    @DisplayName("paisOrigen en DTO NO vacío -> no se pisa; fabricanteId nulo -> no consulta proveedorService")
    void paisOrigenNoVacio_noOverride_yNoConsultaFabricante() throws Exception {
        // given
        LoteDTO dto = new LoteDTO();
        dto.setFabricanteId(null);            // no debe consultar proveedorService
        dto.setPaisOrigen("CL");              // NO vacío -> no se pisa
        dto.setBultosTotales(2);
        dto.setCantidadesBultos(java.util.List.of(BigDecimal.ONE, BigDecimal.TEN));
        dto.setUnidadMedidaBultos(java.util.List.of(UnidadMedidaEnum.GRAMO, UnidadMedidaEnum.GRAMO));
        dto.setFechaIngreso(LocalDate.now());
        dto.setFechaYHoraCreacion(LocalDateTime.of(2025, 1, 1, 12, 0, 0));

        Producto producto = new Producto();
        producto.setCodigoInterno("P-999");

        Proveedor proveedor = new Proveedor();
        proveedor.setPais("AR");

        Lote lote = new Lote();
        lote.setPaisOrigen("MX"); // valor previo que NO debe ser sobreescrito

        // when
        LoteEntityUtils.getInstance().populateLoteAltaStockCompra(lote, dto, producto, proveedor, null);

        // then
        assertEquals("L-P-999-25.01.01_12.00.00", lote.getCodigoInterno());
        assertSame(producto, lote.getProducto());
        assertSame(proveedor, lote.getProveedor());
        assertEquals("CL", lote.getPaisOrigen()); // NO pisado

        assertEquals(2, lote.getBultos().size());
        for (int i = 0; i < 2; i++) {
            Bulto b = lote.getBultos().get(i);
            assertSame(lote, b.getLote());
            assertEquals(i + 1, b.getNroBulto());
        }
    }

}
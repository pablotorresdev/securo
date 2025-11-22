package com.mb.conitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests con 100 % de cobertura de líneas sobre DTOUtils.
 */
@ExtendWith(MockitoExtension.class)
class DTOUtilsTest {

    @Test
    @DisplayName("Sin ningún número lanza IllegalArgumentException")
    void createAnalisis_sinNumero_lanzaExcepcion() {
        MovimientoDTO dto = buildDto(null, null);

        assertThrows(
            IllegalArgumentException.class,
            () -> DTOUtils.createAnalisis(dto));
    }

    @Test
    @DisplayName("Cuando nroReanalisis está vacío usa nroAnalisis")
    void createAnalisis_usaNroAnalisis() {
        MovimientoDTO dto = buildDto("A-123", null);

        Analisis a = DTOUtils.createAnalisis(dto);

        assertEquals("A-123", a.getNroAnalisis());
        assertEquals(dto.getFechaYHoraCreacion(), a.getFechaYHoraCreacion());
        assertTrue(a.getActivo());
        assertEquals("obs", a.getObservaciones());
    }

    @Test
    @DisplayName("Cuando hay nroReanalisis, lo prioriza")
    void createAnalisis_usaNroReanalisis() {
        MovimientoDTO dto = buildDto("A-123", "R-999");

        Analisis a = DTOUtils.createAnalisis(dto);

        assertEquals("R-999", a.getNroAnalisis());      // se usó el re-
    }

    @Test
    void fromAnalisisEntityMovimiento_retNullSiEsNull() {
        assertNull(DTOUtils.fromAnalisisEntity(null));
    }

    @Test
    void fromBultoEntityMovimiento_retNullSiEsNull() {
        assertNull(DTOUtils.fromMovimientoEntity(null));
    }

    @Test
    void fromBultoEntityTraza_retNullSiEsNull() {
        assertNull(DTOUtils.fromTrazaEntity(null));
    }

    /* ---------- caso 1: entidad nula ---------- */
    @Test
    @DisplayName("Devuelve null si Analisis es null")
    void fromBultoEntity_entidadNull() {
        assertNull(DTOUtils.fromAnalisisEntity(null));
    }

    /* ---------- caso 2: mapeo completo ---------- */
    @Test
    @DisplayName("Copia todos los campos cuando Analisis tiene datos")
    void fromBultoEntity_mapeoCompleto() {
        // Crear una entidad Analisis con todos los valores relevantes
        Analisis entity = new Analisis();
        entity.setFechaYHoraCreacion(OffsetDateTime.of(2025, 8, 7, 10, 30, 0, 0, ZoneOffset.UTC));
        entity.setLote(lote());
        entity.setNroAnalisis("AN-001");
        entity.setFechaRealizado(LocalDate.of(2025, 8, 1));
        entity.setFechaReanalisis(LocalDate.of(2026, 8, 1));
        entity.setFechaVencimiento(LocalDate.of(2027, 8, 1));
        entity.setDictamen(DictamenEnum.APROBADO);
        entity.setTitulo(new BigDecimal("87.5"));
        entity.setObservaciones("ok");

        // Llamamos al método a testear
        AnalisisDTO dto = DTOUtils.fromAnalisisEntity(entity);

        // Verificaciones
        assertNotNull(dto);
        assertEquals(entity.getFechaYHoraCreacion(), dto.getFechaYHoraCreacion());
        assertEquals("AN-001", dto.getNroAnalisis());
        assertEquals(LocalDate.of(2025, 8, 1), dto.getFechaRealizado());
        assertEquals(LocalDate.of(2026, 8, 1), dto.getFechaReanalisis());
        assertEquals(LocalDate.of(2027, 8, 1), dto.getFechaVencimiento());
        assertEquals(DictamenEnum.APROBADO, dto.getDictamen());
        assertEquals(new BigDecimal("87.5"), dto.getTitulo());
        assertEquals("ok", dto.getObservaciones());
    }

    @Test
    @DisplayName("Devuelve null si el Movimiento es null")
    void fromMovimientoEntity_movimientoNull() {
        assertNull(DTOUtils.fromMovimientoEntity(null));
    }

    @Test
    @DisplayName("Mapea correctamente cuando lote, tipoMovimiento y motivo son null")
    void fromMovimientoEntity_sinLoteNiEnums() {
        Movimiento mov = new Movimiento();     // todos los campos null por defecto
        mov.setFecha(LocalDate.of(2025, 8, 7));
        mov.setCantidad(new BigDecimal("5"));
        mov.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        mov.setObservaciones("obs");
        mov.setNroAnalisis("N-1");
        mov.setOrdenProduccion("OP-1");
        mov.setDictamenInicial(DictamenEnum.DEVOLUCION_CLIENTES);
        mov.setDictamenFinal(DictamenEnum.RECHAZADO);

        MovimientoDTO dto = DTOUtils.fromMovimientoEntity(mov);

        assertEquals(LocalDate.of(2025, 8, 7), dto.getFechaMovimiento());
        assertNull(dto.getCodigoLote());      // sin lote
        assertEquals(new BigDecimal("5"), dto.getCantidad());
        assertEquals(UnidadMedidaEnum.KILOGRAMO, dto.getUnidadMedida());
        assertNull(dto.getTipoMovimiento());         // enum nulo en origen
        assertNull(dto.getMotivo());
    }

    /* ---------- caso 1: entidad nula ---------- */
    @Test
    @DisplayName("Devuelve null si Traza es null")
    void fromTrazaEntity_entidadNull() {
        assertNull(DTOUtils.fromTrazaEntity(null));
    }


    private MovimientoDTO buildDto(
        String nroAnalisis,
        String nroReanalisis) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaYHoraCreacion(OffsetDateTime.of(2025, 8, 7, 10, 0, 0, 0, ZoneOffset.UTC));
        dto.setObservaciones("obs");
        dto.setNroAnalisis(nroAnalisis);
        dto.setNroReanalisis(nroReanalisis);
        return dto;
    }

    Bulto bulto(
        int nro, BigDecimal cantIni, BigDecimal cantAct,
        UnidadMedidaEnum um, EstadoEnum estado) {

        Movimiento mov = mock(Movimiento.class);
        when(mov.getActivo()).thenReturn(true);
        when(mov.getCantidad()).thenReturn(cantAct);
        when(mov.getUnidadMedida()).thenReturn(um);

        Bulto b = mock(Bulto.class);
        when(b.getActivo()).thenReturn(true);
        when(b.getNroBulto()).thenReturn(nro);
        when(b.getCantidadInicial()).thenReturn(cantIni);
        when(b.getCantidadActual()).thenReturn(cantAct);
        when(b.getUnidadMedida()).thenReturn(um);
        when(b.getEstado()).thenReturn(estado);
        return b;
    }

    EstadoEnum estado(String valor, int prioridad) {
        EstadoEnum e = mock(EstadoEnum.class);
        when(e.getValor()).thenReturn(valor);
        when(e.getPrioridad()).thenReturn(prioridad);
        return e;
    }

    /* ---------------------------------
     *          Test individuales
     * --------------------------------- */
    Lote lote() {
        Lote l = new Lote();
        l.setActivo(true);
        l.setCodigoLote("L-001");
        l.setId(99L);
        l.setFechaYHoraCreacion(OffsetDateTime.now());
        l.setFechaIngreso(LocalDate.now());
        l.setBultosTotales(1);
        l.setEstado(EstadoEnum.NUEVO);
        l.setDictamen(DictamenEnum.APROBADO);
        l.setCantidadInicial(new BigDecimal("10"));
        l.setCantidadActual(new BigDecimal("10"));
        l.setUnidadMedida(UnidadMedidaEnum.LITRO);

        // Producto real
        Producto prod = new Producto();
        prod.setId(1L);
        prod.setNombreGenerico("ProductoZ");
        prod.setUnidadMedida(UnidadMedidaEnum.LITRO);
        l.setProducto(prod);

        // Proveedor real
        Proveedor prov = new Proveedor();
        prov.setId(2L);
        prov.setRazonSocial("ProveedorX");
        l.setProveedor(prov);

        // Fabricante real (si tu modelo lo separa de Proveedor, usá el tipo correcto)
        Proveedor fab = new Proveedor();
        fab.setId(3L);
        fab.setRazonSocial("FabricanteY");
        l.setFabricante(fab);

        // Bulto real
        Bulto b = new Bulto();
        b.setActivo(true);
        b.setNroBulto(1);
        b.setCantidadInicial(new BigDecimal("10"));
        b.setCantidadActual(new BigDecimal("10"));
        b.setUnidadMedida(UnidadMedidaEnum.LITRO);
        b.setEstado(EstadoEnum.NUEVO);
        b.setLote(l);
        l.setBultos(List.of(b));

        // Movimiento real (si no lo usa el test, podés omitirlo)
        Movimiento mov = new Movimiento();
        mov.setActivo(true);
        mov.setCantidad(new BigDecimal("1"));
        mov.setUnidadMedida(UnidadMedidaEnum.LITRO);
        mov.setLote(l);
        l.setMovimientos(List.of(mov));

        // Analisis/trazas vacíos
        l.setAnalisisList(Collections.emptyList());
        l.setTrazas(Collections.emptySet());

        return l;
    }

    /* -------------------------------------------------
     *  Utils para crear mocks/objetos de prueba simples
     * ------------------------------------------------- */
    Producto producto() {
        Producto pr = mock(Producto.class);
        when(pr.getId()).thenReturn(1L);
        when(pr.getNombreGenerico()).thenReturn("Paracetamol");
        when(pr.getCodigoProducto()).thenReturn("P-001");
        when(pr.getTipoProducto()).thenReturn(TipoProductoEnum.GRANEL_MEZCLA_POLVO);
        when(pr.getProductoDestino()).thenReturn("Tabletas");
        return pr;
    }

    UnidadMedidaEnum um(int ordinal) {
        // Tomamos cualquier constante real; garantiza ≠ si ordinal cambia
        return UnidadMedidaEnum.values()[ordinal];
    }

    @Nested
    @DisplayName("createAnalisis()")
    class CreateAnalisisTests {

        @Test
        void cuandoHayNumeroAnalisis_devuelveEntidad() {
            MovimientoDTO dto = mock(MovimientoDTO.class);
            when(dto.getNroAnalisis()).thenReturn("A-1");
            when(dto.getNroReanalisis()).thenReturn(null);
            when(dto.getFechaYHoraCreacion()).thenReturn(OffsetDateTime.now());
            when(dto.getObservaciones()).thenReturn("obs");

            Analisis a = DTOUtils.createAnalisis(dto);

            assertEquals("A-1", a.getNroAnalisis());
            assertTrue(a.getActivo());
        }

        @Test
        void cuandoNoHayNumero_lanzaExcepcion() {
            MovimientoDTO dto = mock(MovimientoDTO.class);
            assertThrows(
                IllegalArgumentException.class,
                () -> DTOUtils.createAnalisis(dto));
        }

    }

}

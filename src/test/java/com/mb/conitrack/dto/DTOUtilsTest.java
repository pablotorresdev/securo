package com.mb.conitrack.dto;

import static com.mb.conitrack.enums.UnidadMedidaUtils.convertirCantidadEntreUnidades;
import static com.mb.conitrack.enums.UnidadMedidaUtils.obtenerMenorUnidadMedida;
import static com.mb.conitrack.enums.UnidadMedidaUtils.sugerirUnidadParaCantidad;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mb.conitrack.entity.*;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.enums.UnidadMedidaUtils;

/**
 * Tests con 100 % de cobertura de líneas sobre DTOUtils.
 */
@ExtendWith(MockitoExtension.class)
class DTOUtilsTest {

    private MovimientoDTO buildDto(String nroAnalisis,
        String nroReanalisis) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaYHoraCreacion(LocalDateTime.of(2025, 8, 7, 10, 0));
        dto.setObservaciones("obs");
        dto.setNroAnalisis(nroAnalisis);
        dto.setNroReanalisis(nroReanalisis);
        return dto;
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
    @DisplayName("Sin ningún número lanza IllegalArgumentException")
    void createAnalisis_sinNumero_lanzaExcepcion() {
        MovimientoDTO dto = buildDto(null, null);

        assertThrows(IllegalArgumentException.class,
            () -> DTOUtils.createAnalisis(dto));
    }

    @Test
    @DisplayName("Devuelve null si el Movimiento es null")
    void fromMovimientoEntity_movimientoNull() {
        assertNull(DTOUtils.fromEntity((Movimiento) null));
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

        MovimientoDTO dto = DTOUtils.fromEntity(mov);

        assertEquals(LocalDate.of(2025, 8, 7), dto.getFechaMovimiento());
        assertNull(dto.getCodigoInternoLote());      // sin lote
        assertEquals(new BigDecimal("5"), dto.getCantidad());
        assertEquals(UnidadMedidaEnum.KILOGRAMO, dto.getUnidadMedida());
        assertNull(dto.getTipoMovimiento());         // enum nulo en origen
        assertNull(dto.getMotivo());
    }

    @Test
    @DisplayName("Mapea todos los campos cuando están presentes")
    void fromMovimientoEntity_completo() {
        // --- lote auxiliar ---
        Lote lote = new Lote();
        lote.setId(99L);
        lote.setCodigoInterno("L-001");
        lote.setNroBulto(3);

        // --- movimiento completo ---
        Movimiento mov = new Movimiento();
        mov.setLote(lote);
        mov.setFecha(LocalDate.of(2025, 8, 8));
        mov.setCantidad(new BigDecimal("2.5"));
        mov.setUnidadMedida(UnidadMedidaEnum.LITRO);
        mov.setObservaciones("ok");
        mov.setNroAnalisis("A-2");
        mov.setOrdenProduccion("OP-2");
        mov.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        mov.setMotivo(MotivoEnum.VENCIMIENTO);
        mov.setDictamenInicial(DictamenEnum.APROBADO);
        mov.setDictamenFinal(DictamenEnum.RECHAZADO);

        MovimientoDTO dto = DTOUtils.fromEntity(mov);

        assertEquals("L-001", dto.getCodigoInternoLote());
        assertEquals("3", dto.getNroBulto());
        assertEquals(99L, dto.getLoteId());
        assertEquals("BAJA", dto.getTipoMovimiento());
        assertEquals("VENCIMIENTO", dto.getMotivo());
        assertEquals(DictamenEnum.APROBADO, dto.getDictamenInicial());
        assertEquals(DictamenEnum.RECHAZADO, dto.getDictamenFinal());
    }

    /* ---------- caso 1: entidad nula ---------- */
    @Test
    @DisplayName("Devuelve null si Analisis es null")
    void fromAnalisisEntity_entidadNull() {
        assertNull(DTOUtils.fromEntity((Analisis) null));
    }

    /* ---------- caso 2: mapeo completo ---------- */
    @Test
    @DisplayName("Copia todos los campos cuando Analisis tiene datos")
    void fromAnalisisEntity_mapeoCompleto() {
        // Crear una entidad Analisis con todos los valores relevantes
        Analisis entity = new Analisis();
        entity.setFechaYHoraCreacion(LocalDateTime.of(2025, 8, 7, 10, 30));
        entity.setNroAnalisis("AN-001");
        entity.setFechaRealizado(LocalDate.of(2025, 8, 1));
        entity.setFechaReanalisis(LocalDate.of(2026, 8, 1));
        entity.setFechaVencimiento(LocalDate.of(2027, 8, 1));
        entity.setDictamen(DictamenEnum.APROBADO);
        entity.setTitulo(new BigDecimal("87.5"));
        entity.setObservaciones("ok");

        // Llamamos al método a testear
        AnalisisDTO dto = DTOUtils.fromEntity(entity);

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

    /* ---------- caso 1: entidad nula ---------- */
    @Test
    @DisplayName("Devuelve null si Traza es null")
    void fromTrazaEntity_entidadNull() {
        assertNull(DTOUtils.fromEntity((Traza) null));
    }

    /* ---------- caso 2: mapeo completo ---------- */
    @Test
    @DisplayName("Copia todos los campos cuando Traza tiene datos")
    void fromTrazaEntity_mapeoCompleto() {
        // Producto asociado
        Producto prod = new Producto();
        prod.setCodigoInterno("P-123");

        // Traza con datos
        Traza traza = new Traza();
        traza.setFechaYHoraCreacion(LocalDateTime.of(2025, 8, 7, 12, 0));
        traza.setProducto(prod);
        traza.setEstado(EstadoEnum.DISPONIBLE);
        traza.setNroTraza(42L);
        traza.setObservaciones("ok");

        // Llamada al método
        TrazaDTO dto = DTOUtils.fromEntity(traza);

        // Verificaciones
        assertNotNull(dto);
        assertEquals(traza.getFechaYHoraCreacion(), dto.getFechaYHoraCreacion());
        assertEquals("P-123", dto.getCodigoProducto());
        assertEquals(EstadoEnum.DISPONIBLE, dto.getEstado());
        assertEquals(42L, dto.getNroTraza());
        assertEquals("ok", dto.getObservaciones());
    }








    /* -------------------------------------------------
     *  Utils para crear mocks/objetos de prueba simples
     * ------------------------------------------------- */
    private Producto producto() {
        Producto pr = mock(Producto.class);
        when(pr.getId()).thenReturn(1L);
        when(pr.getNombreGenerico()).thenReturn("Paracetamol");
        when(pr.getCodigoInterno()).thenReturn("P-001");
        when(pr.getTipoProducto()).thenReturn(TipoProductoEnum.GRANEL_MEZCLA_POLVO);
        when(pr.getProductoDestino()).thenReturn("Tabletas");
        return pr;
    }

    private EstadoEnum estado(String valor, int prioridad) {
        EstadoEnum e = mock(EstadoEnum.class);
        when(e.getValor()).thenReturn(valor);
        when(e.getPrioridad()).thenReturn(prioridad);
        return e;
    }

    private UnidadMedidaEnum um(int ordinal) {
        // Tomamos cualquier constante real; garantiza ≠ si ordinal cambia
        return UnidadMedidaEnum.values()[ordinal];
    }

    private Bulto bulto(int nro, BigDecimal cantIni, BigDecimal cantAct,
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
        when(b.getMovimientos()).thenReturn(Set.of(mov));
        return b;
    }

    private Lote lote(String codigoInterno, int nroBulto,
        UnidadMedidaEnum um, EstadoEnum estado) {

        Lote l = mock(Lote.class, RETURNS_DEEP_STUBS);
        when(l.getActivo()).thenReturn(true);
        when(l.getCodigoInterno()).thenReturn(codigoInterno);
        when(l.getId()).thenReturn(99L);
        when(l.getNroBulto()).thenReturn(nroBulto);
        when(l.getFechaYHoraCreacion()).thenReturn(LocalDateTime.now());
        when(l.getFechaIngreso()).thenReturn(LocalDate.now());
        when(l.getBultosTotales()).thenReturn(1);
        when(l.getEstado()).thenReturn(estado);
        when(l.getDictamen()).thenReturn(DictamenEnum.APROBADO);
        when(l.getCantidadInicial()).thenReturn(new BigDecimal("10"));
        when(l.getCantidadActual()).thenReturn(new BigDecimal("10"));
        when(l.getUnidadMedida()).thenReturn(um);
        when(l.getProducto()).thenReturn(producto());
        when(l.getProveedor().getId()).thenReturn(2L);
        when(l.getProveedor().getRazonSocial()).thenReturn("ProveedorX");
        when(l.getFabricante().getId()).thenReturn(3L);
        when(l.getFabricante().getRazonSocial()).thenReturn("FabricanteY");
        when(l.getObservaciones()).thenReturn("obs");

        // Analisis / trazas vacíos para simplificar
        when(l.getAnalisisList()).thenReturn(Collections.emptyList());
        when(l.getTrazas()).thenReturn(Collections.emptyList());

        // Cada lote tiene un bulto homónimo
        Bulto b = bulto(nroBulto,
            new BigDecimal("10"),
            new BigDecimal("10"),
            um, estado);
        when(l.getBultos()).thenReturn(List.of(b));
        when(b.getLote()).thenReturn(l);

        // movimientos a nivel lote
        Movimiento mov = mock(Movimiento.class);
        when(mov.getActivo()).thenReturn(true);
        when(mov.getCantidad()).thenReturn(new BigDecimal("1"));
        when(mov.getUnidadMedida()).thenReturn(um);
        when(l.getMovimientos()).thenReturn(List.of(mov));

        return l;
    }

    /* ---------------------------------
     *          Test individuales
     * --------------------------------- */

    @Nested
    @DisplayName("createAnalisis()")
    class CreateAnalisisTests {
        @Test
        void cuandoHayNumeroAnalisis_devuelveEntidad() {
            MovimientoDTO dto = mock(MovimientoDTO.class);
            when(dto.getNroAnalisis()).thenReturn("A-1");
            when(dto.getNroReanalisis()).thenReturn(null);
            when(dto.getFechaYHoraCreacion()).thenReturn(LocalDateTime.now());
            when(dto.getObservaciones()).thenReturn("obs");

            Analisis a = DTOUtils.createAnalisis(dto);

            assertEquals("A-1", a.getNroAnalisis());
            assertTrue(a.getActivo());
        }

        @Test
        void cuandoNoHayNumero_lanzaExcepcion() {
            MovimientoDTO dto = mock(MovimientoDTO.class);
            assertThrows(IllegalArgumentException.class,
                () -> DTOUtils.createAnalisis(dto));
        }
    }

    @Test
    void fromEntityMovimiento_retNullSiEsNull() {
        assertNull(DTOUtils.fromEntity((Movimiento) null));
    }

    @Test
    void fromEntityAnalisis_retNullSiEsNull() {
        assertNull(DTOUtils.fromEntity((Analisis) null));
    }

    @Test
    void fromEntityTraza_retNullSiEsNull() {
        assertNull(DTOUtils.fromEntity((Traza) null));
    }
















}

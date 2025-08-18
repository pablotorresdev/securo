package com.mb.conitrack.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.utils.UnidadMedidaUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoteServiceTest {

    @Mock
    LoteRepository loteRepository;

    @Mock
    ProveedorService proveedorService;

    @Mock
    ProductoService productoService;

    @Mock
    BultoService bultoService;

    @Mock
    MovimientoService movimientoService;

    @Mock
    AnalisisService analisisService;

    @Mock
    TrazaService trazaService;

    @Mock
    QueryServiceLote queryServiceLote;

    @InjectMocks
    LoteService service;

    @Test
    @DisplayName("altaStockPorCompra - producto inexistente -> IllegalArgumentException")
    void altaStockPorCompra_sinProducto() {
        // given
        LoteDTO dto = dtoBase();

        Proveedor prov = new Proveedor();
        when(proveedorService.findById(1L)).thenReturn(Optional.of(prov));

        when(productoService.findById(2L)).thenReturn(Optional.empty());

        // when / then
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.altaStockPorCompra(dto));
        assertEquals("El producto no existe.", ex.getMessage());

        verify(proveedorService).findById(1L);
        verify(productoService).findById(2L);
        verifyNoInteractions(loteRepository, bultoService, movimientoService);
    }

    @Test
    @DisplayName("altaStockPorCompra - proveedor inexistente -> IllegalArgumentException")
    void altaStockPorCompra_sinProveedor() {
        // given
        LoteDTO dto = dtoBase();
        when(proveedorService.findById(1L)).thenReturn(Optional.empty());

        // when / then
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.altaStockPorCompra(dto));
        assertEquals("El proveedor no existe.", ex.getMessage());

        // No debería avanzar a producto ni a guardar
        verify(proveedorService).findById(1L);
        verifyNoInteractions(productoService, loteRepository, bultoService, movimientoService);
    }

    @Test
    void altaStockPorProduccion() {
    }

    @Test
    void bajaConsumoProduccion() {
    }

    @Test
    void bajaDevolucionCompra() {
    }

    @Test
    @DisplayName("bajaMuestreo - nroAnalisis no coincide con el del lote -> IllegalArgumentException")
    void bajaMuestreo_nroAnalisisMismatch() {
        // given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setNroAnalisis("AN-DTO");

        Lote lote = Mockito.spy(new Lote());
        doReturn("AN-LOTE").when(lote).getUltimoNroAnalisis(); // difiere

        Producto prod = new Producto();
        prod.setTipoProducto(TipoProductoEnum.API);
        lote.setProducto(prod);

        Bulto b = new Bulto();
        b.setLote(lote);
        lote.getBultos().add(b);

        // when / then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.bajaMuestreo(dto, b));
        assertEquals("El número de análisis no coincide con el análisis en curso", ex.getMessage());

        verifyNoInteractions(movimientoService, trazaService, loteRepository);
    }

    @Test
    @DisplayName("bajaMuestreo - producto NO UNIDAD_VENTA -> no usa traza; estados y guardado")
    void bajaMuestreo_productoNoUnidadVenta() {
        // given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setNroAnalisis("AN-OK");

        Lote lote = Mockito.spy(new Lote());
        doReturn("AN-OK").when(lote).getUltimoNroAnalisis();

        Producto prod = new Producto();
        prod.setTipoProducto(TipoProductoEnum.API); // != UNIDAD_VENTA
        lote.setProducto(prod);

        Bulto b = new Bulto();
        b.setLote(lote);
        b.setCantidadActual(new BigDecimal("10"));
        lote.getBultos().add(b);

        Movimiento mov = new Movimiento();
        mov.setCantidad(new BigDecimal("3.00"));
        mov.setUnidadMedida(UnidadMedidaEnum.GRAMO);

        when(movimientoService.persistirMovimientoMuestreo(dto, b)).thenReturn(mov);

        // restarMovimientoConvertido -> deja 4 (no consumido)
        try (MockedStatic<UnidadMedidaUtils> ms = mockStatic(UnidadMedidaUtils.class)) {
            ms.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(dto, b)).thenReturn(new BigDecimal("4.0000"));

            when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            Lote out = service.bajaMuestreo(dto, b);

            // then
            assertSame(lote, out);
            assertEquals(new BigDecimal("4.0000"), b.getCantidadActual());
            assertEquals(EstadoEnum.EN_USO, b.getEstado());   // >0 => EN_USO
            assertEquals(EstadoEnum.EN_USO, out.getEstado()); // no todos consumidos
            assertTrue(out.getMovimientos().contains(mov));

            verify(movimientoService).persistirMovimientoMuestreo(dto, b);
            ms.verify(() -> UnidadMedidaUtils.restarMovimientoConvertido(dto, b));
            verify(loteRepository).save(out);
            verifyNoInteractions(trazaService);
        }
    }

    @Test
    @DisplayName("bajaMuestreo - UNIDAD_VENTA con unidad ≠ UNIDAD -> IllegalStateException")
    void bajaMuestreo_unidadVenta_unidadIncorrecta() {
        // given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setNroAnalisis("AN-OK");

        Lote lote = Mockito.spy(new Lote());
        doReturn("AN-OK").when(lote).getUltimoNroAnalisis();

        Producto prod = new Producto();
        prod.setTipoProducto(TipoProductoEnum.UNIDAD_VENTA);
        lote.setProducto(prod);

        Bulto b = new Bulto();
        b.setLote(lote);
        b.setCantidadActual(new BigDecimal("5"));
        lote.getBultos().add(b);

        Movimiento mov = new Movimiento();
        mov.setCantidad(BigDecimal.ONE);
        mov.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO); // != UNIDAD

        when(movimientoService.persistirMovimientoMuestreo(dto, b)).thenReturn(mov);

        try (MockedStatic<UnidadMedidaUtils> ms = mockStatic(UnidadMedidaUtils.class)) {
            ms.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(dto, b)).thenReturn(new BigDecimal("4"));

            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.bajaMuestreo(dto, b));
            assertEquals("La traza solo es aplicable a UNIDADES", ex.getMessage());

            verify(movimientoService).persistirMovimientoMuestreo(dto, b);
            ms.verify(() -> UnidadMedidaUtils.restarMovimientoConvertido(dto, b));
            verifyNoInteractions(trazaService, loteRepository);
        }
    }

    @Test
    @DisplayName("bajaMuestreo - UNIDAD_VENTA con UNIDAD pero cantidad no-entera -> IllegalStateException")
    void bajaMuestreo_unidadVenta_unidadValida_cantidadNoEntera() {
        // given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setNroAnalisis("AN-OK");

        Lote lote = Mockito.spy(new Lote());
        doReturn("AN-OK").when(lote).getUltimoNroAnalisis();

        Producto prod = new Producto();
        prod.setTipoProducto(TipoProductoEnum.UNIDAD_VENTA);
        lote.setProducto(prod);

        Bulto b = new Bulto();
        b.setLote(lote);
        b.setCantidadActual(new BigDecimal("5"));
        lote.getBultos().add(b);

        Movimiento mov = new Movimiento();
        mov.setCantidad(new BigDecimal("1.50"));       // no entero
        mov.setUnidadMedida(UnidadMedidaEnum.UNIDAD);  // unidad válida

        when(movimientoService.persistirMovimientoMuestreo(dto, b)).thenReturn(mov);

        try (MockedStatic<UnidadMedidaUtils> ms = mockStatic(UnidadMedidaUtils.class)) {
            ms.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(dto, b)).thenReturn(new BigDecimal("3.5"));

            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.bajaMuestreo(dto, b));
            assertEquals("La cantidad de Unidades debe ser entero", ex.getMessage());

            verify(movimientoService).persistirMovimientoMuestreo(dto, b);
            ms.verify(() -> UnidadMedidaUtils.restarMovimientoConvertido(dto, b));
            verifyNoInteractions(trazaService, loteRepository);
        }
    }

    @Test
    void bajaVentaProducto() {
    }

    @Test
    void findAllForConsumoProduccion() {
    }

    @Test
    void findAllForCuarentena() {
    }

    @Test
    void findAllForDevolucionCompra() {
    }

    @Test
    void findAllForDevolucionVenta() {
    }

    @Test
    void findAllForLiberacionProducto() {
    }

    @Test
    void findAllForMuestreo() {
    }

    @Test
    void findAllForReanalisisProducto() {
    }

    @Test
    void findAllForResultadoAnalisis() {
    }

    @Test
    void findAllForVentaProducto() {
    }

    @Test
    void findAllLotesAnalisisExpirado() {
    }

    @Test
    void findAllLotesDictaminados() {
    }

    @Test
    void findAllLotesVencidos() {
    }

    @Test
    void findAllSortByDate() {
    }

    @Test
    void findAllSortByDateAudit() {
    }



    @Test
    void findLoteBultoByCodigoAndBulto() {
    }

    @Test
    void findLoteBultoById() {
    }

    @Test
    void findLoteListByCodigoInterno() {
    }

    @Test
    void findMaxNroTraza() {
    }

    @Test
    void persistirDictamenCuarentena() {
    }

    @Test
    @DisplayName("persistirDictamenCuarentena - análisis existente: NO crea nuevo, usa nroAnalisis de DTO")
    void persistirDictamenCuarentena_conAnalisisExistente() {
        // given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setNroAnalisis("AN-001"); // nroReanalisis vacío => usa nroAnalisis
        dto.setFechaYHoraCreacion(LocalDateTime.now());

        Analisis existente = new Analisis();
        existente.setNroAnalisis("AN-001");
        when(analisisService.findByNroAnalisis("AN-001")).thenReturn(existente);

        // 2 lotes para ejercitar el loop
        Lote lote1 = new Lote();

        Movimiento m1 = new Movimiento();
        m1.setDictamenFinal(DictamenEnum.CUARENTENA);
        Movimiento m2 = new Movimiento();
        m2.setDictamenFinal(DictamenEnum.CUARENTENA);

        when(movimientoService.persistirMovimientoCuarentenaPorAnalisis(
            eq(dto),
            eq(lote1),
            eq("AN-001"))).thenReturn(m1);

        when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Lote out = service.persistirDictamenCuarentena(dto, lote1);

        // then
        assertNotNull(out);

        // Se setea dictamen y se agrega el movimiento
        assertEquals(DictamenEnum.CUARENTENA, out.getDictamen());
        assertTrue(out.getMovimientos().contains(m1));
        assertTrue(out.getAnalisisList().isEmpty()); // NO agrega análisis

        verify(analisisService).findByNroAnalisis("AN-001");
        verify(analisisService, never()).save(any());

        verify(movimientoService).persistirMovimientoCuarentenaPorAnalisis(dto, lote1, "AN-001");

        verify(loteRepository).save(lote1);
        verifyNoMoreInteractions(loteRepository, movimientoService, analisisService);
    }

    @Test
    @DisplayName("persistirDictamenCuarentena - sin análisis existente: crea y usa el nro del nuevo Analisis, lo agrega a los lotes")
    void persistirDictamenCuarentena_creaNuevoAnalisis() {
        // given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setNroAnalisis(null);
        dto.setNroReanalisis("RE-009"); // no vacío => toma éste
        dto.setFechaYHoraCreacion(LocalDateTime.now());

        when(analisisService.findByNroAnalisis("RE-009")).thenReturn(null);

        // El save devuelve un Analisis con nro distinto para verificar que se usa el del "save"
        Analisis nuevoPersistido = new Analisis();
        nuevoPersistido.setNroAnalisis("AN-SAVED");
        when(analisisService.save(any(Analisis.class))).thenReturn(nuevoPersistido);

        Lote lote1 = new Lote();

        Movimiento m1 = new Movimiento();
        m1.setDictamenFinal(DictamenEnum.CUARENTENA);
        Movimiento m2 = new Movimiento();
        m2.setDictamenFinal(DictamenEnum.CUARENTENA);

        when(movimientoService.persistirMovimientoCuarentenaPorAnalisis(eq(dto), eq(lote1), eq("AN-SAVED"))).thenReturn(
            m1);

        when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Lote out = service.persistirDictamenCuarentena(dto, lote1);

        // then
        assertNotNull(out);

        // Se setea dictamen, agrega movimiento y agrega el mismo Analisis a ambos lotes
        assertEquals(DictamenEnum.CUARENTENA, out.getDictamen());
        assertTrue(out.getMovimientos().contains(m1));
        assertTrue(out.getAnalisisList().contains(nuevoPersistido));

        // Se creó con nro del DTO (RE-009) y se guardó
        ArgumentCaptor<Analisis> analisisCaptor = ArgumentCaptor.forClass(Analisis.class);
        verify(analisisService).save(analisisCaptor.capture());
        assertEquals("RE-009", analisisCaptor.getValue().getNroAnalisis());

        verify(analisisService).findByNroAnalisis("RE-009");
        verify(movimientoService).persistirMovimientoCuarentenaPorAnalisis(dto, lote1, "AN-SAVED");

        verify(loteRepository).save(lote1);
        verifyNoMoreInteractions(loteRepository, movimientoService, analisisService);
    }

    @Test
    void persistirExpiracionAnalisis() {
    }

    @Test
    void persistirLiberacionProducto() {
    }

    @Test
    void persistirProductosVencidos() {
    }

    @Test
    void persistirReanalisisProducto() {
    }

    @Test
    void persistirResultadoAnalisis() {
    }

    @BeforeEach
    void setup() {
        service = new LoteService(
            loteRepository,
            proveedorService,
            productoService,
            bultoService,
            movimientoService,
            analisisService,
            trazaService);
    }

    private LoteDTO dtoBase() {
        LoteDTO dto = new LoteDTO();
        dto.setProveedorId(1L);
        dto.setProductoId(2L);
        dto.setFechaYHoraCreacion(LocalDateTime.now());
        dto.setBultosTotales(1);
        dto.setCantidadInicial(new BigDecimal("5"));
        dto.setUnidadMedida(UnidadMedidaEnum.GRAMO);
        return dto;
    }

}
package com.mb.conitrack.service;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.utils.EntityUtils;
import com.mb.conitrack.utils.UnidadMedidaUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

    @InjectMocks
    LoteService service;

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


    @Test
    void altaStockPorCompra_ok() {
        // given
        LoteDTO dto = dtoBase();

        Proveedor prov = new Proveedor();
        prov.setPais("AR");
        when(proveedorService.findById(1L)).thenReturn(Optional.of(prov));

        Producto prod = new Producto();
        prod.setCodigoInterno("P-123");
        when(productoService.findById(2L)).thenReturn(Optional.of(prod));

        when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> inv.getArgument(0));

        Movimiento movimiento = new Movimiento();

        // mock static getInstance() -> returns our instance mock
        try (MockedStatic<EntityUtils> mockedStatic = mockStatic(EntityUtils.class)) {
            EntityUtils utilsMock = mock(EntityUtils.class);
            mockedStatic.when(EntityUtils::getInstance).thenReturn(utilsMock);

            Lote loteCreado = new Lote();
            when(utilsMock.createLoteIngreso(dto)).thenReturn(loteCreado);
            when(utilsMock.createBultoIngreso(dto)).thenReturn(new Bulto());

            // when
            Lote result = service.altaStockPorCompra(dto);

            // then
            assertNotNull(result);
            assertTrue(result.getCodigoInterno().startsWith("L-" + prod.getCodigoInterno() + "-"));
            assertSame(prod, result.getProducto());
            assertSame(prov, result.getProveedor());
            assertEquals(1, result.getBultos().size());

            verify(loteRepository).save(loteCreado);
            verify(bultoService).save(result.getBultos());
            verify(movimientoService).save(any());

            // Verify instance methods were actually used
            verify(utilsMock).createLoteIngreso(dto);
            verify(utilsMock, atLeastOnce()).createBultoIngreso(dto);
        }
    }

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
    @DisplayName("bajaMuestreo - UNIDAD_VENTA con UNIDAD y cantidad entera -> consume trazas, estados y guardado")
    void bajaMuestreo_unidadVenta_ok() {
        // given
        MovimientoDTO dto = new MovimientoDTO();
        dto.setNroAnalisis("AN-OK");

        // spy para stubear getUltimoNroAnalisis() y getFirstAvailableTrazaList()
        Lote lote = Mockito.spy(new Lote());
        doReturn("AN-OK").when(lote).getUltimoNroAnalisis();

        Producto prod = new Producto();
        prod.setTipoProducto(TipoProductoEnum.UNIDAD_VENTA);
        lote.setProducto(prod);

        Bulto b = new Bulto();
        b.setLote(lote);
        b.setCantidadActual(new BigDecimal("2")); // va a quedar en 0 para probar CONSUMIDO
        lote.getBultos().add(b);

        Movimiento mov = new Movimiento();
        mov.setCantidad(new BigDecimal("2"));           // entero
        mov.setUnidadMedida(UnidadMedidaEnum.UNIDAD);   // unidad correcta
        when(movimientoService.persistirMovimientoMuestreo(dto, b)).thenReturn(mov);

        // dos trazas a consumir
        Traza t1 = new Traza();
        t1.setEstado(EstadoEnum.DISPONIBLE);
        Traza t2 = new Traza();
        t2.setEstado(EstadoEnum.DISPONIBLE);
        List<Traza> trazas = List.of(t1, t2);
        doReturn(trazas).when(lote).getFirstAvailableTrazaList(2);

        try (
            MockedStatic<UnidadMedidaUtils> ms = mockStatic(UnidadMedidaUtils.class);
            MockedStatic<DTOUtils> msDto = mockStatic(DTOUtils.class)) {

            // restar -> 0 -> bulto CONSUMIDO
            ms.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(dto, b)).thenReturn(BigDecimal.ZERO);

            // mockeamos el mapping a DTO para poblar dto.trazaDTOs
            msDto.when(() -> DTOUtils.fromEntity(any(Traza.class))).thenAnswer(inv -> new TrazaDTO());

            when(trazaService.save(trazas)).thenReturn(trazas);
            when(loteRepository.save(any(Lote.class))).thenAnswer(inv -> inv.getArgument(0));

            // when
            Lote out = service.bajaMuestreo(dto, b);

            // then
            assertSame(lote, out);

            // trazas consumidas y vinculadas al movimiento
            assertEquals(EstadoEnum.CONSUMIDO, t1.getEstado());
            assertEquals(EstadoEnum.CONSUMIDO, t2.getEstado());
            assertTrue(t1.getMovimientos().contains(mov));
            assertTrue(t2.getMovimientos().contains(mov));
            verify(trazaService).save(trazas);

            // dto recibió DTOs de traza
            assertNotNull(dto.getTrazaDTOs());
            assertEquals(2, dto.getTrazaDTOs().size());

            // estados bulto/lote
            assertEquals(BigDecimal.ZERO, b.getCantidadActual());
            assertEquals(EstadoEnum.CONSUMIDO, b.getEstado());
            assertEquals(EstadoEnum.CONSUMIDO, out.getEstado()); // único bulto => todos consumidos

            // persistencias y vínculos
            assertTrue(out.getMovimientos().contains(mov));
            verify(movimientoService).persistirMovimientoMuestreo(dto, b);
            ms.verify(() -> UnidadMedidaUtils.restarMovimientoConvertido(dto, b));
            verify(loteRepository).save(out);
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

        when(proveedorService.findById(99L)).thenReturn(Optional.of(fabricante));

        Lote lote = new Lote();

        service.populateLoteAltaStockCompra(lote, dto, producto, proveedor);

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

        verify(proveedorService).findById(99L);
        verifyNoMoreInteractions(proveedorService);
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

        when(proveedorService.findById(77L)).thenReturn(Optional.empty());

        Lote lote = new Lote();

        // when
        service.populateLoteAltaStockCompra(lote, dto, producto, proveedor);
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

        verify(proveedorService).findById(77L);
        verifyNoMoreInteractions(proveedorService);
    }

    @Test
    void findAllForConsumoProduccion() {
    }

    @Test
    void findAllForCuarentena() {
    }

    @Test
    @DisplayName("findAllForCuarentena - filtra por dictámenes permitidos")
    void findAllForCuarentena_ok() {
        LoteService spyService = Mockito.spy(service);

        Lote l1 = new Lote();
        l1.setDictamen(DictamenEnum.RECIBIDO);
        Lote l2 = new Lote();
        l2.setDictamen(DictamenEnum.APROBADO);
        Lote l3 = new Lote();
        l3.setDictamen(DictamenEnum.ANALISIS_EXPIRADO);
        Lote l4 = new Lote();
        l4.setDictamen(DictamenEnum.LIBERADO);
        Lote l5 = new Lote();
        l5.setDictamen(DictamenEnum.DEVOLUCION_CLIENTES);
        Lote l6 = new Lote();
        l6.setDictamen(DictamenEnum.RETIRO_MERCADO);
        Lote e1 = new Lote();
        e1.setDictamen(DictamenEnum.RECHAZADO);
        Lote e2 = new Lote();
        e2.setDictamen(DictamenEnum.CUARENTENA);

        List<Lote> preorden = List.of(l1, l2, l3, l4, l5, l6, e1, e2);

        // Importante: doReturn(...) para no ejecutar el real en un spy
        doReturn(preorden).when(spyService).findAllSortByDateAndNroBulto();

        // when
        List<Lote> out = spyService.findAllForCuarentena();

        // then
        assertEquals(List.of(l1, l2, l3, l4, l5, l6), out);

        // Verificá la llamada externa y la interna
        verify(spyService).findAllForCuarentena();
        verify(spyService).findAllSortByDateAndNroBulto();

        verifyNoMoreInteractions(spyService);
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
    void findAllSortByDateAndNroBulto() {
    }

    @Test
    void findAllSortByDateAndNroBultoAudit() {
    }

    @Test
    @DisplayName("findAllSortByDateAndNroBulto - filtra activos y ordena por fecha, código y nroBulto")
    void findAllSortByDateAndNroBulto_ok() {
        // given: mezcla de activos e inactivos y desordenados
        Lote a = new Lote();
        a.setActivo(true);
        a.setFechaIngreso(LocalDate.of(2024, 1, 10));
        a.setCodigoInterno("L-02");

        Lote b = new Lote();
        b.setActivo(true);
        b.setFechaIngreso(LocalDate.of(2024, 1, 10)); // misma fecha que 'a'
        b.setCodigoInterno("L-01");                   // código anterior => debe ir antes que 'a'

        Lote c = new Lote();
        c.setActivo(true);
        c.setFechaIngreso(LocalDate.of(2023, 12, 31)); // fecha más vieja => primero
        c.setCodigoInterno("L-99");

        Lote d = new Lote();
        d.setActivo(false); // inactivo => debe ser filtrado
        d.setFechaIngreso(LocalDate.of(2025, 5, 5));
        d.setCodigoInterno("L-00");

        when(loteRepository.findAll()).thenReturn(Arrays.asList(a, b, c, d));

        // when
        List<Lote> out = service.findAllSortByDateAndNroBulto();

        // then: sólo activos (c, b, a) en orden por fecha/código/nro
        assertEquals(List.of(c, b, a), out);
        verify(loteRepository).findAll();
        verifyNoMoreInteractions(loteRepository);
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
        service.populateLoteAltaStockCompra(lote, dto, producto, proveedor);

        // then
        assertEquals("L-P-999-25.01.01_12.00.00", lote.getCodigoInterno());
        assertSame(producto, lote.getProducto());
        assertSame(proveedor, lote.getProveedor());
        assertEquals("MX", lote.getPaisOrigen()); // NO pisado

        assertEquals(2, lote.getBultos().size());
        for (int i = 0; i < 2; i++) {
            Bulto b = lote.getBultos().get(i);
            assertSame(lote, b.getLote());
            assertEquals(i + 1, b.getNroBulto());
        }

        verifyNoInteractions(proveedorService);
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
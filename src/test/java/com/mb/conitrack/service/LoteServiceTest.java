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

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.utils.EntityUtils;

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
    void altaStockPorCompra() {
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
        when(movimientoService.persistirMovimientoAltaIngresoCompra(any(Lote.class)))
            .thenReturn(movimiento);

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
            verify(movimientoService).persistirMovimientoAltaIngresoCompra(result);

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
    void altaStockPorProduccion() {
    }

    @Test
    void bajaConsumoProduccion() {
    }

    @Test
    void bajaDevolucionCompra() {
    }

    @Test
    void bajaMuestreo() {
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
        dto.setFechaYHoraCreacion(LocalDateTime.now());

        Producto producto = new Producto();
        producto.setCodigoInterno("P-123");

        Proveedor proveedor = new Proveedor();
        proveedor.setPais("AR");

        Proveedor fabricante = new Proveedor();
        fabricante.setPais("BR");

        when(proveedorService.findById(99L)).thenReturn(Optional.of(fabricante));

        Lote lote = new Lote();

        // when
        callPopulateLote(dto, lote, producto, "TS", proveedor);

        // then
        assertEquals("L-P-123-TS", lote.getCodigoInterno());
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
        dto.setFechaYHoraCreacion(LocalDateTime.now());

        Producto producto = new Producto();
        producto.setCodigoInterno("P-XYZ");

        Proveedor proveedor = new Proveedor();
        proveedor.setPais("UY");

        when(proveedorService.findById(77L)).thenReturn(Optional.empty());

        Lote lote = new Lote();

        // when
        callPopulateLote(dto, lote, producto, "STAMP", proveedor);

        // then
        assertEquals("L-P-XYZ-STAMP", lote.getCodigoInterno());
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
        a.setNroBulto(2);

        Lote b = new Lote();
        b.setActivo(true);
        b.setFechaIngreso(LocalDate.of(2024, 1, 10)); // misma fecha que 'a'
        b.setCodigoInterno("L-01");                   // código anterior => debe ir antes que 'a'
        b.setNroBulto(5);

        Lote c = new Lote();
        c.setActivo(true);
        c.setFechaIngreso(LocalDate.of(2023, 12, 31)); // fecha más vieja => primero
        c.setCodigoInterno("L-99");
        c.setNroBulto(1);

        Lote d = new Lote();
        d.setActivo(false); // inactivo => debe ser filtrado
        d.setFechaIngreso(LocalDate.of(2025, 5, 5));
        d.setCodigoInterno("L-00");
        d.setNroBulto(1);

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
        dto.setFechaYHoraCreacion(LocalDateTime.now());

        Producto producto = new Producto();
        producto.setCodigoInterno("P-999");

        Proveedor proveedor = new Proveedor();
        proveedor.setPais("AR");

        Lote lote = new Lote();
        lote.setPaisOrigen("MX"); // valor previo que NO debe ser sobreescrito

        // when
        callPopulateLote(dto, lote, producto, "T", proveedor);

        // then
        assertEquals("L-P-999-T", lote.getCodigoInterno());
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

        when(movimientoService.persistirMovimientoCuarentenaPorAnalisis(eq(dto), eq(lote1), eq("AN-001")))
            .thenReturn(m1);

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

        when(movimientoService.persistirMovimientoCuarentenaPorAnalisis(eq(dto), eq(lote1), eq("AN-SAVED")))
            .thenReturn(m1);

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

    @Test
    void save() {
    }

    @BeforeEach
    void setup() {
        service = new LoteService(
            loteRepository, proveedorService, productoService,
            bultoService, movimientoService, analisisService, trazaService
        );
    }

    private void callPopulateLote(
        LoteDTO dto,
        Lote lote,
        Producto producto,
        String ts,
        Proveedor prov) throws Exception {
        Method m = LoteService.class.getDeclaredMethod(
            "populateLote", LoteDTO.class, Lote.class, Producto.class, String.class, Proveedor.class
        );
        m.setAccessible(true);
        m.invoke(service, dto, lote, producto, ts, prov);
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
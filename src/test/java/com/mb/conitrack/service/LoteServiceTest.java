package com.mb.conitrack.service;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.utils.EntityUtils;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.LoteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoteServiceTest {

    @Mock LoteRepository loteRepository;
    @Mock ProveedorService proveedorService;
    @Mock ProductoService productoService;
    @Mock BultoService bultoService;
    @Mock MovimientoService movimientoService;
    @Mock AnalisisService analisisService;
    @Mock TrazaService trazaService;

    @InjectMocks
    LoteService service;

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
    void findAllSortByDateAndNroBultoAudit() {
    }

    @Test
    void findAllSortByDateAndNroBulto() {
    }

    @Test
    void findAllLotesDictaminados() {
    }

    @Test
    void findAllLotesVencidos() {
    }

    @Test
    void findAllLotesAnalisisExpirado() {
    }

    @Test
    void save() {
    }

    @Test
    void altaStockPorCompra() {
    }

    @Test
    void findAllForCuarentena() {
    }

    @Test
    void persistirDictamenCuarentena() {
    }

    @Test
    void findAllForReanalisisProducto() {
    }

    @Test
    void findAllForMuestreo() {
    }

    @Test
    void findAllForDevolucionCompra() {
    }

    @Test
    void findAllForResultadoAnalisis() {
    }

    @Test
    void findAllForConsumoProduccion() {
    }

    @Test
    void findAllForLiberacionProducto() {
    }

    @Test
    void findAllForVentaProducto() {
    }

    @Test
    void findMaxNroTraza() {
    }

    @Test
    void persistirLiberacionProducto() {
    }

    @Test
    void persistirReanalisisProducto() {
    }

    @Test
    void bajaMuestreo() {
    }

    @Test
    void bajaDevolucionCompra() {
    }

    @Test
    void persistirResultadoAnalisis() {
    }

    @Test
    void bajaConsumoProduccion() {
    }

    @Test
    void altaStockPorProduccion() {
    }

    @Test
    void persistirProductosVencidos() {
    }

    @Test
    void persistirExpiracionAnalisis() {
    }

    @Test
    void bajaVentaProducto() {
    }

    @Test
    void findAllForDevolucionVenta() {
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

    @BeforeEach
    void setup() {
        service = new LoteService(
            loteRepository, proveedorService, productoService,
            bultoService, movimientoService, analisisService, trazaService
        );
    }

    @Test
    void altaStockPorCompra_ok() {
        // given
        LoteDTO dto = dtoBase();

        Proveedor prov = new Proveedor(); prov.setPais("AR");
        when(proveedorService.findById(1L)).thenReturn(Optional.of(prov));

        Producto prod = new Producto(); prod.setCodigoInterno("P-123");
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
    @DisplayName("altaStockPorCompra - proveedor inexistente -> IllegalArgumentException")
    void altaStockPorCompra_sinProveedor() {
        // given
        LoteDTO dto = dtoBase();
        when(proveedorService.findById(1L)).thenReturn(Optional.empty());

        // when / then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.altaStockPorCompra(dto));
        assertEquals("El proveedor no existe.", ex.getMessage());

        // No debería avanzar a producto ni a guardar
        verify(proveedorService).findById(1L);
        verifyNoInteractions(productoService, loteRepository, bultoService, movimientoService);
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
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> service.altaStockPorCompra(dto));
        assertEquals("El producto no existe.", ex.getMessage());

        verify(proveedorService).findById(1L);
        verify(productoService).findById(2L);
        verifyNoInteractions(loteRepository, bultoService, movimientoService);
    }




    private void callPopulateLote(LoteDTO dto, Lote lote, Producto producto, String ts, Proveedor prov) throws Exception {
        Method m = LoteService.class.getDeclaredMethod(
            "populateLote", LoteDTO.class, Lote.class, Producto.class, String.class, Proveedor.class
        );
        m.setAccessible(true);
        m.invoke(service, dto, lote, producto, ts, prov);
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
        dto.setUnidadMedidaBultos(java.util.List.of(UnidadMedidaEnum.GRAMO, UnidadMedidaEnum.GRAMO, UnidadMedidaEnum.GRAMO));
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

}
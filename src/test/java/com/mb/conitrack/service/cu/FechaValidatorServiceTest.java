package com.mb.conitrack.service.cu;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.LoteRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FechaValidatorServiceTest {

    @InjectMocks
    FechaValidatorService service;

    @Mock
    LoteRepository loteRepository;

    private PrintStream originalOut;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void captureStdout() {
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStdout() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("@Scheduled presente con el cron esperado")
    void tieneScheduledConCronEsperado() throws Exception {
        Scheduled sch = FechaValidatorService.class
            .getMethod("validarFecha")
            .getAnnotation(Scheduled.class);

        String expected = "0 0 5 * * *";
        assertEquals(expected, sch.cron());
    }

    // ========== Tests para CU9: Análisis Expirado ==========

    @Test
    @DisplayName("CU9: Lote con fecha reanálisis PASADA debe procesarse como análisis expirado")
    void findAllLotesAnalisisExpirado_fechaPasada_debeIncluirse() {
        // Given: Lote con fecha de reanálisis ayer
        LocalDate ayer = LocalDate.now().minusDays(1);
        Lote lote = crearLoteConFechaReanalisis(ayer);

        when(loteRepository.findLotesConStockOrder()).thenReturn(List.of(lote));

        // When
        List<Lote> resultado = service.findAllLotesAnalisisExpirado();

        // Then: Debe incluirse porque fecha <= hoy
        assertEquals(1, resultado.size());
        assertEquals(lote, resultado.get(0));
    }

    @Test
    @DisplayName("CU9: Lote con fecha reanálisis HOY debe procesarse como análisis expirado")
    void findAllLotesAnalisisExpirado_fechaHoy_debeIncluirse() {
        // Given: Lote con fecha de reanálisis hoy
        LocalDate hoy = LocalDate.now();
        Lote lote = crearLoteConFechaReanalisis(hoy);

        when(loteRepository.findLotesConStockOrder()).thenReturn(List.of(lote));

        // When
        List<Lote> resultado = service.findAllLotesAnalisisExpirado();

        // Then: Debe incluirse porque fecha <= hoy
        assertEquals(1, resultado.size());
        assertEquals(lote, resultado.get(0));
    }

    @Test
    @DisplayName("CU9: Lote con fecha reanálisis FUTURA NO debe procesarse")
    void findAllLotesAnalisisExpirado_fechaFutura_noDebeIncluirse() {
        // Given: Lote con fecha de reanálisis mañana
        LocalDate manana = LocalDate.now().plusDays(1);
        Lote lote = crearLoteConFechaReanalisis(manana);

        when(loteRepository.findLotesConStockOrder()).thenReturn(List.of(lote));

        // When
        List<Lote> resultado = service.findAllLotesAnalisisExpirado();

        // Then: NO debe incluirse porque fecha > hoy
        assertEquals(0, resultado.size());
    }

    @Test
    @DisplayName("CU9: Lote sin fecha de reanálisis NO debe procesarse")
    void findAllLotesAnalisisExpirado_sinFecha_noDebeIncluirse() {
        // Given: Lote sin fecha de reanálisis (null)
        Lote lote = crearLoteConFechaReanalisis(null);

        when(loteRepository.findLotesConStockOrder()).thenReturn(List.of(lote));

        // When
        List<Lote> resultado = service.findAllLotesAnalisisExpirado();

        // Then: NO debe incluirse
        assertEquals(0, resultado.size());
    }

    @Test
    @DisplayName("CU9: Múltiples lotes - solo deben incluirse los que cumplan condición")
    void findAllLotesAnalisisExpirado_multiplesLotes_filtraCorrectamente() {
        // Given: 4 lotes con fechas distintas
        LocalDate ayer = LocalDate.now().minusDays(1);
        LocalDate hoy = LocalDate.now();
        LocalDate manana = LocalDate.now().plusDays(1);

        Lote loteAyer = crearLoteConFechaReanalisis(ayer);
        loteAyer.setId(1L);
        Lote loteHoy = crearLoteConFechaReanalisis(hoy);
        loteHoy.setId(2L);
        Lote loteManana = crearLoteConFechaReanalisis(manana);
        loteManana.setId(3L);
        Lote loteSinFecha = crearLoteConFechaReanalisis(null);
        loteSinFecha.setId(4L);

        when(loteRepository.findLotesConStockOrder())
            .thenReturn(List.of(loteAyer, loteHoy, loteManana, loteSinFecha));

        // When
        List<Lote> resultado = service.findAllLotesAnalisisExpirado();

        // Then: Solo deben incluirse ayer y hoy (fecha <= hoy)
        assertEquals(2, resultado.size());

        List<Long> ids = resultado.stream().map(Lote::getId).toList();
        assertTrue(ids.contains(1L), "Debe contener lote con fecha ayer");
        assertTrue(ids.contains(2L), "Debe contener lote con fecha hoy");
        assertFalse(ids.contains(3L), "NO debe contener lote con fecha futura");
        assertFalse(ids.contains(4L), "NO debe contener lote sin fecha");
    }

    // ========== Tests para CU10: Vencimiento ==========

    @Test
    @DisplayName("CU10: Lote con fecha vencimiento PASADA debe procesarse como vencido")
    void findAllLotesVencidos_fechaPasada_debeIncluirse() {
        // Given: Lote con fecha de vencimiento ayer
        LocalDate ayer = LocalDate.now().minusDays(1);
        Lote lote = crearLoteConFechaVencimiento(ayer);

        when(loteRepository.findLotesConStockOrder()).thenReturn(List.of(lote));

        // When
        List<Lote> resultado = service.findAllLotesVencidos();

        // Then: Debe incluirse porque fecha <= hoy
        assertEquals(1, resultado.size());
        assertEquals(lote, resultado.get(0));
    }

    @Test
    @DisplayName("CU10: Lote con fecha vencimiento HOY debe procesarse como vencido")
    void findAllLotesVencidos_fechaHoy_debeIncluirse() {
        // Given: Lote con fecha de vencimiento hoy
        LocalDate hoy = LocalDate.now();
        Lote lote = crearLoteConFechaVencimiento(hoy);

        when(loteRepository.findLotesConStockOrder()).thenReturn(List.of(lote));

        // When
        List<Lote> resultado = service.findAllLotesVencidos();

        // Then: Debe incluirse porque fecha <= hoy
        assertEquals(1, resultado.size());
        assertEquals(lote, resultado.get(0));
    }

    @Test
    @DisplayName("CU10: Lote con fecha vencimiento FUTURA NO debe procesarse")
    void findAllLotesVencidos_fechaFutura_noDebeIncluirse() {
        // Given: Lote con fecha de vencimiento mañana
        LocalDate manana = LocalDate.now().plusDays(1);
        Lote lote = crearLoteConFechaVencimiento(manana);

        when(loteRepository.findLotesConStockOrder()).thenReturn(List.of(lote));

        // When
        List<Lote> resultado = service.findAllLotesVencidos();

        // Then: NO debe incluirse porque fecha > hoy
        assertEquals(0, resultado.size());
    }

    @Test
    @DisplayName("CU10: Lote sin fecha de vencimiento NO debe procesarse")
    void findAllLotesVencidos_sinFecha_noDebeIncluirse() {
        // Given: Lote sin fecha de vencimiento (null)
        Lote lote = crearLoteConFechaVencimiento(null);

        when(loteRepository.findLotesConStockOrder()).thenReturn(List.of(lote));

        // When
        List<Lote> resultado = service.findAllLotesVencidos();

        // Then: NO debe incluirse
        assertEquals(0, resultado.size());
    }

    @Test
    @DisplayName("CU10: Múltiples lotes - solo deben incluirse los que cumplan condición")
    void findAllLotesVencidos_multiplesLotes_filtraCorrectamente() {
        // Given: 4 lotes con fechas distintas
        LocalDate ayer = LocalDate.now().minusDays(1);
        LocalDate hoy = LocalDate.now();
        LocalDate manana = LocalDate.now().plusDays(1);

        Lote loteAyer = crearLoteConFechaVencimiento(ayer);
        loteAyer.setId(5L);
        Lote loteHoy = crearLoteConFechaVencimiento(hoy);
        loteHoy.setId(6L);
        Lote loteManana = crearLoteConFechaVencimiento(manana);
        loteManana.setId(7L);
        Lote loteSinFecha = crearLoteConFechaVencimiento(null);
        loteSinFecha.setId(8L);

        when(loteRepository.findLotesConStockOrder())
            .thenReturn(List.of(loteAyer, loteHoy, loteManana, loteSinFecha));

        // When
        List<Lote> resultado = service.findAllLotesVencidos();

        // Then: Solo deben incluirse ayer y hoy (fecha <= hoy)
        assertEquals(2, resultado.size());

        List<Long> ids = resultado.stream().map(Lote::getId).toList();
        assertTrue(ids.contains(5L), "Debe contener lote con fecha ayer");
        assertTrue(ids.contains(6L), "Debe contener lote con fecha hoy");
        assertFalse(ids.contains(7L), "NO debe contener lote con fecha futura");
        assertFalse(ids.contains(8L), "NO debe contener lote sin fecha");
    }

    // ========== Métodos auxiliares para crear lotes de prueba ==========

    private Lote crearLoteConFechaReanalisis(LocalDate fechaReanalisis) {
        Lote lote = crearLoteBase();

        Analisis analisis = new Analisis();
        analisis.setNroAnalisis("A-001");
        analisis.setFechaReanalisis(fechaReanalisis);
        analisis.setDictamen(DictamenEnum.APROBADO);
        analisis.setFechaRealizado(LocalDate.now().minusDays(30));
        analisis.setActivo(true);

        lote.getAnalisisList().add(analisis);

        return lote;
    }

    private Lote crearLoteConFechaVencimiento(LocalDate fechaVencimiento) {
        Lote lote = crearLoteBase();

        Analisis analisis = new Analisis();
        analisis.setNroAnalisis("A-002");
        analisis.setFechaVencimiento(fechaVencimiento);
        analisis.setDictamen(DictamenEnum.APROBADO);
        analisis.setFechaRealizado(LocalDate.now().minusDays(30));
        analisis.setActivo(true);

        lote.getAnalisisList().add(analisis);

        return lote;
    }

    private Lote crearLoteBase() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setCodigoProducto("PROD-001");
        producto.setNombreGenerico("Producto Test");
        producto.setTipoProducto(TipoProductoEnum.API);
        producto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        producto.setActivo(true);

        Proveedor proveedor = new Proveedor();
        proveedor.setId(1L);
        proveedor.setRazonSocial("Proveedor Test");
        proveedor.setCuit("20-12345678-9");
        proveedor.setActivo(true);

        Lote lote = new Lote();
        lote.setId(1L);
        lote.setCodigoLote("L-TEST-001");
        lote.setProducto(producto);
        lote.setProveedor(proveedor);
        lote.setFechaYHoraCreacion(OffsetDateTime.now());
        lote.setFechaIngreso(LocalDate.now());
        lote.setLoteProveedor("LP-001");
        lote.setCantidadInicial(new BigDecimal("100"));
        lote.setCantidadActual(new BigDecimal("100"));
        lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        lote.setEstado(EstadoEnum.DISPONIBLE);
        lote.setDictamen(DictamenEnum.APROBADO);
        lote.setBultosTotales(1);
        lote.setTrazado(false);
        lote.setActivo(true);

        return lote;
    }

}

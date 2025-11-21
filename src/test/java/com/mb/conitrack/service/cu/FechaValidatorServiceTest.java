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
import java.util.ArrayList;
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
import com.mb.conitrack.utils.MovimientoModificacionUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FechaValidatorServiceTest {

    @InjectMocks
    FechaValidatorService service;

    @Mock
    LoteRepository loteRepository;

    @Mock
    com.mb.conitrack.repository.maestro.UserRepository userRepository;

    @Mock
    com.mb.conitrack.repository.maestro.RoleRepository roleRepository;

    @Mock
    com.mb.conitrack.repository.AnalisisRepository analisisRepository;

    @Mock
    com.mb.conitrack.repository.MovimientoRepository movimientoRepository;

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
        analisis.setFechaYHoraCreacion(OffsetDateTime.now());

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

    // ========== Tests adicionales para cobertura completa (14% -> 100%) ==========

    @Nested
    @DisplayName("validarFecha() - Método @Scheduled - Cobertura líneas 44-46")
    class ValidarFechaScheduledTests {

        @Test
        @DisplayName("Debe llamar a procesarLotesAnalisisExpirado y procesarLotesVencidos")
        void validarFecha_debeProcegarAmbosMetodos() {
            // Given
            when(loteRepository.findLotesConStockOrder()).thenReturn(List.of());

            // When
            service.validarFecha();

            // Then
            // Verificar que ambos métodos fueron invocados
            verify(loteRepository, atLeastOnce()).findLotesConStockOrder();
        }
    }

    @Nested
    @DisplayName("persistirExpiracionAnalisis() - Cobertura líneas 79-86")
    class PersistirExpiracionAnalisisTests {

        @Test
        @DisplayName("Debe procesar lista vacía sin errores")
        void persistirExpiracionAnalisis_listaVacia_debeRetornarVacia() {
            // Given
            com.mb.conitrack.dto.MovimientoDTO dto = new com.mb.conitrack.dto.MovimientoDTO();
            List<Lote> lotesVacios = List.of();

            // When
            List<Lote> resultado = service.persistirExpiracionAnalisis(dto, lotesVacios);

            // Then
            assertEquals(0, resultado.size());
            verify(movimientoRepository, never()).save(any());
            verify(loteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe procesar lotes y persistir movimientos correctamente")
        void persistirExpiracionAnalisis_conLotes_debeProcesarYPersistir() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                com.mb.conitrack.dto.MovimientoDTO dto = new com.mb.conitrack.dto.MovimientoDTO();
                dto.setFechaYHoraCreacion(OffsetDateTime.now());
                dto.setObservaciones("Test");

                Lote lote1 = crearLoteConFechaReanalisis(LocalDate.now());
                Lote lote2 = crearLoteConFechaReanalisis(LocalDate.now().minusDays(1));

                // Mock system user
                com.mb.conitrack.entity.maestro.Role adminRole = com.mb.conitrack.entity.maestro.Role.fromEnum(com.mb.conitrack.enums.RoleEnum.ADMIN);
                com.mb.conitrack.entity.maestro.User systemUser = new com.mb.conitrack.entity.maestro.User("system_auto", "N/A", adminRole);
                when(userRepository.findByUsername("system_auto")).thenReturn(java.util.Optional.of(systemUser));

                // Mock static method
                com.mb.conitrack.entity.Movimiento movMock = new com.mb.conitrack.entity.Movimiento();
                movMock.setDictamenFinal(DictamenEnum.ANALISIS_EXPIRADO);
                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(any(), any(), any()))
                    .thenReturn(movMock);

                when(movimientoRepository.save(any(com.mb.conitrack.entity.Movimiento.class))).thenReturn(movMock);
                when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // When
                List<Lote> resultado = service.persistirExpiracionAnalisis(dto, List.of(lote1, lote2));

                // Then
                assertEquals(2, resultado.size());
                verify(movimientoRepository, times(2)).save(any(com.mb.conitrack.entity.Movimiento.class));
                verify(loteRepository, times(2)).save(any(Lote.class));
            }
        }
    }

    @Nested
    @DisplayName("persistirMovimientoExpiracionAnalisis() - Cobertura líneas 92-100")
    class PersistirMovimientoExpiracionAnalisisTests {

        @Test
        @DisplayName("Debe crear movimiento con dictamen ANALISIS_EXPIRADO")
        void persistirMovimientoExpiracionAnalisis_debeCrearMovimiento() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                com.mb.conitrack.dto.MovimientoDTO dto = new com.mb.conitrack.dto.MovimientoDTO();
                dto.setFechaYHoraCreacion(OffsetDateTime.now());
                dto.setObservaciones("Test expiracion");

                Lote lote = crearLoteConFechaReanalisis(LocalDate.now());

                // Mock system user
                com.mb.conitrack.entity.maestro.Role adminRole = com.mb.conitrack.entity.maestro.Role.fromEnum(com.mb.conitrack.enums.RoleEnum.ADMIN);
                com.mb.conitrack.entity.maestro.User systemUser = new com.mb.conitrack.entity.maestro.User("system_auto", "N/A", adminRole);
                when(userRepository.findByUsername("system_auto")).thenReturn(java.util.Optional.of(systemUser));

                // Mock static method and movement save
                com.mb.conitrack.entity.Movimiento movSaved = new com.mb.conitrack.entity.Movimiento();
                movSaved.setId(1L);
                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(any(), any(), any()))
                    .thenReturn(movSaved);
                when(movimientoRepository.save(any(com.mb.conitrack.entity.Movimiento.class))).thenReturn(movSaved);

                // When
                com.mb.conitrack.entity.Movimiento resultado = service.persistirMovimientoExpiracionAnalisis(dto, lote);

                // Then
                assertNotNull(resultado);
                assertEquals(1L, resultado.getId());
                verify(userRepository).findByUsername("system_auto");
                verify(movimientoRepository).save(any(com.mb.conitrack.entity.Movimiento.class));
            }
        }
    }

    @Nested
    @DisplayName("persistirMovimientoProductoVencido() - Cobertura líneas 106-114")
    class PersistirMovimientoProductoVencidoTests {

        @Test
        @DisplayName("Debe crear movimiento con dictamen VENCIDO")
        void persistirMovimientoProductoVencido_debeCrearMovimiento() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                com.mb.conitrack.dto.MovimientoDTO dto = new com.mb.conitrack.dto.MovimientoDTO();
                dto.setFechaYHoraCreacion(OffsetDateTime.now());
                dto.setObservaciones("Test vencimiento");

                Lote lote = crearLoteConFechaVencimiento(LocalDate.now());

                // Mock system user
                com.mb.conitrack.entity.maestro.Role adminRole = com.mb.conitrack.entity.maestro.Role.fromEnum(com.mb.conitrack.enums.RoleEnum.ADMIN);
                com.mb.conitrack.entity.maestro.User systemUser = new com.mb.conitrack.entity.maestro.User("system_auto", "N/A", adminRole);
                when(userRepository.findByUsername("system_auto")).thenReturn(java.util.Optional.of(systemUser));

                // Mock static method and movement save
                com.mb.conitrack.entity.Movimiento movSaved = new com.mb.conitrack.entity.Movimiento();
                movSaved.setId(2L);
                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(any(), any(), any()))
                    .thenReturn(movSaved);
                when(movimientoRepository.save(any(com.mb.conitrack.entity.Movimiento.class))).thenReturn(movSaved);

                // When
                com.mb.conitrack.entity.Movimiento resultado = service.persistirMovimientoProductoVencido(dto, lote);

                // Then
                assertNotNull(resultado);
                assertEquals(2L, resultado.getId());
                verify(userRepository).findByUsername("system_auto");
                verify(movimientoRepository).save(any(com.mb.conitrack.entity.Movimiento.class));
            }
        }
    }

    @Nested
    @DisplayName("persistirProductosVencidos() - Cobertura líneas 120-134")
    class PersistirProductosVencidosTests {

        @Test
        @DisplayName("Debe procesar lista vacía sin errores")
        void persistirProductosVencidos_listaVacia_debeRetornarVacia() {
            // Given
            com.mb.conitrack.dto.MovimientoDTO dto = new com.mb.conitrack.dto.MovimientoDTO();
            List<Lote> lotesVacios = List.of();

            // When
            List<Lote> resultado = service.persistirProductosVencidos(dto, lotesVacios);

            // Then
            assertEquals(0, resultado.size());
            verify(movimientoRepository, never()).save(any());
            verify(loteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe cancelar análisis en curso si existe - líneas 127-129")
        void persistirProductosVencidos_conAnalisisEnCurso_debeCancelar() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                com.mb.conitrack.dto.MovimientoDTO dto = new com.mb.conitrack.dto.MovimientoDTO();
                dto.setFechaYHoraCreacion(OffsetDateTime.now());
                dto.setObservaciones("Test");

                Lote lote = crearLoteConFechaVencimiento(LocalDate.now());
                // Análisis en curso (dictamen = null) - debe ser el más reciente
                Analisis analisisEnCurso = new Analisis();
                analisisEnCurso.setId(10L);
                analisisEnCurso.setNroAnalisis("A-EN-CURSO");
                analisisEnCurso.setDictamen(null); // EN CURSO
                analisisEnCurso.setActivo(true);
                analisisEnCurso.setFechaYHoraCreacion(OffsetDateTime.now().plusDays(1)); // Más reciente que el análisis base
                lote.getAnalisisList().add(analisisEnCurso);

                // Mock system user
                com.mb.conitrack.entity.maestro.Role adminRole = com.mb.conitrack.entity.maestro.Role.fromEnum(com.mb.conitrack.enums.RoleEnum.ADMIN);
                com.mb.conitrack.entity.maestro.User systemUser = new com.mb.conitrack.entity.maestro.User("system_auto", "N/A", adminRole);
                when(userRepository.findByUsername("system_auto")).thenReturn(java.util.Optional.of(systemUser));

                com.mb.conitrack.entity.Movimiento movMock = new com.mb.conitrack.entity.Movimiento();
                movMock.setDictamenFinal(DictamenEnum.VENCIDO);
                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(any(), any(), any()))
                    .thenReturn(movMock);
                when(movimientoRepository.save(any(com.mb.conitrack.entity.Movimiento.class))).thenReturn(movMock);
                when(analisisRepository.save(any(Analisis.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // When
                List<Lote> resultado = service.persistirProductosVencidos(dto, List.of(lote));

                // Then
                assertEquals(1, resultado.size());
                // Verificar que el análisis fue cancelado
                assertEquals(DictamenEnum.CANCELADO, lote.getUltimoAnalisis().getDictamen());
                verify(analisisRepository).save(analisisEnCurso);
                verify(movimientoRepository).save(any(com.mb.conitrack.entity.Movimiento.class));
                verify(loteRepository).save(lote);
            }
        }

        @Test
        @DisplayName("No debe cancelar análisis si ya tiene dictamen")
        void persistirProductosVencidos_analisisDictaminado_noDebeCancelar() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                com.mb.conitrack.dto.MovimientoDTO dto = new com.mb.conitrack.dto.MovimientoDTO();
                dto.setFechaYHoraCreacion(OffsetDateTime.now());
                dto.setObservaciones("Test");

                Lote lote = crearLoteConFechaVencimiento(LocalDate.now());
                // Análisis ya dictaminado
                lote.getAnalisisList().get(0).setDictamen(DictamenEnum.APROBADO);

                // Mock system user
                com.mb.conitrack.entity.maestro.Role adminRole = com.mb.conitrack.entity.maestro.Role.fromEnum(com.mb.conitrack.enums.RoleEnum.ADMIN);
                com.mb.conitrack.entity.maestro.User systemUser = new com.mb.conitrack.entity.maestro.User("system_auto", "N/A", adminRole);
                when(userRepository.findByUsername("system_auto")).thenReturn(java.util.Optional.of(systemUser));

                com.mb.conitrack.entity.Movimiento movMock = new com.mb.conitrack.entity.Movimiento();
                movMock.setDictamenFinal(DictamenEnum.VENCIDO);
                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(any(), any(), any()))
                    .thenReturn(movMock);
                when(movimientoRepository.save(any(com.mb.conitrack.entity.Movimiento.class))).thenReturn(movMock);
                when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // When
                List<Lote> resultado = service.persistirProductosVencidos(dto, List.of(lote));

                // Then
                assertEquals(1, resultado.size());
                // Verificar que el análisis NO fue modificado
                assertEquals(DictamenEnum.APROBADO, lote.getUltimoAnalisis().getDictamen());
                verify(analisisRepository, never()).save(any(Analisis.class));
            }
        }

        @Test
        @DisplayName("No debe cancelar análisis cuando lote no tiene análisis - cubre branch faltante línea 127")
        void persistirProductosVencidos_sinAnalisis_noDebeCancelar() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                com.mb.conitrack.dto.MovimientoDTO dto = new com.mb.conitrack.dto.MovimientoDTO();
                dto.setFechaYHoraCreacion(OffsetDateTime.now());
                dto.setObservaciones("Test");

                Lote lote = crearLoteConFechaVencimiento(LocalDate.now());
                // CLAVE: Lote SIN análisis - lista vacía para que getUltimoAnalisis() retorne null
                lote.setAnalisisList(new ArrayList<>());

                // Mock system user
                com.mb.conitrack.entity.maestro.Role adminRole = com.mb.conitrack.entity.maestro.Role.fromEnum(com.mb.conitrack.enums.RoleEnum.ADMIN);
                com.mb.conitrack.entity.maestro.User systemUser = new com.mb.conitrack.entity.maestro.User("system_auto", "N/A", adminRole);
                when(userRepository.findByUsername("system_auto")).thenReturn(java.util.Optional.of(systemUser));

                com.mb.conitrack.entity.Movimiento movMock = new com.mb.conitrack.entity.Movimiento();
                movMock.setDictamenFinal(DictamenEnum.VENCIDO);
                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(any(), any(), any()))
                    .thenReturn(movMock);
                when(movimientoRepository.save(any(com.mb.conitrack.entity.Movimiento.class))).thenReturn(movMock);
                when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // When
                List<Lote> resultado = service.persistirProductosVencidos(dto, List.of(lote));

                // Then
                assertEquals(1, resultado.size());
                // Verificar que getUltimoAnalisis() es null y por lo tanto no se intenta cancelar
                assertNull(lote.getUltimoAnalisis());
                // No se debe guardar ningún análisis porque no existe
                verify(analisisRepository, never()).save(any(Analisis.class));
                // El lote se procesa normalmente
                verify(loteRepository).save(lote);
            }
        }
    }

    @Nested
    @DisplayName("procesarLotesAnalisisExpirado() - Cobertura líneas 139-155")
    class ProcesarLotesAnalisisExpiradoTests {

        @Test
        @DisplayName("Debe retornar inmediatamente si lista está vacía - línea 141")
        void procesarLotesAnalisisExpirado_listaVacia_debeRetornar() {
            // Given
            List<Lote> lotesVacios = List.of();

            // When
            service.procesarLotesAnalisisExpirado(lotesVacios);

            // Then
            verify(movimientoRepository, never()).save(any());
            verify(loteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe procesar lotes y mostrar log - líneas 143-154")
        void procesarLotesAnalisisExpirado_conLotes_debeProcesarYLoggear() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                Lote lote1 = crearLoteConFechaReanalisis(LocalDate.now());
                lote1.setLoteProveedor("LP-001");

                // Mock system user
                com.mb.conitrack.entity.maestro.Role adminRole = com.mb.conitrack.entity.maestro.Role.fromEnum(com.mb.conitrack.enums.RoleEnum.ADMIN);
                com.mb.conitrack.entity.maestro.User systemUser = new com.mb.conitrack.entity.maestro.User("system_auto", "N/A", adminRole);
                when(userRepository.findByUsername("system_auto")).thenReturn(java.util.Optional.of(systemUser));

                com.mb.conitrack.entity.Movimiento movMock = new com.mb.conitrack.entity.Movimiento();
                movMock.setDictamenFinal(DictamenEnum.ANALISIS_EXPIRADO);
                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(any(), any(), any()))
                    .thenReturn(movMock);
                when(movimientoRepository.save(any(com.mb.conitrack.entity.Movimiento.class))).thenReturn(movMock);
                when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // When
                service.procesarLotesAnalisisExpirado(List.of(lote1));

                // Then
                verify(movimientoRepository).save(any(com.mb.conitrack.entity.Movimiento.class));
                verify(loteRepository).save(lote1);
                String output = outContent.toString();
                assertTrue(output.contains("Reanalisis expirado"));
                assertTrue(output.contains("LP-001"));
            }
        }
    }

    @Nested
    @DisplayName("procesarLotesVencidos() - Cobertura líneas 159-172")
    class ProcesarLotesVencidosTests {

        @Test
        @DisplayName("Debe retornar inmediatamente si lista está vacía - línea 161")
        void procesarLotesVencidos_listaVacia_debeRetornar() {
            // Given
            List<Lote> lotesVacios = List.of();

            // When
            service.procesarLotesVencidos(lotesVacios);

            // Then
            verify(movimientoRepository, never()).save(any());
            verify(loteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe procesar lotes y mostrar log - líneas 163-171")
        void procesarLotesVencidos_conLotes_debeProcesarYLoggear() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                Lote lote1 = crearLoteConFechaVencimiento(LocalDate.now());
                lote1.setLoteProveedor("LP-002");

                // Mock system user
                com.mb.conitrack.entity.maestro.Role adminRole = com.mb.conitrack.entity.maestro.Role.fromEnum(com.mb.conitrack.enums.RoleEnum.ADMIN);
                com.mb.conitrack.entity.maestro.User systemUser = new com.mb.conitrack.entity.maestro.User("system_auto", "N/A", adminRole);
                when(userRepository.findByUsername("system_auto")).thenReturn(java.util.Optional.of(systemUser));

                com.mb.conitrack.entity.Movimiento movMock = new com.mb.conitrack.entity.Movimiento();
                movMock.setDictamenFinal(DictamenEnum.VENCIDO);
                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(any(), any(), any()))
                    .thenReturn(movMock);
                when(movimientoRepository.save(any(com.mb.conitrack.entity.Movimiento.class))).thenReturn(movMock);
                when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // When
                service.procesarLotesVencidos(List.of(lote1));

                // Then
                verify(movimientoRepository).save(any(com.mb.conitrack.entity.Movimiento.class));
                verify(loteRepository).save(lote1);
                String output = outContent.toString();
                assertTrue(output.contains("Vencido"));
                assertTrue(output.contains("LP-002"));
            }
        }
    }

    @Nested
    @DisplayName("getSystemUser() - Cobertura completa de lambdas")
    class GetSystemUserTests {

        @Test
        @DisplayName("Debe crear nuevo usuario y rol cuando no existen - líneas 33-37")
        void getSystemUser_noExiste_debeCrearNuevoUsuarioYRol() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                Lote lote = crearLoteConFechaVencimiento(LocalDate.now());
                com.mb.conitrack.dto.MovimientoDTO dto = new com.mb.conitrack.dto.MovimientoDTO();
                dto.setFechaYHoraCreacion(OffsetDateTime.now());
                dto.setObservaciones("Test");

                // Mock que NO existe usuario system_auto
                when(userRepository.findByUsername("system_auto")).thenReturn(java.util.Optional.empty());

                // Mock que NO existe el rol ADMIN
                when(roleRepository.findByName("ADMIN")).thenReturn(java.util.Optional.empty());

                // Mock que guarda un nuevo rol
                com.mb.conitrack.entity.maestro.Role nuevoRole = com.mb.conitrack.entity.maestro.Role.fromEnum(com.mb.conitrack.enums.RoleEnum.ADMIN);
                when(roleRepository.save(any(com.mb.conitrack.entity.maestro.Role.class))).thenReturn(nuevoRole);

                // Mock que guarda un nuevo usuario
                com.mb.conitrack.entity.maestro.User nuevoUsuario = new com.mb.conitrack.entity.maestro.User("system_auto", "N/A", nuevoRole);
                when(userRepository.save(any(com.mb.conitrack.entity.maestro.User.class))).thenReturn(nuevoUsuario);

                // Mock movimiento
                com.mb.conitrack.entity.Movimiento movMock = new com.mb.conitrack.entity.Movimiento();
                movMock.setDictamenFinal(DictamenEnum.VENCIDO);
                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(any(), any(), any()))
                        .thenReturn(movMock);
                when(movimientoRepository.save(any(com.mb.conitrack.entity.Movimiento.class))).thenReturn(movMock);

                // When - Llamar a un método que internamente usa getSystemUser()
                service.persistirMovimientoProductoVencido(dto, lote);

                // Then - Verificar que se creó el rol y el usuario
                verify(userRepository).findByUsername("system_auto");
                verify(roleRepository).findByName("ADMIN");
                verify(roleRepository).save(any(com.mb.conitrack.entity.maestro.Role.class));
                verify(userRepository).save(any(com.mb.conitrack.entity.maestro.User.class));
                verify(movimientoRepository).save(any(com.mb.conitrack.entity.Movimiento.class));
            }
        }

        @Test
        @DisplayName("Debe crear solo usuario cuando rol ya existe - línea 34")
        void getSystemUser_rolExiste_debeCrearSoloUsuario() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                Lote lote = crearLoteConFechaVencimiento(LocalDate.now());
                com.mb.conitrack.dto.MovimientoDTO dto = new com.mb.conitrack.dto.MovimientoDTO();
                dto.setFechaYHoraCreacion(OffsetDateTime.now());
                dto.setObservaciones("Test");

                // Mock que NO existe usuario system_auto
                when(userRepository.findByUsername("system_auto")).thenReturn(java.util.Optional.empty());

                // Mock que SÍ existe el rol ADMIN
                com.mb.conitrack.entity.maestro.Role roleExistente = com.mb.conitrack.entity.maestro.Role.fromEnum(com.mb.conitrack.enums.RoleEnum.ADMIN);
                when(roleRepository.findByName("ADMIN")).thenReturn(java.util.Optional.of(roleExistente));

                // Mock que guarda un nuevo usuario
                com.mb.conitrack.entity.maestro.User nuevoUsuario = new com.mb.conitrack.entity.maestro.User("system_auto", "N/A", roleExistente);
                when(userRepository.save(any(com.mb.conitrack.entity.maestro.User.class))).thenReturn(nuevoUsuario);

                // Mock movimiento
                com.mb.conitrack.entity.Movimiento movMock = new com.mb.conitrack.entity.Movimiento();
                movMock.setDictamenFinal(DictamenEnum.VENCIDO);
                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(any(), any(), any()))
                        .thenReturn(movMock);
                when(movimientoRepository.save(any(com.mb.conitrack.entity.Movimiento.class))).thenReturn(movMock);

                // When
                service.persistirMovimientoProductoVencido(dto, lote);

                // Then - Verificar que NO se creó el rol pero SÍ el usuario
                verify(userRepository).findByUsername("system_auto");
                verify(roleRepository).findByName("ADMIN");
                verify(roleRepository, never()).save(any(com.mb.conitrack.entity.maestro.Role.class));
                verify(userRepository).save(any(com.mb.conitrack.entity.maestro.User.class));
            }
        }
    }

}

package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.AnalisisRepository;
import com.mb.conitrack.repository.BultoRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.TrazaRepository;
import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.service.cu.validator.AnalisisValidator;
import com.mb.conitrack.service.cu.validator.CantidadValidator;
import com.mb.conitrack.service.cu.validator.FechaValidator;
import com.mb.conitrack.utils.LoteEntityUtils;
import com.mb.conitrack.utils.MovimientoBajaUtils;
import com.mb.conitrack.utils.UnidadMedidaUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MuestreoTrazableService - Tests")
class MuestreoTrazableServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private BultoRepository bultoRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private TrazaRepository trazaRepository;

    @Mock
    private AnalisisRepository analisisRepository;

    @InjectMocks
    private MuestreoTrazableService service;

    private MockedStatic<LoteEntityUtils> loteEntityUtilsMock;
    private MockedStatic<MovimientoBajaUtils> movimientoBajaUtilsMock;
    private MockedStatic<UnidadMedidaUtils> unidadMedidaUtilsMock;
    private MockedStatic<DTOUtils> dtoUtilsMock;

    private Lote lote;
    private Bulto bulto;
    private Producto producto;
    private User currentUser;
    private MovimientoDTO dto;
    private Analisis analisis;

    private MockedStatic<FechaValidator> fechaValidatorMock;
    private MockedStatic<AnalisisValidator> analisisValidatorMock;
    private MockedStatic<CantidadValidator> cantidadValidatorMock;

    @BeforeEach
    void setUp() {
        // Setup MockedStatic for validators
        fechaValidatorMock = mockStatic(FechaValidator.class);
        analisisValidatorMock = mockStatic(AnalisisValidator.class);
        cantidadValidatorMock = mockStatic(CantidadValidator.class);
        loteEntityUtilsMock = mockStatic(LoteEntityUtils.class);
        movimientoBajaUtilsMock = mockStatic(MovimientoBajaUtils.class);
        unidadMedidaUtilsMock = mockStatic(UnidadMedidaUtils.class);
        dtoUtilsMock = mockStatic(DTOUtils.class);

        // Configure default validator responses
        analisisValidatorMock.when(() -> AnalisisValidator.validarNroAnalisisNotNull(any(), any())).thenReturn(true);
        fechaValidatorMock.when(() -> FechaValidator.validarFechaMovimientoPosteriorIngresoLote(any(), any(), any())).thenReturn(true);
        fechaValidatorMock.when(() -> FechaValidator.validarFechaAnalisisPosteriorIngresoLote(any(), any(), any())).thenReturn(true);
        cantidadValidatorMock.when(() -> CantidadValidator.validarCantidadesMovimiento(any(), any(), any())).thenReturn(true);
        // Setup producto
        producto = new Producto();
        producto.setId(1L);
        producto.setCodigoProducto("PROD-001");
        producto.setTipoProducto(TipoProductoEnum.API);
        producto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);

        // Setup proveedor y fabricante
        Proveedor proveedor = new Proveedor();
        proveedor.setId(1L);
        proveedor.setRazonSocial("Proveedor Test");

        Proveedor fabricante = new Proveedor();
        fabricante.setId(2L);
        fabricante.setRazonSocial("Fabricante Test");

        // Setup lote
        lote = new Lote();
        lote.setId(1L);
        lote.setCodigoLote("LOTE-001");
        lote.setProducto(producto);
        lote.setProveedor(proveedor);
        lote.setFabricante(fabricante);
        lote.setCantidadInicial(new BigDecimal("100"));
        lote.setCantidadActual(new BigDecimal("100"));
        lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        lote.setFechaIngreso(LocalDate.of(2024, 1, 1));
        lote.setEstado(EstadoEnum.NUEVO);
        lote.setDictamen(DictamenEnum.RECIBIDO);
        lote.setMovimientos(new ArrayList<>());
        lote.setBultos(new ArrayList<>());
        lote.setTrazas(new HashSet<>());

        // Setup bulto
        bulto = new Bulto();
        bulto.setId(1L);
        bulto.setNroBulto(1);
        bulto.setLote(lote);
        bulto.setCantidadInicial(new BigDecimal("100"));
        bulto.setCantidadActual(new BigDecimal("100"));
        bulto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        bulto.setEstado(EstadoEnum.NUEVO);
        lote.getBultos().add(bulto);

        // Setup análisis
        analisis = new Analisis();
        analisis.setId(1L);
        analisis.setNroAnalisis("AN-001");
        analisis.setDictamen(null); // En curso
        analisis.setLote(lote);
        analisis.setActivo(true); // Required for getUltimoAnalisis()
        analisis.setFechaYHoraCreacion(OffsetDateTime.now()); // Required for getUltimoAnalisis()
        lote.setAnalisisList(new ArrayList<>(List.of(analisis)));

        // Setup user
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");

        // Setup DTO
        dto = new MovimientoDTO();
        dto.setCodigoLote("LOTE-001");
        dto.setNroBulto("1");
        dto.setNroAnalisis("AN-001");
        dto.setCantidad(new BigDecimal("10"));
        dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        dto.setFechaMovimiento(LocalDate.of(2024, 1, 20));
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setObservaciones("Test");
    }

    @AfterEach
    void tearDown() {
        // Close all MockedStatic instances
        if (fechaValidatorMock != null) {
            fechaValidatorMock.close();
        }
        if (analisisValidatorMock != null) {
            analisisValidatorMock.close();
        }
        if (cantidadValidatorMock != null) {
            cantidadValidatorMock.close();
        }
        if (loteEntityUtilsMock != null) {
            loteEntityUtilsMock.close();
        }
        if (movimientoBajaUtilsMock != null) {
            movimientoBajaUtilsMock.close();
        }
        if (unidadMedidaUtilsMock != null) {
            unidadMedidaUtilsMock.close();
        }
        if (dtoUtilsMock != null) {
            dtoUtilsMock.close();
        }
    }

    @Nested
    @DisplayName("procesarMuestreoTrazable() - Tests")
    class ProcesarMuestreoTrazableTests {



        @Test
        @DisplayName("Debe lanzar excepción cuando lote no existe")
        void procesarMuestreoTrazable_loteNoExiste_debeLanzarExcepcion() {
            // Given
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.procesarMuestreoTrazable(dto, currentUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El lote no existe.");
        }

    }

    @Nested
    @DisplayName("persistirMovimientoMuestreo() - Tests")
    class PersistirMovimientoMuestreoTests {

        @Test
        @DisplayName("Debe lanzar excepción cuando no hay análisis - cubre línea 82-83")
        void persistirMovimientoMuestreo_sinAnalisis_debeLanzarExcepcion() {
            // Given
            lote.setAnalisisList(new ArrayList<>());

            // When & Then
            assertThatThrownBy(() -> service.persistirMovimientoMuestreo(dto, bulto, currentUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No hay Analisis con al que asociar el muestreo");
        }

        @Test
        @DisplayName("Debe crear movimiento con análisis en curso - cubre línea 86-88")
        void persistirMovimientoMuestreo_analisisEnCurso_debeCrearMovimiento() {
            // Given
            analisis.setDictamen(null); // En curso

            // Mock utility methods
            loteEntityUtilsMock.when(() -> LoteEntityUtils.getAnalisisEnCurso(any())).thenReturn(Optional.of(analisis));

            Movimiento mockMovimiento = new Movimiento();
            mockMovimiento.setId(1L);
            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoMuestreoConAnalisis(any(), any(), any(), any()))
                    .thenReturn(mockMovimiento);

            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
                Movimiento mov = invocation.getArgument(0);
                mov.setId(1L);
                return mov;
            });

            // When
            Movimiento resultado = service.persistirMovimientoMuestreo(dto, bulto, currentUser);

            // Then
            assertNotNull(resultado);
            verify(movimientoRepository).save(any(Movimiento.class));
        }

        @Test
        @DisplayName("Debe crear movimiento con análisis dictaminado - cubre línea 90-91")
        void persistirMovimientoMuestreo_analisisDictaminado_debeCrearMovimiento() {
            // Given
            analisis.setDictamen(DictamenEnum.APROBADO); // Dictaminado

            // Mock utility methods - getAnalisisEnCurso will return empty for dictaminado
            loteEntityUtilsMock.when(() -> LoteEntityUtils.getAnalisisEnCurso(any())).thenReturn(Optional.empty());

            Movimiento mockMovimiento = new Movimiento();
            mockMovimiento.setId(1L);
            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoMuestreoConAnalisis(any(), any(), any(), any()))
                    .thenReturn(mockMovimiento);

            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
                Movimiento mov = invocation.getArgument(0);
                mov.setId(1L);
                return mov;
            });

            // When
            Movimiento resultado = service.persistirMovimientoMuestreo(dto, bulto, currentUser);

            // Then
            assertNotNull(resultado);
            verify(movimientoRepository).save(any(Movimiento.class));
        }
    }

    @Nested
    @DisplayName("crearMovimientoMuestreoConAnalisisEnCurso() - Tests")
    class CrearMovimientoMuestreoConAnalisisEnCursoTests {

        @Test
        @DisplayName("Debe crear movimiento cuando número de análisis coincide")
        void crearMovimientoMuestreoConAnalisisEnCurso_nroAnalisisCoincide_debeCrearMovimiento() {
            // Given
            Movimiento mockMovimiento = new Movimiento();
            mockMovimiento.setId(1L);
            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoMuestreoConAnalisis(any(), any(), any(), any()))
                    .thenReturn(mockMovimiento);

            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
                Movimiento mov = invocation.getArgument(0);
                mov.setId(1L);
                return mov;
            });

            // When
            Movimiento resultado = service.crearMovimientoMuestreoConAnalisisEnCurso(dto, bulto, Optional.of(analisis), currentUser);

            // Then
            assertNotNull(resultado);
            verify(movimientoRepository).save(any(Movimiento.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando número de análisis no coincide")
        void crearMovimientoMuestreoConAnalisisEnCurso_nroAnalisisNoCoincide_debeLanzarExcepcion() {
            // Given
            dto.setNroAnalisis("AN-999");

            // When & Then
            assertThatThrownBy(() -> service.crearMovimientoMuestreoConAnalisisEnCurso(dto, bulto, Optional.of(analisis), currentUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El número de análisis no coincide con el análisis en curso");
        }
    }

    @Nested
    @DisplayName("crearMovimientoMuestreoConAnalisisDictaminado() - Tests")
    class CrearMovimientoMuestreoConAnalisisDictaminadoTests {

        @Test
        @DisplayName("Debe crear movimiento cuando número de análisis coincide")
        void crearMovimientoMuestreoConAnalisisDictaminado_nroAnalisisCoincide_debeCrearMovimiento() {
            // Given
            analisis.setDictamen(DictamenEnum.APROBADO);

            Movimiento mockMovimiento = new Movimiento();
            mockMovimiento.setId(1L);
            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoMuestreoConAnalisis(any(), any(), any(), any()))
                    .thenReturn(mockMovimiento);

            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
                Movimiento mov = invocation.getArgument(0);
                mov.setId(1L);
                return mov;
            });

            // When
            Movimiento resultado = service.crearMovimientoMuestreoConAnalisisDictaminado(dto, bulto, currentUser);

            // Then
            assertNotNull(resultado);
            verify(movimientoRepository).save(any(Movimiento.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando número de análisis no coincide")
        void crearMovimientoMuestreoConAnalisisDictaminado_nroAnalisisNoCoincide_debeLanzarExcepcion() {
            // Given
            dto.setNroAnalisis("AN-999");
            analisis.setDictamen(DictamenEnum.APROBADO);

            // When & Then
            assertThatThrownBy(() -> service.crearMovimientoMuestreoConAnalisisDictaminado(dto, bulto, currentUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El número de análisis no coincide con el análisis en curso");
        }
    }

    @Nested
    @DisplayName("procesarTrazasUnidadVenta() - Tests (via procesarMuestreoTrazable)")
    class ProcesarTrazasUnidadVentaTests {

    }

    @Nested
    @DisplayName("validarMuestreoTrazableInput() - Tests")
    class ValidarMuestreoTrazableInputTests {

        @Test
        @DisplayName("Debe retornar false cuando bindingResult tiene errores")
        void validarMuestreoTrazableInput_bindingResultConErrores_debeRetornarFalse() {
            // Given
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(true);

            // When
            boolean resultado = service.validarMuestreoTrazableInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
        }

        @Test
        @DisplayName("Debe retornar false cuando lote no existe")
        void validarMuestreoTrazableInput_loteNoExiste_debeRetornarFalse() {
            // Given
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.empty());

            // When
            boolean resultado = service.validarMuestreoTrazableInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).rejectValue("codigoLote", "", "Lote no encontrado.");
        }

        @Test
        @DisplayName("Debe retornar false cuando producto es UNIDAD_VENTA y trazaDTOs es null - cubre línea 154 branch 1")
        void validarMuestreoTrazableInput_unidadVentaTrazasNull_debeRetornarFalse() {
            // Given
            producto.setTipoProducto(TipoProductoEnum.UNIDAD_VENTA);
            dto.setTrazaDTOs(null);

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));

            // When
            boolean resultado = service.validarMuestreoTrazableInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).rejectValue("trazaDTOs", "", "Debe seleccionar al menos una unidad a muestrear.");
        }

        @Test
        @DisplayName("Debe retornar false cuando producto es UNIDAD_VENTA y trazaDTOs está vacío")
        void validarMuestreoTrazableInput_unidadVentaTrazasVacias_debeRetornarFalse() {
            // Given
            producto.setTipoProducto(TipoProductoEnum.UNIDAD_VENTA);
            dto.setTrazaDTOs(new ArrayList<>());

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));

            // When
            boolean resultado = service.validarMuestreoTrazableInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).rejectValue("trazaDTOs", "", "Debe seleccionar al menos una unidad a muestrear.");
        }

        @Test
        @DisplayName("Debe retornar false cuando bulto no existe")
        void validarMuestreoTrazableInput_bultoNoExiste_debeRetornarFalse() {
            // Given
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));
            when(bultoRepository.findFirstByLoteCodigoLoteAndNroBultoAndActivoTrue(eq("LOTE-001"), eq(1))).thenReturn(Optional.empty());

            // When
            boolean resultado = service.validarMuestreoTrazableInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).rejectValue("nroBulto", "", "Bulto no encontrado.");
        }

        @Test
        @DisplayName("Debe validar correctamente cuando producto NO es UNIDAD_VENTA - cubre branch trazaDTOs NOT null/empty")
        void validarMuestreoTrazableInput_productoNoUnidadVenta_debeValidarCorrectamente() {
            // Given
            producto.setTipoProducto(TipoProductoEnum.API);
            dto.setTrazaDTOs(List.of(new TrazaDTO())); // NOT null - cubre branch faltante línea 154

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));
            when(bultoRepository.findFirstByLoteCodigoLoteAndNroBultoAndActivoTrue(eq("LOTE-001"), eq(1))).thenReturn(Optional.of(bulto));

            lenient().when(bindingResult.hasFieldErrors(anyString())).thenReturn(false);

            // When
            boolean resultado = service.validarMuestreoTrazableInput(dto, bindingResult);

            // Then
            assertTrue(resultado);
        }
    }

    // ========== Direct Tests for Package-Protected Methods ==========

    @Test
    @DisplayName("procesarTrazasUnidadVenta - Debe procesar trazas exitosamente")
    void procesarTrazasUnidadVenta_conTrazasValidas_debeProcesarCorrectamente() {
        // Given
        dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        dto.setCantidad(new BigDecimal("2"));

        TrazaDTO trazaDTO1 = new TrazaDTO();
        trazaDTO1.setNroTraza(1L);
        TrazaDTO trazaDTO2 = new TrazaDTO();
        trazaDTO2.setNroTraza(2L);
        dto.setTrazaDTOs(new ArrayList<>(List.of(trazaDTO1, trazaDTO2)));

        Traza traza1 = crearTraza(1L);
        Traza traza2 = crearTraza(2L);
        Set<Traza> trazasSet = new HashSet<>();
        trazasSet.add(traza1);
        trazasSet.add(traza2);
        lote.setTrazas(trazasSet);

        Movimiento movimiento = new Movimiento();
        movimiento.setId(1L);
        movimiento.setCantidad(new BigDecimal("2"));
        movimiento.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        DetalleMovimiento detalle = new DetalleMovimiento();
        detalle.setId(1L);
        detalle.setTrazas(new HashSet<>());
        detalle.setMovimiento(movimiento);

        Set<DetalleMovimiento> detalles = new HashSet<>();
        detalles.add(detalle);
        movimiento.setDetalles(detalles);

        when(trazaRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        dtoUtilsMock.when(() -> DTOUtils.fromTrazaEntity(any(Traza.class))).thenReturn(new TrazaDTO());

        // When
        service.procesarTrazasUnidadVenta(dto, lote, movimiento);

        // Then
        assertThat(traza1.getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);
        assertThat(traza2.getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);
        assertThat(detalle.getTrazas()).hasSize(2);
        assertThat(detalle.getTrazas()).contains(traza1, traza2);
        verify(trazaRepository).saveAll(any());
    }

    @Test
    @DisplayName("obtenerTrazasSeleccionadas - Debe retornar trazas que coinciden con DTOs")
    void obtenerTrazasSeleccionadas_conTrazasCoincidentes_debeRetornarLista() {
        // Given
        TrazaDTO trazaDTO1 = new TrazaDTO();
        trazaDTO1.setNroTraza(1L);
        TrazaDTO trazaDTO2 = new TrazaDTO();
        trazaDTO2.setNroTraza(2L);
        dto.setTrazaDTOs(List.of(trazaDTO1, trazaDTO2));

        Traza traza1 = crearTraza(1L);
        Traza traza2 = crearTraza(2L);
        Traza traza3 = crearTraza(3L);

        Set<Traza> trazasSet = new HashSet<>();
        trazasSet.add(traza1);
        trazasSet.add(traza2);
        trazasSet.add(traza3);
        lote.setTrazas(trazasSet);

        // When
        List<Traza> resultado = service.obtenerTrazasSeleccionadas(dto, lote);

        // Then
        assertThat(resultado).hasSize(2);
        assertThat(resultado).contains(traza1, traza2);
        assertThat(resultado).doesNotContain(traza3);
    }

    @Test
    @DisplayName("actualizarEstadoBulto - Debe cambiar estado a CONSUMIDO cuando cantidad es cero")
    void actualizarEstadoBulto_cantidadCero_debeCambiarAConsumido() {
        // Given
        bulto.setCantidadActual(BigDecimal.ZERO);
        bulto.setEstado(EstadoEnum.DISPONIBLE);

        // When
        service.actualizarEstadoBulto(bulto);

        // Then
        assertThat(bulto.getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);
    }

    @Test
    @DisplayName("actualizarEstadoBulto - Debe cambiar estado a EN_USO cuando cantidad es mayor a cero")
    void actualizarEstadoBulto_cantidadMayorCero_debeCambiarAEnUso() {
        // Given
        bulto.setCantidadActual(new BigDecimal("50"));
        bulto.setEstado(EstadoEnum.DISPONIBLE);

        // When
        service.actualizarEstadoBulto(bulto);

        // Then
        assertThat(bulto.getEstado()).isEqualTo(EstadoEnum.EN_USO);
    }

    @Test
    @DisplayName("actualizarEstadoLote - Debe marcar lote como CONSUMIDO cuando todos los bultos están consumidos")
    void actualizarEstadoLote_todosBultosConsumidos_debeCambiarAConsumido() {
        // Given
        Bulto bulto1 = new Bulto();
        bulto1.setEstado(EstadoEnum.CONSUMIDO);
        Bulto bulto2 = new Bulto();
        bulto2.setEstado(EstadoEnum.CONSUMIDO);

        lote.setBultos(List.of(bulto1, bulto2));
        lote.setEstado(EstadoEnum.DISPONIBLE);

        // When
        service.actualizarEstadoLote(lote);

        // Then
        assertThat(lote.getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);
    }

    @Test
    @DisplayName("actualizarEstadoLote - Debe marcar lote como EN_USO cuando al menos un bulto no está consumido")
    void actualizarEstadoLote_algunBultoNoConsumido_debeCambiarAEnUso() {
        // Given
        Bulto bulto1 = new Bulto();
        bulto1.setEstado(EstadoEnum.CONSUMIDO);
        Bulto bulto2 = new Bulto();
        bulto2.setEstado(EstadoEnum.EN_USO);

        lote.setBultos(List.of(bulto1, bulto2));
        lote.setEstado(EstadoEnum.DISPONIBLE);

        // When
        service.actualizarEstadoLote(lote);

        // Then
        assertThat(lote.getEstado()).isEqualTo(EstadoEnum.EN_USO);
    }

    // ========== Helper Methods ==========

    private Traza crearTraza(Long nroTraza) {
        Traza traza = new Traza();
        traza.setId(nroTraza);
        traza.setNroTraza(nroTraza);
        traza.setEstado(EstadoEnum.DISPONIBLE);
        traza.setActivo(true);
        traza.setLote(lote);
        traza.setDetalles(new ArrayList<>()); // Initialize detalles list
        return traza;
    }
}

package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.AnalisisRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.service.SecurityContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios con mocks para ModifResultadoAnalisisService (CU5/6).
 * Usa Mockito para verificar lógica del service sin dependencias de BD.
 *
 * Cobertura completa del flujo CU5/6:
 * - Registro de resultado de análisis (APROBADO, RECHAZADO, CUARENTENA)
 * - Validación completa de entrada
 * - Actualización de análisis existente o creación de nuevo
 * - Creación de movimiento MODIFICACION/RESULTADO_ANALISIS
 * - Actualización de dictamen del lote
 * - Manejo de errores
 * - Casos edge (fechas, reanalisis, titulo)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - ModifResultadoAnalisisService (CU5/6)")
class ModifResultadoAnalisisServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private AnalisisRepository analisisRepository;

    @Mock
    private SecurityContextService securityContextService;

    @Spy
    @InjectMocks
    private ModifResultadoAnalisisService service;

    private User testUser;
    private Lote loteTest;
    private Producto productoTest;
    private Proveedor proveedorTest;
    private Analisis analisisTest;

    @BeforeEach
    void setUp() {
        // Crear usuario de test
        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setId(1L);
        testUser = new User("testuser", "password", adminRole);
        testUser.setId(1L);

        // Crear producto de test
        productoTest = new Producto();
        productoTest.setId(1L);
        productoTest.setCodigoProducto("API-TEST-001");
        productoTest.setNombreGenerico("Paracetamol Test");
        productoTest.setTipoProducto(TipoProductoEnum.API);
        productoTest.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        productoTest.setActivo(true);

        // Crear proveedor de test
        proveedorTest = new Proveedor();
        proveedorTest.setId(1L);
        proveedorTest.setRazonSocial("Proveedor Test");
        proveedorTest.setPais("Argentina");
        proveedorTest.setCuit("20-12345678-9");
        proveedorTest.setDireccion("Calle Test 123");
        proveedorTest.setCiudad("Buenos Aires");
        proveedorTest.setActivo(true);

        // Crear lote de test
        loteTest = crearLoteTest();

        // Crear análisis de test
        analisisTest = crearAnalisisTest();

        // Mock SecurityContextService (lenient to avoid UnnecessaryStubbingException)
        lenient().when(securityContextService.getCurrentUser()).thenReturn(testUser);
    }

    private Lote crearLoteTest() {
        Lote lote = new Lote();
        lote.setId(1L);
        lote.setCodigoLote("L-TEST-001");
        lote.setLoteProveedor("LP-2025-001");
        lote.setProducto(productoTest);
        lote.setProveedor(proveedorTest);
        lote.setFechaIngreso(LocalDate.now().minusDays(10));
        lote.setCantidadInicial(new BigDecimal("100.00"));
        lote.setCantidadActual(new BigDecimal("100.00"));
        lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        lote.setBultosTotales(2);
        lote.setEstado(EstadoEnum.NUEVO);
        lote.setDictamen(DictamenEnum.RECIBIDO);
        lote.setActivo(true);

        // Usar ArrayList para permitir modificación
        lote.setMovimientos(new ArrayList<>());
        lote.setAnalisisList(new ArrayList<>());

        return lote;
    }

    private Analisis crearAnalisisTest() {
        Analisis analisis = new Analisis();
        analisis.setId(1L);
        analisis.setNroAnalisis("AN-2025-001");
        analisis.setLote(loteTest);
        analisis.setFechaRealizado(null); // Sin resultado aún
        analisis.setDictamen(null); // Pendiente
        analisis.setActivo(true);
        return analisis;
    }

    @Nested
    @DisplayName("persistirResultadoAnalisis() - Flujo completo")
    class PersistirResultadoAnalisis {

        @Test
        @DisplayName("test_resultadoAnalisisAprobado_debe_actualizarLoteYAnalisis")
        void test_resultadoAnalisisAprobado_debe_actualizarLoteYAnalisis() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setFechaReanalisis(LocalDate.now().plusMonths(12));
            dto.setFechaVencimiento(LocalDate.now().plusYears(2));
            dto.setTitulo(new BigDecimal("99.5"));
            dto.setObservaciones("Análisis aprobado - conforme a especificaciones");

            // Mock repository responses
            when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-2025-001")).thenReturn(analisisTest);
            when(analisisRepository.save(any(Analisis.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
                Movimiento mov = invocation.getArgument(0);
                mov.setId(1L);
                return mov;
            });
            when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoteDTO resultado = service.persistirResultadoAnalisis(dto);

            // Then - Verificar DTO resultado
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigoLote()).isEqualTo("L-TEST-001");
            assertThat(resultado.getDictamen()).isEqualTo(DictamenEnum.APROBADO);

            // Verificar que el lote fue actualizado
            assertThat(loteTest.getDictamen()).isEqualTo(DictamenEnum.APROBADO);

            // Verificar que el análisis fue actualizado
            assertThat(analisisTest.getDictamen()).isEqualTo(DictamenEnum.APROBADO);
            assertThat(analisisTest.getFechaRealizado()).isEqualTo(LocalDate.now());
            assertThat(analisisTest.getFechaReanalisis()).isEqualTo(dto.getFechaReanalisis());
            assertThat(analisisTest.getFechaVencimiento()).isEqualTo(dto.getFechaVencimiento());
            assertThat(analisisTest.getTitulo()).isEqualByComparingTo(new BigDecimal("99.5"));

            // Verificar que se llamaron los métodos esperados
            verify(analisisRepository).findByNroAnalisisAndActivoTrue("AN-2025-001");
            verify(analisisRepository).save(analisisTest);
            verify(movimientoRepository).save(any(Movimiento.class));
            verify(loteRepository).save(loteTest);
        }

        @Test
        @DisplayName("test_resultadoAnalisisRechazado_debe_actualizarSinFechasVencimiento")
        void test_resultadoAnalisisRechazado_debe_actualizarSinFechasVencimiento() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.RECHAZADO);
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setObservaciones("Análisis rechazado - fuera de especificación");

            // Mock repository responses
            when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-2025-001")).thenReturn(analisisTest);
            when(analisisRepository.save(any(Analisis.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
                Movimiento mov = invocation.getArgument(0);
                mov.setId(1L);
                return mov;
            });
            when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoteDTO resultado = service.persistirResultadoAnalisis(dto);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getDictamen()).isEqualTo(DictamenEnum.RECHAZADO);

            // Verificar que el análisis fue actualizado sin fechas de vencimiento/reanalisis
            assertThat(analisisTest.getDictamen()).isEqualTo(DictamenEnum.RECHAZADO);
            assertThat(analisisTest.getFechaReanalisis()).isNull();
            assertThat(analisisTest.getFechaVencimiento()).isNull();
            assertThat(analisisTest.getTitulo()).isNull();

            verify(loteRepository).save(loteTest);
        }

        @Test
        @DisplayName("test_resultadoAnalisisCuarentena_debe_actualizarDictamenCorrectamente")
        void test_resultadoAnalisisCuarentena_debe_actualizarDictamenCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.CUARENTENA);
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setObservaciones("Requiere reanalisis");

            // Mock repository responses
            when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-2025-001")).thenReturn(analisisTest);
            when(analisisRepository.save(any(Analisis.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
                Movimiento mov = invocation.getArgument(0);
                mov.setId(1L);
                return mov;
            });
            when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoteDTO resultado = service.persistirResultadoAnalisis(dto);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getDictamen()).isEqualTo(DictamenEnum.CUARENTENA);
            assertThat(loteTest.getDictamen()).isEqualTo(DictamenEnum.CUARENTENA);
            assertThat(analisisTest.getDictamen()).isEqualTo(DictamenEnum.CUARENTENA);
        }

        @Test
        @DisplayName("test_resultadoAnalisisNuevo_debe_crearAnalisisYActualizarLote")
        void test_resultadoAnalisisNuevo_debe_crearAnalisisYActualizarLote() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis("AN-2025-999"); // Nuevo análisis
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setFechaVencimiento(LocalDate.now().plusYears(2));
            dto.setTitulo(new BigDecimal("98.0"));
            dto.setObservaciones("Nuevo análisis");

            // Mock repository responses - No existe análisis previo
            when(analisisRepository.findByNroAnalisisAndActivoTrue("AN-2025-999")).thenReturn(null);
            when(analisisRepository.save(any(Analisis.class))).thenAnswer(invocation -> {
                Analisis analisis = invocation.getArgument(0);
                analisis.setId(2L);
                analisis.setLote(loteTest);
                return analisis;
            });
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
                Movimiento mov = invocation.getArgument(0);
                mov.setId(1L);
                return mov;
            });
            when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoteDTO resultado = service.persistirResultadoAnalisis(dto);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getDictamen()).isEqualTo(DictamenEnum.APROBADO);

            // Verificar que se creó un nuevo análisis
            verify(analisisRepository).findByNroAnalisisAndActivoTrue("AN-2025-999");
            verify(analisisRepository).save(any(Analisis.class));
        }
    }

    @Nested
    @DisplayName("persistirMovimientoResultadoAnalisis() - Creación de movimiento")
    class PersistirMovimientoResultadoAnalisis {

        @Test
        @DisplayName("test_crearMovimiento_debe_configurarCorrectamente")
        void test_crearMovimiento_debe_configurarCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaRealizadoAnalisis(LocalDate.now().minusDays(2));
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setObservaciones("Test observations");

            loteTest.setDictamen(DictamenEnum.RECIBIDO);

            // Mock repository
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
                Movimiento mov = invocation.getArgument(0);
                mov.setId(1L);
                return mov;
            });

            // When
            Movimiento resultado = service.persistirMovimientoResultadoAnalisis(dto, loteTest, testUser);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getTipoMovimiento()).isEqualTo(TipoMovimientoEnum.MODIFICACION);
            assertThat(resultado.getMotivo()).isEqualTo(MotivoEnum.RESULTADO_ANALISIS);
            assertThat(resultado.getNroAnalisis()).isEqualTo("AN-2025-001");
            assertThat(resultado.getDictamenInicial()).isEqualTo(DictamenEnum.RECIBIDO);
            assertThat(resultado.getDictamenFinal()).isEqualTo(DictamenEnum.APROBADO);
            assertThat(resultado.getFecha()).isEqualTo(dto.getFechaRealizadoAnalisis());
            assertThat(resultado.getObservaciones()).contains("_CU5/6_");
            assertThat(resultado.getObservaciones()).contains("Test observations");
            assertThat(resultado.getCreadoPor()).isEqualTo(testUser);

            verify(movimientoRepository).save(any(Movimiento.class));
        }
    }

    @Nested
    @DisplayName("validarResultadoAnalisisInput() - Validaciones")
    class ValidarResultadoAnalisisInput {

        @Test
        @DisplayName("test_validacionExitosa_debe_retornarTrue")
        void test_validacionExitosa_debe_retornarTrue() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaVencimiento(LocalDate.now().plusYears(2));
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // Mock repository
            when(movimientoRepository.existeMuestreo("L-TEST-001", "AN-2025-001")).thenReturn(true);
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));

            // When
            boolean resultado = service.validarResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("test_validacionConErroresBinding_debe_retornarFalse")
        void test_validacionConErroresBinding_debe_retornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");
            binding.rejectValue("codigoLote", "", "Campo obligatorio");

            // When
            boolean resultado = service.validarResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("test_validacionNroAnalisisVacio_debe_agregarError")
        void test_validacionNroAnalisisVacio_debe_agregarError() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis(""); // Vacío
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setFechaMovimiento(LocalDate.now());
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // When
            boolean resultado = service.validarResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("nroAnalisis")).isNotNull();
            assertThat(binding.getFieldError("nroAnalisis").getDefaultMessage()).contains("obligatorio");
        }

        @Test
        @DisplayName("test_validacionDictamenFinalNulo_debe_agregarError")
        void test_validacionDictamenFinalNulo_debe_agregarError() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(null); // Nulo
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setFechaMovimiento(LocalDate.now());
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // When
            boolean resultado = service.validarResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("dictamenFinal")).isNotNull();
            assertThat(binding.getFieldError("dictamenFinal").getDefaultMessage()).contains("Resultado");
        }

        @Test
        @DisplayName("test_validacionFechaRealizadoAnalisisNula_debe_agregarError")
        void test_validacionFechaRealizadoAnalisisNula_debe_agregarError() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaRealizadoAnalisis(null); // Nula
            dto.setFechaMovimiento(LocalDate.now());
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // When
            boolean resultado = service.validarResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("fechaRealizadoAnalisis")).isNotNull();
        }

        @Test
        @DisplayName("test_validacionAprobadoSinFechas_debe_agregarError")
        void test_validacionAprobadoSinFechas_debe_agregarError() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setFechaMovimiento(LocalDate.now());
            // Sin fechaVencimiento ni fechaReanalisis
            dto.setFechaVencimiento(null);
            dto.setFechaReanalisis(null);
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // When
            boolean resultado = service.validarResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("fechaVencimiento")).isNotNull();
            assertThat(binding.getFieldError("fechaVencimiento").getDefaultMessage())
                .contains("Re Análisis o Vencimiento");
        }

        @Test
        @DisplayName("test_validacionReanalisisPosteriorVencimiento_debe_agregarError")
        void test_validacionReanalisisPosteriorVencimiento_debe_agregarError() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaVencimiento(LocalDate.now().plusYears(1));
            dto.setFechaReanalisis(LocalDate.now().plusYears(2)); // Posterior a vencimiento
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // When
            boolean resultado = service.validarResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("fechaReanalisis")).isNotNull();
        }

        @Test
        @DisplayName("test_validacionTituloMayorCien_debe_agregarError")
        void test_validacionTituloMayorCien_debe_agregarError() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaVencimiento(LocalDate.now().plusYears(2));
            dto.setTitulo(new BigDecimal("101.0")); // Mayor a 100%
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // When
            boolean resultado = service.validarResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("titulo")).isNotNull();
            assertThat(binding.getFieldError("titulo").getDefaultMessage()).contains("mayor al 100%");
        }

        @Test
        @DisplayName("test_validacionTituloCeroONegativo_debe_agregarError")
        void test_validacionTituloCeroONegativo_debe_agregarError() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaVencimiento(LocalDate.now().plusYears(2));
            dto.setTitulo(BigDecimal.ZERO); // Cero
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // When
            boolean resultado = service.validarResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("titulo")).isNotNull();
            assertThat(binding.getFieldError("titulo").getDefaultMessage()).contains("menor o igual a 0");
        }

        @Test
        @DisplayName("test_validacionMuestreoNoExiste_debe_agregarError")
        void test_validacionMuestreoNoExiste_debe_agregarError() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaVencimiento(LocalDate.now().plusYears(2));
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // Mock - No existe muestreo
            when(movimientoRepository.existeMuestreo("L-TEST-001", "AN-2025-001")).thenReturn(false);

            // When
            boolean resultado = service.validarResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("nroAnalisis")).isNotNull();
            assertThat(binding.getFieldError("nroAnalisis").getDefaultMessage()).contains("MUESTREO");
        }

        @Test
        @DisplayName("test_validacionLoteNoEncontrado_debe_agregarError")
        void test_validacionLoteNoEncontrado_debe_agregarError() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-INEXISTENTE-999");
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaVencimiento(LocalDate.now().plusYears(2));
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // Mock
            when(movimientoRepository.existeMuestreo("L-INEXISTENTE-999", "AN-2025-001")).thenReturn(true);
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-INEXISTENTE-999")).thenReturn(Optional.empty());

            // When
            boolean resultado = service.validarResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("codigoLote")).isNotNull();
            assertThat(binding.getFieldError("codigoLote").getDefaultMessage()).contains("no encontrado");
        }

        @Test
        @DisplayName("test_validacionFechaMovimientoAnteriorIngreso_debe_agregarError")
        void test_validacionFechaMovimientoAnteriorIngreso_debe_agregarError() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setFechaMovimiento(LocalDate.now().minusDays(15)); // Anterior al ingreso (hace 10 días)
            dto.setFechaVencimiento(LocalDate.now().plusYears(2));
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // Mock
            when(movimientoRepository.existeMuestreo("L-TEST-001", "AN-2025-001")).thenReturn(true);
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));

            // When
            boolean resultado = service.validarResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("fechaMovimiento")).isNotNull();
        }

        @Test
        @DisplayName("test_validacionFechaAnalisisAnteriorIngreso_debe_agregarError")
        void test_validacionFechaAnalisisAnteriorIngreso_debe_agregarError() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaRealizadoAnalisis(LocalDate.now().minusDays(15)); // Anterior al ingreso
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaVencimiento(LocalDate.now().plusYears(2));
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // Mock
            when(movimientoRepository.existeMuestreo("L-TEST-001", "AN-2025-001")).thenReturn(true);
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));

            // When
            boolean resultado = service.validarResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("fechaRealizadoAnalisis")).isNotNull();
        }

        @Test
        @DisplayName("test_validacionRechazadoSinFechas_debe_retornarTrue")
        void test_validacionRechazadoSinFechas_debe_retornarTrue() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.RECHAZADO); // RECHAZADO no requiere fechas
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setFechaMovimiento(LocalDate.now());
            // Sin fechas de vencimiento/reanalisis
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // Mock
            when(movimientoRepository.existeMuestreo("L-TEST-001", "AN-2025-001")).thenReturn(true);
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));

            // When
            boolean resultado = service.validarResultadoAnalisisInput(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("test_validacionFechasReanalisis_debe_llamarValidacion")
        void test_validacionFechasReanalisis_debe_llamarValidacion() {
            // Given - Test directo del método validarFechasReanalisis desde AbstractCuService
            // Esto cubre las líneas 97-98 cuando validarFechasReanalisis retorna false
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaVencimiento(LocalDate.now().plusYears(1));
            dto.setFechaReanalisis(LocalDate.now().plusYears(2)); // Posterior a vencimiento

            BeanPropertyBindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // When - Llamar directamente al método validarFechasReanalisis
            boolean resultado = service.validarFechasReanalisis(dto, binding);

            // Then - Debe retornar false y agregar error (AbstractCuService:417-427)
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("fechaVencimiento")).isNotNull();
            assertThat(binding.getFieldError("fechaVencimiento").getDefaultMessage())
                .contains("La fecha de reanálisis no puede ser posterior a la fecha de vencimiento");
        }

        @Test
        @DisplayName("test_validarResultadoAnalisisInput_forzarLlamadaValidarFechasReanalisis_conSpy")
        void test_validarResultadoAnalisisInput_forzarLlamadaValidarFechasReanalisis_conSpy() {
            // Given - DTO válido en todos los aspectos
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroAnalisis("AN-2025-001");
            dto.setDictamenFinal(DictamenEnum.APROBADO);
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setFechaMovimiento(LocalDate.now());
            dto.setTitulo(new BigDecimal("95.5"));
            dto.setFechaVencimiento(LocalDate.now().plusYears(1));
            dto.setFechaReanalisis(LocalDate.now().plusMonths(6)); // Válido: antes del vencimiento
            dto.setObservaciones("Test observations");

            BeanPropertyBindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // Mock: muestreo existe
            when(movimientoRepository.existeMuestreo(dto.getCodigoLote(), dto.getNroAnalisis())).thenReturn(true);

            // Mock: lote encontrado
            Lote loteTest = new Lote();
            loteTest.setCodigoLote("L-TEST-001");
            loteTest.setFechaIngreso(LocalDate.now().minusMonths(1));
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));

            // Spy: Forzar que validarFechasReanalisis retorne false para cubrir líneas 97-98
            // Esto simula el caso donde el método es llamado pero retorna false
            doReturn(false).when(service).validarFechasReanalisis(any(MovimientoDTO.class), any(BindingResult.class));

            // When - Validar input completo, llegará hasta línea 97 y entrará a línea 98
            boolean resultado = service.validarResultadoAnalisisInput(dto, binding);

            // Then - Debe retornar false en línea 98 (cubriendo esa línea)
            assertThat(resultado).isFalse();
            verify(service, times(1)).validarFechasReanalisis(any(MovimientoDTO.class), any(BindingResult.class));
        }
    }

}

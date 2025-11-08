package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.AnalisisRepository;
import com.mb.conitrack.repository.BultoRepository;
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
import static org.mockito.Mockito.lenient;

/**
 * Tests unitarios con mocks para BajaDevolucionCompraService (CU4).
 * Usa Mockito para verificar lógica del service sin dependencias de BD.
 *
 * Cobertura completa del flujo CU4:
 * - Baja de stock por devolución a compra
 * - Validación de entrada completa
 * - Actualización de lote y bultos
 * - Creación de movimiento BAJA/DEVOLUCION_COMPRA
 * - Manejo de errores
 * - Casos edge
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - BajaDevolucionCompraService (CU4)")
class BajaDevolucionCompraServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private BultoRepository bultoRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private AnalisisRepository analisisRepository;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private BajaDevolucionCompraService service;

    private User testUser;
    private Lote loteTest;
    private Producto productoTest;
    private Proveedor proveedorTest;

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

        // Crear lote de test con bultos
        loteTest = crearLoteConBultos();

        // Mock SecurityContextService (lenient to avoid UnnecessaryStubbingException)
        lenient().when(securityContextService.getCurrentUser()).thenReturn(testUser);
    }

    private Lote crearLoteConBultos() {
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

        // Crear bultos
        Bulto bulto1 = new Bulto();
        bulto1.setId(1L);
        bulto1.setNroBulto(1);
        bulto1.setLote(lote);
        bulto1.setCantidadInicial(new BigDecimal("50.00"));
        bulto1.setCantidadActual(new BigDecimal("50.00"));
        bulto1.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        bulto1.setEstado(EstadoEnum.NUEVO);
        bulto1.setActivo(true);

        Bulto bulto2 = new Bulto();
        bulto2.setId(2L);
        bulto2.setNroBulto(2);
        bulto2.setLote(lote);
        bulto2.setCantidadInicial(new BigDecimal("50.00"));
        bulto2.setCantidadActual(new BigDecimal("50.00"));
        bulto2.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        bulto2.setEstado(EstadoEnum.NUEVO);
        bulto2.setActivo(true);

        // Usar ArrayList para permitir modificación
        lote.setBultos(new ArrayList<>());
        lote.getBultos().add(bulto1);
        lote.getBultos().add(bulto2);

        // Usar ArrayList para permitir modificación
        lote.setMovimientos(new ArrayList<>());
        lote.setAnalisisList(new ArrayList<>());

        return lote;
    }

    @Nested
    @DisplayName("bajaBultosDevolucionCompra() - Flujo completo")
    class BajaBultosDevolucionCompra {

        @Test
        @DisplayName("test_devolucionCompraExitosa_debe_actualizarLoteYBultosACero")
        void test_devolucionCompraExitosa_debe_actualizarLoteYBultosACero() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setObservaciones("Devolución por defecto de calidad");

            // Mock repository responses
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
                Movimiento mov = invocation.getArgument(0);
                mov.setId(1L);
                return mov;
            });
            when(bultoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoteDTO resultado = service.bajaBultosDevolucionCompra(dto);

            // Then - Verificar DTO resultado
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigoLote()).isEqualTo("L-TEST-001");
            assertThat(resultado.getCantidadActual()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(resultado.getEstado()).isEqualTo(EstadoEnum.DEVUELTO);

            // Verificar que el lote fue actualizado
            assertThat(loteTest.getCantidadActual()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(loteTest.getEstado()).isEqualTo(EstadoEnum.DEVUELTO);

            // Verificar bultos actualizados a CERO
            for (Bulto bulto : loteTest.getBultos()) {
                assertThat(bulto.getCantidadActual()).isEqualByComparingTo(BigDecimal.ZERO);
                assertThat(bulto.getEstado()).isEqualTo(EstadoEnum.DEVUELTO);
            }

            // Verificar que se llamaron los métodos esperados
            verify(loteRepository).findByCodigoLoteAndActivoTrue("L-TEST-001");
            verify(movimientoRepository).save(any(Movimiento.class));
            verify(bultoRepository).saveAll(any());
            verify(loteRepository).save(loteTest);
        }

        @Test
        @DisplayName("test_devolucionCompraConAnalisisPendiente_debe_cancelarAnalisis")
        void test_devolucionCompraConAnalisisPendiente_debe_cancelarAnalisis() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // Crear análisis pendiente (sin dictamen)
            Analisis analisisPendiente = new Analisis();
            analisisPendiente.setId(1L);
            analisisPendiente.setLote(loteTest);
            analisisPendiente.setNroAnalisis("AN-2025-001");
            analisisPendiente.setFechaRealizado(LocalDate.now());
            analisisPendiente.setDictamen(null); // Sin dictamen = pendiente
            analisisPendiente.setActivo(true);

            // Agregar el análisis a la lista del lote
            loteTest.getAnalisisList().add(analisisPendiente);

            // Mock repository responses
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
                Movimiento mov = invocation.getArgument(0);
                mov.setId(1L);
                return mov;
            });
            when(bultoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(analisisRepository.save(any(Analisis.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoteDTO resultado = service.bajaBultosDevolucionCompra(dto);

            // Then
            assertThat(resultado).isNotNull();

            // Verificar que el análisis fue cancelado
            assertThat(analisisPendiente.getDictamen()).isEqualTo(DictamenEnum.CANCELADO);
            verify(analisisRepository).save(analisisPendiente);
        }

        @Test
        @DisplayName("test_devolucionCompraConAnalisisCompletado_noDebeCancelar")
        void test_devolucionCompraConAnalisisCompletado_noDebeCancelar() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // Crear análisis completado (con dictamen)
            Analisis analisisCompletado = new Analisis();
            analisisCompletado.setId(1L);
            analisisCompletado.setLote(loteTest);
            analisisCompletado.setNroAnalisis("AN-2025-002");
            analisisCompletado.setFechaRealizado(LocalDate.now());
            analisisCompletado.setDictamen(DictamenEnum.APROBADO); // Con dictamen
            analisisCompletado.setActivo(true);

            // Agregar el análisis a la lista del lote
            loteTest.getAnalisisList().add(analisisCompletado);

            // Mock repository responses
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
                Movimiento mov = invocation.getArgument(0);
                mov.setId(1L);
                return mov;
            });
            when(bultoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoteDTO resultado = service.bajaBultosDevolucionCompra(dto);

            // Then
            assertThat(resultado).isNotNull();

            // Verificar que el análisis NO fue cancelado (mantiene dictamen original)
            assertThat(analisisCompletado.getDictamen()).isEqualTo(DictamenEnum.APROBADO);
            verify(analisisRepository, never()).save(any(Analisis.class));
        }

        @Test
        @DisplayName("test_devolucionCompraSinAnalisis_debe_procesarNormalmente")
        void test_devolucionCompraSinAnalisis_debe_procesarNormalmente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // El lote no tiene análisis (lista vacía)
            loteTest.getAnalisisList().clear();

            // Mock repository responses
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
                Movimiento mov = invocation.getArgument(0);
                mov.setId(1L);
                return mov;
            });
            when(bultoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoteDTO resultado = service.bajaBultosDevolucionCompra(dto);

            // Then
            assertThat(resultado).isNotNull();

            // Verificar que no se intentó guardar ningún análisis
            verify(analisisRepository, never()).save(any(Analisis.class));
        }

        @Test
        @DisplayName("test_devolucionCompraLoteInexistente_debe_lanzarExcepcion")
        void test_devolucionCompraLoteInexistente_debe_lanzarExcepcion() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-INEXISTENTE-999");
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // Mock repository to return empty
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-INEXISTENTE-999")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> service.bajaBultosDevolucionCompra(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El lote no existe.");
        }
    }

    @Nested
    @DisplayName("validarDevolucionCompraInput() - Validaciones")
    class ValidarDevolucionCompraInput {

        @Test
        @DisplayName("test_validacionExitosa_debe_retornarTrue")
        void test_validacionExitosa_debe_retornarTrue() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaMovimiento(LocalDate.now());
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // Mock repository
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));

            // When
            boolean resultado = service.validarDevolucionCompraInput(dto, binding);

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
            boolean resultado = service.validarDevolucionCompraInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("test_validacionLoteNoEncontrado_debe_agregarError")
        void test_validacionLoteNoEncontrado_debe_agregarError() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-INEXISTENTE-999");
            dto.setFechaMovimiento(LocalDate.now());
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // Mock repository to return empty
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-INEXISTENTE-999")).thenReturn(Optional.empty());

            // When
            boolean resultado = service.validarDevolucionCompraInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("codigoLote")).isNotNull();
            assertThat(binding.getFieldError("codigoLote").getDefaultMessage()).isEqualTo("Lote no encontrado.");
        }

        @Test
        @DisplayName("test_validacionFechaMovimientoAnteriorIngreso_debe_agregarError")
        void test_validacionFechaMovimientoAnteriorIngreso_debe_agregarError() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            // Fecha de movimiento ANTES de la fecha de ingreso del lote (que es hace 10 días)
            dto.setFechaMovimiento(LocalDate.now().minusDays(15));
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // Mock repository
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));

            // When
            boolean resultado = service.validarDevolucionCompraInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("fechaMovimiento")).isNotNull();
        }

        @Test
        @DisplayName("test_validacionFechaMovimientoValidaPosteriorIngreso_debe_retornarTrue")
        void test_validacionFechaMovimientoValidaPosteriorIngreso_debe_retornarTrue() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            // Fecha válida: posterior al ingreso (hace 10 días) y no futura
            dto.setFechaMovimiento(LocalDate.now().minusDays(5));
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            // Mock repository
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));

            // When
            boolean resultado = service.validarDevolucionCompraInput(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }
    }
}

package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.BultoDTO;
import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
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
import com.mb.conitrack.repository.TrazaRepository;
import com.mb.conitrack.service.SecurityContextService;
import com.mb.conitrack.utils.MovimientoBajaUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios con mocks para BajaVentaProductoService (CU22).
 * Usa Mockito para verificar lógica del service sin dependencias de BD.
 *
 * Cobertura completa del flujo CU22:
 * - Baja de stock por venta de producto
 * - Validación de entrada completa
 * - Actualización de lote y bultos
 * - Gestión de trazas (lotes trazados)
 * - Creación de movimiento BAJA/VENTA
 * - Cancelación de análisis pendientes
 * - Manejo de errores
 * - Casos edge
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - BajaVentaProductoService (CU22)")
class BajaVentaProductoServiceTest {

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

    @Mock
    private SecurityContextService securityContextService;

    @Spy
    @InjectMocks
    private BajaVentaProductoService service;

    private MockedStatic<MovimientoBajaUtils> movimientoBajaUtilsMock;
    private MockedStatic<DTOUtils> dtoUtilsMock;

    private User testUser;
    private Lote loteTest;
    private Lote loteTrazadoTest;
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
        productoTest.setCodigoProducto("PROD-TEST-001");
        productoTest.setNombreGenerico("Producto Test");
        productoTest.setTipoProducto(TipoProductoEnum.API);
        productoTest.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
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

        // Crear lote de test NO trazado
        loteTest = crearLoteConBultos(false);

        // Crear lote de test TRAZADO
        loteTrazadoTest = crearLoteConBultos(true);

        // Mock SecurityContextService
        lenient().when(securityContextService.getCurrentUser()).thenReturn(testUser);

        // Mock estáticos
        movimientoBajaUtilsMock = mockStatic(MovimientoBajaUtils.class);
        dtoUtilsMock = mockStatic(DTOUtils.class);
    }

    @AfterEach
    void tearDown() {
        if (movimientoBajaUtilsMock != null) {
            movimientoBajaUtilsMock.close();
        }
        if (dtoUtilsMock != null) {
            dtoUtilsMock.close();
        }
    }

    private Lote crearLoteConBultos(boolean trazado) {
        Lote lote = new Lote();
        lote.setId(trazado ? 2L : 1L);
        lote.setCodigoLote(trazado ? "L-TRAZADO-001" : "L-TEST-001");
        lote.setLoteProveedor("LP-2025-001");
        lote.setProducto(productoTest);
        lote.setProveedor(proveedorTest);
        lote.setFechaIngreso(LocalDate.now().minusDays(10));
        lote.setCantidadInicial(new BigDecimal("100"));
        lote.setCantidadActual(new BigDecimal("100"));
        lote.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        lote.setBultosTotales(2);
        lote.setEstado(EstadoEnum.DISPONIBLE);
        lote.setDictamen(DictamenEnum.APROBADO);
        lote.setTrazado(trazado);
        lote.setActivo(true);

        // Crear bultos
        Bulto bulto1 = new Bulto();
        bulto1.setId(trazado ? 3L : 1L);
        bulto1.setNroBulto(1);
        bulto1.setLote(lote);
        bulto1.setCantidadInicial(new BigDecimal("50"));
        bulto1.setCantidadActual(new BigDecimal("50"));
        bulto1.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        bulto1.setEstado(EstadoEnum.DISPONIBLE);
        bulto1.setActivo(true);

        Bulto bulto2 = new Bulto();
        bulto2.setId(trazado ? 4L : 2L);
        bulto2.setNroBulto(2);
        bulto2.setLote(lote);
        bulto2.setCantidadInicial(new BigDecimal("50"));
        bulto2.setCantidadActual(new BigDecimal("50"));
        bulto2.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        bulto2.setEstado(EstadoEnum.DISPONIBLE);
        bulto2.setActivo(true);

        // Si es trazado, agregar trazas
        if (trazado) {
            bulto1.setTrazas(new HashSet<>());
            bulto2.setTrazas(new HashSet<>());

            // Crear trazas para bulto 1
            for (int i = 1; i <= 50; i++) {
                Traza traza = new Traza();
                traza.setId((long) i);
                traza.setLote(lote);
                traza.setBulto(bulto1);
                traza.setProducto(productoTest);
                traza.setNroTraza((long) i);
                traza.setEstado(EstadoEnum.DISPONIBLE);
                traza.setFechaYHoraCreacion(OffsetDateTime.now());
                traza.setActivo(true);
                bulto1.getTrazas().add(traza);
            }

            // Crear trazas para bulto 2
            for (int i = 51; i <= 100; i++) {
                Traza traza = new Traza();
                traza.setId((long) i);
                traza.setLote(lote);
                traza.setBulto(bulto2);
                traza.setProducto(productoTest);
                traza.setNroTraza((long) i);
                traza.setEstado(EstadoEnum.DISPONIBLE);
                traza.setFechaYHoraCreacion(OffsetDateTime.now());
                traza.setActivo(true);
                bulto2.getTrazas().add(traza);
            }
        }

        lote.setBultos(new ArrayList<>());
        lote.getBultos().add(bulto1);
        lote.getBultos().add(bulto2);

        lote.setMovimientos(new ArrayList<>());
        lote.setAnalisisList(new ArrayList<>());

        return lote;
    }

    @Nested
    @DisplayName("bajaVentaProducto() - Flujo completo")
    class BajaVentaProducto {

        @Test
        @DisplayName("test_ventaExitosaSinTrazas_debe_actualizarLoteYBultos")
        void test_ventaExitosaSinTrazas_debe_actualizarLoteYBultos() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaEgreso(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setObservaciones("Venta a cliente X");
            dto.setNroBultoList(Arrays.asList(1, 2));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("20"), new BigDecimal("30")));
            dto.setBultosDTOs(new ArrayList<>());
            dto.setTrazaDTOs(new ArrayList<>());

            Movimiento movimiento = new Movimiento();
            movimiento.setId(1L);
            movimiento.setDetalles(new HashSet<>());

            // Mock repository responses
            when(loteRepository.findFirstByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));
            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoBajaVenta(dto, loteTest, testUser))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);
            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromBultoEntity(any(Bulto.class))).thenReturn(new BultoDTO());
            when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(dto);

            // When
            LoteDTO resultado = service.bajaVentaProducto(dto);

            // Then - Verificar DTO resultado
            assertThat(resultado).isNotNull();

            // Verificar que el lote fue actualizado (100 - 20 - 30 = 50)
            assertThat(loteTest.getCantidadActual()).isEqualByComparingTo(new BigDecimal("50"));
            assertThat(loteTest.getEstado()).isEqualTo(EstadoEnum.EN_USO);

            // Verificar bultos actualizados
            assertThat(loteTest.getBultos().get(0).getCantidadActual()).isEqualByComparingTo(new BigDecimal("30"));
            assertThat(loteTest.getBultos().get(0).getEstado()).isEqualTo(EstadoEnum.EN_USO);
            assertThat(loteTest.getBultos().get(1).getCantidadActual()).isEqualByComparingTo(new BigDecimal("20"));
            assertThat(loteTest.getBultos().get(1).getEstado()).isEqualTo(EstadoEnum.EN_USO);

            // Verificar llamadas
            verify(loteRepository).findFirstByCodigoLoteAndActivoTrue("L-TEST-001");
            verify(movimientoRepository).save(any(Movimiento.class));
            verify(bultoRepository, times(2)).save(any(Bulto.class));
            verify(loteRepository).save(loteTest);
        }

        @Test
        @DisplayName("test_ventaExitosaConTrazas_debe_incluirTrazasEnDTO")
        void test_ventaExitosaConTrazas_debe_incluirTrazasEnDTO() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TRAZADO-001");
            dto.setFechaEgreso(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setObservaciones("Venta trazada");
            dto.setNroBultoList(Arrays.asList(1));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("10")));
            dto.setBultosDTOs(new ArrayList<>());
            dto.setTrazaDTOs(new ArrayList<>());

            Movimiento movimiento = new Movimiento();
            movimiento.setId(1L);
            movimiento.setDetalles(new HashSet<>());

            // Mock repository responses
            when(loteRepository.findFirstByCodigoLoteAndActivoTrue("L-TRAZADO-001")).thenReturn(Optional.of(loteTrazadoTest));
            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoBajaVenta(dto, loteTrazadoTest, testUser))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);
            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromBultoEntity(any(Bulto.class))).thenReturn(new BultoDTO());
            dtoUtilsMock.when(() -> DTOUtils.fromTrazaEntity(any(Traza.class))).thenReturn(new TrazaDTO());
            when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(dto);

            // When
            LoteDTO resultado = service.bajaVentaProducto(dto);

            // Then
            assertThat(resultado).isNotNull();
            verify(loteRepository).findFirstByCodigoLoteAndActivoTrue("L-TRAZADO-001");
        }

        @Test
        @DisplayName("test_ventaConCantidadBultoCero_debe_saltarBulto")
        void test_ventaConCantidadBultoCero_debe_saltarBulto() {
            // Given - Caso línea 56-58: cantidad de bulto es 0
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaEgreso(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setNroBultoList(Arrays.asList(1, 2));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("20"), BigDecimal.ZERO));
            dto.setBultosDTOs(new ArrayList<>());
            dto.setTrazaDTOs(new ArrayList<>());

            Movimiento movimiento = new Movimiento();
            movimiento.setId(1L);
            movimiento.setDetalles(new HashSet<>());

            // Mock
            when(loteRepository.findFirstByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));
            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoBajaVenta(dto, loteTest, testUser))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);
            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromBultoEntity(any(Bulto.class))).thenReturn(new BultoDTO());
            when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(dto);

            // When
            LoteDTO resultado = service.bajaVentaProducto(dto);

            // Then
            assertThat(resultado).isNotNull();
            // Solo el bulto 1 debió actualizarse
            assertThat(loteTest.getBultos().get(0).getCantidadActual()).isEqualByComparingTo(new BigDecimal("30"));
            // El bulto 2 debe permanecer sin cambios (cantidad 0 = continue)
            assertThat(loteTest.getBultos().get(1).getCantidadActual()).isEqualByComparingTo(new BigDecimal("50"));
        }

        @Test
        @DisplayName("test_ventaBultoQuedaConsumido_debe_marcarComoConsumido")
        void test_ventaBultoQuedaConsumido_debe_marcarComoConsumido() {
            // Given - Caso línea 63-64: bulto queda CONSUMIDO
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaEgreso(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setNroBultoList(Arrays.asList(1, 2));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("50"), new BigDecimal("30")));
            dto.setBultosDTOs(new ArrayList<>());
            dto.setTrazaDTOs(new ArrayList<>());

            Movimiento movimiento = new Movimiento();
            movimiento.setId(1L);
            movimiento.setDetalles(new HashSet<>());

            // Mock
            when(loteRepository.findFirstByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));
            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoBajaVenta(dto, loteTest, testUser))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);
            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromBultoEntity(any(Bulto.class))).thenReturn(new BultoDTO());
            when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(dto);

            // When
            LoteDTO resultado = service.bajaVentaProducto(dto);

            // Then
            assertThat(resultado).isNotNull();
            // Bulto 1: 50 - 50 = 0 -> CONSUMIDO
            assertThat(loteTest.getBultos().get(0).getCantidadActual()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(loteTest.getBultos().get(0).getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);
            // Bulto 2: 50 - 30 = 20 -> EN_USO
            assertThat(loteTest.getBultos().get(1).getCantidadActual()).isEqualByComparingTo(new BigDecimal("20"));
            assertThat(loteTest.getBultos().get(1).getEstado()).isEqualTo(EstadoEnum.EN_USO);
        }

        @Test
        @DisplayName("test_ventaBultoQuedaEnUso_debe_marcarComoEnUso")
        void test_ventaBultoQuedaEnUso_debe_marcarComoEnUso() {
            // Given - Caso línea 65-67: bulto queda EN_USO
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaEgreso(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setNroBultoList(Arrays.asList(1));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("25")));
            dto.setBultosDTOs(new ArrayList<>());
            dto.setTrazaDTOs(new ArrayList<>());

            Movimiento movimiento = new Movimiento();
            movimiento.setId(1L);
            movimiento.setDetalles(new HashSet<>());

            // Mock
            when(loteRepository.findFirstByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));
            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoBajaVenta(dto, loteTest, testUser))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);
            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromBultoEntity(any(Bulto.class))).thenReturn(new BultoDTO());
            when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(dto);

            // When
            LoteDTO resultado = service.bajaVentaProducto(dto);

            // Then
            assertThat(resultado).isNotNull();
            // Bulto 1: 50 - 25 = 25 -> EN_USO (no es cero)
            assertThat(loteTest.getBultos().get(0).getCantidadActual()).isEqualByComparingTo(new BigDecimal("25"));
            assertThat(loteTest.getBultos().get(0).getEstado()).isEqualTo(EstadoEnum.EN_USO);
        }

        @Test
        @DisplayName("test_ventaTodosBultosConsumidos_debe_marcarLoteConsumido")
        void test_ventaTodosBultosConsumidos_debe_marcarLoteConsumido() {
            // Given - Caso línea 77-79: todos los bultos quedan CONSUMIDOS
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaEgreso(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setNroBultoList(Arrays.asList(1, 2));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("50"), new BigDecimal("50")));
            dto.setBultosDTOs(new ArrayList<>());
            dto.setTrazaDTOs(new ArrayList<>());

            Movimiento movimiento = new Movimiento();
            movimiento.setId(1L);
            movimiento.setDetalles(new HashSet<>());

            // Mock
            when(loteRepository.findFirstByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));
            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoBajaVenta(dto, loteTest, testUser))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);
            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromBultoEntity(any(Bulto.class))).thenReturn(new BultoDTO());
            when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(dto);

            // When
            LoteDTO resultado = service.bajaVentaProducto(dto);

            // Then
            assertThat(resultado).isNotNull();
            // Todos los bultos CONSUMIDOS -> lote CONSUMIDO
            assertThat(loteTest.getCantidadActual()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(loteTest.getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);
            assertThat(loteTest.getBultos().get(0).getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);
            assertThat(loteTest.getBultos().get(1).getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);
        }

        @Test
        @DisplayName("test_ventaCancelaAnalisisEnCurso_debe_cancelarAnalisis")
        void test_ventaCancelaAnalisisEnCurso_debe_cancelarAnalisis() {
            // Given - Caso línea 82-87: cancelar análisis en curso
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaEgreso(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setNroBultoList(Arrays.asList(1, 2));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("50"), new BigDecimal("50")));
            dto.setBultosDTOs(new ArrayList<>());
            dto.setTrazaDTOs(new ArrayList<>());

            // Crear análisis pendiente (sin dictamen)
            Analisis analisisPendiente = new Analisis();
            analisisPendiente.setId(1L);
            analisisPendiente.setLote(loteTest);
            analisisPendiente.setNroAnalisis("AN-2025-001");
            analisisPendiente.setFechaRealizado(LocalDate.now());
            analisisPendiente.setDictamen(null); // Sin dictamen = pendiente
            analisisPendiente.setActivo(true);
            loteTest.getAnalisisList().add(analisisPendiente);

            Movimiento movimiento = new Movimiento();
            movimiento.setId(1L);
            movimiento.setDetalles(new HashSet<>());

            // Mock
            when(loteRepository.findFirstByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));
            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoBajaVenta(dto, loteTest, testUser))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);
            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(analisisRepository.save(any(Analisis.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromBultoEntity(any(Bulto.class))).thenReturn(new BultoDTO());
            when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(dto);

            // When
            LoteDTO resultado = service.bajaVentaProducto(dto);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(loteTest.getCantidadActual()).isEqualByComparingTo(BigDecimal.ZERO);
            // Verificar que el análisis fue cancelado
            assertThat(analisisPendiente.getDictamen()).isEqualTo(DictamenEnum.CANCELADO);
            verify(analisisRepository).save(analisisPendiente);
        }

        @Test
        @DisplayName("test_ventaSinAnalisis_debe_procesarNormalmente")
        void test_ventaSinAnalisis_debe_procesarNormalmente() {
            // Given - Caso línea 83: lote.getUltimoAnalisis() es null
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaEgreso(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setNroBultoList(Arrays.asList(1, 2));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("50"), new BigDecimal("50")));
            dto.setBultosDTOs(new ArrayList<>());
            dto.setTrazaDTOs(new ArrayList<>());

            // El lote NO tiene análisis (getUltimoAnalisis() retorna null)
            loteTest.getAnalisisList().clear();

            Movimiento movimiento = new Movimiento();
            movimiento.setId(1L);
            movimiento.setDetalles(new HashSet<>());

            // Mock
            when(loteRepository.findFirstByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));
            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoBajaVenta(dto, loteTest, testUser))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);
            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromBultoEntity(any(Bulto.class))).thenReturn(new BultoDTO());
            when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(dto);

            // When
            LoteDTO resultado = service.bajaVentaProducto(dto);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(loteTest.getCantidadActual()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(loteTest.getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);
            // No debe intentar guardar ningún análisis
            verify(analisisRepository, never()).save(any(Analisis.class));
        }

        @Test
        @DisplayName("test_ventaConAnalisisCompletado_noDebeCancelar")
        void test_ventaConAnalisisCompletado_noDebeCancelar() {
            // Given - Caso línea 83: cantidad=0 pero análisis con dictamen completado
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaEgreso(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setNroBultoList(Arrays.asList(1, 2));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("50"), new BigDecimal("50")));
            dto.setBultosDTOs(new ArrayList<>());
            dto.setTrazaDTOs(new ArrayList<>());

            // Crear análisis COMPLETADO (con dictamen aprobado)
            Analisis analisisCompletado = new Analisis();
            analisisCompletado.setId(2L);
            analisisCompletado.setLote(loteTest);
            analisisCompletado.setNroAnalisis("AN-2025-002");
            analisisCompletado.setFechaRealizado(LocalDate.now());
            analisisCompletado.setDictamen(DictamenEnum.APROBADO); // Con dictamen = NO debe cancelarse
            analisisCompletado.setActivo(true);
            loteTest.getAnalisisList().add(analisisCompletado);

            Movimiento movimiento = new Movimiento();
            movimiento.setId(1L);
            movimiento.setDetalles(new HashSet<>());

            // Mock
            when(loteRepository.findFirstByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));
            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoBajaVenta(dto, loteTest, testUser))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);
            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromBultoEntity(any(Bulto.class))).thenReturn(new BultoDTO());
            when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(dto);

            // When
            LoteDTO resultado = service.bajaVentaProducto(dto);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(loteTest.getCantidadActual()).isEqualByComparingTo(BigDecimal.ZERO);
            // El análisis NO debe cancelarse porque ya tiene dictamen
            assertThat(analisisCompletado.getDictamen()).isEqualTo(DictamenEnum.APROBADO);
            verify(analisisRepository, never()).save(any(Analisis.class));
        }

        @Test
        @DisplayName("test_ventaLoteInexistente_debe_lanzarExcepcion")
        void test_ventaLoteInexistente_debe_lanzarExcepcion() {
            // Given - Caso línea 40-42: lote no existe
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-INEXISTENTE-999");
            dto.setFechaEgreso(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setNroBultoList(Arrays.asList(1));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("10")));

            // Mock repository to return empty
            when(loteRepository.findFirstByCodigoLoteAndActivoTrue("L-INEXISTENTE-999")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> service.bajaVentaProducto(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El lote no existe.");

            verify(loteRepository).findFirstByCodigoLoteAndActivoTrue("L-INEXISTENTE-999");
        }
    }

    @Nested
    @DisplayName("persistirMovimientoBajaVenta() - Creación de movimiento")
    class PersistirMovimientoBajaVenta {

        @Test
        @DisplayName("test_crearMovimientoVentaSinTrazas_debe_configurarCorrectamente")
        void test_crearMovimientoVentaSinTrazas_debe_configurarCorrectamente() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaEgreso(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setObservaciones("Test venta");
            dto.setNroBultoList(Arrays.asList(1));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("25")));

            Movimiento movimiento = new Movimiento();
            movimiento.setId(1L);
            movimiento.setDetalles(new HashSet<>());

            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoBajaVenta(dto, loteTest, testUser))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
                Movimiento mov = invocation.getArgument(0);
                mov.setId(1L);
                return mov;
            });

            // When
            Movimiento resultado = service.persistirMovimientoBajaVenta(dto, loteTest, testUser);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCantidad()).isEqualByComparingTo(new BigDecimal("25"));
            assertThat(resultado.getUnidadMedida()).isEqualTo(UnidadMedidaEnum.UNIDAD);
            assertThat(resultado.getDetalles()).hasSize(1);
            verify(movimientoRepository).save(any(Movimiento.class));
        }

        @Test
        @DisplayName("test_crearMovimientoVentaConTrazas_debe_incluirTrazas")
        void test_crearMovimientoVentaConTrazas_debe_incluirTrazas() {
            // Given - Caso línea 131-142: lote trazado con trazas
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TRAZADO-001");
            dto.setFechaEgreso(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setNroBultoList(Arrays.asList(1));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("10")));

            Movimiento movimiento = new Movimiento();
            movimiento.setId(1L);
            movimiento.setDetalles(new HashSet<>());

            // Obtener las primeras 10 trazas del bulto 1
            List<Traza> trazasDisponibles = loteTrazadoTest.getBultos().get(0).getTrazas().stream().limit(10).toList();

            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoBajaVenta(dto, loteTrazadoTest, testUser))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(trazaRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            // Mock getFirstAvailableTrazaList para retornar las trazas
            Bulto bulto1 = loteTrazadoTest.getBultos().get(0);
            Bulto spyBulto1 = spy(bulto1);
            lenient().when(spyBulto1.getFirstAvailableTrazaList(10)).thenReturn(trazasDisponibles);
            loteTrazadoTest.getBultos().set(0, spyBulto1);

            // When
            Movimiento resultado = service.persistirMovimientoBajaVenta(dto, loteTrazadoTest, testUser);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCantidad()).isEqualByComparingTo(new BigDecimal("10"));
            verify(trazaRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("test_crearMovimientoVentaConCantidadCero_debe_saltarBulto")
        void test_crearMovimientoVentaConCantidadCero_debe_saltarBulto() {
            // Given - Caso línea 116-118: cantidad es 0
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaEgreso(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setNroBultoList(Arrays.asList(1, 2));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("25"), BigDecimal.ZERO));

            Movimiento movimiento = new Movimiento();
            movimiento.setId(1L);
            movimiento.setDetalles(new HashSet<>());

            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoBajaVenta(dto, loteTest, testUser))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            Movimiento resultado = service.persistirMovimientoBajaVenta(dto, loteTest, testUser);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCantidad()).isEqualByComparingTo(new BigDecimal("25"));
            // Solo debe haber 1 detalle (el bulto 2 no se incluye porque cantidad es CERO)
            assertThat(resultado.getDetalles()).hasSize(1);
        }

        @Test
        @DisplayName("test_crearMovimientoVentaConTrazasNull_debe_procesarNormalmente")
        void test_crearMovimientoVentaConTrazasNull_debe_procesarNormalmente() {
            // Given - Caso línea 134: trazas es null
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TRAZADO-001");
            dto.setFechaEgreso(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setNroBultoList(Arrays.asList(1));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("10")));

            Movimiento movimiento = new Movimiento();
            movimiento.setId(1L);
            movimiento.setDetalles(new HashSet<>());

            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoBajaVenta(dto, loteTrazadoTest, testUser))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Mock getFirstAvailableTrazaList para retornar null
            Bulto bulto1 = loteTrazadoTest.getBultos().get(0);
            Bulto spyBulto1 = spy(bulto1);
            when(spyBulto1.getFirstAvailableTrazaList(10)).thenReturn(null);
            loteTrazadoTest.getBultos().set(0, spyBulto1);

            // When
            Movimiento resultado = service.persistirMovimientoBajaVenta(dto, loteTrazadoTest, testUser);

            // Then
            assertThat(resultado).isNotNull();
            // No debe llamarse a trazaRepository.saveAll si trazas es null
            verify(trazaRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("test_crearMovimientoVentaConTrazasVacia_debe_procesarNormalmente")
        void test_crearMovimientoVentaConTrazasVacia_debe_procesarNormalmente() {
            // Given - Caso línea 134: trazas es vacía
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TRAZADO-001");
            dto.setFechaEgreso(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setNroBultoList(Arrays.asList(1));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("10")));

            Movimiento movimiento = new Movimiento();
            movimiento.setId(1L);
            movimiento.setDetalles(new HashSet<>());

            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoBajaVenta(dto, loteTrazadoTest, testUser))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Mock getFirstAvailableTrazaList para retornar lista vacía
            Bulto bulto1 = loteTrazadoTest.getBultos().get(0);
            Bulto spyBulto1 = spy(bulto1);
            when(spyBulto1.getFirstAvailableTrazaList(10)).thenReturn(List.of());
            loteTrazadoTest.getBultos().set(0, spyBulto1);

            // When
            Movimiento resultado = service.persistirMovimientoBajaVenta(dto, loteTrazadoTest, testUser);

            // Then
            assertThat(resultado).isNotNull();
            // No debe llamarse a trazaRepository.saveAll si trazas es vacía
            verify(trazaRepository, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("validarVentaProductoInput() - Validaciones")
    class ValidarVentaProductoInput {

        @Test
        @DisplayName("test_validacionExitosa_debe_retornarTrue")
        void test_validacionExitosa_debe_retornarTrue() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaEgreso(LocalDate.now());
            dto.setNroBultoList(Arrays.asList(1));
            dto.setCantidadesBultos(Arrays.asList(new BigDecimal("25")));
            dto.setUnidadMedidaBultos(Arrays.asList(UnidadMedidaEnum.UNIDAD));
            BindingResult binding = new BeanPropertyBindingResult(dto, "loteDTO");

            // Mock repository
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));

            // When
            boolean resultado = service.validarVentaProductoInput(dto, binding);

            // Then
            assertThat(resultado).isTrue();
            assertThat(binding.hasErrors()).isFalse();
        }

        @Test
        @DisplayName("test_validacionConErroresBinding_debe_retornarFalse")
        void test_validacionConErroresBinding_debe_retornarFalse() {
            // Given
            LoteDTO dto = new LoteDTO();
            BindingResult binding = new BeanPropertyBindingResult(dto, "loteDTO");
            binding.rejectValue("codigoLote", "", "Campo obligatorio");

            // When
            boolean resultado = service.validarVentaProductoInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("test_validacionLoteNoEncontrado_debe_agregarError")
        void test_validacionLoteNoEncontrado_debe_agregarError() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-INEXISTENTE-999");
            dto.setFechaEgreso(LocalDate.now());
            BindingResult binding = new BeanPropertyBindingResult(dto, "loteDTO");

            // Mock repository to return empty
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-INEXISTENTE-999")).thenReturn(Optional.empty());

            // When
            boolean resultado = service.validarVentaProductoInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("codigoLote")).isNotNull();
            assertThat(binding.getFieldError("codigoLote").getDefaultMessage()).isEqualTo("Lote no encontrado.");
        }

        @Test
        @DisplayName("test_validacionUnidadMedidaVentaFalla_debe_retornarFalse")
        void test_validacionUnidadMedidaVentaFalla_debe_retornarFalse() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaEgreso(LocalDate.now());
            BindingResult binding = new BeanPropertyBindingResult(dto, "loteDTO");

            // Mock repository
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));

            // Spy para interceptar validarUnidadMedidaVenta y forzar fallo
            doReturn(false).when(service).validarUnidadMedidaVenta(eq(dto), eq(loteTest), any(BindingResult.class));

            // When
            boolean resultado = service.validarVentaProductoInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            verify(service, times(1)).validarUnidadMedidaVenta(eq(dto), eq(loteTest), any(BindingResult.class));
        }

        @Test
        @DisplayName("test_validacionFechaEgresoLoteDtoPosteriorLoteFalla_debe_retornarFalse")
        void test_validacionFechaEgresoLoteDtoPosteriorLoteFalla_debe_retornarFalse() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaEgreso(LocalDate.now().minusDays(20)); // Fecha anterior al ingreso
            BindingResult binding = new BeanPropertyBindingResult(dto, "loteDTO");

            // Mock repository
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));

            // Spy para interceptar validaciones
            doReturn(true).when(service).validarUnidadMedidaVenta(eq(dto), eq(loteTest), any(BindingResult.class));
            doReturn(false).when(service).validarFechaEgresoLoteDtoPosteriorLote(eq(dto), eq(loteTest), any(BindingResult.class));

            // When
            boolean resultado = service.validarVentaProductoInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            verify(service, times(1)).validarFechaEgresoLoteDtoPosteriorLote(eq(dto), eq(loteTest), any(BindingResult.class));
        }

        @Test
        @DisplayName("test_validacionCantidadesPorMedidasFalla_debe_retornarFalse")
        void test_validacionCantidadesPorMedidasFalla_debe_retornarFalse() {
            // Given
            LoteDTO dto = new LoteDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaEgreso(LocalDate.now());
            BindingResult binding = new BeanPropertyBindingResult(dto, "loteDTO");

            // Mock repository
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001")).thenReturn(Optional.of(loteTest));

            // Spy para interceptar validaciones
            doReturn(true).when(service).validarUnidadMedidaVenta(eq(dto), eq(loteTest), any(BindingResult.class));
            doReturn(true).when(service).validarFechaEgresoLoteDtoPosteriorLote(eq(dto), eq(loteTest), any(BindingResult.class));
            doReturn(false).when(service).validarCantidadesPorMedidas(eq(dto), eq(loteTest), any(BindingResult.class));

            // When
            boolean resultado = service.validarVentaProductoInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            verify(service, times(1)).validarCantidadesPorMedidas(eq(dto), eq(loteTest), any(BindingResult.class));
        }
    }
}

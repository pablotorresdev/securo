package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.*;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.*;
import com.mb.conitrack.repository.*;
import com.mb.conitrack.service.SecurityContextService;
import com.mb.conitrack.utils.MovimientoBajaUtils;
import com.mb.conitrack.utils.UnidadMedidaUtils;
import org.junit.jupiter.api.*;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios con mocks para BajaAjusteStockService (CU25).
 * Usa Mockito para verificar lógica del service sin dependencias de BD.
 *
 * Cobertura completa del flujo CU25:
 * - Baja de stock por ajuste de inventario
 * - Validación de entrada completa
 * - Actualización de lote y bultos
 * - Manejo de trazas para productos UNIDAD_VENTA
 * - Creación de movimiento BAJA/AJUSTE
 * - Cancelación de análisis pendientes
 * - Manejo de errores
 * - Casos edge
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - BajaAjusteStockService (CU25)")
class BajaAjusteStockServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private BultoRepository bultoRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private AnalisisRepository analisisRepository;

    @Mock
    private TrazaRepository trazaRepository;

    @Mock
    private SecurityContextService securityContextService;

    @Spy
    @InjectMocks
    private BajaAjusteStockService service;

    private MockedStatic<MovimientoBajaUtils> movimientoBajaUtilsMock;
    private MockedStatic<UnidadMedidaUtils> unidadMedidaUtilsMock;
    private MockedStatic<DTOUtils> dtoUtilsMock;

    private User testUser;
    private Lote loteTest;
    private Lote loteTestTrazable;
    private Producto productoTest;
    private Producto productoTrazable;
    private Proveedor proveedorTest;

    @BeforeEach
    void setUp() {
        // Inicializar mocks estáticos
        movimientoBajaUtilsMock = mockStatic(MovimientoBajaUtils.class);
        unidadMedidaUtilsMock = mockStatic(UnidadMedidaUtils.class);
        dtoUtilsMock = mockStatic(DTOUtils.class);

        // Crear usuario de test
        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setId(1L);
        testUser = new User("testuser", "password", adminRole);
        testUser.setId(1L);

        // Crear producto de test (NO trazable)
        productoTest = new Producto();
        productoTest.setId(1L);
        productoTest.setCodigoProducto("API-TEST-001");
        productoTest.setNombreGenerico("Paracetamol Test");
        productoTest.setTipoProducto(TipoProductoEnum.API);
        productoTest.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        productoTest.setActivo(true);

        // Crear producto trazable (UNIDAD_VENTA)
        productoTrazable = new Producto();
        productoTrazable.setId(2L);
        productoTrazable.setCodigoProducto("UV-TEST-001");
        productoTrazable.setNombreGenerico("Medicamento Trazable");
        productoTrazable.setTipoProducto(TipoProductoEnum.UNIDAD_VENTA);
        productoTrazable.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        productoTrazable.setActivo(true);

        // Crear proveedor de test
        proveedorTest = new Proveedor();
        proveedorTest.setId(1L);
        proveedorTest.setRazonSocial("Proveedor Test");
        proveedorTest.setPais("Argentina");
        proveedorTest.setCuit("20-12345678-9");
        proveedorTest.setDireccion("Calle Test 123");
        proveedorTest.setCiudad("Buenos Aires");
        proveedorTest.setActivo(true);

        // Crear lote de test con bultos (NO trazable)
        loteTest = crearLoteConBultos();

        // Crear lote trazable con bultos
        loteTestTrazable = crearLoteTrazableConBultos();

        // Mock SecurityContextService
        lenient().when(securityContextService.getCurrentUser()).thenReturn(testUser);
    }

    @AfterEach
    void tearDown() {
        // Cerrar mocks estáticos
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
        lote.setEstado(EstadoEnum.DISPONIBLE);
        lote.setDictamen(DictamenEnum.APROBADO);
        lote.setActivo(true);
        lote.setTrazado(false);

        // Crear bultos
        Bulto bulto1 = new Bulto();
        bulto1.setId(1L);
        bulto1.setNroBulto(1);
        bulto1.setLote(lote);
        bulto1.setCantidadInicial(new BigDecimal("50.00"));
        bulto1.setCantidadActual(new BigDecimal("50.00"));
        bulto1.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        bulto1.setEstado(EstadoEnum.DISPONIBLE);
        bulto1.setActivo(true);

        Bulto bulto2 = new Bulto();
        bulto2.setId(2L);
        bulto2.setNroBulto(2);
        bulto2.setLote(lote);
        bulto2.setCantidadInicial(new BigDecimal("50.00"));
        bulto2.setCantidadActual(new BigDecimal("50.00"));
        bulto2.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        bulto2.setEstado(EstadoEnum.DISPONIBLE);
        bulto2.setActivo(true);

        lote.setBultos(new ArrayList<>());
        lote.getBultos().add(bulto1);
        lote.getBultos().add(bulto2);

        lote.setMovimientos(new ArrayList<>());
        lote.setAnalisisList(new ArrayList<>());
        lote.setTrazas(new HashSet<>());

        return lote;
    }

    private Lote crearLoteTrazableConBultos() {
        Lote lote = new Lote();
        lote.setId(2L);
        lote.setCodigoLote("L-TRAZ-001");
        lote.setLoteProveedor("LP-TRAZ-2025-001");
        lote.setProducto(productoTrazable);
        lote.setProveedor(proveedorTest);
        lote.setFechaIngreso(LocalDate.now().minusDays(10));
        lote.setCantidadInicial(new BigDecimal("100"));
        lote.setCantidadActual(new BigDecimal("100"));
        lote.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        lote.setBultosTotales(1);
        lote.setEstado(EstadoEnum.DISPONIBLE);
        lote.setDictamen(DictamenEnum.APROBADO);
        lote.setActivo(true);
        lote.setTrazado(true);

        Bulto bulto1 = new Bulto();
        bulto1.setId(3L);
        bulto1.setNroBulto(1);
        bulto1.setLote(lote);
        bulto1.setCantidadInicial(new BigDecimal("100"));
        bulto1.setCantidadActual(new BigDecimal("100"));
        bulto1.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        bulto1.setEstado(EstadoEnum.DISPONIBLE);
        bulto1.setActivo(true);

        lote.setBultos(new ArrayList<>());
        lote.getBultos().add(bulto1);

        lote.setMovimientos(new ArrayList<>());
        lote.setAnalisisList(new ArrayList<>());
        lote.setTrazas(new HashSet<>());

        return lote;
    }

    @Nested
    @DisplayName("bajaAjusteStock() - Flujo completo")
    class BajaAjusteStock {

        @Test
        @DisplayName("test_ajusteStockExitosoSinTrazas_debe_actualizarLoteYBultos")
        void test_ajusteStockExitosoSinTrazas_debe_actualizarLoteYBultos() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroBulto("1");
            dto.setCantidad(new BigDecimal("10.00"));
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());
            dto.setObservaciones("Ajuste por pérdida de stock");

            Bulto bulto = loteTest.getBultos().get(0);

            // Mock MovimientoBajaUtils
            Movimiento movimientoMock = new Movimiento();
            movimientoMock.setId(1L);
            movimientoMock.setCantidad(new BigDecimal("10.00"));
            movimientoMock.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            movimientoMock.setDetalles(new HashSet<>());
            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoAjusteStock(
                any(MovimientoDTO.class), any(Bulto.class), any(User.class)))
                .thenReturn(movimientoMock);

            // Mock UnidadMedidaUtils
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Bulto.class)))
                .thenReturn(new BigDecimal("40.00"));
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Lote.class)))
                .thenReturn(new BigDecimal("90.00"));

            // Mock DTOUtils
            LoteDTO loteDTOMock = new LoteDTO();
            loteDTOMock.setCodigoLote("L-TEST-001");
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class)))
                .thenReturn(loteDTOMock);

            // Mock repositories
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001"))
                .thenReturn(Optional.of(loteTest));
            when(movimientoRepository.save(any(Movimiento.class)))
                .thenReturn(movimientoMock);
            when(loteRepository.save(any(Lote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoteDTO resultado = service.bajaAjusteStock(dto);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigoLote()).isEqualTo("L-TEST-001");
            assertThat(bulto.getEstado()).isEqualTo(EstadoEnum.EN_USO);
            assertThat(loteTest.getEstado()).isEqualTo(EstadoEnum.EN_USO);

            verify(loteRepository).findByCodigoLoteAndActivoTrue("L-TEST-001");
            verify(movimientoRepository).save(any(Movimiento.class));
            verify(loteRepository).save(loteTest);
        }

        @Test
        @DisplayName("test_ajusteStockExitosoConTrazas_debe_actualizarTrazas")
        void test_ajusteStockExitosoConTrazas_debe_actualizarTrazas() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TRAZ-001");
            dto.setNroBulto("1");
            dto.setCantidad(new BigDecimal("2"));
            dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            TrazaDTO traza1 = new TrazaDTO();
            traza1.setNroTraza(1L);
            TrazaDTO traza2 = new TrazaDTO();
            traza2.setNroTraza(2L);
            dto.setTrazaDTOs(Arrays.asList(traza1, traza2));

            Bulto bulto = loteTestTrazable.getBultos().get(0);

            // Crear trazas del lote
            Traza trazaEntity1 = new Traza();
            trazaEntity1.setId(1L);
            trazaEntity1.setNroTraza(1L);
            trazaEntity1.setEstado(EstadoEnum.DISPONIBLE);
            trazaEntity1.setDetalles(new ArrayList<>());
            trazaEntity1.setActivo(true);

            Traza trazaEntity2 = new Traza();
            trazaEntity2.setId(2L);
            trazaEntity2.setNroTraza(2L);
            trazaEntity2.setEstado(EstadoEnum.DISPONIBLE);
            trazaEntity2.setDetalles(new ArrayList<>());
            trazaEntity2.setActivo(true);

            loteTestTrazable.setTrazas(new HashSet<>(Arrays.asList(trazaEntity1, trazaEntity2)));

            // Mock MovimientoBajaUtils
            Movimiento movimientoMock = new Movimiento();
            movimientoMock.setId(1L);
            movimientoMock.setCantidad(new BigDecimal("2"));
            movimientoMock.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

            DetalleMovimiento detalle = new DetalleMovimiento();
            detalle.setId(1L);
            detalle.setTrazas(new HashSet<>());
            movimientoMock.setDetalles(new HashSet<>(Arrays.asList(detalle)));

            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoAjusteStock(
                any(MovimientoDTO.class), any(Bulto.class), any(User.class)))
                .thenReturn(movimientoMock);

            // Mock UnidadMedidaUtils
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Bulto.class)))
                .thenReturn(new BigDecimal("98"));
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Lote.class)))
                .thenReturn(new BigDecimal("98"));

            // Mock DTOUtils
            LoteDTO loteDTOMock = new LoteDTO();
            loteDTOMock.setCodigoLote("L-TRAZ-001");
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class)))
                .thenReturn(loteDTOMock);
            dtoUtilsMock.when(() -> DTOUtils.fromTrazaEntity(any(Traza.class)))
                .thenReturn(traza1);

            // Mock repositories
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TRAZ-001"))
                .thenReturn(Optional.of(loteTestTrazable));
            when(movimientoRepository.save(any(Movimiento.class)))
                .thenReturn(movimientoMock);
            when(loteRepository.save(any(Lote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
            when(trazaRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoteDTO resultado = service.bajaAjusteStock(dto);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(trazaEntity1.getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);
            assertThat(trazaEntity2.getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);

            verify(trazaRepository).saveAll(anyList());
            verify(loteRepository).save(loteTestTrazable);
        }

        @Test
        @DisplayName("test_ajusteStockUnidadNoEsUNIDAD_debe_lanzarExcepcion_linea58_60")
        void test_ajusteStockUnidadNoEsUNIDAD_debe_lanzarExcepcion_linea58_60() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TRAZ-001");
            dto.setNroBulto("1");
            dto.setCantidad(new BigDecimal("2"));
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO); // NO es UNIDAD
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            TrazaDTO traza1 = new TrazaDTO();
            traza1.setNroTraza(1L);
            dto.setTrazaDTOs(Arrays.asList(traza1));

            Bulto bulto = loteTestTrazable.getBultos().get(0);

            // Mock MovimientoBajaUtils
            Movimiento movimientoMock = new Movimiento();
            movimientoMock.setId(1L);
            movimientoMock.setCantidad(new BigDecimal("2"));
            movimientoMock.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            movimientoMock.setDetalles(new HashSet<>());

            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoAjusteStock(
                any(MovimientoDTO.class), any(Bulto.class), any(User.class)))
                .thenReturn(movimientoMock);

            // Mock UnidadMedidaUtils
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Bulto.class)))
                .thenReturn(new BigDecimal("98"));
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Lote.class)))
                .thenReturn(new BigDecimal("98"));

            // Mock repositories
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TRAZ-001"))
                .thenReturn(Optional.of(loteTestTrazable));
            when(movimientoRepository.save(any(Movimiento.class)))
                .thenReturn(movimientoMock);

            // When/Then
            assertThatThrownBy(() -> service.bajaAjusteStock(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("La traza solo es aplicable a UNIDADES");
        }

        @Test
        @DisplayName("test_ajusteStockCantidadConDecimales_debe_lanzarExcepcion_linea62_64")
        void test_ajusteStockCantidadConDecimales_debe_lanzarExcepcion_linea62_64() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TRAZ-001");
            dto.setNroBulto("1");
            dto.setCantidad(new BigDecimal("2.5")); // Cantidad decimal
            dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            TrazaDTO traza1 = new TrazaDTO();
            traza1.setNroTraza(1L);
            dto.setTrazaDTOs(Arrays.asList(traza1));

            Bulto bulto = loteTestTrazable.getBultos().get(0);

            // Mock MovimientoBajaUtils
            Movimiento movimientoMock = new Movimiento();
            movimientoMock.setId(1L);
            movimientoMock.setCantidad(new BigDecimal("2.5"));
            movimientoMock.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            movimientoMock.setDetalles(new HashSet<>());

            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoAjusteStock(
                any(MovimientoDTO.class), any(Bulto.class), any(User.class)))
                .thenReturn(movimientoMock);

            // Mock UnidadMedidaUtils
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Bulto.class)))
                .thenReturn(new BigDecimal("97.5"));
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Lote.class)))
                .thenReturn(new BigDecimal("97.5"));

            // Mock repositories
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TRAZ-001"))
                .thenReturn(Optional.of(loteTestTrazable));
            when(movimientoRepository.save(any(Movimiento.class)))
                .thenReturn(movimientoMock);

            // When/Then
            assertThatThrownBy(() -> service.bajaAjusteStock(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("La cantidad de Unidades debe ser entero");
        }

        @Test
        @DisplayName("test_ajusteStockMultiplesDetalles_debe_lanzarExcepcion_linea77_79")
        void test_ajusteStockMultiplesDetalles_debe_lanzarExcepcion_linea77_79() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TRAZ-001");
            dto.setNroBulto("1");
            dto.setCantidad(new BigDecimal("2"));
            dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            TrazaDTO traza1 = new TrazaDTO();
            traza1.setNroTraza(1L);
            dto.setTrazaDTOs(Arrays.asList(traza1));

            Bulto bulto = loteTestTrazable.getBultos().get(0);

            // Mock MovimientoBajaUtils - con múltiples detalles
            Movimiento movimientoMock = new Movimiento();
            movimientoMock.setId(1L);
            movimientoMock.setCantidad(new BigDecimal("2"));
            movimientoMock.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

            DetalleMovimiento detalle1 = new DetalleMovimiento();
            detalle1.setId(1L);
            DetalleMovimiento detalle2 = new DetalleMovimiento();
            detalle2.setId(2L);
            movimientoMock.setDetalles(new HashSet<>(Arrays.asList(detalle1, detalle2)));

            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoAjusteStock(
                any(MovimientoDTO.class), any(Bulto.class), any(User.class)))
                .thenReturn(movimientoMock);

            // Mock UnidadMedidaUtils
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Bulto.class)))
                .thenReturn(new BigDecimal("98"));
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Lote.class)))
                .thenReturn(new BigDecimal("98"));

            // Mock repositories
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TRAZ-001"))
                .thenReturn(Optional.of(loteTestTrazable));
            when(movimientoRepository.save(any(Movimiento.class)))
                .thenReturn(movimientoMock);

            // When/Then
            assertThatThrownBy(() -> service.bajaAjusteStock(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Multiajuste no soportado aun");
        }

        @Test
        @DisplayName("test_ajusteStockBultoQuedaEnCero_debe_marcarConsumido_linea94_95")
        void test_ajusteStockBultoQuedaEnCero_debe_marcarConsumido_linea94_95() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroBulto("1");
            dto.setCantidad(new BigDecimal("50.00")); // Todo el bulto
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            Bulto bulto = loteTest.getBultos().get(0);

            // Mock MovimientoBajaUtils
            Movimiento movimientoMock = new Movimiento();
            movimientoMock.setId(1L);
            movimientoMock.setCantidad(new BigDecimal("50.00"));
            movimientoMock.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            movimientoMock.setDetalles(new HashSet<>());

            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoAjusteStock(
                any(MovimientoDTO.class), any(Bulto.class), any(User.class)))
                .thenReturn(movimientoMock);

            // Mock UnidadMedidaUtils
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Bulto.class)))
                .thenReturn(BigDecimal.ZERO);
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Lote.class)))
                .thenReturn(new BigDecimal("50.00"));

            // Mock DTOUtils
            LoteDTO loteDTOMock = new LoteDTO();
            loteDTOMock.setCodigoLote("L-TEST-001");
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class)))
                .thenReturn(loteDTOMock);

            // Mock repositories
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001"))
                .thenReturn(Optional.of(loteTest));
            when(movimientoRepository.save(any(Movimiento.class)))
                .thenReturn(movimientoMock);
            when(loteRepository.save(any(Lote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoteDTO resultado = service.bajaAjusteStock(dto);

            // Then
            assertThat(bulto.getCantidadActual()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(bulto.getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);
            assertThat(loteTest.getEstado()).isEqualTo(EstadoEnum.EN_USO); // Otro bulto aún disponible
        }

        @Test
        @DisplayName("test_ajusteStockBultoQuedaConStock_debe_marcarEnUso_linea96_98")
        void test_ajusteStockBultoQuedaConStock_debe_marcarEnUso_linea96_98() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroBulto("1");
            dto.setCantidad(new BigDecimal("10.00")); // Parte del bulto
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            Bulto bulto = loteTest.getBultos().get(0);

            // Mock MovimientoBajaUtils
            Movimiento movimientoMock = new Movimiento();
            movimientoMock.setId(1L);
            movimientoMock.setCantidad(new BigDecimal("10.00"));
            movimientoMock.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            movimientoMock.setDetalles(new HashSet<>());

            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoAjusteStock(
                any(MovimientoDTO.class), any(Bulto.class), any(User.class)))
                .thenReturn(movimientoMock);

            // Mock UnidadMedidaUtils
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Bulto.class)))
                .thenReturn(new BigDecimal("40.00"));
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Lote.class)))
                .thenReturn(new BigDecimal("90.00"));

            // Mock DTOUtils
            LoteDTO loteDTOMock = new LoteDTO();
            loteDTOMock.setCodigoLote("L-TEST-001");
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class)))
                .thenReturn(loteDTOMock);

            // Mock repositories
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001"))
                .thenReturn(Optional.of(loteTest));
            when(movimientoRepository.save(any(Movimiento.class)))
                .thenReturn(movimientoMock);
            when(loteRepository.save(any(Lote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoteDTO resultado = service.bajaAjusteStock(dto);

            // Then
            assertThat(bulto.getCantidadActual()).isEqualByComparingTo(new BigDecimal("40.00"));
            assertThat(bulto.getEstado()).isEqualTo(EstadoEnum.EN_USO);
        }

        @Test
        @DisplayName("test_ajusteStockTodosBultosConsumidos_debe_marcarLoteConsumido_linea100_102")
        void test_ajusteStockTodosBultosConsumidos_debe_marcarLoteConsumido_linea100_102() {
            // Given - Preparar lote con un bulto ya consumido
            loteTest.getBultos().get(0).setCantidadActual(BigDecimal.ZERO);
            loteTest.getBultos().get(0).setEstado(EstadoEnum.CONSUMIDO);

            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroBulto("2");
            dto.setCantidad(new BigDecimal("50.00")); // Consumir todo el segundo bulto
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            Bulto bulto = loteTest.getBultos().get(1);

            // Mock MovimientoBajaUtils
            Movimiento movimientoMock = new Movimiento();
            movimientoMock.setId(1L);
            movimientoMock.setCantidad(new BigDecimal("50.00"));
            movimientoMock.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            movimientoMock.setDetalles(new HashSet<>());

            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoAjusteStock(
                any(MovimientoDTO.class), any(Bulto.class), any(User.class)))
                .thenReturn(movimientoMock);

            // Mock UnidadMedidaUtils
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Bulto.class)))
                .thenReturn(BigDecimal.ZERO);
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Lote.class)))
                .thenReturn(BigDecimal.ZERO);

            // Mock DTOUtils
            LoteDTO loteDTOMock = new LoteDTO();
            loteDTOMock.setCodigoLote("L-TEST-001");
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class)))
                .thenReturn(loteDTOMock);

            // Mock repositories
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001"))
                .thenReturn(Optional.of(loteTest));
            when(movimientoRepository.save(any(Movimiento.class)))
                .thenReturn(movimientoMock);
            when(loteRepository.save(any(Lote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoteDTO resultado = service.bajaAjusteStock(dto);

            // Then
            assertThat(bulto.getCantidadActual()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(bulto.getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);
            assertThat(loteTest.getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);
        }

        @Test
        @DisplayName("test_ajusteStockCancelaAnalisisSinDictamen_debe_cancelarAnalisis_linea105_110")
        void test_ajusteStockCancelaAnalisisSinDictamen_debe_cancelarAnalisis_linea105_110() {
            // Given - Lote con análisis sin dictamen
            Analisis analisisPendiente = new Analisis();
            analisisPendiente.setId(1L);
            analisisPendiente.setNroAnalisis("AN-2025-001");
            analisisPendiente.setDictamen(null); // Sin dictamen
            analisisPendiente.setLote(loteTest);
            analisisPendiente.setActivo(true);
            loteTest.getAnalisisList().add(analisisPendiente);

            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroBulto("1");
            dto.setCantidad(new BigDecimal("50.00"));
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            // Preparar para consumir todo el lote
            loteTest.getBultos().get(1).setCantidadActual(BigDecimal.ZERO);
            loteTest.getBultos().get(1).setEstado(EstadoEnum.CONSUMIDO);

            Bulto bulto = loteTest.getBultos().get(0);

            // Mock MovimientoBajaUtils
            Movimiento movimientoMock = new Movimiento();
            movimientoMock.setId(1L);
            movimientoMock.setCantidad(new BigDecimal("50.00"));
            movimientoMock.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            movimientoMock.setDetalles(new HashSet<>());

            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoAjusteStock(
                any(MovimientoDTO.class), any(Bulto.class), any(User.class)))
                .thenReturn(movimientoMock);

            // Mock UnidadMedidaUtils
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Bulto.class)))
                .thenReturn(BigDecimal.ZERO);
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Lote.class)))
                .thenReturn(BigDecimal.ZERO);

            // Mock DTOUtils
            LoteDTO loteDTOMock = new LoteDTO();
            loteDTOMock.setCodigoLote("L-TEST-001");
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class)))
                .thenReturn(loteDTOMock);

            // Mock repositories
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001"))
                .thenReturn(Optional.of(loteTest));
            when(movimientoRepository.save(any(Movimiento.class)))
                .thenReturn(movimientoMock);
            when(loteRepository.save(any(Lote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
            when(analisisRepository.save(any(Analisis.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            LoteDTO resultado = service.bajaAjusteStock(dto);

            // Then
            assertThat(loteTest.getCantidadActual()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(analisisPendiente.getDictamen()).isEqualTo(DictamenEnum.CANCELADO);
            verify(analisisRepository).save(analisisPendiente);
        }

        @Test
        @DisplayName("test_ajusteStockLoteNoExiste_debe_lanzarExcepcion")
        void test_ajusteStockLoteNoExiste_debe_lanzarExcepcion() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-INEXISTENTE-999");
            dto.setNroBulto("1");
            dto.setCantidad(new BigDecimal("10.00"));
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);

            when(loteRepository.findByCodigoLoteAndActivoTrue("L-INEXISTENTE-999"))
                .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> service.bajaAjusteStock(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El lote no existe.");
        }
    }

    @Nested
    @DisplayName("persistirMovimientoAjuste() - Persistencia de movimiento")
    class PersistirMovimientoAjuste {

        @Test
        @DisplayName("test_persistirMovimientoAjuste_debe_crearYGuardarMovimiento")
        void test_persistirMovimientoAjuste_debe_crearYGuardarMovimiento() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroBulto("1");
            dto.setCantidad(new BigDecimal("10.00"));
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setFechaMovimiento(LocalDate.now());
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            Bulto bulto = loteTest.getBultos().get(0);

            Movimiento movimientoMock = new Movimiento();
            movimientoMock.setId(1L);
            movimientoMock.setCantidad(new BigDecimal("10.00"));
            movimientoMock.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);

            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoAjusteStock(
                any(MovimientoDTO.class), any(Bulto.class), any(User.class)))
                .thenReturn(movimientoMock);

            when(movimientoRepository.save(any(Movimiento.class)))
                .thenReturn(movimientoMock);

            // When
            Movimiento resultado = service.persistirMovimientoAjuste(dto, bulto, testUser);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getCantidad()).isEqualByComparingTo(new BigDecimal("10.00"));
            verify(movimientoRepository).save(any(Movimiento.class));
        }
    }

    @Nested
    @DisplayName("validarAjusteStockInput() - Validaciones de entrada")
    class ValidarAjusteStockInput {

        @Test
        @DisplayName("test_validacionExitosa_debe_retornarTrue")
        void test_validacionExitosa_debe_retornarTrue() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroBulto("1");
            dto.setCantidad(new BigDecimal("10.00"));
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setFechaMovimiento(LocalDate.now());
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001"))
                .thenReturn(Optional.of(loteTest));
            when(bultoRepository.findFirstByLoteCodigoLoteAndNroBultoAndActivoTrue("L-TEST-001", 1))
                .thenReturn(Optional.of(loteTest.getBultos().get(0)));
            doReturn(true).when(service).validarFechaMovimientoPosteriorIngresoLote(
                any(MovimientoDTO.class), any(LocalDate.class), any(BindingResult.class));
            doReturn(true).when(service).validarCantidadesMovimiento(
                any(MovimientoDTO.class), any(Bulto.class), any(BindingResult.class));

            // When
            boolean resultado = service.validarAjusteStockInput(dto, binding);

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
            binding.rejectValue("cantidad", "", "Cantidad requerida");

            // When
            boolean resultado = service.validarAjusteStockInput(dto, binding);

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

            when(loteRepository.findByCodigoLoteAndActivoTrue("L-INEXISTENTE-999"))
                .thenReturn(Optional.empty());

            // When
            boolean resultado = service.validarAjusteStockInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("codigoLote")).isNotNull();
            assertThat(binding.getFieldError("codigoLote").getDefaultMessage())
                .isEqualTo("Lote no encontrado.");
        }

        @Test
        @DisplayName("test_validacionLoteTrazadoSinTrazas_debe_agregarError")
        void test_validacionLoteTrazadoSinTrazas_debe_agregarError() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TRAZ-001");
            dto.setFechaMovimiento(LocalDate.now());
            dto.setTrazaDTOs(null); // Sin trazas
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TRAZ-001"))
                .thenReturn(Optional.of(loteTestTrazable));

            // When
            boolean resultado = service.validarAjusteStockInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("trazaDTOs")).isNotNull();
            assertThat(binding.getFieldError("trazaDTOs").getDefaultMessage())
                .isEqualTo("Debe seleccionar al menos una unidad a muestrear.");
        }

        @Test
        @DisplayName("test_validacionFechaMovimientoInvalida_debe_agregarError")
        void test_validacionFechaMovimientoInvalida_debe_agregarError() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setFechaMovimiento(LocalDate.now().minusDays(20)); // Antes del ingreso
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001"))
                .thenReturn(Optional.of(loteTest));
            doReturn(false).when(service).validarFechaMovimientoPosteriorIngresoLote(
                any(MovimientoDTO.class), any(LocalDate.class), any(BindingResult.class));

            // When
            boolean resultado = service.validarAjusteStockInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("test_validacionBultoNoEncontrado_debe_agregarError")
        void test_validacionBultoNoEncontrado_debe_agregarError() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroBulto("999"); // Bulto inexistente
            dto.setFechaMovimiento(LocalDate.now());
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001"))
                .thenReturn(Optional.of(loteTest));
            doReturn(true).when(service).validarFechaMovimientoPosteriorIngresoLote(
                any(MovimientoDTO.class), any(LocalDate.class), any(BindingResult.class));
            when(bultoRepository.findFirstByLoteCodigoLoteAndNroBultoAndActivoTrue("L-TEST-001", 999))
                .thenReturn(Optional.empty());

            // When
            boolean resultado = service.validarAjusteStockInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("nroBulto")).isNotNull();
            assertThat(binding.getFieldError("nroBulto").getDefaultMessage())
                .isEqualTo("Bulto no encontrado.");
        }

        @Test
        @DisplayName("test_validacionCantidadesInvalidas_debe_agregarError")
        void test_validacionCantidadesInvalidas_debe_agregarError() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroBulto("1");
            dto.setCantidad(new BigDecimal("100.00")); // Excede stock
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            dto.setFechaMovimiento(LocalDate.now());
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001"))
                .thenReturn(Optional.of(loteTest));
            doReturn(true).when(service).validarFechaMovimientoPosteriorIngresoLote(
                any(MovimientoDTO.class), any(LocalDate.class), any(BindingResult.class));
            when(bultoRepository.findFirstByLoteCodigoLoteAndNroBultoAndActivoTrue("L-TEST-001", 1))
                .thenReturn(Optional.of(loteTest.getBultos().get(0)));
            doReturn(false).when(service).validarCantidadesMovimiento(
                any(MovimientoDTO.class), any(Bulto.class), any(BindingResult.class));

            // When
            boolean resultado = service.validarAjusteStockInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("test_validacionLoteTrazadoConListaVacia_debe_agregarError - cubre línea 135-137")
        void test_validacionLoteTrazadoConListaVacia_debe_agregarError() {
            // Given
            loteTestTrazable.setTrazado(true);
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TRAZ-001");
            dto.setNroBulto("1");
            dto.setTrazaDTOs(new ArrayList<>()); // Lista vacía (no null)
            dto.setFechaMovimiento(LocalDate.now());
            BindingResult binding = new BeanPropertyBindingResult(dto, "movimientoDTO");

            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TRAZ-001"))
                .thenReturn(Optional.of(loteTestTrazable));

            // When
            boolean resultado = service.validarAjusteStockInput(dto, binding);

            // Then
            assertThat(resultado).isFalse();
            assertThat(binding.hasErrors()).isTrue();
            assertThat(binding.getFieldError("trazaDTOs")).isNotNull();
        }
    }

    @Nested
    @DisplayName("Tests adicionales para 100% cobertura")
    class TestsCobertura100 {

        @Test
        @DisplayName("test_ajusteStockSinDetallesEnMovimiento_debe_lanzarExcepcion - cubre línea 83")
        void test_ajusteStockSinDetallesEnMovimiento_debe_lanzarExcepcion() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TRAZ-001");
            dto.setNroBulto("1");
            dto.setCantidad(new BigDecimal("2"));
            dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            TrazaDTO trazaDTO = new TrazaDTO();
            trazaDTO.setNroTraza(1L);
            dto.setTrazaDTOs(List.of(trazaDTO));

            when(securityContextService.getCurrentUser()).thenReturn(testUser);
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TRAZ-001"))
                .thenReturn(Optional.of(loteTestTrazable));

            // Crear movimiento sin detalles
            Movimiento movimientoSinDetalles = new Movimiento();
            movimientoSinDetalles.setId(1L);
            movimientoSinDetalles.setDetalles(new HashSet<>()); // Sin detalles
            movimientoSinDetalles.setCantidad(new BigDecimal("2"));
            movimientoSinDetalles.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoAjusteStock(
                any(MovimientoDTO.class), any(Bulto.class), any(User.class)))
                .thenReturn(movimientoSinDetalles);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoSinDetalles);

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Bulto.class)))
                .thenReturn(new BigDecimal("8"));
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Lote.class)))
                .thenReturn(new BigDecimal("8"));

            // When & Then
            assertThatThrownBy(() -> service.bajaAjusteStock(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El detalle del movimiento de ajuste no existe.");
        }

        @Test
        @DisplayName("test_ajusteStockLoteSinStockYSinAnalisis_noDebeCancelarAnalisis - cubre línea 106")
        void test_ajusteStockLoteSinStockYSinAnalisis_noDebeCancelarAnalisis() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("L-TEST-001");
            dto.setNroBulto("1");
            dto.setCantidad(new BigDecimal("10")); // Toda la cantidad del bulto
            dto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);

            // Lote sin análisis
            loteTest.setAnalisisList(new ArrayList<>()); // Sin análisis

            when(securityContextService.getCurrentUser()).thenReturn(testUser);
            when(loteRepository.findByCodigoLoteAndActivoTrue("L-TEST-001"))
                .thenReturn(Optional.of(loteTest));

            Movimiento movimiento = crearMovimientoTest(dto);
            movimientoBajaUtilsMock.when(() -> MovimientoBajaUtils.createMovimientoAjusteStock(
                any(MovimientoDTO.class), any(Bulto.class), any(User.class)))
                .thenReturn(movimiento);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Bulto.class)))
                .thenReturn(BigDecimal.ZERO); // Bulto queda sin stock
            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.restarMovimientoConvertido(
                any(MovimientoDTO.class), any(Lote.class)))
                .thenReturn(BigDecimal.ZERO); // Lote queda sin stock

            when(loteRepository.save(any(Lote.class))).thenReturn(loteTest);
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class)))
                .thenReturn(new LoteDTO());

            // When
            LoteDTO resultado = service.bajaAjusteStock(dto);

            // Then
            assertThat(resultado).isNotNull();
            verify(analisisRepository, never()).save(any(Analisis.class)); // No debe guardar análisis
        }

        private Movimiento crearMovimientoTest(MovimientoDTO dto) {
            Movimiento mov = new Movimiento();
            mov.setId(1L);
            mov.setCantidad(dto.getCantidad());
            mov.setUnidadMedida(dto.getUnidadMedida());

            DetalleMovimiento detalle = new DetalleMovimiento();
            detalle.setId(1L);
            detalle.setCantidad(dto.getCantidad());
            detalle.setUnidadMedida(dto.getUnidadMedida());
            detalle.setTrazas(new HashSet<>());

            mov.setDetalles(new HashSet<>(Set.of(detalle)));
            return mov;
        }
    }
}

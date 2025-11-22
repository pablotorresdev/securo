package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.BultoDTO;
import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.BultoRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.TrazaRepository;
import com.mb.conitrack.repository.maestro.ProductoRepository;
import com.mb.conitrack.repository.maestro.ProveedorRepository;
import com.mb.conitrack.service.SecurityContextService;
import com.mb.conitrack.utils.LoteEntityUtils;
import com.mb.conitrack.utils.MovimientoAltaUtils;
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
import org.springframework.validation.BeanPropertyBindingResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AltaIngresoProduccionService - Tests")
class AltaIngresoProduccionServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private BultoRepository bultoRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private TrazaRepository trazaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private AltaIngresoProduccionService service;

    private MockedStatic<LoteEntityUtils> loteEntityUtilsMock;
    private MockedStatic<MovimientoAltaUtils> movimientoAltaUtilsMock;
    private MockedStatic<DTOUtils> dtoUtilsMock;

    private LoteDTO loteDTO;
    private Producto producto;
    private Proveedor conifarma;
    private User currentUser;
    private Lote lote;
    private Movimiento movimiento;

    @BeforeEach
    void setUp() {
        // Setup MockedStatic
        loteEntityUtilsMock = mockStatic(LoteEntityUtils.class);
        movimientoAltaUtilsMock = mockStatic(MovimientoAltaUtils.class);
        dtoUtilsMock = mockStatic(DTOUtils.class);

        // Setup current user
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");

        // Setup producto
        producto = new Producto();
        producto.setId(1L);
        producto.setCodigoProducto("PROD-001");
        producto.setTipoProducto(TipoProductoEnum.API);
        producto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);

        // Setup conifarma
        conifarma = new Proveedor();
        conifarma.setId(1L);
        conifarma.setRazonSocial("Conifarma");

        // Setup lote
        lote = new Lote();
        lote.setId(1L);
        lote.setCodigoLote("LOTE-PROD-001");
        lote.setProducto(producto);
        lote.setProveedor(conifarma);
        lote.setFabricante(conifarma);
        lote.setCantidadInicial(new BigDecimal("100"));
        lote.setCantidadActual(new BigDecimal("100"));
        lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        lote.setFechaIngreso(LocalDate.of(2024, 1, 1));
        lote.setEstado(EstadoEnum.NUEVO);
        lote.setDictamen(DictamenEnum.RECIBIDO);
        lote.setBultos(new ArrayList<>());

        // Setup bulto
        Bulto bulto = new Bulto();
        bulto.setId(1L);
        bulto.setNroBulto(1);
        bulto.setLote(lote);
        bulto.setCantidadInicial(new BigDecimal("100"));
        bulto.setCantidadActual(new BigDecimal("100"));
        bulto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        bulto.setEstado(EstadoEnum.NUEVO);
        lote.getBultos().add(bulto);

        // Setup movimiento
        movimiento = new Movimiento();
        movimiento.setId(1L);
        movimiento.setLote(lote);
        movimiento.setDetalles(new HashSet<>());

        // Setup LoteDTO
        loteDTO = new LoteDTO();
        loteDTO.setProductoId(1L);
        loteDTO.setCodigoLote("LOTE-PROD-001");
        loteDTO.setCantidadInicial(new BigDecimal("100"));
        loteDTO.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        loteDTO.setFechaIngreso(LocalDate.of(2024, 1, 1));
        loteDTO.setFechaVencimientoProveedor(LocalDate.of(2025, 1, 1));
        loteDTO.setBultosTotales(1);

        BultoDTO bultoDTO = new BultoDTO();
        bultoDTO.setNroBulto(1);
        bultoDTO.setCantidadInicial(new BigDecimal("100"));
        bultoDTO.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        loteDTO.setBultosDTOs(List.of(bultoDTO));
    }

    @AfterEach
    void tearDown() {
        if (loteEntityUtilsMock != null) {
            loteEntityUtilsMock.close();
        }
        if (movimientoAltaUtilsMock != null) {
            movimientoAltaUtilsMock.close();
        }
        if (dtoUtilsMock != null) {
            dtoUtilsMock.close();
        }
    }

    @Nested
    @DisplayName("altaStockPorProduccion() - Tests")
    class AltaStockPorProduccionTests {

        @Test
        @DisplayName("Debe crear lote de producción exitosamente - cubre líneas 38-70")
        void altaStockPorProduccion_datosValidos_debeCrearLote() {
            // Given
            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(proveedorRepository.findConifarma()).thenReturn(Optional.of(conifarma));
            when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

            loteEntityUtilsMock.when(() -> LoteEntityUtils.createLoteIngreso(any(LoteDTO.class)))
                    .thenReturn(lote);
            loteEntityUtilsMock.when(() -> LoteEntityUtils.populateLoteAltaProduccionPropia(
                    any(Lote.class), any(LoteDTO.class), any(Producto.class), any(Proveedor.class)))
                    .thenAnswer(invocation -> null);

            when(loteRepository.save(any(Lote.class))).thenReturn(lote);
            when(bultoRepository.saveAll(any())).thenReturn(lote.getBultos());

            movimientoAltaUtilsMock.when(() -> MovimientoAltaUtils.createMovimientoAltaIngresoProduccion(
                    any(Lote.class), any(User.class)))
                    .thenReturn(movimiento);

            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);

            LoteDTO resultDTO = new LoteDTO();
            resultDTO.setCodigoLote("LOTE-PROD-001");
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(resultDTO);

            // When
            LoteDTO resultado = service.altaStockPorProduccion(loteDTO);

            // Then
            assertNotNull(resultado);
            assertThat(resultado.getCodigoLote()).isEqualTo("LOTE-PROD-001");
            verify(loteRepository).save(any(Lote.class));
            verify(bultoRepository).saveAll(any());
            verify(movimientoRepository, times(2)).save(any(Movimiento.class));
            verify(securityContextService).getCurrentUser();
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando Conifarma no existe - cubre línea 40")
        void altaStockPorProduccion_conifarmanoExiste_debeLanzarExcepcion() {
            // Given
            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(proveedorRepository.findConifarma()).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.altaStockPorProduccion(loteDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El proveedor Conifarma no existe.");

            verify(productoRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando producto no existe - cubre línea 43")
        void altaStockPorProduccion_productoNoExiste_debeLanzarExcepcion() {
            // Given
            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(proveedorRepository.findConifarma()).thenReturn(Optional.of(conifarma));
            when(productoRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.altaStockPorProduccion(loteDTO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El producto no existe.");

            verify(loteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("validarIngresoProduccionInput() - Tests")
    class ValidarIngresoProduccionInputTests {

        @Test
        @DisplayName("Debe retornar false cuando bindingResult tiene errores - cubre línea 76-77")
        void validarIngresoProduccionInput_bindingResultConErrores_debeRetornarFalse() {
            // Given
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(true);

            // When
            boolean resultado = service.validarIngresoProduccionInput(loteDTO, bindingResult);

            // Then
            assertFalse(resultado);
        }

        @Test
        @DisplayName("Debe retornar false cuando validarCantidadIngreso falla - cubre línea 79-80")
        void validarIngresoProduccionInput_cantidadInvalida_debeRetornarFalse() {
            // Given
            loteDTO.setCantidadInicial(null); // Cantidad nula es inválida
            BindingResult bindingResult = new BeanPropertyBindingResult(loteDTO, "loteDTO");

            // When
            boolean resultado = service.validarIngresoProduccionInput(loteDTO, bindingResult);

            // Then
            assertFalse(resultado);
            assertTrue(bindingResult.hasErrors());
        }

        @Test
        @DisplayName("Debe retornar false cuando validarBultos falla - cubre línea 82-83")
        void validarIngresoProduccionInput_bultosInvalidos_debeRetornarFalse() {
            // Given
            loteDTO.setBultosTotales(2); // Más de 1 bulto

            BultoDTO bulto1 = new BultoDTO();
            bulto1.setNroBulto(1);
            bulto1.setCantidadInicial(new BigDecimal("50"));
            bulto1.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);

            BultoDTO bulto2 = new BultoDTO();
            bulto2.setNroBulto(2);
            bulto2.setCantidadInicial(new BigDecimal("30")); // Total 80, pero cantidadInicial del lote es 100
            bulto2.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);

            loteDTO.setBultosDTOs(List.of(bulto1, bulto2)); // Suma no coincide con cantidad inicial
            BindingResult bindingResult = new BeanPropertyBindingResult(loteDTO, "loteDTO");

            // When
            boolean resultado = service.validarIngresoProduccionInput(loteDTO, bindingResult);

            // Then
            assertFalse(resultado);
            assertTrue(bindingResult.hasErrors());
        }

        @Test
        @DisplayName("Debe retornar false cuando validarTraza falla - cubre línea 85")
        void validarIngresoProduccionInput_trazaInvalida_debeRetornarFalse() {
            // Given
            loteDTO.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            loteDTO.setTrazaInicial(100L); // Traza con unidad que no es UNIDAD
            BindingResult bindingResult = new BeanPropertyBindingResult(loteDTO, "loteDTO");

            // When
            boolean resultado = service.validarIngresoProduccionInput(loteDTO, bindingResult);

            // Then
            assertFalse(resultado);
            assertTrue(bindingResult.hasErrors());
        }

        @Test
        @DisplayName("Debe retornar true cuando todas las validaciones pasan - cubre líneas 76-85")
        void validarIngresoProduccionInput_datosValidos_debeRetornarTrue() {
            // Given
            BindingResult bindingResult = new BeanPropertyBindingResult(loteDTO, "loteDTO");

            // When
            boolean resultado = service.validarIngresoProduccionInput(loteDTO, bindingResult);

            // Then
            assertTrue(resultado);
            assertFalse(bindingResult.hasErrors());
        }
    }

    @Nested
    @DisplayName("validarTraza() - Tests")
    class ValidarTrazaTests {

        @Test
        @DisplayName("Debe retornar false cuando bindingResult tiene errores - cubre línea 90-91")
        void validarTraza_bindingResultConErrores_debeRetornarFalse() {
            // Given
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(true);

            // When
            boolean resultado = service.validarTraza(loteDTO, bindingResult);

            // Then
            assertFalse(resultado);
        }

        @Test
        @DisplayName("Debe retornar true cuando trazaInicial es null - cubre línea 93, 107")
        void validarTraza_trazaInicialNull_debeRetornarTrue() {
            // Given
            loteDTO.setTrazaInicial(null);
            BindingResult bindingResult = new BeanPropertyBindingResult(loteDTO, "loteDTO");

            // When
            boolean resultado = service.validarTraza(loteDTO, bindingResult);

            // Then
            assertTrue(resultado);
            assertFalse(bindingResult.hasErrors());
        }

        @Test
        @DisplayName("Debe retornar false cuando trazaInicial con unidad NO UNIDAD - cubre líneas 94-96")
        void validarTraza_trazaConUnidadNoUnidad_debeRetornarFalse() {
            // Given
            loteDTO.setTrazaInicial(100L);
            loteDTO.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
            BindingResult bindingResult = new BeanPropertyBindingResult(loteDTO, "loteDTO");

            // When
            boolean resultado = service.validarTraza(loteDTO, bindingResult);

            // Then
            assertFalse(resultado);
            assertTrue(bindingResult.hasErrors());
            assertThat(bindingResult.getFieldError("trazaInicial")).isNotNull();
            assertThat(bindingResult.getFieldError("trazaInicial").getDefaultMessage())
                    .contains("solo aplica a unidades de venta");
        }

        @Test
        @DisplayName("Debe retornar false cuando trazaInicial <= maxNroTraza - cubre líneas 98-104")
        void validarTraza_trazaInicialMenorOIgualQueMax_debeRetornarFalse() {
            // Given
            loteDTO.setTrazaInicial(50L);
            loteDTO.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            loteDTO.setProductoId(1L);
            BindingResult bindingResult = new BeanPropertyBindingResult(loteDTO, "loteDTO");

            when(trazaRepository.findMaxNroTraza(1L)).thenReturn(100L);

            // When
            boolean resultado = service.validarTraza(loteDTO, bindingResult);

            // Then
            assertFalse(resultado);
            assertTrue(bindingResult.hasErrors());
            assertThat(bindingResult.getFieldError("trazaInicial")).isNotNull();
            assertThat(bindingResult.getFieldError("trazaInicial").getDefaultMessage())
                    .contains("debe ser mayor al último registrado");
        }

        @Test
        @DisplayName("Debe retornar true cuando trazaInicial > maxNroTraza - cubre líneas 98-107")
        void validarTraza_trazaInicialMayorQueMax_debeRetornarTrue() {
            // Given
            loteDTO.setTrazaInicial(150L);
            loteDTO.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            loteDTO.setProductoId(1L);
            BindingResult bindingResult = new BeanPropertyBindingResult(loteDTO, "loteDTO");

            when(trazaRepository.findMaxNroTraza(1L)).thenReturn(100L);

            // When
            boolean resultado = service.validarTraza(loteDTO, bindingResult);

            // Then
            assertTrue(resultado);
            assertFalse(bindingResult.hasErrors());
        }

        @Test
        @DisplayName("Debe retornar true cuando maxNroTraza es 0 - cubre línea 99 branch (maxNroTraza <= 0)")
        void validarTraza_maxNroTrazaCero_debeRetornarTrue() {
            // Given
            loteDTO.setTrazaInicial(1L);
            loteDTO.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            loteDTO.setProductoId(1L);
            BindingResult bindingResult = new BeanPropertyBindingResult(loteDTO, "loteDTO");

            when(trazaRepository.findMaxNroTraza(1L)).thenReturn(0L);

            // When
            boolean resultado = service.validarTraza(loteDTO, bindingResult);

            // Then
            assertTrue(resultado);
            assertFalse(bindingResult.hasErrors());
        }

        @Test
        @DisplayName("Debe retornar true cuando maxNroTraza es -1 (no hay trazas previas) - cubre línea 99 branch")
        void validarTraza_maxNroTrazaMenosUno_debeRetornarTrue() {
            // Given
            loteDTO.setTrazaInicial(1L);
            loteDTO.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
            loteDTO.setProductoId(1L);
            BindingResult bindingResult = new BeanPropertyBindingResult(loteDTO, "loteDTO");

            when(trazaRepository.findMaxNroTraza(1L)).thenReturn(-1L); // COALESCE retorna -1 cuando no hay trazas

            // When
            boolean resultado = service.validarTraza(loteDTO, bindingResult);

            // Then
            assertTrue(resultado);
            assertFalse(bindingResult.hasErrors());
        }
    }
}

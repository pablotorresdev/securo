package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.*;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.*;

import static com.mb.conitrack.utils.MovimientoModificacionUtils.createMovimientoReverso;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test completo para ReversoAltaService
 * Objetivo: 100% cobertura de código
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReversoAltaService Tests")
class ReversoAltaServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private BultoRepository bultoRepository;

    @Mock
    private TrazaRepository trazaRepository;

    @Mock
    private AnalisisRepository analisisRepository;

    @InjectMocks
    private ReversoAltaService service;

    private MockedStatic<DTOUtils> dtoUtilsMock;
    private MockedStatic<com.mb.conitrack.utils.MovimientoModificacionUtils> movimientoUtilsMock;

    private User currentUser;
    private MovimientoDTO dto;
    private Movimiento movimientoOrigen;
    private Movimiento movimientoReverso;
    private Lote lote;
    private LoteDTO loteDTO;
    private List<Bulto> bultos;
    private List<DetalleMovimiento> detalles;

    @BeforeEach
    void setUp() {
        dtoUtilsMock = mockStatic(DTOUtils.class);
        movimientoUtilsMock = mockStatic(com.mb.conitrack.utils.MovimientoModificacionUtils.class);

        // Setup user
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("test.user");

        // Setup DTO
        dto = new MovimientoDTO();
        dto.setObservaciones("Test reverso");

        // Setup lote
        lote = new Lote();
        lote.setId(1L);
        lote.setActivo(true);
        lote.setTrazado(false);
        lote.setCantidadInicial(new BigDecimal("100"));
        lote.setCantidadActual(new BigDecimal("100"));
        lote.setEstado(EstadoEnum.NUEVO);
        lote.setDictamen(DictamenEnum.APROBADO);

        // Setup bultos
        Bulto bulto1 = new Bulto();
        bulto1.setId(1L);
        bulto1.setNroBulto(1);
        bulto1.setActivo(true);
        bulto1.setCantidadInicial(new BigDecimal("100"));
        bulto1.setCantidadActual(new BigDecimal("100"));
        bulto1.setEstado(EstadoEnum.NUEVO);
        bulto1.setLote(lote);

        DetalleMovimiento detalle1 = new DetalleMovimiento();
        detalle1.setId(1L);
        detalle1.setActivo(true);
        detalle1.setBulto(bulto1);
        detalle1.setTrazas(new HashSet<>());

        bulto1.setDetalles(new HashSet<>(List.of(detalle1)));

        bultos = new ArrayList<>(List.of(bulto1));
        lote.setBultos(bultos);

        // Setup movimiento origen
        movimientoOrigen = new Movimiento();
        movimientoOrigen.setId(1L);
        movimientoOrigen.setCodigoMovimiento("MOV-001");
        movimientoOrigen.setActivo(true);
        movimientoOrigen.setLote(lote);
        movimientoOrigen.setTipoMovimiento(TipoMovimientoEnum.ALTA);

        detalles = new ArrayList<>(List.of(detalle1));
        movimientoOrigen.setDetalles(new HashSet<>(detalles));

        // Setup movimiento reverso
        movimientoReverso = new Movimiento();
        movimientoReverso.setId(2L);
        movimientoReverso.setCodigoMovimiento("MOV-002");
        movimientoReverso.setActivo(false);
        movimientoReverso.setMovimientoOrigen(movimientoOrigen);

        // Setup DTO response
        loteDTO = new LoteDTO();
        loteDTO.setCodigoLote("LOTE-001");

        // Default mocking for repositories
        lenient().when(loteRepository.save(any(Lote.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(bultoRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(trazaRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<?> arg = invocation.getArgument(0);
            return arg instanceof List ? arg : new ArrayList<>((Collection<?>) arg);
        });
    }

    @AfterEach
    void tearDown() {
        if (dtoUtilsMock != null) {
            dtoUtilsMock.close();
        }
        if (movimientoUtilsMock != null) {
            movimientoUtilsMock.close();
        }
    }

    @Nested
    @DisplayName("reversarAltaIngresoCompra Tests")
    class ReversarAltaIngresoCompraTests {

        @BeforeEach
        void setUpMocks() {
            movimientoUtilsMock.when(() -> createMovimientoReverso(any(), any(), any()))
                    .thenReturn(movimientoReverso);
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class)))
                    .thenReturn(loteDTO);
        }

        @Test
        @DisplayName("debe reversar ingreso compra correctamente - cubre líneas 30-46")
        void reversarAltaIngresoCompra_debeReversarCorrectamente() {
            // When
            LoteDTO resultado = service.reversarAltaIngresoCompra(dto, movimientoOrigen, currentUser);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigoLote()).isEqualTo("LOTE-001");

            // Verify entities set to inactive
            assertFalse(movimientoOrigen.getActivo());
            assertFalse(movimientoReverso.getActivo());
            assertFalse(lote.getActivo());

            // Verify bultos and detalles set to inactive
            bultos.forEach(b -> assertFalse(b.getActivo()));
            bultos.forEach(b -> b.getDetalles().forEach(d -> assertFalse(d.getActivo())));

            // Verify saves
            verify(bultoRepository).saveAll(bultos);
            verify(movimientoRepository, times(2)).save(any(Movimiento.class));
            verify(loteRepository).save(lote);

            // Verify static calls
            movimientoUtilsMock.verify(() -> createMovimientoReverso(dto, movimientoOrigen, currentUser), times(1));
            dtoUtilsMock.verify(() -> DTOUtils.fromLoteEntity(lote), times(1));
        }

        @Test
        @DisplayName("debe marcar todos los bultos como inactivos - cubre línea 37")
        void reversarAltaIngresoCompra_debeMarcarBultosInactivos() {
            // Given - add multiple bultos
            Bulto bulto2 = new Bulto();
            bulto2.setId(2L);
            bulto2.setNroBulto(2);
            bulto2.setActivo(true);

            DetalleMovimiento detalle2 = new DetalleMovimiento();
            detalle2.setId(2L);
            detalle2.setActivo(true);

            bulto2.setDetalles(new HashSet<>(List.of(detalle2)));
            bultos.add(bulto2);

            // When
            service.reversarAltaIngresoCompra(dto, movimientoOrigen, currentUser);

            // Then
            assertThat(bultos).hasSize(2);
            bultos.forEach(b -> assertFalse(b.getActivo(), "Bulto should be inactive"));
        }

        @Test
        @DisplayName("debe marcar todos los detalles como inactivos - cubre línea 38")
        void reversarAltaIngresoCompra_debeMarcarDetallesInactivos() {
            // Given - add multiple detalles
            DetalleMovimiento detalle2 = new DetalleMovimiento();
            detalle2.setId(2L);
            detalle2.setActivo(true);

            bultos.get(0).getDetalles().add(detalle2);

            // When
            service.reversarAltaIngresoCompra(dto, movimientoOrigen, currentUser);

            // Then
            bultos.forEach(b -> {
                assertThat(b.getDetalles()).isNotEmpty();
                b.getDetalles().forEach(d -> assertFalse(d.getActivo(), "Detalle should be inactive"));
            });
        }
    }

    @Nested
    @DisplayName("reversarAltaIngresoProduccion Tests")
    class ReversarAltaIngresoProduccionTests {

        @BeforeEach
        void setUpMocks() {
            movimientoUtilsMock.when(() -> createMovimientoReverso(any(), any(), any()))
                    .thenReturn(movimientoReverso);
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class)))
                    .thenReturn(loteDTO);
        }

        @Test
        @DisplayName("debe reversar ingreso producción correctamente - cubre líneas 49-65")
        void reversarAltaIngresoProduccion_debeReversarCorrectamente() {
            // When
            LoteDTO resultado = service.reversarAltaIngresoProduccion(dto, movimientoOrigen, currentUser);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigoLote()).isEqualTo("LOTE-001");

            // Verify entities set to inactive
            assertFalse(movimientoOrigen.getActivo());
            assertFalse(movimientoReverso.getActivo());
            assertFalse(lote.getActivo());

            // Verify saves
            verify(bultoRepository).saveAll(bultos);
            verify(movimientoRepository, times(2)).save(any(Movimiento.class));
            verify(loteRepository).save(lote);

            // Verify static calls
            movimientoUtilsMock.verify(() -> createMovimientoReverso(dto, movimientoOrigen, currentUser), times(1));
            dtoUtilsMock.verify(() -> DTOUtils.fromLoteEntity(lote), times(1));
        }
    }

    @Nested
    @DisplayName("reversarAltaDevolucionVenta Tests")
    class ReversarAltaDevolucionVentaTests {

        private Lote loteVentaOrigen;

        @BeforeEach
        void setUpDevolucion() {
            movimientoUtilsMock.when(() -> createMovimientoReverso(any(), any(), any()))
                    .thenReturn(movimientoReverso);
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class)))
                    .thenReturn(loteDTO);


            // Setup lote venta origen
            loteVentaOrigen = new Lote();
            loteVentaOrigen.setId(2L);
            loteVentaOrigen.setActivo(true);
            loteVentaOrigen.setTrazado(false);

            Bulto bultoVentaOrigen = new Bulto();
            bultoVentaOrigen.setId(10L);
            bultoVentaOrigen.setNroBulto(1);
            bultoVentaOrigen.setActivo(true);
            bultoVentaOrigen.setLote(loteVentaOrigen);
            loteVentaOrigen.setBultos(new ArrayList<>(List.of(bultoVentaOrigen)));

            lote.setLoteOrigen(loteVentaOrigen);
        }

        @Test
        @DisplayName("debe reversar devolucion venta sin trazado - cubre líneas 68-99")
        void reversarAltaDevolucionVenta_sinTrazado_debeReversarCorrectamente() {
            // Given
            loteVentaOrigen.setTrazado(false);

            // When
            LoteDTO resultado = service.reversarAltaDevolucionVenta(dto, movimientoOrigen, currentUser);

            // Then
            assertThat(resultado).isNotNull();
            assertFalse(movimientoOrigen.getActivo());
            assertFalse(movimientoReverso.getActivo());
            assertFalse(lote.getActivo());

            // Verify no traza processing
            verify(trazaRepository, never()).saveAll(any());

            // Verify saves
            verify(bultoRepository).saveAll(anyCollection());
            verify(movimientoRepository, times(2)).save(any(Movimiento.class));
            verify(loteRepository).save(lote);
        }

        @Test
        @DisplayName("debe reversar devolucion venta con trazado - cubre líneas 82-93")
        void reversarAltaDevolucionVenta_conTrazado_debeReversarYRestaurarTrazas() {
            // Given
            loteVentaOrigen.setTrazado(true);

            Traza traza1 = new Traza();
            traza1.setId(1L);
            traza1.setNroTraza(1L);
            traza1.setEstado(EstadoEnum.DISPONIBLE);

            Traza traza2 = new Traza();
            traza2.setId(2L);
            traza2.setNroTraza(2L);
            traza2.setEstado(EstadoEnum.DISPONIBLE);

            Set<Traza> trazas = new HashSet<>(List.of(traza1, traza2));
            detalles.get(0).setTrazas(trazas);

            // When
            LoteDTO resultado = service.reversarAltaDevolucionVenta(dto, movimientoOrigen, currentUser);

            // Then
            assertThat(resultado).isNotNull();

            // Verify trazas updated to VENDIDO
            trazas.forEach(t -> assertThat(t.getEstado()).isEqualTo(EstadoEnum.VENDIDO));
            trazas.forEach(t -> assertThat(t.getLote()).isEqualTo(loteVentaOrigen));

            // Verify traza save
            verify(trazaRepository, times(1)).saveAll(any());
        }

        @Test
        @DisplayName("debe procesar múltiples detalles con trazas - cubre líneas 84-92 (for loop)")
        void reversarAltaDevolucionVenta_conMultiplesDetalles_debeProcesarTodos() {
            // Given
            loteVentaOrigen.setTrazado(true);

            // Detalle 1 con trazas
            Traza traza1 = new Traza();
            traza1.setId(1L);
            traza1.setNroTraza(1L);
            traza1.setEstado(EstadoEnum.DISPONIBLE);

            detalles.get(0).setTrazas(new HashSet<>(List.of(traza1)));

            // Detalle 2 con trazas
            Bulto bulto2 = new Bulto();
            bulto2.setId(2L);
            bulto2.setNroBulto(2);

            Traza traza2 = new Traza();
            traza2.setId(2L);
            traza2.setNroTraza(2L);
            traza2.setEstado(EstadoEnum.DISPONIBLE);

            DetalleMovimiento detalle2 = new DetalleMovimiento();
            detalle2.setId(2L);
            detalle2.setBulto(bulto2);
            detalle2.setTrazas(new HashSet<>(List.of(traza2)));

            Set<DetalleMovimiento> detallesSet = new HashSet<>();
            detallesSet.add(detalles.get(0));
            detallesSet.add(detalle2);
            movimientoOrigen.setDetalles(detallesSet);

            Bulto bultoVentaOrigen2 = new Bulto();
            bultoVentaOrigen2.setId(20L);
            bultoVentaOrigen2.setNroBulto(2);
            bultoVentaOrigen2.setActivo(true);
            bultoVentaOrigen2.setLote(loteVentaOrigen);
            List<Bulto> bultosList = new ArrayList<>(loteVentaOrigen.getBultos());
            bultosList.add(bultoVentaOrigen2);
            loteVentaOrigen.setBultos(bultosList);

            // When
            service.reversarAltaDevolucionVenta(dto, movimientoOrigen, currentUser);

            // Then
            assertThat(traza1.getEstado()).isEqualTo(EstadoEnum.VENDIDO);
            assertThat(traza2.getEstado()).isEqualTo(EstadoEnum.VENDIDO);
            verify(trazaRepository, times(2)).saveAll(any());
        }
    }

    @Nested
    @DisplayName("reversarRetiroMercado Tests")
    class ReversarRetiroMercadoTests {

        private Movimiento movimientoVentaOrigen;
        private Movimiento movimientoModificacionRecall;
        private Lote loteVentaOrigen;

        @BeforeEach
        void setUpRecall() {
            movimientoUtilsMock.when(() -> createMovimientoReverso(any(), any(), any()))
                    .thenReturn(movimientoReverso);
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class)))
                    .thenReturn(loteDTO);


            // Setup lote venta origen
            loteVentaOrigen = new Lote();
            loteVentaOrigen.setId(2L);
            loteVentaOrigen.setActivo(true);
            loteVentaOrigen.setTrazado(false);
            loteVentaOrigen.setDictamen(DictamenEnum.RETIRO_MERCADO);
            loteVentaOrigen.setEstado(EstadoEnum.EN_USO);
            loteVentaOrigen.setCantidadInicial(new BigDecimal("100"));
            loteVentaOrigen.setCantidadActual(new BigDecimal("50"));

            Bulto bultoVentaOrigen = new Bulto();
            bultoVentaOrigen.setId(10L);
            bultoVentaOrigen.setNroBulto(1);
            bultoVentaOrigen.setCantidadInicial(new BigDecimal("100"));
            bultoVentaOrigen.setCantidadActual(new BigDecimal("50"));
            bultoVentaOrigen.setEstado(EstadoEnum.EN_USO);
            loteVentaOrigen.setBultos(List.of(bultoVentaOrigen));

            lote.setLoteOrigen(loteVentaOrigen);

            // Setup movimiento venta origen
            movimientoVentaOrigen = new Movimiento();
            movimientoVentaOrigen.setId(10L);
            movimientoVentaOrigen.setCodigoMovimiento("MOV-VENTA-001");
            movimientoVentaOrigen.setActivo(true);
            movimientoVentaOrigen.setTipoMovimiento(TipoMovimientoEnum.BAJA);
            movimientoVentaOrigen.setLote(loteVentaOrigen);

            movimientoOrigen.setMovimientoOrigen(movimientoVentaOrigen);

            // Setup movimiento modificación recall
            movimientoModificacionRecall = new Movimiento();
            movimientoModificacionRecall.setId(11L);
            movimientoModificacionRecall.setCodigoMovimiento("MOV-MODIF-RECALL-001");
            movimientoModificacionRecall.setActivo(true);
            movimientoModificacionRecall.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
            movimientoModificacionRecall.setLote(loteVentaOrigen);
            movimientoModificacionRecall.setDictamenInicial(DictamenEnum.APROBADO);
            movimientoModificacionRecall.setDictamenFinal(DictamenEnum.RETIRO_MERCADO);
        }

        @Test
        @DisplayName("debe reversar retiro mercado con un solo movimiento - cubre líneas 102-107, 147-158 (size==1)")
        void reversarRetiroMercado_conUnSoloMovimiento_debeReversarModificacion() {
            // Given
            when(movimientoRepository.findByMovimientoOrigen("MOV-VENTA-001"))
                    .thenReturn(List.of(movimientoModificacionRecall));

            // When
            LoteDTO resultado = service.reversarRetiroMercado(dto, movimientoOrigen, currentUser);

            // Then
            assertThat(resultado).isNotNull();

            // Verify reverso alta was called (through method)
            assertFalse(movimientoOrigen.getActivo());
            assertFalse(lote.getActivo());

            // Verify modification reverso was called
            verify(movimientoRepository).findByMovimientoOrigen("MOV-VENTA-001");
            verify(loteRepository, atLeastOnce()).save(any(Lote.class));
        }

        @Test
        @DisplayName("debe reversar retiro mercado con múltiples movimientos - cubre líneas 155-157 (else)")
        void reversarRetiroMercado_conMultiplesMovimientos_noDebeReversarModificacion() {
            // Given
            Movimiento otroMovimiento = new Movimiento();
            otroMovimiento.setId(12L);
            otroMovimiento.setCodigoMovimiento("MOV-OTRO-001");

            when(movimientoRepository.findByMovimientoOrigen("MOV-VENTA-001"))
                    .thenReturn(List.of(movimientoModificacionRecall, otroMovimiento));

            // When
            LoteDTO resultado = service.reversarRetiroMercado(dto, movimientoOrigen, currentUser);

            // Then
            assertThat(resultado).isNotNull();

            // Verify only reverso alta was called, not modification reverso
            verify(movimientoRepository).findByMovimientoOrigen("MOV-VENTA-001");

            // Should return the lote from movimientoOrigen (not process modification)
            dtoUtilsMock.verify(() -> DTOUtils.fromLoteEntity(lote), atLeastOnce());
        }
    }

    @Nested
    @DisplayName("reversarAltaRecall Tests")
    class ReversarAltaRecallTests {

        private Lote loteVentaOrigen;

        @BeforeEach
        void setUpRecall() {
            movimientoUtilsMock.when(() -> createMovimientoReverso(any(), any(), any()))
                    .thenReturn(movimientoReverso);
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class)))
                    .thenReturn(loteDTO);

            loteVentaOrigen = new Lote();
            loteVentaOrigen.setId(2L);
            loteVentaOrigen.setActivo(true);
            loteVentaOrigen.setTrazado(false);

            Bulto bultoVentaOrigen = new Bulto();
            bultoVentaOrigen.setId(10L);
            bultoVentaOrigen.setNroBulto(1);
            bultoVentaOrigen.setActivo(true);
            bultoVentaOrigen.setLote(loteVentaOrigen);
            loteVentaOrigen.setBultos(new ArrayList<>(List.of(bultoVentaOrigen)));

            lote.setLoteOrigen(loteVentaOrigen);
        }

        @Test
        @DisplayName("debe reversar alta recall sin trazado - cubre líneas 112-142")
        void reversarAltaRecall_sinTrazado_debeReversarCorrectamente() {
            // Given
            loteVentaOrigen.setTrazado(false);

            // When
            service.reversarAltaRecall(dto, movimientoOrigen, currentUser);

            // Then
            assertFalse(movimientoOrigen.getActivo());
            assertFalse(movimientoReverso.getActivo());
            assertFalse(lote.getActivo());

            verify(bultoRepository).saveAll(anyCollection());
            verify(movimientoRepository, times(2)).save(any(Movimiento.class));
            verify(loteRepository).save(lote);
            verify(trazaRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("debe reversar alta recall con trazado - cubre líneas 125-136")
        void reversarAltaRecall_conTrazado_debeRestaurarTrazas() {
            // Given
            loteVentaOrigen.setTrazado(true);

            Traza traza1 = new Traza();
            traza1.setId(1L);
            traza1.setNroTraza(1L);
            traza1.setEstado(EstadoEnum.RECALL);

            Set<Traza> trazas = new HashSet<>(List.of(traza1));
            detalles.get(0).setTrazas(trazas);

            // When
            service.reversarAltaRecall(dto, movimientoOrigen, currentUser);

            // Then
            assertThat(traza1.getEstado()).isEqualTo(EstadoEnum.VENDIDO);
            assertThat(traza1.getLote()).isEqualTo(loteVentaOrigen);
            verify(trazaRepository).saveAll(any());
        }
    }

    @Nested
    @DisplayName("reversarModificacionRecallInterno Tests")
    class ReversarModificacionRecallInternoTests {

        private Movimiento movimientoModificacion;
        private Lote loteOrigen;

        @BeforeEach
        void setUpModificacionRecall() {
            movimientoUtilsMock.when(() -> createMovimientoReverso(any(), any(), any()))
                    .thenReturn(movimientoReverso);
            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class)))
                    .thenReturn(loteDTO);

            loteOrigen = new Lote();
            loteOrigen.setId(2L);
            loteOrigen.setActivo(true);
            loteOrigen.setTrazado(false);
            loteOrigen.setDictamen(DictamenEnum.RETIRO_MERCADO);
            loteOrigen.setEstado(EstadoEnum.EN_USO);
            loteOrigen.setCantidadInicial(new BigDecimal("100"));
            loteOrigen.setCantidadActual(new BigDecimal("50"));

            Bulto bulto1 = new Bulto();
            bulto1.setId(10L);
            bulto1.setNroBulto(1);
            bulto1.setCantidadInicial(new BigDecimal("100"));
            bulto1.setCantidadActual(new BigDecimal("50"));
            bulto1.setEstado(EstadoEnum.EN_USO);
            loteOrigen.setBultos(List.of(bulto1));

            movimientoModificacion = new Movimiento();
            movimientoModificacion.setId(10L);
            movimientoModificacion.setCodigoMovimiento("MOV-MODIF-001");
            movimientoModificacion.setActivo(true);
            movimientoModificacion.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
            movimientoModificacion.setLote(loteOrigen);
            movimientoModificacion.setDictamenInicial(DictamenEnum.APROBADO);
            movimientoModificacion.setDictamenFinal(DictamenEnum.RETIRO_MERCADO);
        }

        @Test
        @DisplayName("debe lanzar excepción si movimiento no es MODIFICACION - cubre líneas 164-166")
        void reversarModificacionRecallInterno_movimientoNoEsModificacion_debeLanzarExcepcion() {
            // Given
            movimientoModificacion.setTipoMovimiento(TipoMovimientoEnum.ALTA);

            // When & Then
            assertThatThrownBy(() -> service.reversarModificacionRecallInterno(dto, movimientoModificacion, currentUser))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("El movimiento de venta asociado al recall no es de modificacion.");
        }

        @Test
        @DisplayName("debe lanzar excepción si lote es null - cubre líneas 173-175")
        void reversarModificacionRecallInterno_loteNull_debeLanzarExcepcion() {
            // Given
            movimientoModificacion.setLote(null);

            // When & Then
            assertThatThrownBy(() -> service.reversarModificacionRecallInterno(dto, movimientoModificacion, currentUser))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("No se encontraron movimientos para reversar.");
        }

        @Test
        @DisplayName("debe reversar modificación recall sin trazado - cubre líneas 163-207 (sin trazado)")
        void reversarModificacionRecallInterno_sinTrazado_debeReversarCorrectamente() {
            // Given
            loteOrigen.setTrazado(false);

            // When
            LoteDTO resultado = service.reversarModificacionRecallInterno(dto, movimientoModificacion, currentUser);

            // Then
            assertThat(resultado).isNotNull();
            assertFalse(movimientoModificacion.getActivo());
            assertFalse(movimientoReverso.getActivo());
            assertThat(loteOrigen.getDictamen()).isEqualTo(DictamenEnum.APROBADO); // Restored

            verify(bultoRepository).saveAll(loteOrigen.getBultos());
            verify(movimientoRepository).save(movimientoReverso);
            verify(loteRepository).save(loteOrigen);
            verify(trazaRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("debe reversar modificación recall con trazas en RECALL - cubre líneas 187-194")
        void reversarModificacionRecallInterno_conTrazasRECALL_debeRestaurarADISPONIBLE() {
            // Given
            loteOrigen.setTrazado(true);

            Traza traza1 = new Traza();
            traza1.setId(1L);
            traza1.setNroTraza(1L);
            traza1.setEstado(EstadoEnum.RECALL);
            traza1.setActivo(true);

            Traza traza2 = new Traza();
            traza2.setId(2L);
            traza2.setNroTraza(2L);
            traza2.setEstado(EstadoEnum.RECALL);
            traza2.setActivo(true);

            Traza traza3 = new Traza();
            traza3.setId(3L);
            traza3.setNroTraza(3L);
            traza3.setEstado(EstadoEnum.VENDIDO); // Not RECALL
            traza3.setActivo(true);

            loteOrigen.setTrazas(new HashSet<>(List.of(traza1, traza2, traza3)));

            // When
            service.reversarModificacionRecallInterno(dto, movimientoModificacion, currentUser);

            // Then
            assertThat(traza1.getEstado()).isEqualTo(EstadoEnum.DISPONIBLE);
            assertThat(traza2.getEstado()).isEqualTo(EstadoEnum.DISPONIBLE);
            assertThat(traza3.getEstado()).isEqualTo(EstadoEnum.VENDIDO); // Unchanged
            verify(trazaRepository).saveAll(any());
        }

        @Test
        @DisplayName("debe restaurar análisis cancelado - cubre líneas 200-204")
        void reversarModificacionRecallInterno_conAnalisisCancelado_debeRestaurar() {
            // Given
            Analisis analisis = new Analisis();
            analisis.setId(1L);
            analisis.setDictamen(DictamenEnum.CANCELADO);
            analisis.setLote(loteOrigen);
            analisis.setActivo(true);
            analisis.setFechaYHoraCreacion(java.time.OffsetDateTime.now());
            loteOrigen.getAnalisisList().add(analisis);

            when(analisisRepository.save(any(Analisis.class))).thenAnswer(i -> i.getArgument(0));

            // When
            service.reversarModificacionRecallInterno(dto, movimientoModificacion, currentUser);

            // Then
            assertNull(analisis.getDictamen());
            verify(analisisRepository).save(analisis);
        }

        @Test
        @DisplayName("no debe restaurar análisis si no está cancelado - cubre líneas 200-204 (else)")
        void reversarModificacionRecallInterno_conAnalisisNoCANCELADO_noDebeRestaurar() {
            // Given
            Analisis analisis = new Analisis();
            analisis.setId(1L);
            analisis.setDictamen(DictamenEnum.APROBADO);
            analisis.setLote(loteOrigen);
            analisis.setActivo(true);
            analisis.setFechaYHoraCreacion(java.time.OffsetDateTime.now());
            loteOrigen.getAnalisisList().add(analisis);

            // When
            service.reversarModificacionRecallInterno(dto, movimientoModificacion, currentUser);

            // Then
            assertThat(analisis.getDictamen()).isEqualTo(DictamenEnum.APROBADO); // Unchanged
            verify(analisisRepository, never()).save(any(Analisis.class));
        }

        @Test
        @DisplayName("no debe restaurar análisis si es null - cubre líneas 200 (null check)")
        void reversarModificacionRecallInterno_conAnalisisNull_noDebeRestaurar() {
            // Given
            // loteOrigen has empty analisisList by default

            // When
            service.reversarModificacionRecallInterno(dto, movimientoModificacion, currentUser);

            // Then
            verify(analisisRepository, never()).save(any(Analisis.class));
        }
    }

    @Nested
    @DisplayName("restaurarEstadoBulto Tests")
    class RestaurarEstadoBultoTests {

        @Test
        @DisplayName("debe setear NUEVO cuando cantidad actual == inicial - cubre líneas 213-214")
        void restaurarEstadoBulto_cantidadIgualInicial_debeSetearNUEVO() {
            // Given
            Bulto bulto = new Bulto();
            bulto.setCantidadInicial(new BigDecimal("100"));
            bulto.setCantidadActual(new BigDecimal("100"));
            bulto.setEstado(EstadoEnum.EN_USO);

            // When
            service.restaurarEstadoBulto(bulto);

            // Then
            assertThat(bulto.getEstado()).isEqualTo(EstadoEnum.NUEVO);
        }

        @Test
        @DisplayName("debe setear CONSUMIDO cuando cantidad actual == 0 - cubre líneas 216-217")
        void restaurarEstadoBulto_cantidadCero_debeSetearCONSUMIDO() {
            // Given
            Bulto bulto = new Bulto();
            bulto.setCantidadInicial(new BigDecimal("100"));
            bulto.setCantidadActual(BigDecimal.ZERO);
            bulto.setEstado(EstadoEnum.EN_USO);

            // When
            service.restaurarEstadoBulto(bulto);

            // Then
            assertThat(bulto.getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);
        }

        @Test
        @DisplayName("debe setear EN_USO cuando cantidad está entre 0 e inicial - cubre líneas 219")
        void restaurarEstadoBulto_cantidadEntre_debeSetearEN_USO() {
            // Given
            Bulto bulto = new Bulto();
            bulto.setCantidadInicial(new BigDecimal("100"));
            bulto.setCantidadActual(new BigDecimal("50"));
            bulto.setEstado(EstadoEnum.NUEVO);

            // When
            service.restaurarEstadoBulto(bulto);

            // Then
            assertThat(bulto.getEstado()).isEqualTo(EstadoEnum.EN_USO);
        }
    }

    @Nested
    @DisplayName("restaurarEstadoLote Tests")
    class RestaurarEstadoLoteTests {

        @Test
        @DisplayName("debe setear NUEVO cuando cantidad actual == inicial - cubre líneas 228-229")
        void restaurarEstadoLote_cantidadIgualInicial_debeSetearNUEVO() {
            // Given
            Lote lote = new Lote();
            lote.setCantidadInicial(new BigDecimal("100"));
            lote.setCantidadActual(new BigDecimal("100"));
            lote.setEstado(EstadoEnum.EN_USO);

            // When
            service.restaurarEstadoLote(lote);

            // Then
            assertThat(lote.getEstado()).isEqualTo(EstadoEnum.NUEVO);
        }

        @Test
        @DisplayName("debe setear CONSUMIDO cuando cantidad actual == 0 - cubre líneas 231-232")
        void restaurarEstadoLote_cantidadCero_debeSetearCONSUMIDO() {
            // Given
            Lote lote = new Lote();
            lote.setCantidadInicial(new BigDecimal("100"));
            lote.setCantidadActual(BigDecimal.ZERO);
            lote.setEstado(EstadoEnum.EN_USO);

            // When
            service.restaurarEstadoLote(lote);

            // Then
            assertThat(lote.getEstado()).isEqualTo(EstadoEnum.CONSUMIDO);
        }

        @Test
        @DisplayName("debe setear EN_USO cuando cantidad está entre 0 e inicial - cubre líneas 234")
        void restaurarEstadoLote_cantidadEntre_debeSetearEN_USO() {
            // Given
            Lote lote = new Lote();
            lote.setCantidadInicial(new BigDecimal("100"));
            lote.setCantidadActual(new BigDecimal("50"));
            lote.setEstado(EstadoEnum.NUEVO);

            // When
            service.restaurarEstadoLote(lote);

            // Then
            assertThat(lote.getEstado()).isEqualTo(EstadoEnum.EN_USO);
        }
    }
}

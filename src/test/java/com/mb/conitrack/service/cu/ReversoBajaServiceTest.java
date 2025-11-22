package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.*;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.AnalisisRepository;
import com.mb.conitrack.repository.BultoRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.TrazaRepository;
import com.mb.conitrack.utils.MovimientoModificacionUtils;
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

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReversoBajaService - Tests")
class ReversoBajaServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private AnalisisRepository analisisRepository;

    @Mock
    private BultoRepository bultoRepository;

    @Mock
    private TrazaRepository trazaRepository;

    @InjectMocks
    private ReversoBajaService service;

    private MockedStatic<MovimientoModificacionUtils> movimientoUtilsMock;
    private MockedStatic<DTOUtils> dtoUtilsMock;
    private MockedStatic<UnidadMedidaUtils> unidadMedidaUtilsMock;

    private MovimientoDTO movimientoDTO;
    private Movimiento movimientoOrigen;
    private Movimiento movimientoReverso;
    private Lote lote;
    private Bulto bulto1;
    private Bulto bulto2;
    private DetalleMovimiento detalle1;
    private DetalleMovimiento detalle2;
    private Analisis analisis;
    private User currentUser;
    private LoteDTO loteDTO;

    @BeforeEach
    void setUp() {
        // Setup MockedStatic
        movimientoUtilsMock = mockStatic(MovimientoModificacionUtils.class);
        dtoUtilsMock = mockStatic(DTOUtils.class);
        unidadMedidaUtilsMock = mockStatic(UnidadMedidaUtils.class);

        // Setup user
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");

        // Setup lote
        lote = new Lote();
        lote.setId(1L);
        lote.setCodigoLote("LOTE-001");
        lote.setCantidadInicial(new BigDecimal("100.0000"));
        lote.setCantidadActual(new BigDecimal("80.0000"));
        lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        lote.setEstado(EstadoEnum.EN_USO);
        lote.setDictamen(DictamenEnum.APROBADO);
        lote.setTrazado(false);

        // Setup análisis
        analisis = new Analisis();
        analisis.setId(1L);
        analisis.setNroAnalisis("AN-001");
        analisis.setDictamen(DictamenEnum.CANCELADO);
        analisis.setLote(lote);
        analisis.setActivo(true);

        List<Analisis> analisisList = new ArrayList<>();
        analisisList.add(analisis);
        lote.setAnalisisList(analisisList);

        // Setup bultos
        bulto1 = new Bulto();
        bulto1.setId(1L);
        bulto1.setNroBulto(1);
        bulto1.setLote(lote);
        bulto1.setCantidadInicial(new BigDecimal("50.0000"));
        bulto1.setCantidadActual(new BigDecimal("40.0000"));
        bulto1.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        bulto1.setEstado(EstadoEnum.EN_USO);
        bulto1.setActivo(true);

        bulto2 = new Bulto();
        bulto2.setId(2L);
        bulto2.setNroBulto(2);
        bulto2.setLote(lote);
        bulto2.setCantidadInicial(new BigDecimal("50.0000"));
        bulto2.setCantidadActual(new BigDecimal("40.0000"));
        bulto2.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        bulto2.setEstado(EstadoEnum.EN_USO);
        bulto2.setActivo(true);

        // Setup detalles
        detalle1 = new DetalleMovimiento();
        detalle1.setId(1L);
        detalle1.setBulto(bulto1);
        detalle1.setCantidad(new BigDecimal("10.0000"));
        detalle1.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        detalle1.setActivo(true);
        detalle1.setTrazas(new HashSet<>());

        detalle2 = new DetalleMovimiento();
        detalle2.setId(2L);
        detalle2.setBulto(bulto2);
        detalle2.setCantidad(new BigDecimal("10.0000"));
        detalle2.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        detalle2.setActivo(true);
        detalle2.setTrazas(new HashSet<>());

        // Setup movimiento origen
        movimientoOrigen = new Movimiento();
        movimientoOrigen.setId(1L);
        movimientoOrigen.setCodigoMovimiento("MOV-001");
        movimientoOrigen.setLote(lote);
        movimientoOrigen.setCantidad(new BigDecimal("20.0000"));
        movimientoOrigen.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        movimientoOrigen.setActivo(true);
        movimientoOrigen.setDetalles(new HashSet<>(Arrays.asList(detalle1, detalle2)));

        // Setup movimiento reverso
        movimientoReverso = new Movimiento();
        movimientoReverso.setId(2L);
        movimientoReverso.setCodigoMovimiento("MOV-REV-001");
        movimientoReverso.setLote(lote);
        movimientoReverso.setMovimientoOrigen(movimientoOrigen);
        movimientoReverso.setActivo(true);

        // Setup MovimientoDTO
        movimientoDTO = new MovimientoDTO();
        movimientoDTO.setCodigoLote("LOTE-001");
        movimientoDTO.setObservaciones("Reverso de prueba");
        movimientoDTO.setCantidad(new BigDecimal("20.0000"));
        movimientoDTO.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);

        // Setup LoteDTO
        loteDTO = new LoteDTO();
        loteDTO.setCodigoLote("LOTE-001");
    }

    @AfterEach
    void tearDown() {
        if (movimientoUtilsMock != null) {
            movimientoUtilsMock.close();
        }
        if (dtoUtilsMock != null) {
            dtoUtilsMock.close();
        }
        if (unidadMedidaUtilsMock != null) {
            unidadMedidaUtilsMock.close();
        }
    }

    @Nested
    @DisplayName("reversarBajaDevolucionCompra() - Tests")
    class ReversarBajaDevolucionCompraTests {

        @Test
        @DisplayName("Debe delegar a reversarBajaGranel exitosamente - cubre líneas 35-37")
        void reversarBajaDevolucionCompra_datosValidos_debeDelegar() {
            // Given
            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("50.0000"));

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("100.0000"));

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaDevolucionCompra(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);
            assertThat(resultado.getCodigoLote()).isEqualTo("LOTE-001");

            // Verifica que se ejecutó la lógica de granel
            verify(bultoRepository, times(2)).save(any(Bulto.class));
            verify(loteRepository).save(lote);
        }
    }

    @Nested
    @DisplayName("reversarBajaConsumoProduccion() - Tests")
    class ReversarBajaConsumoProduccionTests {

        @Test
        @DisplayName("Debe delegar a reversarBajaGranel exitosamente - cubre líneas 40-42")
        void reversarBajaConsumoProduccion_datosValidos_debeDelegar() {
            // Given
            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("50.0000"));

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("100.0000"));

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaConsumoProduccion(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);
            assertThat(resultado.getCodigoLote()).isEqualTo("LOTE-001");

            // Verifica que se ejecutó la lógica de granel
            verify(bultoRepository, times(2)).save(any(Bulto.class));
            verify(loteRepository).save(lote);
        }
    }

    @Nested
    @DisplayName("reversarBajaGranel() - Tests")
    class ReversarBajaGranelTests {

        @Test
        @DisplayName("Debe reversar baja granel con bultos restaurados a NUEVO - cubre líneas 45-88 (líneas 57-58)")
        void reversarBajaGranel_bultosRestauradosANuevo_debeReversarExitosamente() {
            // Given
            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("50.0000")); // Restaura a cantidad inicial

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("100.0000")); // Restaura a cantidad inicial

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(analisisRepository.save(any(Analisis.class))).thenReturn(analisis);
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaGranel(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que los bultos fueron restaurados a NUEVO
            assertThat(bulto1.getEstado()).isEqualTo(EstadoEnum.NUEVO);
            assertThat(bulto2.getEstado()).isEqualTo(EstadoEnum.NUEVO);

            // Verifica que el lote fue restaurado a NUEVO
            assertThat(lote.getEstado()).isEqualTo(EstadoEnum.NUEVO);

            // Verifica que el análisis CANCELADO fue restaurado
            assertNull(analisis.getDictamen());

            // Verifica que se marcaron como inactivos
            assertFalse(movimientoOrigen.getActivo());
            assertFalse(movimientoReverso.getActivo());

            // Verifica que se guardaron las entidades
            verify(bultoRepository, times(2)).save(any(Bulto.class));
            verify(analisisRepository).save(analisis);
            verify(movimientoRepository, times(2)).save(any(Movimiento.class));
            verify(loteRepository).save(lote);
        }

        @Test
        @DisplayName("Debe reversar baja granel con bultos mantenidos EN_USO - cubre líneas 45-88 (líneas 59-61)")
        void reversarBajaGranel_bultosEnUso_debeReversarExitosamente() {
            // Given
            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("45.0000")); // No restaura a cantidad inicial

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("90.0000")); // No restaura a cantidad inicial

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            lote.setAnalisisList(new ArrayList<>()); // Sin análisis para cubrir línea 77

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaGranel(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que los bultos se mantienen EN_USO
            assertThat(bulto1.getEstado()).isEqualTo(EstadoEnum.EN_USO);
            assertThat(bulto2.getEstado()).isEqualTo(EstadoEnum.EN_USO);

            // Verifica que el lote se mantiene EN_USO
            assertThat(lote.getEstado()).isEqualTo(EstadoEnum.EN_USO);

            // Verifica que no se guardó análisis (era null)
            verify(analisisRepository, never()).save(any());

            // Verifica que se marcaron como inactivos
            assertFalse(movimientoOrigen.getActivo());
            assertFalse(movimientoReverso.getActivo());

            verify(bultoRepository, times(2)).save(any(Bulto.class));
            verify(movimientoRepository, times(2)).save(any(Movimiento.class));
            verify(loteRepository).save(lote);
        }

        @Test
        @DisplayName("Debe reversar sin restaurar análisis cuando dictamen no es CANCELADO - cubre líneas 77-80")
        void reversarBajaGranel_analisisNoCANCELADO_noDebeRestaurar() {
            // Given
            analisis.setDictamen(DictamenEnum.APROBADO); // No CANCELADO

            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("50.0000"));

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("100.0000"));

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaGranel(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que el análisis NO fue restaurado (mantiene APROBADO)
            assertThat(analisis.getDictamen()).isEqualTo(DictamenEnum.APROBADO);

            // Verifica que no se guardó análisis
            verify(analisisRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("reversarBajaMuestreoBulto() - Tests")
    class ReversarBajaMuestreoBultoTests {

        @Test
        @DisplayName("Debe reversar muestreo con múltiples detalles - cubre líneas 91-159 (líneas 98-120)")
        void reversarBajaMuestreoBulto_multipleDetalles_debeReversarExitosamente() {
            // Given - 2 detalles (size > 1)
            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("50.0000"));

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("100.0000"));

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaMuestreoBulto(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que los bultos fueron restaurados a NUEVO
            assertThat(bulto1.getEstado()).isEqualTo(EstadoEnum.NUEVO);
            assertThat(bulto2.getEstado()).isEqualTo(EstadoEnum.NUEVO);

            // Verifica que el lote fue restaurado a NUEVO
            assertThat(lote.getEstado()).isEqualTo(EstadoEnum.NUEVO);

            // Verifica que se marcaron como inactivos
            assertFalse(movimientoOrigen.getActivo());
            assertFalse(movimientoReverso.getActivo());

            // Verifica que se guardaron las entidades
            verify(bultoRepository, times(2)).save(any(Bulto.class));
            verify(movimientoRepository, times(2)).save(any(Movimiento.class));
            verify(loteRepository).save(lote);
            verify(trazaRepository, never()).saveAll(anyCollection()); // No se guardan trazas en este camino
        }

        @Test
        @DisplayName("Debe reversar muestreo con múltiples detalles manteniendo EN_USO - cubre líneas 105-109")
        void reversarBajaMuestreoBulto_multipleDetallesEnUso_debeReversarExitosamente() {
            // Given
            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("45.0000")); // No restaura a cantidad inicial

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("90.0000")); // No restaura a cantidad inicial

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaMuestreoBulto(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que los bultos se mantienen EN_USO
            assertThat(bulto1.getEstado()).isEqualTo(EstadoEnum.EN_USO);
            assertThat(bulto2.getEstado()).isEqualTo(EstadoEnum.EN_USO);

            // Verifica que el lote se mantiene EN_USO
            assertThat(lote.getEstado()).isEqualTo(EstadoEnum.EN_USO);

            verify(bultoRepository, times(2)).save(any(Bulto.class));
            verify(loteRepository).save(lote);
        }

        @Test
        @DisplayName("Debe reversar muestreo con un detalle con trazas - cubre líneas 124-153")
        void reversarBajaMuestreoBulto_unDetalleConTrazas_debeReversarExitosamente() {
            // Given - Solo 1 detalle con trazas
            Traza traza1 = new Traza();
            traza1.setId(1L);
            traza1.setNroTraza(1L);
            traza1.setEstado(EstadoEnum.CONSUMIDO);
            traza1.setActivo(true);

            Traza traza2 = new Traza();
            traza2.setId(2L);
            traza2.setNroTraza(2L);
            traza2.setEstado(EstadoEnum.CONSUMIDO);
            traza2.setActivo(true);

            detalle1.setTrazas(new HashSet<>(Arrays.asList(traza1, traza2)));

            movimientoOrigen.setDetalles(new HashSet<>(Collections.singletonList(detalle1))); // Solo 1 detalle

            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("50.0000"));

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("100.0000"));

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(trazaRepository.saveAll(anyCollection())).thenReturn(new ArrayList<>());
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaMuestreoBulto(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que las trazas fueron restauradas a DISPONIBLE
            assertThat(traza1.getEstado()).isEqualTo(EstadoEnum.DISPONIBLE);
            assertThat(traza2.getEstado()).isEqualTo(EstadoEnum.DISPONIBLE);

            // Verifica que el bulto fue restaurado a NUEVO
            assertThat(bulto1.getEstado()).isEqualTo(EstadoEnum.NUEVO);

            // Verifica que el lote fue restaurado a NUEVO
            assertThat(lote.getEstado()).isEqualTo(EstadoEnum.NUEVO);

            // Verifica que se marcaron como inactivos
            assertFalse(movimientoOrigen.getActivo());
            assertFalse(movimientoReverso.getActivo());

            // Verifica que se guardaron las entidades
            verify(trazaRepository).saveAll(anyCollection());
            verify(bultoRepository).save(bulto1);
            verify(movimientoRepository, times(2)).save(any(Movimiento.class));
            verify(loteRepository).save(lote);
        }

        @Test
        @DisplayName("Debe reversar muestreo con un detalle con trazas manteniendo EN_USO - cubre líneas 134-138, 142-146")
        void reversarBajaMuestreoBulto_unDetalleConTrazasEnUso_debeReversarExitosamente() {
            // Given
            Traza traza1 = new Traza();
            traza1.setId(1L);
            traza1.setNroTraza(1L);
            traza1.setEstado(EstadoEnum.CONSUMIDO);
            traza1.setActivo(true);

            detalle1.setTrazas(new HashSet<>(Collections.singletonList(traza1)));
            movimientoOrigen.setDetalles(new HashSet<>(Collections.singletonList(detalle1)));

            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("45.0000")); // No restaura a cantidad inicial

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("90.0000")); // No restaura a cantidad inicial

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(trazaRepository.saveAll(anyCollection())).thenReturn(new ArrayList<>());
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaMuestreoBulto(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que las trazas fueron restauradas a DISPONIBLE
            assertThat(traza1.getEstado()).isEqualTo(EstadoEnum.DISPONIBLE);

            // Verifica que el bulto se mantiene EN_USO
            assertThat(bulto1.getEstado()).isEqualTo(EstadoEnum.EN_USO);

            // Verifica que el lote se mantiene EN_USO
            assertThat(lote.getEstado()).isEqualTo(EstadoEnum.EN_USO);

            verify(trazaRepository).saveAll(anyCollection());
            verify(bultoRepository).save(bulto1);
            verify(loteRepository).save(lote);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando no hay detalles - cubre líneas 124-126")
        void reversarBajaMuestreoBulto_sinDetalles_debeLanzarExcepcion() {
            // Given - Sin detalles
            movimientoOrigen.setDetalles(new HashSet<>());

            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            // When & Then
            assertThatThrownBy(() -> service.reversarBajaMuestreoBulto(movimientoDTO, movimientoOrigen, currentUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El detalle del movimiento a reversar no existe.");

            // Verifica que no se guardó nada
            verify(bultoRepository, never()).save(any());
            verify(trazaRepository, never()).saveAll(anyCollection());
            verify(loteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("reversarBajaVentaProducto() - Tests")
    class ReversarBajaVentaProductoTests {

        @Test
        @DisplayName("Debe reversar venta de producto exitosamente - cubre líneas 162-216")
        void reversarBajaVentaProducto_datosValidos_debeReversarExitosamente() {
            // Given
            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            when(loteRepository.findLotesByLoteOrigen(anyString())).thenReturn(Collections.emptyList());

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("50.0000"));

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("100.0000"));

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(analisisRepository.save(any(Analisis.class))).thenReturn(analisis);
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaVentaProducto(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que los bultos fueron restaurados a NUEVO
            assertThat(bulto1.getEstado()).isEqualTo(EstadoEnum.NUEVO);
            assertThat(bulto2.getEstado()).isEqualTo(EstadoEnum.NUEVO);

            // Verifica que el lote fue restaurado a NUEVO
            assertThat(lote.getEstado()).isEqualTo(EstadoEnum.NUEVO);

            // Verifica que el análisis CANCELADO fue restaurado
            assertNull(analisis.getDictamen());

            // Verifica que los detalles fueron marcados como inactivos
            assertFalse(detalle1.getActivo());
            assertFalse(detalle2.getActivo());

            // Verifica que se marcaron como inactivos
            assertFalse(movimientoOrigen.getActivo());
            assertFalse(movimientoReverso.getActivo());

            // Verifica que se guardaron las entidades
            verify(loteRepository).findLotesByLoteOrigen("LOTE-001");
            verify(bultoRepository, times(2)).save(any(Bulto.class));
            verify(analisisRepository).save(analisis);
            verify(movimientoRepository, times(2)).save(any(Movimiento.class));
            verify(loteRepository).save(lote);
        }

        @Test
        @DisplayName("Debe reversar venta con trazas y restaurarlas a DISPONIBLE - cubre líneas 186-189")
        void reversarBajaVentaProducto_conTrazas_debeRestaurarTrazas() {
            // Given
            lote.setTrazado(true);

            Traza traza1 = new Traza();
            traza1.setId(1L);
            traza1.setNroTraza(1L);
            traza1.setEstado(EstadoEnum.VENDIDO);
            traza1.setActivo(true);

            Traza traza2 = new Traza();
            traza2.setId(2L);
            traza2.setNroTraza(2L);
            traza2.setEstado(EstadoEnum.VENDIDO);
            traza2.setActivo(true);

            detalle1.setTrazas(new HashSet<>(Collections.singletonList(traza1)));
            detalle2.setTrazas(new HashSet<>(Collections.singletonList(traza2)));

            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            when(loteRepository.findLotesByLoteOrigen(anyString())).thenReturn(Collections.emptyList());

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("50.0000"));

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("100.0000"));

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(trazaRepository.saveAll(anyCollection())).thenReturn(new ArrayList<>());
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            lote.setAnalisisList(new ArrayList<>()); // Sin análisis para no cubrir líneas 204-207

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaVentaProducto(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que las trazas fueron restauradas a DISPONIBLE
            assertThat(traza1.getEstado()).isEqualTo(EstadoEnum.DISPONIBLE);
            assertThat(traza2.getEstado()).isEqualTo(EstadoEnum.DISPONIBLE);

            // Verifica que se guardaron las trazas
            verify(trazaRepository, times(2)).saveAll(anyCollection());
        }

        @Test
        @DisplayName("Debe reversar venta manteniendo EN_USO - cubre líneas 197-201")
        void reversarBajaVentaProducto_enUso_debeMantenerse() {
            // Given
            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            when(loteRepository.findLotesByLoteOrigen(anyString())).thenReturn(Collections.emptyList());

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("45.0000")); // No restaura a cantidad inicial

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("90.0000")); // No restaura a cantidad inicial

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            lote.setAnalisisList(new ArrayList<>());

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaVentaProducto(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que los bultos se mantienen EN_USO
            assertThat(bulto1.getEstado()).isEqualTo(EstadoEnum.EN_USO);
            assertThat(bulto2.getEstado()).isEqualTo(EstadoEnum.EN_USO);

            // Verifica que el lote se mantiene EN_USO
            assertThat(lote.getEstado()).isEqualTo(EstadoEnum.EN_USO);

            verify(bultoRepository, times(2)).save(any(Bulto.class));
            verify(loteRepository).save(lote);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando existen lotes derivados - cubre líneas 165-169")
        void reversarBajaVentaProducto_lotesDerivadosExisten_debeLanzarExcepcion() {
            // Given
            Lote loteDerivado = new Lote();
            loteDerivado.setCodigoLote("LOTE-DEV-001");

            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            when(loteRepository.findLotesByLoteOrigen(anyString())).thenReturn(Collections.singletonList(loteDerivado));

            // When & Then
            assertThatThrownBy(() -> service.reversarBajaVentaProducto(movimientoDTO, movimientoOrigen, currentUser))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("El lote origen tiene una devolucion asociada, no se puede reversar el movimiento.");

            // Verifica que no se guardó nada
            verify(bultoRepository, never()).save(any());
            verify(loteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe no restaurar análisis cuando dictamen no es CANCELADO - cubre líneas 204-207")
        void reversarBajaVentaProducto_analisisNoCANCELADO_noDebeRestaurar() {
            // Given
            analisis.setDictamen(DictamenEnum.APROBADO); // No CANCELADO

            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            when(loteRepository.findLotesByLoteOrigen(anyString())).thenReturn(Collections.emptyList());

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("50.0000"));

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("100.0000"));

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaVentaProducto(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que el análisis NO fue restaurado (mantiene APROBADO)
            assertThat(analisis.getDictamen()).isEqualTo(DictamenEnum.APROBADO);

            // Verifica que no se guardó análisis
            verify(analisisRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("reversarBajaAjuste() - Tests")
    class ReversarBajaAjusteTests {

        @Test
        @DisplayName("Debe reversar ajuste de stock exitosamente - cubre líneas 219-274")
        void reversarBajaAjuste_datosValidos_debeReversarExitosamente() {
            // Given
            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            when(loteRepository.findLotesByLoteOrigen(anyString())).thenReturn(Collections.emptyList());

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("50.0000"));

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("100.0000"));

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(analisisRepository.save(any(Analisis.class))).thenReturn(analisis);
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaAjuste(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que los bultos fueron restaurados a NUEVO
            assertThat(bulto1.getEstado()).isEqualTo(EstadoEnum.NUEVO);
            assertThat(bulto2.getEstado()).isEqualTo(EstadoEnum.NUEVO);

            // Verifica que el lote fue restaurado a NUEVO
            assertThat(lote.getEstado()).isEqualTo(EstadoEnum.NUEVO);

            // Verifica que el análisis CANCELADO fue restaurado
            assertNull(analisis.getDictamen());

            // Verifica que se marcaron como inactivos
            assertFalse(movimientoOrigen.getActivo());
            assertFalse(movimientoReverso.getActivo());

            // Verifica que se guardaron las entidades
            verify(loteRepository).findLotesByLoteOrigen("LOTE-001");
            verify(bultoRepository, times(2)).save(any(Bulto.class));
            verify(analisisRepository).save(analisis);
            verify(movimientoRepository, times(2)).save(any(Movimiento.class));
            verify(loteRepository).save(lote);
        }

        @Test
        @DisplayName("Debe reversar ajuste con trazas y restaurarlas a DISPONIBLE - cubre líneas 244-248")
        void reversarBajaAjuste_conTrazas_debeRestaurarTrazas() {
            // Given
            Traza traza1 = new Traza();
            traza1.setId(1L);
            traza1.setNroTraza(1L);
            traza1.setEstado(EstadoEnum.DESCARTADO);
            traza1.setActivo(true);

            Traza traza2 = new Traza();
            traza2.setId(2L);
            traza2.setNroTraza(2L);
            traza2.setEstado(EstadoEnum.DESCARTADO);
            traza2.setActivo(true);

            detalle1.setTrazas(new HashSet<>(Collections.singletonList(traza1)));
            detalle2.setTrazas(new HashSet<>(Collections.singletonList(traza2)));

            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            when(loteRepository.findLotesByLoteOrigen(anyString())).thenReturn(Collections.emptyList());

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("50.0000"));

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("100.0000"));

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(trazaRepository.saveAll(anyCollection())).thenReturn(new ArrayList<>());
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            lote.setAnalisisList(new ArrayList<>()); // Sin análisis

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaAjuste(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que las trazas fueron restauradas a DISPONIBLE
            assertThat(traza1.getEstado()).isEqualTo(EstadoEnum.DISPONIBLE);
            assertThat(traza2.getEstado()).isEqualTo(EstadoEnum.DISPONIBLE);

            // Verifica que se guardaron las trazas
            verify(trazaRepository, times(2)).saveAll(anyCollection());
        }

        @Test
        @DisplayName("Debe reversar ajuste sin trazas - cubre líneas 244-248 (trazas vacías)")
        void reversarBajaAjuste_sinTrazas_debeReversarExitosamente() {
            // Given - Sin trazas en detalles
            detalle1.setTrazas(new HashSet<>());
            detalle2.setTrazas(new HashSet<>());

            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            when(loteRepository.findLotesByLoteOrigen(anyString())).thenReturn(Collections.emptyList());

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("50.0000"));

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("100.0000"));

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            lote.setAnalisisList(new ArrayList<>());

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaAjuste(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que NO se guardaron trazas (estaban vacías)
            verify(trazaRepository, never()).saveAll(anyCollection());

            // Verifica que se guardaron los bultos
            verify(bultoRepository, times(2)).save(any(Bulto.class));
        }

        @Test
        @DisplayName("Debe reversar ajuste manteniendo EN_USO - cubre líneas 256-260")
        void reversarBajaAjuste_enUso_debeMantenerse() {
            // Given
            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            when(loteRepository.findLotesByLoteOrigen(anyString())).thenReturn(Collections.emptyList());

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("45.0000")); // No restaura a cantidad inicial

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("90.0000")); // No restaura a cantidad inicial

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            lote.setAnalisisList(new ArrayList<>());

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaAjuste(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que los bultos se mantienen EN_USO
            assertThat(bulto1.getEstado()).isEqualTo(EstadoEnum.EN_USO);
            assertThat(bulto2.getEstado()).isEqualTo(EstadoEnum.EN_USO);

            // Verifica que el lote se mantiene EN_USO
            assertThat(lote.getEstado()).isEqualTo(EstadoEnum.EN_USO);

            verify(bultoRepository, times(2)).save(any(Bulto.class));
            verify(loteRepository).save(lote);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando existen lotes derivados - cubre líneas 222-226")
        void reversarBajaAjuste_lotesDerivadosExisten_debeLanzarExcepcion() {
            // Given
            Lote loteDerivado = new Lote();
            loteDerivado.setCodigoLote("LOTE-AJUSTE-001");

            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            when(loteRepository.findLotesByLoteOrigen(anyString())).thenReturn(Collections.singletonList(loteDerivado));

            // When & Then
            assertThatThrownBy(() -> service.reversarBajaAjuste(movimientoDTO, movimientoOrigen, currentUser))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("El lote origen tiene un ajuste asociado, no se puede reversar el movimiento.");

            // Verifica que no se guardó nada
            verify(bultoRepository, never()).save(any());
            verify(loteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe no restaurar análisis cuando dictamen no es CANCELADO - cubre líneas 263-266")
        void reversarBajaAjuste_analisisNoCANCELADO_noDebeRestaurar() {
            // Given
            analisis.setDictamen(DictamenEnum.APROBADO); // No CANCELADO

            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            when(loteRepository.findLotesByLoteOrigen(anyString())).thenReturn(Collections.emptyList());

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Bulto.class)))
                    .thenReturn(new BigDecimal("50.0000"));

            unidadMedidaUtilsMock.when(() -> UnidadMedidaUtils.sumarMovimientoConvertido(any(MovimientoDTO.class), any(Lote.class)))
                    .thenReturn(new BigDecimal("100.0000"));

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarBajaAjuste(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que el análisis NO fue restaurado (mantiene APROBADO)
            assertThat(analisis.getDictamen()).isEqualTo(DictamenEnum.APROBADO);

            // Verifica que no se guardó análisis
            verify(analisisRepository, never()).save(any());
        }
    }
}

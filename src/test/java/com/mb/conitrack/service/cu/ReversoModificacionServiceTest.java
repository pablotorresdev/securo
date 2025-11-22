package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.repository.AnalisisRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.TrazaRepository;
import com.mb.conitrack.utils.MovimientoModificacionUtils;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReversoModificacionService - Tests")
class ReversoModificacionServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private AnalisisRepository analisisRepository;

    @Mock
    private TrazaRepository trazaRepository;

    @InjectMocks
    private ReversoModificacionService service;

    private MockedStatic<MovimientoModificacionUtils> movimientoUtilsMock;
    private MockedStatic<DTOUtils> dtoUtilsMock;

    private MovimientoDTO movimientoDTO;
    private Movimiento movimientoOrigen;
    private Movimiento movimientoReverso;
    private Lote lote;
    private Analisis analisis;
    private User currentUser;
    private LoteDTO loteDTO;

    @BeforeEach
    void setUp() {
        // Setup MockedStatic
        movimientoUtilsMock = mockStatic(MovimientoModificacionUtils.class);
        dtoUtilsMock = mockStatic(DTOUtils.class);

        // Setup user
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");

        // Setup lote
        lote = new Lote();
        lote.setId(1L);
        lote.setCodigoLote("LOTE-001");
        lote.setDictamen(DictamenEnum.APROBADO);
        lote.setTrazado(false);

        // Setup análisis
        analisis = new Analisis();
        analisis.setId(1L);
        analisis.setNroAnalisis("AN-001");
        analisis.setDictamen(DictamenEnum.APROBADO);
        analisis.setLote(lote);
        analisis.setActivo(true);

        List<Analisis> analisisList = new ArrayList<>();
        analisisList.add(analisis);
        lote.setAnalisisList(analisisList);

        // Setup movimiento origen
        movimientoOrigen = new Movimiento();
        movimientoOrigen.setId(1L);
        movimientoOrigen.setCodigoMovimiento("MOV-001");
        movimientoOrigen.setLote(lote);
        movimientoOrigen.setDictamenInicial(DictamenEnum.CUARENTENA);
        movimientoOrigen.setDictamenFinal(DictamenEnum.APROBADO);
        movimientoOrigen.setActivo(true);

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
    }

    @Nested
    @DisplayName("reversarModifDictamenCuarentena() - Tests")
    class ReversarModifDictamenCuarentenaTests {

        @Test
        @DisplayName("Debe reversar dictamen de cuarentena exitosamente - cubre líneas 35-56")
        void reversarModifDictamenCuarentena_datosValidos_debeReversarExitosamente() {
            // Given
            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoReverso);
            when(analisisRepository.save(any(Analisis.class))).thenReturn(analisis);
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarModifDictamenCuarentena(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);
            assertThat(resultado.getCodigoLote()).isEqualTo("LOTE-001");

            // Verifica que el dictamen del lote fue revertido
            assertThat(lote.getDictamen()).isEqualTo(DictamenEnum.CUARENTENA);

            // Verifica que los dictámenes del movimiento fueron intercambiados
            assertThat(movimientoReverso.getDictamenInicial()).isEqualTo(DictamenEnum.APROBADO);
            assertThat(movimientoReverso.getDictamenFinal()).isEqualTo(DictamenEnum.CUARENTENA);

            // Verifica que se marcaron como inactivos
            assertFalse(movimientoOrigen.getActivo());
            assertFalse(movimientoReverso.getActivo());
            assertFalse(analisis.getActivo());

            // Verifica que se guardaron las entidades
            verify(analisisRepository).save(analisis);
            verify(movimientoRepository, times(2)).save(any(Movimiento.class));
            verify(loteRepository).save(lote);
        }
    }

    @Nested
    @DisplayName("reversarModifResultadoAnalisis() - Tests")
    class ReversarModifResultadoAnalisisTests {

        @Test
        @DisplayName("Debe reversar resultado de análisis exitosamente - cubre líneas 59-86")
        void reversarModifResultadoAnalisis_datosValidos_debeReversarExitosamente() {
            // Given
            analisis.setFechaRealizado(LocalDate.of(2024, 1, 15));
            analisis.setDictamen(DictamenEnum.APROBADO);
            analisis.setFechaReanalisis(LocalDate.of(2025, 1, 15));
            analisis.setFechaVencimiento(LocalDate.of(2026, 1, 15));
            analisis.setTitulo(new java.math.BigDecimal("98.5"));
            analisis.setObservaciones("Análisis completado");

            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoReverso);
            when(analisisRepository.save(any(Analisis.class))).thenReturn(analisis);
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarModifResultadoAnalisis(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que el dictamen del lote fue revertido
            assertThat(lote.getDictamen()).isEqualTo(DictamenEnum.CUARENTENA);

            // Verifica que los dictámenes del movimiento fueron intercambiados
            assertThat(movimientoReverso.getDictamenInicial()).isEqualTo(DictamenEnum.APROBADO);
            assertThat(movimientoReverso.getDictamenFinal()).isEqualTo(DictamenEnum.CUARENTENA);

            // Verifica que los datos del análisis fueron limpiados
            assertNull(analisis.getFechaRealizado());
            assertNull(analisis.getDictamen());
            assertNull(analisis.getFechaReanalisis());
            assertNull(analisis.getFechaVencimiento());
            assertNull(analisis.getTitulo());
            assertNull(analisis.getObservaciones());

            // Verifica que se marcaron como inactivos
            assertFalse(movimientoOrigen.getActivo());
            assertFalse(movimientoReverso.getActivo());

            // Verifica que se guardaron las entidades
            verify(analisisRepository).save(analisis);
            verify(movimientoRepository, times(2)).save(any(Movimiento.class));
            verify(loteRepository).save(lote);
        }
    }

    @Nested
    @DisplayName("reversarModifLiberacionProducto() - Tests")
    class ReversarModifLiberacionProductoTests {

        @Test
        @DisplayName("Debe reversar liberación de producto exitosamente - cubre líneas 89-105")
        void reversarModifLiberacionProducto_datosValidos_debeReversarExitosamente() {
            // Given
            lote.setFechaVencimientoProveedor(LocalDate.of(2025, 12, 31));

            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoReverso);
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarModifLiberacionProducto(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que el dictamen del lote fue revertido
            assertThat(lote.getDictamen()).isEqualTo(DictamenEnum.CUARENTENA);

            // Verifica que los dictámenes del movimiento fueron intercambiados
            assertThat(movimientoReverso.getDictamenInicial()).isEqualTo(DictamenEnum.APROBADO);
            assertThat(movimientoReverso.getDictamenFinal()).isEqualTo(DictamenEnum.CUARENTENA);

            // Verifica que la fecha de vencimiento fue limpiada
            assertNull(lote.getFechaVencimientoProveedor());

            // Verifica que se marcaron como inactivos
            assertFalse(movimientoOrigen.getActivo());
            assertFalse(movimientoReverso.getActivo());

            // Verifica que se guardaron las entidades
            verify(movimientoRepository, times(2)).save(any(Movimiento.class));
            verify(loteRepository).save(lote);
        }
    }

    @Nested
    @DisplayName("reversarModifTrazadoLote() - Tests")
    class ReversarModifTrazadoLoteTests {

        @Test
        @DisplayName("Debe reversar trazado de lote exitosamente - cubre líneas 108-127")
        void reversarModifTrazadoLote_datosValidos_debeReversarExitosamente() {
            // Given
            lote.setTrazado(true);

            // Crear trazas activas
            Traza traza1 = new Traza();
            traza1.setId(1L);
            traza1.setNroTraza(1L);
            traza1.setActivo(true);
            traza1.setEstado(EstadoEnum.DISPONIBLE);
            traza1.setLote(lote);
            traza1.setDetalles(new ArrayList<>());

            DetalleMovimiento detalle1 = new DetalleMovimiento();
            detalle1.setId(1L);
            detalle1.setActivo(true);
            traza1.getDetalles().add(detalle1);

            Traza traza2 = new Traza();
            traza2.setId(2L);
            traza2.setNroTraza(2L);
            traza2.setActivo(true);
            traza2.setEstado(EstadoEnum.DISPONIBLE);
            traza2.setLote(lote);
            traza2.setDetalles(new ArrayList<>());

            DetalleMovimiento detalle2 = new DetalleMovimiento();
            detalle2.setId(2L);
            detalle2.setActivo(true);
            traza2.getDetalles().add(detalle2);

            List<Traza> trazas = List.of(traza1, traza2);
            lote.setTrazas(new HashSet<>(trazas));

            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            when(trazaRepository.saveAll(anyList())).thenReturn(trazas);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoReverso);
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarModifTrazadoLote(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que las trazas fueron marcadas como inactivas y descartadas
            assertFalse(traza1.getActivo());
            assertThat(traza1.getEstado()).isEqualTo(EstadoEnum.DESCARTADO);
            assertFalse(traza2.getActivo());
            assertThat(traza2.getEstado()).isEqualTo(EstadoEnum.DESCARTADO);

            // Verifica que los detalles fueron marcados como inactivos
            assertFalse(detalle1.getActivo());
            assertFalse(detalle2.getActivo());

            // Verifica que el lote fue marcado como no trazado
            assertFalse(lote.getTrazado());

            // Verifica que se marcaron como inactivos
            assertFalse(movimientoOrigen.getActivo());
            assertFalse(movimientoReverso.getActivo());

            // Verifica que se guardaron las entidades
            verify(trazaRepository).saveAll(anyList());
            verify(movimientoRepository, times(2)).save(any(Movimiento.class));
            verify(loteRepository).save(lote);
        }
    }

    @Nested
    @DisplayName("reversarAnulacionAnalisis() - Tests")
    class ReversarAnulacionAnalisisTests {

        @Test
        @DisplayName("Debe reversar anulación de análisis exitosamente - cubre líneas 130-149")
        void reversarAnulacionAnalisis_datosValidos_debeReversarExitosamente() {
            // Given
            analisis.setDictamen(DictamenEnum.ANULADO);

            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            when(analisisRepository.save(any(Analisis.class))).thenReturn(analisis);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoReverso);
            when(loteRepository.save(any(Lote.class))).thenReturn(lote);

            dtoUtilsMock.when(() -> DTOUtils.fromLoteEntity(any(Lote.class))).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.reversarAnulacionAnalisis(movimientoDTO, movimientoOrigen, currentUser);

            // Then
            assertNotNull(resultado);

            // Verifica que el dictamen del análisis fue limpiado
            assertNull(analisis.getDictamen());

            // Verifica que el dictamen del lote fue revertido
            assertThat(lote.getDictamen()).isEqualTo(DictamenEnum.CUARENTENA);

            // Verifica que se marcaron como inactivos
            assertFalse(movimientoOrigen.getActivo());
            assertFalse(movimientoReverso.getActivo());

            // Verifica que se guardaron las entidades
            verify(analisisRepository).save(analisis);
            verify(movimientoRepository, times(2)).save(any(Movimiento.class));
            verify(loteRepository).save(lote);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando análisis no está anulado - cubre línea 135-137")
        void reversarAnulacionAnalisis_analisisNoAnulado_debeLanzarExcepcion() {
            // Given
            analisis.setDictamen(DictamenEnum.APROBADO); // No está anulado

            movimientoUtilsMock.when(() -> MovimientoModificacionUtils.createMovimientoReverso(
                    any(MovimientoDTO.class), any(Movimiento.class), any(User.class)))
                    .thenReturn(movimientoReverso);

            // When & Then
            assertThatThrownBy(() -> service.reversarAnulacionAnalisis(movimientoDTO, movimientoOrigen, currentUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El ultimo analisis no esta anulado");

            // Verifica que no se guardó nada
            verify(analisisRepository, never()).save(any());
            verify(movimientoRepository, never()).save(any());
            verify(loteRepository, never()).save(any());
        }
    }
}

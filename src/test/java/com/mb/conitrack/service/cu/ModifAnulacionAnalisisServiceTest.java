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
import com.mb.conitrack.enums.*;
import com.mb.conitrack.repository.AnalisisRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.service.SecurityContextService;
import com.mb.conitrack.utils.MovimientoModificacionUtils;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ModifAnulacionAnalisisService - Tests")
class ModifAnulacionAnalisisServiceTest {

    @InjectMocks
    ModifAnulacionAnalisisService service;

    @Mock
    SecurityContextService securityContextService;

    @Mock
    LoteRepository loteRepository;

    @Mock
    AnalisisRepository analisisRepository;

    @Mock
    MovimientoRepository movimientoRepository;

    @Nested
    @DisplayName("persistirAnulacionAnalisis() - Tests")
    class PersistirAnulacionAnalisisTests {

        @Test
        @DisplayName("Debe persistir anulación correctamente cuando hay un movimiento de análisis")
        void persistirAnulacionAnalisis_unMovimiento_debePersistir() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                MovimientoDTO dto = new MovimientoDTO();
                dto.setNroAnalisis("A-001");
                dto.setFechaMovimiento(LocalDate.now());

                User currentUser = crearUsuario();
                Lote lote = crearLote();

                Analisis analisis = new Analisis();
                analisis.setId(1L);
                analisis.setNroAnalisis("A-001");
                analisis.setLote(lote);

                Movimiento movAnterior = new Movimiento();
                movAnterior.setDictamenInicial(DictamenEnum.APROBADO);

                Movimiento movNuevo = new Movimiento();

                when(securityContextService.getCurrentUser()).thenReturn(currentUser);
                when(analisisRepository.findByNroAnalisisAndActivoTrue("A-001")).thenReturn(analisis);
                when(analisisRepository.save(any(Analisis.class))).thenReturn(analisis);
                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(any(), any(), any()))
                        .thenReturn(movNuevo);
                when(movimientoRepository.save(any())).thenReturn(movNuevo);
                when(movimientoRepository.findMovModifAnalisisByNro("A-001")).thenReturn(List.of(movAnterior));
                when(loteRepository.save(any())).thenReturn(lote);

                // When
                LoteDTO resultado = service.persistirAnulacionAnalisis(dto);

                // Then
                assertNotNull(resultado);
                assertEquals(DictamenEnum.APROBADO, lote.getDictamen());
                verify(loteRepository).save(lote);
            }
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando hay múltiples movimientos de análisis")
        void persistirAnulacionAnalisis_multiplesMovimientos_debeLanzarExcepcion() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                MovimientoDTO dto = new MovimientoDTO();
                dto.setNroAnalisis("A-001");

                User currentUser = crearUsuario();
                Lote lote = crearLote();

                Analisis analisis = new Analisis();
                analisis.setId(1L);
                analisis.setLote(lote);

                Movimiento mov1 = new Movimiento();
                Movimiento mov2 = new Movimiento();

                when(securityContextService.getCurrentUser()).thenReturn(currentUser);
                when(analisisRepository.findByNroAnalisisAndActivoTrue("A-001")).thenReturn(analisis);
                when(analisisRepository.save(any(Analisis.class))).thenReturn(analisis);
                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(any(), any(), any()))
                        .thenReturn(new Movimiento());
                when(movimientoRepository.save(any())).thenReturn(new Movimiento());
                when(movimientoRepository.findMovModifAnalisisByNro("A-001")).thenReturn(List.of(mov1, mov2));

                // When & Then
                assertThatThrownBy(() -> service.persistirAnulacionAnalisis(dto))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Existen 2 movimientos de análisis iguales para ese lote");
            }
        }
    }

    @Nested
    @DisplayName("addAnulacionAnalisis() - Tests")
    class AddAnulacionAnalisisTests {

        @Test
        @DisplayName("Debe crear nuevo análisis cuando no existe")
        void addAnulacionAnalisis_analisisNoExiste_debeCrear() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis("A-NEW");
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setObservaciones("Test");

            Analisis nuevoAnalisis = new Analisis();
            nuevoAnalisis.setNroAnalisis("A-NEW");

            when(analisisRepository.findByNroAnalisisAndActivoTrue("A-NEW")).thenReturn(null);
            when(analisisRepository.save(any(Analisis.class))).thenReturn(nuevoAnalisis);

            // When
            Analisis resultado = service.addAnulacionAnalisis(dto);

            // Then
            assertNotNull(resultado);
            verify(analisisRepository).save(any(Analisis.class));
        }

        @Test
        @DisplayName("Debe actualizar análisis existente")
        void addAnulacionAnalisis_analisisExiste_debeActualizar() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis("A-001");
            dto.setFechaRealizadoAnalisis(LocalDate.now());
            dto.setObservaciones("Anulado");

            Analisis analisisExistente = new Analisis();
            analisisExistente.setId(1L);
            analisisExistente.setNroAnalisis("A-001");

            when(analisisRepository.findByNroAnalisisAndActivoTrue("A-001")).thenReturn(analisisExistente);
            when(analisisRepository.save(any(Analisis.class))).thenReturn(analisisExistente);

            // When
            Analisis resultado = service.addAnulacionAnalisis(dto);

            // Then
            assertNotNull(resultado);
            assertEquals(DictamenEnum.ANULADO, analisisExistente.getDictamen());
            verify(analisisRepository).save(analisisExistente);
        }
    }

    @Nested
    @DisplayName("validarAnulacionAnalisisInput() - Tests")
    class ValidarAnulacionAnalisisInputTests {

        @Test
        @DisplayName("Debe retornar false cuando bindingResult tiene errores")
        void validarAnulacionAnalisisInput_bindingResultConErrores_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(true);

            // When
            boolean resultado = service.validarAnulacionAnalisisInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).hasErrors();
        }

        @Test
        @DisplayName("Debe retornar false cuando datos mandatorios son inválidos")
        void validarAnulacionAnalisisInput_datosMandatoriosInvalidos_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis(null);

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);

            // When
            boolean resultado = service.validarAnulacionAnalisisInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).rejectValue("nroAnalisis", "", "Debe indicar el Nro de Análisis.");
        }

        @Test
        @DisplayName("Debe retornar false cuando lote no existe")
        void validarAnulacionAnalisisInput_loteNoExiste_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-999");
            dto.setNroAnalisis("A-001");

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-999")).thenReturn(Optional.empty());

            // When
            boolean resultado = service.validarAnulacionAnalisisInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).rejectValue("codigoLote", "", "Lote no encontrado.");
        }

        @Test
        @DisplayName("Debe retornar false cuando fecha movimiento es inválida")
        void validarAnulacionAnalisisInput_fechaInvalida_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-001");
            dto.setNroAnalisis("A-001");
            dto.setFechaMovimiento(LocalDate.of(2024, 1, 1));

            Lote lote = crearLote();
            lote.setFechaIngreso(LocalDate.of(2024, 6, 1));

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));

            // When
            boolean resultado = service.validarAnulacionAnalisisInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).rejectValue("fechaMovimiento", "", "La fecha del movimiento debe ser posterior a la fecha de ingreso del lote.");
        }

        @Test
        @DisplayName("Debe validar correctamente cuando todas las validaciones pasan")
        void validarAnulacionAnalisisInput_validacionesCorrectas_debeRetornarTrue() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-001");
            dto.setNroAnalisis("A-001");
            dto.setFechaMovimiento(LocalDate.of(2024, 6, 15));

            Lote lote = crearLote();
            lote.setFechaIngreso(LocalDate.of(2024, 6, 1));

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));

            // When
            boolean resultado = service.validarAnulacionAnalisisInput(dto, bindingResult);

            // Then
            assertTrue(resultado);
        }
    }

    // ========== Métodos auxiliares ==========

    private User crearUsuario() {
        Role role = new Role();
        role.setId(1L);
        role.setName("ADMIN");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(role);
        return user;
    }

    private Lote crearLote() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setCodigoProducto("PROD-001");
        producto.setNombreGenerico("Producto Test");
        producto.setTipoProducto(TipoProductoEnum.API);
        producto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);

        Proveedor proveedor = new Proveedor();
        proveedor.setId(1L);
        proveedor.setRazonSocial("Proveedor Test");

        Lote lote = new Lote();
        lote.setId(1L);
        lote.setCodigoLote("LOTE-001");
        lote.setProducto(producto);
        lote.setProveedor(proveedor);
        lote.setFechaYHoraCreacion(OffsetDateTime.now());
        lote.setFechaIngreso(LocalDate.of(2024, 6, 1));
        lote.setCantidadInicial(new BigDecimal("100"));
        lote.setCantidadActual(new BigDecimal("100"));
        lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        lote.setEstado(EstadoEnum.DISPONIBLE);
        lote.setDictamen(DictamenEnum.APROBADO);
        lote.setActivo(true);
        lote.setMovimientos(new ArrayList<>());

        return lote;
    }
}

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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ModifReanalisisLoteService - Tests")
class ModifReanalisisLoteServiceTest {

    @InjectMocks
    ModifReanalisisLoteService service;

    @Mock
    SecurityContextService securityContextService;

    @Mock
    LoteRepository loteRepository;

    @Mock
    AnalisisRepository analisisRepository;

    @Mock
    MovimientoRepository movimientoRepository;

    @Nested
    @DisplayName("persistirReanalisisLote() - Tests")
    class PersistirReanalisisLoteTests {

        @Test
        @DisplayName("Debe lanzar excepción cuando lote no existe")
        void persistirReanalisisLote_loteNoExiste_debeLanzarExcepcion() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-999");

            User currentUser = crearUsuario();
            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-999")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.persistirReanalisisLote(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El lote no existe.");

            verify(loteRepository).findByCodigoLoteAndActivoTrue("LOTE-999");
        }

        @Test
        @DisplayName("Debe persistir reanálisis correctamente")
        void persistirReanalisisLote_loteExiste_debePersistir() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                MovimientoDTO dto = new MovimientoDTO();
                dto.setCodigoLote("LOTE-001");
                dto.setFechaMovimiento(LocalDate.now());
                dto.setNroAnalisis("A-002");
                dto.setObservaciones("Reanálisis test");

                User currentUser = crearUsuario();
                Lote lote = crearLote();

                Analisis nuevoAnalisis = new Analisis();
                nuevoAnalisis.setId(2L);
                nuevoAnalisis.setNroAnalisis("A-002");

                Movimiento movMock = new Movimiento();
                movMock.setId(1L);

                when(securityContextService.getCurrentUser()).thenReturn(currentUser);
                when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));
                when(analisisRepository.save(any(Analisis.class))).thenReturn(nuevoAnalisis);
                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(any(), any(), any()))
                        .thenReturn(movMock);
                when(movimientoRepository.save(any())).thenReturn(movMock);
                when(loteRepository.save(any())).thenReturn(lote);

                // When
                LoteDTO resultado = service.persistirReanalisisLote(dto);

                // Then
                assertNotNull(resultado);
                verify(analisisRepository).save(any(Analisis.class));
                verify(movimientoRepository).save(any(Movimiento.class));
                verify(loteRepository).save(lote);
            }
        }
    }

    @Nested
    @DisplayName("validarReanalisisLoteInput() - Tests")
    class ValidarReanalisisLoteInputTests {

        @Test
        @DisplayName("Debe retornar false cuando bindingResult tiene errores")
        void validarReanalisisLoteInput_bindingResultConErrores_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(true);

            // When
            boolean resultado = service.validarReanalisisLoteInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).hasErrors();
        }

        @Test
        @DisplayName("Debe retornar false cuando nro análisis es null")
        void validarReanalisisLoteInput_nroAnalisisNull_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis(null);

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);

            // When
            boolean resultado = service.validarReanalisisLoteInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).rejectValue("nroAnalisis", "", "Debe indicar el Nro de Análisis.");
        }

        @Test
        @DisplayName("Debe retornar false cuando nro análisis no es único")
        void validarReanalisisLoteInput_nroAnalisisNoUnico_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setNroAnalisis("A-001");

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(analisisRepository.existsByNroAnalisisAndActivoTrue("A-001")).thenReturn(true);

            // When
            boolean resultado = service.validarReanalisisLoteInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).rejectValue("nroAnalisis", "", "El nro de Análisis ya existe.");
        }

        @Test
        @DisplayName("Debe retornar false cuando lote no existe")
        void validarReanalisisLoteInput_loteNoExiste_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-999");
            dto.setNroAnalisis("A-NEW");

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(analisisRepository.existsByNroAnalisisAndActivoTrue("A-NEW")).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-999")).thenReturn(Optional.empty());

            // When
            boolean resultado = service.validarReanalisisLoteInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).rejectValue("codigoLote", "", "Lote no encontrado.");
        }

        @Test
        @DisplayName("Debe retornar false cuando fecha movimiento es inválida")
        void validarReanalisisLoteInput_fechaMovimientoInvalida_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-001");
            dto.setNroAnalisis("A-NEW");
            dto.setFechaMovimiento(LocalDate.of(2024, 1, 1));

            Lote lote = crearLote();
            lote.setFechaIngreso(LocalDate.of(2024, 6, 1));

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(analisisRepository.existsByNroAnalisisAndActivoTrue("A-NEW")).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));

            // When
            boolean resultado = service.validarReanalisisLoteInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).rejectValue("fechaMovimiento", "", "La fecha del movimiento debe ser posterior a la fecha de ingreso del lote.");
        }

        @Test
        @DisplayName("Debe validar correctamente cuando todas las validaciones pasan")
        void validarReanalisisLoteInput_validacionesCorrectas_debeRetornarTrue() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-001");
            dto.setNroAnalisis("A-NEW");
            dto.setFechaMovimiento(LocalDate.of(2024, 6, 15));
            dto.setFechaAnalisis(LocalDate.of(2024, 6, 10));

            Lote lote = crearLote();
            lote.setFechaIngreso(LocalDate.of(2024, 6, 1));

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(analisisRepository.existsByNroAnalisisAndActivoTrue("A-NEW")).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));

            // When
            boolean resultado = service.validarReanalisisLoteInput(dto, bindingResult);

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
        lote.setAnalisisList(new ArrayList<>());

        return lote;
    }
}

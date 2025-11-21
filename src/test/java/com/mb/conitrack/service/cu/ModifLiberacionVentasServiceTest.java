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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ModifLiberacionVentasService - Tests")
class ModifLiberacionVentasServiceTest {

    @InjectMocks
    ModifLiberacionVentasService service;

    @Mock
    SecurityContextService securityContextService;

    @Mock
    LoteRepository loteRepository;

    @Mock
    MovimientoRepository movimientoRepository;

    @Nested
    @DisplayName("persistirLiberacionProducto() - Tests")
    class PersistirLiberacionProductoTests {

        @Test
        @DisplayName("Debe lanzar excepción cuando lote no existe")
        void persistirLiberacionProducto_loteNoExiste_debeLanzarExcepcion() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-999");

            User currentUser = crearUsuario();
            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-999")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.persistirLiberacionProducto(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El lote no existe.");

            verify(loteRepository).findByCodigoLoteAndActivoTrue("LOTE-999");
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando no hay análisis con fecha vencimiento")
        void persistirLiberacionProducto_sinAnalisisConFechaVencimiento_debeLanzarExcepcion() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                MovimientoDTO dto = new MovimientoDTO();
                dto.setCodigoLote("LOTE-001");
                dto.setFechaMovimiento(LocalDate.now());

                User currentUser = crearUsuario();
                Lote lote = crearLote();
                lote.setAnalisisList(new ArrayList<>()); // Sin análisis

                Movimiento movMock = new Movimiento();
                movMock.setDictamenFinal(DictamenEnum.LIBERADO);

                when(securityContextService.getCurrentUser()).thenReturn(currentUser);
                when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));
                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(any(), any(), any()))
                        .thenReturn(movMock);
                when(movimientoRepository.save(any())).thenReturn(movMock);

                // When & Then
                assertThatThrownBy(() -> service.persistirLiberacionProducto(dto))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("NO hay Analisis con fecha de vencimiento para el lote");
            }
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando hay más de un análisis con fecha vencimiento")
        void persistirLiberacionProducto_multipleAnalisisConFechaVencimiento_debeLanzarExcepcion() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                MovimientoDTO dto = new MovimientoDTO();
                dto.setCodigoLote("LOTE-001");
                dto.setFechaMovimiento(LocalDate.now());

                User currentUser = crearUsuario();
                Lote lote = crearLote();

                Analisis analisis1 = crearAnalisis();
                analisis1.setFechaVencimiento(LocalDate.now().plusDays(30));
                Analisis analisis2 = crearAnalisis();
                analisis2.setFechaVencimiento(LocalDate.now().plusDays(60));
                lote.setAnalisisList(List.of(analisis1, analisis2));

                Movimiento movMock = new Movimiento();
                movMock.setDictamenFinal(DictamenEnum.LIBERADO);

                when(securityContextService.getCurrentUser()).thenReturn(currentUser);
                when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));
                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(any(), any(), any()))
                        .thenReturn(movMock);
                when(movimientoRepository.save(any())).thenReturn(movMock);

                // When & Then
                assertThatThrownBy(() -> service.persistirLiberacionProducto(dto))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("Hay más de un análisis activo con fecha de vencimiento");
            }
        }

        @Test
        @DisplayName("Debe persistir liberación correctamente cuando hay un análisis con fecha vencimiento")
        void persistirLiberacionProducto_unAnalisisConFechaVencimiento_debePersistir() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                MovimientoDTO dto = new MovimientoDTO();
                dto.setCodigoLote("LOTE-001");
                dto.setFechaMovimiento(LocalDate.now());
                dto.setFechaYHoraCreacion(OffsetDateTime.now());

                User currentUser = crearUsuario();
                Lote lote = crearLote();

                LocalDate fechaVencimiento = LocalDate.now().plusDays(30);
                Analisis analisis = crearAnalisis();
                analisis.setFechaVencimiento(fechaVencimiento);
                analisis.setLote(lote);
                lote.setAnalisisList(new ArrayList<>(List.of(analisis)));

                Movimiento movMock = new Movimiento();
                movMock.setDictamenFinal(DictamenEnum.LIBERADO);

                when(securityContextService.getCurrentUser()).thenReturn(currentUser);
                when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));
                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModificacion(any(), any(), any()))
                        .thenReturn(movMock);
                when(movimientoRepository.save(any())).thenReturn(movMock);
                when(loteRepository.save(any())).thenReturn(lote);

                // When
                LoteDTO resultado = service.persistirLiberacionProducto(dto);

                // Then
                assertNotNull(resultado);
                assertEquals(DictamenEnum.LIBERADO, lote.getDictamen());
                assertEquals(fechaVencimiento, lote.getFechaVencimientoProveedor());
                verify(loteRepository).save(lote);
            }
        }
    }

    @Nested
    @DisplayName("validarLiberacionProductoInput() - Tests")
    class ValidarLiberacionProductoInputTests {

        @Test
        @DisplayName("Debe retornar false cuando bindingResult tiene errores")
        void validarLiberacionProductoInput_bindingResultConErrores_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(true);

            // When
            boolean resultado = service.validarLiberacionProductoInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).hasErrors();
            verify(loteRepository, never()).findByCodigoLoteAndActivoTrue(any());
        }

        @Test
        @DisplayName("Debe retornar false cuando lote no existe")
        void validarLiberacionProductoInput_loteNoExiste_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-999");

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-999")).thenReturn(Optional.empty());

            // When
            boolean resultado = service.validarLiberacionProductoInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(loteRepository).findByCodigoLoteAndActivoTrue("LOTE-999");
            verify(bindingResult).rejectValue("codigoLote", "", "Lote no encontrado.");
        }

        @Test
        @DisplayName("Debe retornar false cuando fecha movimiento es anterior a fecha ingreso lote")
        void validarLiberacionProductoInput_fechaInvalida_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-001");
            dto.setFechaMovimiento(LocalDate.of(2024, 1, 1));

            Lote lote = crearLote();
            lote.setFechaIngreso(LocalDate.of(2024, 6, 1));

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));

            // When
            boolean resultado = service.validarLiberacionProductoInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).rejectValue("fechaMovimiento", "", "La fecha del movmiento no puede ser anterior a la fecha de ingreso del lote");
        }

        @Test
        @DisplayName("Debe validar correctamente cuando todas las validaciones pasan")
        void validarLiberacionProductoInput_validacionesCorrectas_debeRetornarTrue() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-001");
            dto.setFechaMovimiento(LocalDate.of(2024, 6, 15));
            dto.setFechaRealizadoAnalisis(LocalDate.of(2024, 6, 10));

            Lote lote = crearLote();
            lote.setFechaIngreso(LocalDate.of(2024, 6, 1));

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));

            // When
            boolean resultado = service.validarLiberacionProductoInput(dto, bindingResult);

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

    private Analisis crearAnalisis() {
        Analisis analisis = new Analisis();
        analisis.setId(1L);
        analisis.setNroAnalisis("A-001");
        analisis.setDictamen(DictamenEnum.APROBADO);
        analisis.setFechaRealizado(LocalDate.now());
        analisis.setActivo(true);
        analisis.setFechaYHoraCreacion(OffsetDateTime.now());
        return analisis;
    }
}

package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
@DisplayName("ModifRetiroMercadoService - Tests")
class ModifRetiroMercadoServiceTest {

    @InjectMocks
    ModifRetiroMercadoService service;

    @Mock
    SecurityContextService securityContextService;

    @Mock
    AltaRecallService altaRecallService;

    @Mock
    ModificacionRecallService modificacionRecallService;

    @Mock
    LoteRepository loteRepository;

    @Mock
    MovimientoRepository movimientoRepository;

    @Nested
    @DisplayName("persistirRetiroMercado() - Tests")
    class PersistirRetiroMercadoTests {

        @Test
        @DisplayName("Debe procesar recall correctamente cuando lote y movimiento existen")
        void persistirRetiroMercado_loteYMovimientoExisten_debeProcesarCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-001");
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Lote loteVenta = crearLote();
            Movimiento movimientoVenta = crearMovimiento();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(loteRepository.findFirstByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(loteVenta));
            when(movimientoRepository.findByCodigoMovimientoAndActivoTrue("MOV-001")).thenReturn(Optional.of(movimientoVenta));

            doAnswer(invocation -> {
                List<Lote> result = invocation.getArgument(3);
                result.add(crearLote());
                return null;
            }).when(altaRecallService).procesarAltaRecall(any(), any(), any(), any(), any());

            doAnswer(invocation -> {
                List<Lote> result = invocation.getArgument(3);
                result.add(loteVenta);
                return null;
            }).when(modificacionRecallService).procesarModificacionRecall(any(), any(), any(), any(), any());

            // When
            List<LoteDTO> resultado = service.persistirRetiroMercado(dto);

            // Then
            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            verify(securityContextService).getCurrentUser();
            verify(loteRepository).findFirstByCodigoLoteAndActivoTrue("LOTE-001");
            verify(movimientoRepository).findByCodigoMovimientoAndActivoTrue("MOV-001");
            verify(altaRecallService).procesarAltaRecall(eq(dto), eq(loteVenta), eq(movimientoVenta), any(List.class), eq(currentUser));
            verify(modificacionRecallService).procesarModificacionRecall(eq(dto), eq(loteVenta), eq(movimientoVenta), any(List.class), eq(currentUser));
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando lote no existe")
        void persistirRetiroMercado_loteNoExiste_debeLanzarExcepcion() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-999");
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(loteRepository.findFirstByCodigoLoteAndActivoTrue("LOTE-999")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.persistirRetiroMercado(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El lote no existe.");

            verify(loteRepository).findFirstByCodigoLoteAndActivoTrue("LOTE-999");
            verify(movimientoRepository, never()).findByCodigoMovimientoAndActivoTrue(any());
            verify(altaRecallService, never()).procesarAltaRecall(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando movimiento origen no existe")
        void persistirRetiroMercado_movimientoNoExiste_debeLanzarExcepcion() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-001");
            dto.setCodigoMovimientoOrigen("MOV-999");

            User currentUser = crearUsuario();
            Lote loteVenta = crearLote();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(loteRepository.findFirstByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(loteVenta));
            when(movimientoRepository.findByCodigoMovimientoAndActivoTrue("MOV-999")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.persistirRetiroMercado(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El movmiento de origen no existe.");

            verify(loteRepository).findFirstByCodigoLoteAndActivoTrue("LOTE-001");
            verify(movimientoRepository).findByCodigoMovimientoAndActivoTrue("MOV-999");
            verify(altaRecallService, never()).procesarAltaRecall(any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("validarRetiroMercadoInput() - Tests")
    class ValidarRetiroMercadoInputTests {

        @Test
        @DisplayName("Debe retornar false cuando bindingResult tiene errores")
        void validarRetiroMercadoInput_bindingResultConErrores_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(true);

            // When
            boolean resultado = service.validarRetiroMercadoInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).hasErrors();
            verify(loteRepository, never()).findByCodigoLoteAndActivoTrue(any());
        }

        @Test
        @DisplayName("Debe retornar false cuando lote no existe")
        void validarRetiroMercadoInput_loteNoExiste_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-999");

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-999")).thenReturn(Optional.empty());

            // When
            boolean resultado = service.validarRetiroMercadoInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(loteRepository).findByCodigoLoteAndActivoTrue("LOTE-999");
            verify(bindingResult).rejectValue("codigoLote", "", "Lote no encontrado.");
        }

        @Test
        @DisplayName("Debe retornar false cuando fecha movimiento es anterior a fecha ingreso lote")
        void validarRetiroMercadoInput_fechaInvalida_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-001");
            dto.setFechaMovimiento(LocalDate.of(2024, 1, 1)); // Antes del ingreso

            Lote lote = crearLote();
            lote.setFechaIngreso(LocalDate.of(2024, 6, 1)); // Después

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));

            // When
            boolean resultado = service.validarRetiroMercadoInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).rejectValue("fechaMovimiento", "", "La fecha del movmiento no puede ser anterior a la fecha de ingreso del lote");
        }

        @Test
        @DisplayName("Debe retornar false cuando lote trazado y trazas son inválidas")
        void validarRetiroMercadoInput_loteTrazadoSinTrazas_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-001");
            dto.setFechaMovimiento(LocalDate.of(2024, 6, 15));
            dto.setTrazaDTOs(new ArrayList<>()); // Sin trazas

            Lote lote = crearLote();
            lote.setFechaIngreso(LocalDate.of(2024, 6, 1));
            lote.setTrazado(true);

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));

            // When
            boolean resultado = service.validarRetiroMercadoInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).rejectValue("trazaDTOs", "", "Debe seleccionar al menos una traza para devolver.");
        }

        @Test
        @DisplayName("Debe retornar true cuando movimiento origen no existe (caso línea 101)")
        void validarRetiroMercadoInput_movimientoOrigenNoExiste_debeRetornarTrue() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-001");
            dto.setFechaMovimiento(LocalDate.of(2024, 6, 15));
            dto.setCodigoMovimientoOrigen("MOV-999");

            Lote lote = crearLote();
            lote.setFechaIngreso(LocalDate.of(2024, 6, 1));
            lote.setTrazado(false);

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));
            when(movimientoRepository.findByCodigoMovimientoAndActivoTrue("MOV-999")).thenReturn(Optional.empty());

            // When
            boolean resultado = service.validarRetiroMercadoInput(dto, bindingResult);

            // Then
            assertTrue(resultado); // Línea 101 retorna true
            verify(bindingResult).rejectValue("codigoMovimientoOrigen", "", "No se encontro el movimiento de venta origen");
        }

        @Test
        @DisplayName("Debe validar movimiento origen correctamente cuando existe")
        void validarRetiroMercadoInput_movimientoOrigenExiste_debeValidar() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoLote("LOTE-001");
            dto.setFechaMovimiento(LocalDate.of(2024, 6, 15));
            dto.setCodigoMovimientoOrigen("MOV-001");
            dto.setCantidad(new BigDecimal("10"));

            Lote lote = crearLote();
            lote.setFechaIngreso(LocalDate.of(2024, 6, 1));
            lote.setTrazado(false);

            Movimiento movOrigen = crearMovimiento();
            movOrigen.setCantidad(new BigDecimal("50"));
            movOrigen.setFecha(LocalDate.of(2024, 6, 1));

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001")).thenReturn(Optional.of(lote));
            when(movimientoRepository.findByCodigoMovimientoAndActivoTrue("MOV-001")).thenReturn(Optional.of(movOrigen));

            // When
            boolean resultado = service.validarRetiroMercadoInput(dto, bindingResult);

            // Then
            assertTrue(resultado);
            verify(movimientoRepository).findByCodigoMovimientoAndActivoTrue("MOV-001");
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
        lote.setTrazado(false);
        lote.setActivo(true);

        return lote;
    }

    private Movimiento crearMovimiento() {
        Movimiento mov = new Movimiento();
        mov.setId(1L);
        mov.setCodigoMovimiento("MOV-001");
        mov.setTipoMovimiento(TipoMovimientoEnum.BAJA);
        mov.setMotivo(MotivoEnum.VENTA);
        mov.setCantidad(new BigDecimal("50"));
        mov.setFechaYHoraCreacion(OffsetDateTime.now());
        mov.setActivo(true);
        return mov;
    }
}

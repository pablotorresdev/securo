package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.service.ReversoAuthorizationService;
import com.mb.conitrack.service.SecurityContextService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ModifReversoMovimientoService - Tests")
class ModifReversoMovimientoServiceTest {

    @InjectMocks
    ModifReversoMovimientoService service;

    @Mock
    SecurityContextService securityContextService;

    @Mock
    ReversoAuthorizationService reversoAuthorizationService;

    @Mock
    ReversoAltaService reversoAltaService;

    @Mock
    ReversoBajaService reversoBajaService;

    @Mock
    ReversoModificacionService reversoModificacionService;

    @Mock
    MovimientoRepository movimientoRepository;

    @Nested
    @DisplayName("persistirReversoMovmiento() - Tests")
    class PersistirReversoMovmientoTests {

        @Test
        @DisplayName("Debe lanzar excepción cuando movimiento no existe")
        void persistirReversoMovmiento_movimientoNoExiste_debeLanzarExcepcion() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-999");

            User currentUser = crearUsuario();
            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-999")).thenReturn(new ArrayList<>());

            // When & Then
            assertThatThrownBy(() -> service.persistirReversoMovmiento(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("El Movimmiento no existe.");

            verify(movimientoRepository).findAllByCodigoMovimiento("MOV-999");
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando hay múltiples movimientos con mismo código")
        void persistirReversoMovmiento_multiplesMovimientos_debeLanzarExcepcion() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento mov1 = crearMovimiento();
            Movimiento mov2 = crearMovimiento();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(mov1, mov2));

            // When & Then
            assertThatThrownBy(() -> service.persistirReversoMovmiento(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cantidad incorrecta de movimientos");
        }

        // ========== Tests para ALTA ==========

        @Test
        @DisplayName("Debe delegar a reversoAltaService cuando tipo es ALTA con motivo COMPRA")
        void persistirReversoMovmiento_altaCompra_debeDelegarCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.ALTA);
            movOrigen.setMotivo(MotivoEnum.COMPRA);

            LoteDTO loteDTO = new LoteDTO();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);
            when(reversoAltaService.reversarAltaIngresoCompra(dto, movOrigen, currentUser)).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.persistirReversoMovmiento(dto);

            // Then
            assertNotNull(resultado);
            verify(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);
            verify(reversoAltaService).reversarAltaIngresoCompra(dto, movOrigen, currentUser);
        }

        @Test
        @DisplayName("Debe delegar a reversoAltaService cuando tipo es ALTA con motivo PRODUCCION_PROPIA")
        void persistirReversoMovmiento_altaProduccion_debeDelegarCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.ALTA);
            movOrigen.setMotivo(MotivoEnum.PRODUCCION_PROPIA);

            LoteDTO loteDTO = new LoteDTO();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);
            when(reversoAltaService.reversarAltaIngresoProduccion(dto, movOrigen, currentUser)).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.persistirReversoMovmiento(dto);

            // Then
            assertNotNull(resultado);
            verify(reversoAltaService).reversarAltaIngresoProduccion(dto, movOrigen, currentUser);
        }

        @Test
        @DisplayName("Debe delegar a reversoAltaService cuando tipo es ALTA con motivo DEVOLUCION_VENTA")
        void persistirReversoMovmiento_altaDevolucionVenta_debeDelegarCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.ALTA);
            movOrigen.setMotivo(MotivoEnum.DEVOLUCION_VENTA);

            LoteDTO loteDTO = new LoteDTO();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);
            when(reversoAltaService.reversarAltaDevolucionVenta(dto, movOrigen, currentUser)).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.persistirReversoMovmiento(dto);

            // Then
            assertNotNull(resultado);
            verify(reversoAltaService).reversarAltaDevolucionVenta(dto, movOrigen, currentUser);
        }

        @Test
        @DisplayName("Debe delegar a reversoAltaService cuando tipo es ALTA con motivo RETIRO_MERCADO")
        void persistirReversoMovmiento_altaRetiroMercado_debeDelegarCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.ALTA);
            movOrigen.setMotivo(MotivoEnum.RETIRO_MERCADO);

            LoteDTO loteDTO = new LoteDTO();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);
            when(reversoAltaService.reversarRetiroMercado(dto, movOrigen, currentUser)).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.persistirReversoMovmiento(dto);

            // Then
            assertNotNull(resultado);
            verify(reversoAltaService).reversarRetiroMercado(dto, movOrigen, currentUser);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando tipo ALTA tiene motivo no soportado")
        void persistirReversoMovmiento_altaMotivoNoSoportado_debeLanzarExcepcion() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.ALTA);
            movOrigen.setMotivo(MotivoEnum.AJUSTE); // Motivo no soportado para ALTA

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);

            // When & Then
            assertThatThrownBy(() -> service.persistirReversoMovmiento(dto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Motivo de ALTA no soportado para reverso");
        }

        // ========== Tests para BAJA ==========

        @Test
        @DisplayName("Debe delegar a reversoBajaService cuando tipo es BAJA con motivo DEVOLUCION_COMPRA")
        void persistirReversoMovmiento_bajaDevolucionCompra_debeDelegarCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.BAJA);
            movOrigen.setMotivo(MotivoEnum.DEVOLUCION_COMPRA);

            LoteDTO loteDTO = new LoteDTO();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);
            when(reversoBajaService.reversarBajaDevolucionCompra(dto, movOrigen, currentUser)).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.persistirReversoMovmiento(dto);

            // Then
            assertNotNull(resultado);
            verify(reversoBajaService).reversarBajaDevolucionCompra(dto, movOrigen, currentUser);
        }

        @Test
        @DisplayName("Debe delegar a reversoBajaService cuando tipo es BAJA con motivo MUESTREO")
        void persistirReversoMovmiento_bajaMuestreo_debeDelegarCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.BAJA);
            movOrigen.setMotivo(MotivoEnum.MUESTREO);

            LoteDTO loteDTO = new LoteDTO();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);
            when(reversoBajaService.reversarBajaMuestreoBulto(dto, movOrigen, currentUser)).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.persistirReversoMovmiento(dto);

            // Then
            assertNotNull(resultado);
            verify(reversoBajaService).reversarBajaMuestreoBulto(dto, movOrigen, currentUser);
        }

        @Test
        @DisplayName("Debe delegar a reversoBajaService cuando tipo es BAJA con motivo CONSUMO_PRODUCCION")
        void persistirReversoMovmiento_bajaConsumoProduccion_debeDelegarCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.BAJA);
            movOrigen.setMotivo(MotivoEnum.CONSUMO_PRODUCCION);

            LoteDTO loteDTO = new LoteDTO();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);
            when(reversoBajaService.reversarBajaConsumoProduccion(dto, movOrigen, currentUser)).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.persistirReversoMovmiento(dto);

            // Then
            assertNotNull(resultado);
            verify(reversoBajaService).reversarBajaConsumoProduccion(dto, movOrigen, currentUser);
        }

        @Test
        @DisplayName("Debe delegar a reversoBajaService cuando tipo es BAJA con motivo VENTA")
        void persistirReversoMovmiento_bajaVenta_debeDelegarCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.BAJA);
            movOrigen.setMotivo(MotivoEnum.VENTA);

            LoteDTO loteDTO = new LoteDTO();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);
            when(reversoBajaService.reversarBajaVentaProducto(dto, movOrigen, currentUser)).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.persistirReversoMovmiento(dto);

            // Then
            assertNotNull(resultado);
            verify(reversoBajaService).reversarBajaVentaProducto(dto, movOrigen, currentUser);
        }

        @Test
        @DisplayName("Debe delegar a reversoBajaService cuando tipo es BAJA con motivo AJUSTE")
        void persistirReversoMovmiento_bajaAjuste_debeDelegarCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.BAJA);
            movOrigen.setMotivo(MotivoEnum.AJUSTE);

            LoteDTO loteDTO = new LoteDTO();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);
            when(reversoBajaService.reversarBajaAjuste(dto, movOrigen, currentUser)).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.persistirReversoMovmiento(dto);

            // Then
            assertNotNull(resultado);
            verify(reversoBajaService).reversarBajaAjuste(dto, movOrigen, currentUser);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando tipo BAJA tiene motivo no soportado")
        void persistirReversoMovmiento_bajaMotivoNoSoportado_debeLanzarExcepcion() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.BAJA);
            movOrigen.setMotivo(MotivoEnum.COMPRA); // Motivo no soportado para BAJA

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);

            // When & Then
            assertThatThrownBy(() -> service.persistirReversoMovmiento(dto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Motivo de BAJA no soportado para reverso");
        }

        // ========== Tests para MODIFICACION ==========

        @Test
        @DisplayName("Debe delegar a reversoModificacionService cuando tipo es MODIFICACION con motivo ANALISIS")
        void persistirReversoMovmiento_modificacionAnalisis_debeDelegarCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
            movOrigen.setMotivo(MotivoEnum.ANALISIS);

            LoteDTO loteDTO = new LoteDTO();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);
            when(reversoModificacionService.reversarModifDictamenCuarentena(dto, movOrigen, currentUser)).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.persistirReversoMovmiento(dto);

            // Then
            assertNotNull(resultado);
            verify(reversoModificacionService).reversarModifDictamenCuarentena(dto, movOrigen, currentUser);
        }

        @Test
        @DisplayName("Debe delegar a reversoModificacionService cuando tipo es MODIFICACION con motivo RESULTADO_ANALISIS")
        void persistirReversoMovmiento_modificacionResultadoAnalisis_debeDelegarCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
            movOrigen.setMotivo(MotivoEnum.RESULTADO_ANALISIS);

            LoteDTO loteDTO = new LoteDTO();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);
            when(reversoModificacionService.reversarModifResultadoAnalisis(dto, movOrigen, currentUser)).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.persistirReversoMovmiento(dto);

            // Then
            assertNotNull(resultado);
            verify(reversoModificacionService).reversarModifResultadoAnalisis(dto, movOrigen, currentUser);
        }

        @Test
        @DisplayName("Debe delegar a reversoModificacionService cuando tipo es MODIFICACION con motivo LIBERACION")
        void persistirReversoMovmiento_modificacionLiberacion_debeDelegarCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
            movOrigen.setMotivo(MotivoEnum.LIBERACION);

            LoteDTO loteDTO = new LoteDTO();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);
            when(reversoModificacionService.reversarModifLiberacionProducto(dto, movOrigen, currentUser)).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.persistirReversoMovmiento(dto);

            // Then
            assertNotNull(resultado);
            verify(reversoModificacionService).reversarModifLiberacionProducto(dto, movOrigen, currentUser);
        }

        @Test
        @DisplayName("Debe delegar a reversoModificacionService cuando tipo es MODIFICACION con motivo TRAZADO")
        void persistirReversoMovmiento_modificacionTrazado_debeDelegarCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
            movOrigen.setMotivo(MotivoEnum.TRAZADO);

            LoteDTO loteDTO = new LoteDTO();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);
            when(reversoModificacionService.reversarModifTrazadoLote(dto, movOrigen, currentUser)).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.persistirReversoMovmiento(dto);

            // Then
            assertNotNull(resultado);
            verify(reversoModificacionService).reversarModifTrazadoLote(dto, movOrigen, currentUser);
        }

        @Test
        @DisplayName("Debe delegar a reversoModificacionService cuando tipo es MODIFICACION con motivo ANULACION_ANALISIS")
        void persistirReversoMovmiento_modificacionAnulacionAnalisis_debeDelegarCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
            movOrigen.setMotivo(MotivoEnum.ANULACION_ANALISIS);

            LoteDTO loteDTO = new LoteDTO();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);
            when(reversoModificacionService.reversarAnulacionAnalisis(dto, movOrigen, currentUser)).thenReturn(loteDTO);

            // When
            LoteDTO resultado = service.persistirReversoMovmiento(dto);

            // Then
            assertNotNull(resultado);
            verify(reversoModificacionService).reversarAnulacionAnalisis(dto, movOrigen, currentUser);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando tipo MODIFICACION tiene motivo VENCIMIENTO (irreversible)")
        void persistirReversoMovmiento_modificacionVencimiento_debeLanzarExcepcion() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
            movOrigen.setMotivo(MotivoEnum.VENCIMIENTO);

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);

            // When & Then
            assertThatThrownBy(() -> service.persistirReversoMovmiento(dto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No se puede reversar un vencimiento de lote");
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando tipo MODIFICACION tiene motivo EXPIRACION_ANALISIS (irreversible)")
        void persistirReversoMovmiento_modificacionExpiracionAnalisis_debeLanzarExcepcion() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
            movOrigen.setMotivo(MotivoEnum.EXPIRACION_ANALISIS);

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);

            // When & Then
            assertThatThrownBy(() -> service.persistirReversoMovmiento(dto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No se puede reversar una expiración de análisis");
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando tipo MODIFICACION tiene motivo RETIRO_MERCADO")
        void persistirReversoMovmiento_modificacionRetiroMercado_debeLanzarExcepcion() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
            movOrigen.setMotivo(MotivoEnum.RETIRO_MERCADO);

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);

            // When & Then
            assertThatThrownBy(() -> service.persistirReversoMovmiento(dto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("El lote origen tiene un recall asociado");
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando tipo MODIFICACION tiene motivo no soportado")
        void persistirReversoMovmiento_modificacionMotivoNoSoportado_debeLanzarExcepcion() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setCodigoMovimientoOrigen("MOV-001");

            User currentUser = crearUsuario();
            Movimiento movOrigen = crearMovimiento();
            movOrigen.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
            movOrigen.setMotivo(MotivoEnum.COMPRA); // Motivo no soportado para MODIFICACION

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(movimientoRepository.findAllByCodigoMovimiento("MOV-001")).thenReturn(List.of(movOrigen));
            doNothing().when(reversoAuthorizationService).validarPermisoReverso(movOrigen, currentUser);

            // When & Then
            assertThatThrownBy(() -> service.persistirReversoMovmiento(dto))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Motivo de MODIFICACION no soportado para reverso");
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

    private Movimiento crearMovimiento() {
        Movimiento mov = new Movimiento();
        mov.setId(1L);
        mov.setCodigoMovimiento("MOV-001");
        mov.setTipoMovimiento(TipoMovimientoEnum.ALTA);
        mov.setMotivo(MotivoEnum.COMPRA);
        mov.setFechaYHoraCreacion(OffsetDateTime.now());
        mov.setActivo(true);
        return mov;
    }
}

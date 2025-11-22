package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.DetalleMovimientoDTO;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.*;
import com.mb.conitrack.repository.BultoRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.TrazaRepository;
import com.mb.conitrack.service.SecurityContextService;
import com.mb.conitrack.utils.MovimientoAltaUtils;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("AltaDevolucionVentaService - Tests")
class AltaDevolucionVentaServiceTest {

    @InjectMocks
    AltaDevolucionVentaService service;

    @Mock
    LoteRepository loteRepository;

    @Mock
    MovimientoRepository movimientoRepository;

    @Mock
    BultoRepository bultoRepository;

    @Mock
    TrazaRepository trazaRepository;

    @Mock
    SecurityContextService securityContextService;

    @Mock
    BindingResult bindingResult;

    @Nested
    @DisplayName("initBulto() - Static Method Tests")
    class InitBultoTests {

        @Test
        @DisplayName("Debe crear bulto con estado DEVUELTO y unidad UNIDAD")
        void initBulto_debeCrearBultoCorrectamente() {
            // Given
            Lote lote = crearLote();

            // When
            Bulto bulto = AltaDevolucionVentaService.initBulto(lote);

            // Then
            assertNotNull(bulto);
            assertEquals(UnidadMedidaEnum.UNIDAD, bulto.getUnidadMedida());
            assertEquals(EstadoEnum.DEVUELTO, bulto.getEstado());
            assertTrue(bulto.getActivo());
            assertEquals(lote, bulto.getLote());
        }
    }

    @Nested
    @DisplayName("persistirDevolucionVenta() - Main Business Logic Tests")
    class PersistirDevolucionVentaTests {

        @Test
        @DisplayName("Debe procesar devolucion de venta para lote sin trazas")
        void persistirDevolucionVenta_loteSinTrazas_debeProcesarCorrectamente() {
            try (MockedStatic<MovimientoAltaUtils> mockedAltaUtils = mockStatic(MovimientoAltaUtils.class);
                 MockedStatic<DTOUtils> mockedDTOUtils = mockStatic(DTOUtils.class)) {
                // Given
                MovimientoDTO dto = crearMovimientoDTOSinTrazas();
                dto.setCodigoLote("LOTE-001");
                dto.setCodigoMovimientoOrigen("MOV-001");

                User currentUser = crearUsuario();
                Lote loteVentaOrigen = crearLote();
                loteVentaOrigen.setTrazado(false);
                loteVentaOrigen.setCodigoLote("LOTE-001");

                Lote loteDevolucion = crearLoteDevolucion();
                loteDevolucion.setId(2L);
                loteDevolucion.setTrazado(false);

                Bulto bulto = crearBulto();
                bulto.setCantidadActual(new BigDecimal("10"));
                loteDevolucion.getBultos().add(bulto);

                Movimiento movimientoOrigen = crearMovimiento();
                movimientoOrigen.setCodigoMovimiento("MOV-001");

                Movimiento movimientoDevolucion = crearMovimiento();
                movimientoDevolucion.setDetalles(new HashSet<>());

                when(securityContextService.getCurrentUser()).thenReturn(currentUser);
                when(loteRepository.findFirstByCodigoLoteAndActivoTrue("LOTE-001"))
                    .thenReturn(Optional.of(loteVentaOrigen));
                when(loteRepository.findLotesByLoteOrigen("LOTE-001")).thenReturn(new ArrayList<>());
                when(movimientoRepository.findByCodigoMovimientoAndActivoTrue("MOV-001"))
                    .thenReturn(Optional.of(movimientoOrigen));
                when(loteRepository.save(any(Lote.class))).thenReturn(loteDevolucion);
                when(bultoRepository.save(any(Bulto.class))).thenReturn(bulto);
                when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoDevolucion);
                when(loteRepository.findById(2L)).thenReturn(Optional.of(loteDevolucion));
                when(loteRepository.findById(1L)).thenReturn(Optional.of(loteVentaOrigen));

                mockedAltaUtils.when(() -> MovimientoAltaUtils.createMovimientoAltaDevolucion(
                    any(MovimientoDTO.class), any(Lote.class), any(User.class)))
                    .thenReturn(movimientoDevolucion);

                mockedDTOUtils.when(() -> DTOUtils.fromLoteEntities(any()))
                    .thenReturn(List.of(new LoteDTO(), new LoteDTO()));

                // When
                List<LoteDTO> resultado = service.persistirDevolucionVenta(dto);

                // Then
                assertNotNull(resultado);
                assertEquals(2, resultado.size());
                verify(securityContextService).getCurrentUser();
                verify(loteRepository).findFirstByCodigoLoteAndActivoTrue("LOTE-001");
                verify(movimientoRepository).findByCodigoMovimientoAndActivoTrue("MOV-001");
                verify(loteRepository, atLeast(2)).save(any(Lote.class));
                verify(movimientoRepository).save(movimientoDevolucion);
                verify(bultoRepository).save(any(Bulto.class));
            }
        }

        @Test
        @DisplayName("Debe procesar devolucion de venta para lote con trazas")
        void persistirDevolucionVenta_loteConTrazas_debeProcesarCorrectamente() {
            try (MockedStatic<MovimientoAltaUtils> mockedAltaUtils = mockStatic(MovimientoAltaUtils.class);
                 MockedStatic<DTOUtils> mockedDTOUtils = mockStatic(DTOUtils.class)) {
                // Given
                MovimientoDTO dto = crearMovimientoDTOConTrazas();
                dto.setCodigoLote("LOTE-001");
                dto.setCodigoMovimientoOrigen("MOV-001");

                User currentUser = crearUsuario();
                Lote loteVentaOrigen = crearLoteConTrazas();
                loteVentaOrigen.setCodigoLote("LOTE-001");

                Lote loteDevolucion = crearLoteDevolucion();
                loteDevolucion.setId(2L);
                loteDevolucion.setTrazado(true);

                Bulto bultoOrigen = loteVentaOrigen.getBultos().get(0);
                Bulto bultoDevolucion = crearBulto();
                bultoDevolucion.setNroBulto(1);
                loteDevolucion.getBultos().add(bultoDevolucion);

                Movimiento movimientoOrigen = crearMovimiento();
                movimientoOrigen.setCodigoMovimiento("MOV-001");

                Movimiento movimientoDevolucion = crearMovimiento();
                movimientoDevolucion.setDetalles(new HashSet<>());

                when(securityContextService.getCurrentUser()).thenReturn(currentUser);
                when(loteRepository.findFirstByCodigoLoteAndActivoTrue("LOTE-001"))
                    .thenReturn(Optional.of(loteVentaOrigen));
                when(loteRepository.findLotesByLoteOrigen("LOTE-001")).thenReturn(new ArrayList<>());
                when(movimientoRepository.findByCodigoMovimientoAndActivoTrue("MOV-001"))
                    .thenReturn(Optional.of(movimientoOrigen));
                when(loteRepository.save(any(Lote.class))).thenReturn(loteDevolucion);
                when(bultoRepository.save(any(Bulto.class))).thenReturn(bultoDevolucion);
                when(trazaRepository.saveAll(any())).thenReturn(new ArrayList<>());
                when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoDevolucion);
                when(loteRepository.findById(2L)).thenReturn(Optional.of(loteDevolucion));
                when(loteRepository.findById(1L)).thenReturn(Optional.of(loteVentaOrigen));

                mockedAltaUtils.when(() -> MovimientoAltaUtils.createMovimientoAltaDevolucion(
                    any(MovimientoDTO.class), any(Lote.class), any(User.class)))
                    .thenReturn(movimientoDevolucion);

                mockedDTOUtils.when(() -> DTOUtils.fromLoteEntities(any()))
                    .thenReturn(List.of(new LoteDTO(), new LoteDTO()));

                // When
                List<LoteDTO> resultado = service.persistirDevolucionVenta(dto);

                // Then
                assertNotNull(resultado);
                assertEquals(2, resultado.size());
                verify(trazaRepository).saveAll(any());
            }
        }

        @Test
        @DisplayName("Debe lanzar excepcion cuando el lote no existe")
        void persistirDevolucionVenta_loteNoExiste_debeLanzarExcepcion() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            dto.setCodigoLote("LOTE-999");
            User currentUser = crearUsuario();

            when(securityContextService.getCurrentUser()).thenReturn(currentUser);
            when(loteRepository.findFirstByCodigoLoteAndActivoTrue("LOTE-999"))
                .thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                service.persistirDevolucionVenta(dto);
            });
        }

        @Test
        @DisplayName("Debe lanzar excepcion cuando el movimiento origen no existe")
        void persistirDevolucionVenta_movimientoOrigenNoExiste_debeLanzarExcepcion() {
            try (MockedStatic<MovimientoAltaUtils> mockedAltaUtils = mockStatic(MovimientoAltaUtils.class)) {
                // Given
                MovimientoDTO dto = crearMovimientoDTOSinTrazas();
                dto.setCodigoLote("LOTE-001");
                dto.setCodigoMovimientoOrigen("MOV-999");

                User currentUser = crearUsuario();
                Lote loteVentaOrigen = crearLote();
                Lote loteDevolucion = crearLoteDevolucion();
                Movimiento movimientoDevolucion = crearMovimiento();

                lenient().when(securityContextService.getCurrentUser()).thenReturn(currentUser);
                lenient().when(loteRepository.findFirstByCodigoLoteAndActivoTrue("LOTE-001"))
                    .thenReturn(Optional.of(loteVentaOrigen));
                lenient().when(loteRepository.findLotesByLoteOrigen(anyString())).thenReturn(new ArrayList<>());
                lenient().when(loteRepository.save(any(Lote.class))).thenReturn(loteDevolucion);
                lenient().when(movimientoRepository.findByCodigoMovimientoAndActivoTrue("MOV-999"))
                    .thenReturn(Optional.empty());

                mockedAltaUtils.when(() -> MovimientoAltaUtils.createMovimientoAltaDevolucion(
                    any(MovimientoDTO.class), any(Lote.class), any(User.class)))
                    .thenReturn(movimientoDevolucion);

                // When & Then
                assertThrows(IllegalArgumentException.class, () -> {
                    service.persistirDevolucionVenta(dto);
                }, "El movmiento de origen no existe.");
            }
        }
    }

    @Nested
    @DisplayName("altaDevolucionUnidadesPorBulto() - Non-Traced Units Tests")
    class AltaDevolucionUnidadesPorBultoTests {

        @Test
        @DisplayName("Debe procesar bultos sin trazas correctamente")
        void altaDevolucionUnidadesPorBulto_debeProcesarCorrectamente() {
            // Given
            Lote loteDevolucion = crearLoteDevolucion();
            loteDevolucion.setTrazado(false);

            Bulto bulto1 = crearBulto();
            bulto1.setNroBulto(1);
            bulto1.setCantidadActual(new BigDecimal("10"));
            loteDevolucion.getBultos().add(bulto1);

            Bulto bulto2 = crearBulto();
            bulto2.setNroBulto(2);
            bulto2.setCantidadActual(new BigDecimal("5"));
            loteDevolucion.getBultos().add(bulto2);

            Movimiento movimiento = crearMovimiento();
            movimiento.setDetalles(new HashSet<>());

            when(bultoRepository.save(any(Bulto.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);

            // When
            service.altaDevolucionUnidadesPorBulto(loteDevolucion, movimiento);

            // Then
            assertEquals(2, movimiento.getDetalles().size());
            assertEquals(new BigDecimal("15"), movimiento.getCantidad());
            assertEquals(UnidadMedidaEnum.UNIDAD, movimiento.getUnidadMedida());
            assertEquals(2, loteDevolucion.getBultosTotales());
            verify(bultoRepository, times(2)).save(any(Bulto.class));
            verify(movimientoRepository).save(movimiento);
        }

        @Test
        @DisplayName("Debe procesar un solo bulto correctamente")
        void altaDevolucionUnidadesPorBulto_unSoloBulto_debeProcesarCorrectamente() {
            // Given
            Lote loteDevolucion = crearLoteDevolucion();
            Bulto bulto = crearBulto();
            bulto.setCantidadActual(new BigDecimal("20"));
            loteDevolucion.getBultos().add(bulto);

            Movimiento movimiento = crearMovimiento();
            movimiento.setDetalles(new HashSet<>());

            when(bultoRepository.save(any(Bulto.class))).thenReturn(bulto);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);

            // When
            service.altaDevolucionUnidadesPorBulto(loteDevolucion, movimiento);

            // Then
            assertEquals(1, movimiento.getDetalles().size());
            assertEquals(new BigDecimal("20"), movimiento.getCantidad());
            assertEquals(1, loteDevolucion.getBultosTotales());
        }

        @Test
        @DisplayName("Debe crear detalles de movimiento con los datos correctos")
        void altaDevolucionUnidadesPorBulto_debeCrearDetallesCorrectamente() {
            // Given
            Lote loteDevolucion = crearLoteDevolucion();
            Bulto bulto = crearBulto();
            bulto.setCantidadActual(new BigDecimal("8"));
            loteDevolucion.getBultos().add(bulto);

            Movimiento movimiento = crearMovimiento();
            movimiento.setDetalles(new HashSet<>());

            when(bultoRepository.save(any(Bulto.class))).thenReturn(bulto);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);

            // When
            service.altaDevolucionUnidadesPorBulto(loteDevolucion, movimiento);

            // Then
            DetalleMovimiento detalle = movimiento.getDetalles().iterator().next();
            assertEquals(movimiento, detalle.getMovimiento());
            assertEquals(bulto, detalle.getBulto());
            assertEquals(new BigDecimal("8"), detalle.getCantidad());
            assertEquals(UnidadMedidaEnum.UNIDAD, detalle.getUnidadMedida());
            assertTrue(detalle.getActivo());
        }
    }

    @Nested
    @DisplayName("altaDevolucionUnidadesTrazadas() - Traced Units Tests")
    class AltaDevolucionUnidadesTrazadasTests {

        @Test
        @DisplayName("Debe procesar trazas correctamente")
        void altaDevolucionUnidadesTrazadas_debeProcesarCorrectamente() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOConTrazas();
            Lote loteVentaOrigen = crearLoteConTrazas();
            Lote loteDevolucion = crearLoteDevolucion();
            loteDevolucion.setTrazado(true);

            Bulto bultoOrigen = loteVentaOrigen.getBultos().get(0);
            Bulto bultoDevolucion = crearBulto();
            bultoDevolucion.setNroBulto(1);
            loteDevolucion.getBultos().add(bultoDevolucion);

            Movimiento movimiento = crearMovimiento();
            movimiento.setDetalles(new HashSet<>());

            when(trazaRepository.saveAll(any())).thenReturn(new ArrayList<>());
            when(bultoRepository.save(any(Bulto.class))).thenReturn(bultoDevolucion);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);

            // When
            service.altaDevolucionUnidadesTrazadas(dto, loteVentaOrigen, loteDevolucion, movimiento);

            // Then
            assertEquals(1, movimiento.getDetalles().size());
            assertEquals(new BigDecimal("2"), movimiento.getCantidad());
            assertEquals(UnidadMedidaEnum.UNIDAD, movimiento.getUnidadMedida());
            assertEquals(1, loteDevolucion.getBultosTotales());
            verify(trazaRepository).saveAll(any());
            verify(bultoRepository).save(any(Bulto.class));
            verify(movimientoRepository).save(movimiento);
        }

        @Test
        @DisplayName("Debe actualizar estado de trazas a DEVUELTO")
        void altaDevolucionUnidadesTrazadas_debeActualizarEstadoTrazas() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOConTrazas();
            Lote loteVentaOrigen = crearLoteConTrazas();
            Lote loteDevolucion = crearLoteDevolucion();
            loteDevolucion.setTrazado(true);

            Bulto bultoDevolucion = crearBulto();
            bultoDevolucion.setNroBulto(1);
            loteDevolucion.getBultos().add(bultoDevolucion);

            Movimiento movimiento = crearMovimiento();
            movimiento.setDetalles(new HashSet<>());

            when(trazaRepository.saveAll(anySet())).thenAnswer(invocation -> {
                Set<Traza> trazas = invocation.getArgument(0);
                for (Traza traza : trazas) {
                    assertEquals(EstadoEnum.DEVUELTO, traza.getEstado());
                    assertEquals(loteDevolucion, traza.getLote());
                    assertEquals(bultoDevolucion, traza.getBulto());
                }
                return new ArrayList<>(trazas);
            });
            when(bultoRepository.save(any(Bulto.class))).thenReturn(bultoDevolucion);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);

            // When
            service.altaDevolucionUnidadesTrazadas(dto, loteVentaOrigen, loteDevolucion, movimiento);

            // Then
            verify(trazaRepository).saveAll(anySet());
        }

        @Test
        @DisplayName("Debe agrupar trazas por numero de bulto")
        void altaDevolucionUnidadesTrazadas_debeAgruparPorBulto() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            TrazaDTO traza1 = new TrazaDTO();
            traza1.setNroBulto(1);
            traza1.setNroTraza(1L);

            TrazaDTO traza2 = new TrazaDTO();
            traza2.setNroBulto(1);
            traza2.setNroTraza(2L);

            TrazaDTO traza3 = new TrazaDTO();
            traza3.setNroBulto(2);
            traza3.setNroTraza(3L);

            dto.setTrazaDTOs(List.of(traza1, traza2, traza3));

            Lote loteVentaOrigen = crearLoteConMultiplesBultos();
            Lote loteDevolucion = crearLoteDevolucion();
            loteDevolucion.setTrazado(true);

            Bulto bultoDevolucion1 = crearBulto();
            bultoDevolucion1.setNroBulto(1);
            Bulto bultoDevolucion2 = crearBulto();
            bultoDevolucion2.setNroBulto(2);
            loteDevolucion.getBultos().add(bultoDevolucion1);
            loteDevolucion.getBultos().add(bultoDevolucion2);

            Movimiento movimiento = crearMovimiento();
            movimiento.setDetalles(new HashSet<>());

            when(trazaRepository.saveAll(any())).thenReturn(new ArrayList<>());
            when(bultoRepository.save(any(Bulto.class))).thenAnswer(inv -> inv.getArgument(0));
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);

            // When
            service.altaDevolucionUnidadesTrazadas(dto, loteVentaOrigen, loteDevolucion, movimiento);

            // Then
            assertEquals(2, movimiento.getDetalles().size());
            assertEquals(new BigDecimal("3"), movimiento.getCantidad());
            assertEquals(2, loteDevolucion.getBultosTotales());
        }

        @Test
        @DisplayName("Debe vincular trazas al detalle de movimiento")
        void altaDevolucionUnidadesTrazadas_debeVincularTrazasADetalle() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOConTrazas();
            Lote loteVentaOrigen = crearLoteConTrazas();
            Lote loteDevolucion = crearLoteDevolucion();
            loteDevolucion.setTrazado(true);

            Bulto bultoDevolucion = crearBulto();
            bultoDevolucion.setNroBulto(1);
            loteDevolucion.getBultos().add(bultoDevolucion);

            Movimiento movimiento = crearMovimiento();
            movimiento.setDetalles(new HashSet<>());

            when(trazaRepository.saveAll(any())).thenReturn(new ArrayList<>());
            when(bultoRepository.save(any(Bulto.class))).thenReturn(bultoDevolucion);
            when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimiento);

            // When
            service.altaDevolucionUnidadesTrazadas(dto, loteVentaOrigen, loteDevolucion, movimiento);

            // Then
            DetalleMovimiento detalle = movimiento.getDetalles().iterator().next();
            assertEquals(2, detalle.getTrazas().size());
        }
    }

    @Nested
    @DisplayName("validarDevolucionVentaInput() - Validation Tests")
    class ValidarDevolucionVentaInputTests {

        @Test
        @DisplayName("Debe retornar false cuando bindingResult tiene errores")
        void validarDevolucionVentaInput_bindingResultConErrores_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            when(bindingResult.hasErrors()).thenReturn(true);

            // When
            boolean resultado = service.validarDevolucionVentaInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).hasErrors();
        }

        @Test
        @DisplayName("Debe retornar false cuando el lote no existe")
        void validarDevolucionVentaInput_loteNoExiste_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            dto.setCodigoLote("LOTE-999");

            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-999"))
                .thenReturn(Optional.empty());

            // When
            boolean resultado = service.validarDevolucionVentaInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
            verify(bindingResult).rejectValue("codigoLote", "", "Lote no encontrado.");
        }

        @Test
        @DisplayName("Debe retornar false cuando fecha movimiento es anterior a ingreso lote")
        void validarDevolucionVentaInput_fechaInvalida_debeRetornarFalse() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            dto.setCodigoLote("LOTE-001");
            dto.setFechaMovimiento(LocalDate.of(2024, 1, 1));

            Lote lote = crearLote();
            lote.setFechaIngreso(LocalDate.of(2024, 6, 1));

            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001"))
                .thenReturn(Optional.of(lote));

            // When
            boolean resultado = service.validarDevolucionVentaInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
        }

        @Test
        @DisplayName("Debe validar trazas cuando el lote es trazado")
        void validarDevolucionVentaInput_loteTrazado_debeValidarTrazas() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOConTrazas();
            dto.setCodigoLote("LOTE-001");
            dto.setFechaMovimiento(LocalDate.of(2024, 7, 1));
            dto.setCodigoMovimientoOrigen("MOV-001");
            dto.setTrazaDTOs(new ArrayList<>()); // Sin trazas - debería fallar validación

            Lote lote = crearLote();
            lote.setTrazado(true);
            lote.setFechaIngreso(LocalDate.of(2024, 6, 1));

            Movimiento movOrigen = crearMovimiento();
            movOrigen.setFecha(LocalDate.of(2024, 6, 15));

            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001"))
                .thenReturn(Optional.of(lote));

            // When
            boolean resultado = service.validarDevolucionVentaInput(dto, bindingResult);

            // Then
            assertFalse(resultado);
        }

        @Test
        @DisplayName("Debe retornar true cuando movimiento origen no existe")
        void validarDevolucionVentaInput_movimientoOrigenNoExiste_debeRetornarTrue() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            dto.setCodigoLote("LOTE-001");
            dto.setFechaMovimiento(LocalDate.of(2024, 7, 1));
            dto.setCodigoMovimientoOrigen("MOV-999");

            Lote lote = crearLote();
            lote.setTrazado(false);
            lote.setFechaIngreso(LocalDate.of(2024, 6, 1));

            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001"))
                .thenReturn(Optional.of(lote));
            when(movimientoRepository.findByCodigoMovimientoAndActivoTrue("MOV-999"))
                .thenReturn(Optional.empty());

            // When
            boolean resultado = service.validarDevolucionVentaInput(dto, bindingResult);

            // Then
            assertTrue(resultado);
            verify(bindingResult).rejectValue("codigoMovimientoOrigen", "",
                "No se encontro el movimiento de venta origen");
        }

        @Test
        @DisplayName("Debe retornar true cuando todas las validaciones pasan")
        void validarDevolucionVentaInput_validacionesCorrectas_debeRetornarTrue() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            dto.setCodigoLote("LOTE-001");
            dto.setFechaMovimiento(LocalDate.of(2024, 7, 1));
            dto.setCodigoMovimientoOrigen("MOV-001");

            Lote lote = crearLote();
            lote.setTrazado(false);
            lote.setFechaIngreso(LocalDate.of(2024, 6, 1));

            Movimiento movOrigen = crearMovimiento();
            movOrigen.setFecha(LocalDate.of(2024, 6, 15));
            movOrigen.setFechaYHoraCreacion(OffsetDateTime.of(2024, 6, 15, 10, 0, 0, 0,
                OffsetDateTime.now().getOffset()));

            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001"))
                .thenReturn(Optional.of(lote));
            when(movimientoRepository.findByCodigoMovimientoAndActivoTrue("MOV-001"))
                .thenReturn(Optional.of(movOrigen));

            // When
            boolean resultado = service.validarDevolucionVentaInput(dto, bindingResult);

            // Then
            assertTrue(resultado);
        }

        @Test
        @DisplayName("Debe retornar true cuando lote trazado pasa validacion de trazas")
        void validarDevolucionVentaInput_loteTrazadoConTrazasValidas_debeRetornarTrue() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOConTrazas();
            dto.setCodigoLote("LOTE-001");
            dto.setFechaMovimiento(LocalDate.of(2024, 7, 1));
            dto.setCodigoMovimientoOrigen("MOV-001");

            Lote lote = crearLote();
            lote.setTrazado(true);
            lote.setFechaIngreso(LocalDate.of(2024, 6, 1));

            Movimiento movOrigen = crearMovimiento();
            movOrigen.setFecha(LocalDate.of(2024, 6, 15));

            when(bindingResult.hasErrors()).thenReturn(false);
            when(loteRepository.findByCodigoLoteAndActivoTrue("LOTE-001"))
                .thenReturn(Optional.of(lote));
            when(movimientoRepository.findByCodigoMovimientoAndActivoTrue("MOV-001"))
                .thenReturn(Optional.of(movOrigen));

            // When
            boolean resultado = service.validarDevolucionVentaInput(dto, bindingResult);

            // Then
            assertTrue(resultado);
        }
    }

    @Nested
    @DisplayName("crearLoteDevolucion() - Return Lot Creation Tests")
    class CrearLoteDevolucionTests {

        @Test
        @DisplayName("Debe crear lote devolucion para producto sin trazas")
        void crearLoteDevolucion_productoSinTrazas_debeCrearCorrectamente() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            Lote loteOrigen = crearLote();
            loteOrigen.setTrazado(false);
            loteOrigen.setCodigoLote("LOTE-001");

            when(loteRepository.findLotesByLoteOrigen("LOTE-001")).thenReturn(new ArrayList<>());

            // When
            Lote loteDevolucion = service.crearLoteDevolucion(loteOrigen, dto);

            // Then
            assertNotNull(loteDevolucion);
            assertEquals("LOTE-001_D_1", loteDevolucion.getCodigoLote());
            assertEquals(EstadoEnum.DEVUELTO, loteDevolucion.getEstado());
            assertEquals(DictamenEnum.DEVOLUCION_CLIENTES, loteDevolucion.getDictamen());
            assertEquals(UnidadMedidaEnum.UNIDAD, loteDevolucion.getUnidadMedida());
            assertEquals(false, loteDevolucion.getTrazado());
            assertEquals(loteOrigen, loteDevolucion.getLoteOrigen());
            assertEquals(1, loteDevolucion.getBultos().size());
            assertEquals(new BigDecimal("10"), loteDevolucion.getCantidadInicial());
            assertEquals(new BigDecimal("10"), loteDevolucion.getCantidadActual());
            assertEquals(1, loteDevolucion.getBultosTotales());
        }

        @Test
        @DisplayName("Debe crear lote devolucion para producto con trazas")
        void crearLoteDevolucion_productoConTrazas_debeCrearCorrectamente() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOConTrazas();
            Lote loteOrigen = crearLote();
            loteOrigen.setTrazado(true);
            loteOrigen.setCodigoLote("LOTE-001");

            when(loteRepository.findLotesByLoteOrigen("LOTE-001")).thenReturn(new ArrayList<>());

            // When
            Lote loteDevolucion = service.crearLoteDevolucion(loteOrigen, dto);

            // Then
            assertNotNull(loteDevolucion);
            assertEquals("LOTE-001_D_1", loteDevolucion.getCodigoLote());
            assertEquals(true, loteDevolucion.getTrazado());
            assertEquals(1, loteDevolucion.getBultos().size());
            assertEquals(new BigDecimal("2"), loteDevolucion.getCantidadInicial());
            assertEquals(new BigDecimal("2"), loteDevolucion.getCantidadActual());
        }

        @Test
        @DisplayName("Debe generar codigo con sufijo correcto cuando ya existen devoluciones")
        void crearLoteDevolucion_devolucionesExistentes_debeGenerarSufijoIncrementado() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            Lote loteOrigen = crearLote();
            loteOrigen.setCodigoLote("LOTE-001");

            Lote devolucion1 = new Lote();
            devolucion1.setCodigoLote("LOTE-001_D_1");
            Lote devolucion2 = new Lote();
            devolucion2.setCodigoLote("LOTE-001_D_2");

            when(loteRepository.findLotesByLoteOrigen("LOTE-001"))
                .thenReturn(List.of(devolucion1, devolucion2));

            // When
            Lote loteDevolucion = service.crearLoteDevolucion(loteOrigen, dto);

            // Then
            assertEquals("LOTE-001_D_3", loteDevolucion.getCodigoLote());
        }

        @Test
        @DisplayName("Debe omitir detalles con cantidad cero en producto sin trazas")
        void crearLoteDevolucion_detallesConCantidadCero_debeOmitir() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            DetalleMovimientoDTO detalle1 = new DetalleMovimientoDTO();
            detalle1.setNroBulto(1);
            detalle1.setCantidad(new BigDecimal("10"));

            DetalleMovimientoDTO detalle2 = new DetalleMovimientoDTO();
            detalle2.setNroBulto(2);
            detalle2.setCantidad(BigDecimal.ZERO);

            DetalleMovimientoDTO detalle3 = new DetalleMovimientoDTO();
            detalle3.setNroBulto(3);
            detalle3.setCantidad(null);

            dto.setDetalleMovimientoDTOs(List.of(detalle1, detalle2, detalle3));

            Lote loteOrigen = crearLote();
            loteOrigen.setTrazado(false);

            when(loteRepository.findLotesByLoteOrigen(anyString())).thenReturn(new ArrayList<>());

            // When
            Lote loteDevolucion = service.crearLoteDevolucion(loteOrigen, dto);

            // Then
            assertEquals(1, loteDevolucion.getBultos().size());
            assertEquals(new BigDecimal("10"), loteDevolucion.getCantidadInicial());
        }

        @Test
        @DisplayName("Debe copiar propiedades del lote original correctamente")
        void crearLoteDevolucion_propiedadesOriginales_debeCopiar() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            Lote loteOrigen = crearLote();
            loteOrigen.setCodigoLote("LOTE-ORIGINAL");
            loteOrigen.setLoteProveedor("LP-001");
            loteOrigen.setOrdenProduccionOrigen("OP-001");
            loteOrigen.setDetalleConservacion("Conservar en frío");
            loteOrigen.setFechaVencimientoProveedor(LocalDate.of(2025, 12, 31));

            when(loteRepository.findLotesByLoteOrigen("LOTE-ORIGINAL")).thenReturn(new ArrayList<>());

            // When
            Lote loteDevolucion = service.crearLoteDevolucion(loteOrigen, dto);

            // Then
            assertEquals(loteOrigen.getProducto(), loteDevolucion.getProducto());
            assertEquals(loteOrigen.getProveedor(), loteDevolucion.getProveedor());
            assertEquals(loteOrigen.getFabricante(), loteDevolucion.getFabricante());
            assertEquals(loteOrigen.getPaisOrigen(), loteDevolucion.getPaisOrigen());
            assertEquals("LP-001", loteDevolucion.getLoteProveedor());
            assertEquals("OP-001", loteDevolucion.getOrdenProduccionOrigen());
            assertEquals("Conservar en frío", loteDevolucion.getDetalleConservacion());
            assertEquals(LocalDate.of(2025, 12, 31), loteDevolucion.getFechaVencimientoProveedor());
            assertTrue(loteDevolucion.getActivo());
        }

        @Test
        @DisplayName("Debe crear bultos con estado DEVUELTO y unidad UNIDAD")
        void crearLoteDevolucion_bultos_debenTenerEstadoDevueltoYUnidadUnidad() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            Lote loteOrigen = crearLote();
            loteOrigen.setTrazado(false);

            when(loteRepository.findLotesByLoteOrigen(anyString())).thenReturn(new ArrayList<>());

            // When
            Lote loteDevolucion = service.crearLoteDevolucion(loteOrigen, dto);

            // Then
            for (Bulto bulto : loteDevolucion.getBultos()) {
                assertEquals(EstadoEnum.DEVUELTO, bulto.getEstado());
                assertEquals(UnidadMedidaEnum.UNIDAD, bulto.getUnidadMedida());
                assertTrue(bulto.getActivo());
                assertEquals(loteDevolucion, bulto.getLote());
            }
        }

        @Test
        @DisplayName("Debe manejar multiples detalles en producto sin trazas")
        void crearLoteDevolucion_multiplesDetalles_debeManejarCorrectamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            DetalleMovimientoDTO detalle1 = new DetalleMovimientoDTO();
            detalle1.setNroBulto(1);
            detalle1.setCantidad(new BigDecimal("5"));

            DetalleMovimientoDTO detalle2 = new DetalleMovimientoDTO();
            detalle2.setNroBulto(2);
            detalle2.setCantidad(new BigDecimal("3"));

            DetalleMovimientoDTO detalle3 = new DetalleMovimientoDTO();
            detalle3.setNroBulto(3);
            detalle3.setCantidad(new BigDecimal("7"));

            dto.setDetalleMovimientoDTOs(List.of(detalle1, detalle2, detalle3));

            Lote loteOrigen = crearLote();
            loteOrigen.setTrazado(false);

            when(loteRepository.findLotesByLoteOrigen(anyString())).thenReturn(new ArrayList<>());

            // When
            Lote loteDevolucion = service.crearLoteDevolucion(loteOrigen, dto);

            // Then
            assertEquals(3, loteDevolucion.getBultos().size());
            assertEquals(new BigDecimal("15"), loteDevolucion.getCantidadInicial());
            assertEquals(new BigDecimal("15"), loteDevolucion.getCantidadActual());
            assertEquals(3, loteDevolucion.getBultosTotales());
        }
    }

    @Nested
    @DisplayName("initLoteDevolucion() - Initialize Return Lot Tests")
    class InitLoteDevolucionTests {

        @Test
        @DisplayName("Debe inicializar lote devolucion con propiedades basicas")
        void initLoteDevolucion_debeInicializarPropiedadesBasicas() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            Lote loteOrigen = crearLote();
            loteOrigen.setCodigoLote("LOTE-001");

            when(loteRepository.findLotesByLoteOrigen("LOTE-001")).thenReturn(new ArrayList<>());

            // When
            Lote loteDevolucion = service.initLoteDevolucion(loteOrigen, dto);

            // Then
            assertNull(loteDevolucion.getId());
            assertEquals(loteOrigen, loteDevolucion.getLoteOrigen());
            assertEquals(dto.getFechaYHoraCreacion(), loteDevolucion.getFechaYHoraCreacion());
            assertEquals(dto.getFechaYHoraCreacion().toLocalDate(), loteDevolucion.getFechaIngreso());
            assertEquals("LOTE-001_D_1", loteDevolucion.getCodigoLote());
            assertEquals(EstadoEnum.DEVUELTO, loteDevolucion.getEstado());
            assertEquals(DictamenEnum.DEVOLUCION_CLIENTES, loteDevolucion.getDictamen());
            assertEquals(UnidadMedidaEnum.UNIDAD, loteDevolucion.getUnidadMedida());
            assertTrue(loteDevolucion.getActivo());
        }

        @Test
        @DisplayName("Debe copiar campos del lote original")
        void initLoteDevolucion_debeCopiarCamposOriginales() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            Lote loteOrigen = crearLote();
            loteOrigen.setCodigoLote("LOTE-001");
            loteOrigen.setTrazado(true);
            loteOrigen.setPaisOrigen("Argentina");
            loteOrigen.setOrdenProduccionOrigen("OP-123");
            loteOrigen.setLoteProveedor("LP-456");
            loteOrigen.setDetalleConservacion("Conservar refrigerado");
            loteOrigen.setFechaVencimientoProveedor(LocalDate.of(2025, 12, 31));

            when(loteRepository.findLotesByLoteOrigen("LOTE-001")).thenReturn(new ArrayList<>());

            // When
            Lote loteDevolucion = service.initLoteDevolucion(loteOrigen, dto);

            // Then
            assertEquals(true, loteDevolucion.getTrazado());
            assertEquals(loteOrigen.getProducto(), loteDevolucion.getProducto());
            assertEquals(loteOrigen.getProveedor(), loteDevolucion.getProveedor());
            assertEquals(loteOrigen.getFabricante(), loteDevolucion.getFabricante());
            assertEquals("Argentina", loteDevolucion.getPaisOrigen());
            assertEquals("OP-123", loteDevolucion.getOrdenProduccionOrigen());
            assertEquals("LP-456", loteDevolucion.getLoteProveedor());
            assertEquals("Conservar refrigerado", loteDevolucion.getDetalleConservacion());
            assertEquals(LocalDate.of(2025, 12, 31), loteDevolucion.getFechaVencimientoProveedor());
        }

        @Test
        @DisplayName("Debe generar codigo con sufijo incrementado")
        void initLoteDevolucion_devolucionesExistentes_debeIncrementarSufijo() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            Lote loteOrigen = crearLote();
            loteOrigen.setCodigoLote("LOTE-001");

            List<Lote> devolucionesExistentes = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                Lote dev = new Lote();
                dev.setCodigoLote("LOTE-001_D_" + i);
                devolucionesExistentes.add(dev);
            }

            when(loteRepository.findLotesByLoteOrigen("LOTE-001")).thenReturn(devolucionesExistentes);

            // When
            Lote loteDevolucion = service.initLoteDevolucion(loteOrigen, dto);

            // Then
            assertEquals("LOTE-001_D_6", loteDevolucion.getCodigoLote());
        }

        @Test
        @DisplayName("Debe usar fecha vencimiento vigente del lote origen")
        void initLoteDevolucion_debeUsarFechaVencimientoVigente() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            Lote loteOrigen = crearLote();
            loteOrigen.setCodigoLote("LOTE-001");
            LocalDate fechaVencimiento = LocalDate.of(2026, 6, 30);
            loteOrigen.setFechaVencimientoProveedor(fechaVencimiento);

            when(loteRepository.findLotesByLoteOrigen("LOTE-001")).thenReturn(new ArrayList<>());

            // When
            Lote loteDevolucion = service.initLoteDevolucion(loteOrigen, dto);

            // Then
            assertEquals(fechaVencimiento, loteDevolucion.getFechaVencimientoProveedor());
        }

        @Test
        @DisplayName("Debe establecer fecha ingreso desde fecha creacion del DTO")
        void initLoteDevolucion_debeEstablecerFechaIngreso() {
            // Given
            OffsetDateTime fechaCreacion = OffsetDateTime.of(2024, 8, 15, 14, 30, 0, 0,
                OffsetDateTime.now().getOffset());
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            dto.setFechaYHoraCreacion(fechaCreacion);

            Lote loteOrigen = crearLote();
            loteOrigen.setCodigoLote("LOTE-001");

            when(loteRepository.findLotesByLoteOrigen("LOTE-001")).thenReturn(new ArrayList<>());

            // When
            Lote loteDevolucion = service.initLoteDevolucion(loteOrigen, dto);

            // Then
            assertEquals(fechaCreacion, loteDevolucion.getFechaYHoraCreacion());
            assertEquals(LocalDate.of(2024, 8, 15), loteDevolucion.getFechaIngreso());
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
        lote.setBultos(new ArrayList<>());
        lote.setTrazado(false);
        lote.setLoteProveedor("LP-001");
        lote.setFechaVencimientoProveedor(LocalDate.of(2025, 12, 31));
        return lote;
    }

    private Lote crearLoteDevolucion() {
        Lote lote = crearLote();
        lote.setId(2L);
        lote.setCodigoLote("LOTE-001_D_1");
        lote.setEstado(EstadoEnum.DEVUELTO);
        lote.setDictamen(DictamenEnum.DEVOLUCION_CLIENTES);
        lote.setBultos(new ArrayList<>());
        return lote;
    }

    private Lote crearLoteConTrazas() {
        Lote lote = crearLote();
        lote.setTrazado(true);

        Bulto bulto = crearBulto();
        bulto.setNroBulto(1);
        bulto.setLote(lote);

        Traza traza1 = crearTraza();
        traza1.setNroTraza(1L);
        traza1.setBulto(bulto);
        traza1.setLote(lote);

        Traza traza2 = crearTraza();
        traza2.setNroTraza(2L);
        traza2.setBulto(bulto);
        traza2.setLote(lote);

        Set<Traza> trazas = new HashSet<>();
        trazas.add(traza1);
        trazas.add(traza2);
        bulto.setTrazas(trazas);

        lote.getBultos().add(bulto);

        return lote;
    }

    private Lote crearLoteConMultiplesBultos() {
        Lote lote = crearLote();
        lote.setTrazado(true);

        // Bulto 1 con 2 trazas
        Bulto bulto1 = crearBulto();
        bulto1.setNroBulto(1);
        bulto1.setLote(lote);

        Traza traza1 = crearTraza();
        traza1.setId(1L);
        traza1.setNroTraza(1L);
        traza1.setBulto(bulto1);
        traza1.setLote(lote);

        Traza traza2 = crearTraza();
        traza2.setId(2L);
        traza2.setNroTraza(2L);
        traza2.setBulto(bulto1);
        traza2.setLote(lote);

        Set<Traza> trazas1 = new HashSet<>();
        trazas1.add(traza1);
        trazas1.add(traza2);
        bulto1.setTrazas(trazas1);

        // Bulto 2 con 1 traza
        Bulto bulto2 = crearBulto();
        bulto2.setId(2L);
        bulto2.setNroBulto(2);
        bulto2.setLote(lote);

        Traza traza3 = crearTraza();
        traza3.setId(3L);
        traza3.setNroTraza(3L);
        traza3.setBulto(bulto2);
        traza3.setLote(lote);

        Set<Traza> trazas2 = new HashSet<>();
        trazas2.add(traza3);
        bulto2.setTrazas(trazas2);

        lote.getBultos().add(bulto1);
        lote.getBultos().add(bulto2);

        return lote;
    }

    private Bulto crearBulto() {
        Bulto bulto = new Bulto();
        bulto.setId(1L);
        bulto.setNroBulto(1);
        bulto.setCantidadInicial(new BigDecimal("50"));
        bulto.setCantidadActual(new BigDecimal("50"));
        bulto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        bulto.setEstado(EstadoEnum.DISPONIBLE);
        bulto.setActivo(true);
        bulto.setTrazas(new HashSet<>());
        bulto.setDetalles(new HashSet<>());
        return bulto;
    }

    private Traza crearTraza() {
        Traza traza = new Traza();
        traza.setId(1L);
        traza.setNroTraza(1L);
        traza.setEstado(EstadoEnum.DISPONIBLE);
        traza.setActivo(true);
        traza.setFechaYHoraCreacion(OffsetDateTime.now());
        traza.setProducto(crearLote().getProducto());
        return traza;
    }

    private Movimiento crearMovimiento() {
        Movimiento mov = new Movimiento();
        mov.setId(1L);
        mov.setCodigoMovimiento("MOV-001");
        mov.setTipoMovimiento(TipoMovimientoEnum.ALTA);
        mov.setMotivo(MotivoEnum.DEVOLUCION_VENTA);
        mov.setFechaYHoraCreacion(OffsetDateTime.now());
        mov.setFecha(LocalDate.now());
        mov.setActivo(true);
        mov.setDetalles(new HashSet<>());
        return mov;
    }

    private MovimientoDTO crearMovimientoDTOSinTrazas() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setFechaMovimiento(LocalDate.now());
        dto.setCodigoLote("LOTE-001");

        DetalleMovimientoDTO detalle = new DetalleMovimientoDTO();
        detalle.setNroBulto(1);
        detalle.setCantidad(new BigDecimal("10"));

        dto.setDetalleMovimientoDTOs(List.of(detalle));
        return dto;
    }

    private MovimientoDTO crearMovimientoDTOConTrazas() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setFechaMovimiento(LocalDate.now());
        dto.setCodigoLote("LOTE-001");

        TrazaDTO traza1 = new TrazaDTO();
        traza1.setNroBulto(1);
        traza1.setNroTraza(1L);

        TrazaDTO traza2 = new TrazaDTO();
        traza2.setNroBulto(1);
        traza2.setNroTraza(2L);

        dto.setTrazaDTOs(List.of(traza1, traza2));
        return dto;
    }
}

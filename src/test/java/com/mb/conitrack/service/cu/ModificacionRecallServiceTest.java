package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.*;
import com.mb.conitrack.repository.AnalisisRepository;
import com.mb.conitrack.repository.BultoRepository;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.MovimientoRepository;
import com.mb.conitrack.repository.TrazaRepository;
import com.mb.conitrack.utils.MovimientoModificacionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ModificacionRecallService - Tests")
class ModificacionRecallServiceTest {

    @InjectMocks
    ModificacionRecallService service;

    @Mock
    LoteRepository loteRepository;

    @Mock
    MovimientoRepository movimientoRepository;

    @Mock
    BultoRepository bultoRepository;

    @Mock
    TrazaRepository trazaRepository;

    @Mock
    AnalisisRepository analisisRepository;

    @Nested
    @DisplayName("procesarModificacionRecall() - Tests")
    class ProcesarModificacionRecallTests {

        @Test
        @DisplayName("Debe retornar inmediatamente cuando lote ya está en estado RECALL")
        void procesarModificacionRecall_loteYaEnRecall_debeRetornarInmediatamente() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaMovimiento(LocalDate.now());

            User currentUser = crearUsuario();
            Lote loteRecall = crearLote();
            loteRecall.setEstado(EstadoEnum.RECALL);

            Movimiento movimientoVenta = crearMovimiento();
            List<Lote> result = new ArrayList<>();

            when(loteRepository.findById(loteRecall.getId())).thenReturn(Optional.of(loteRecall));

            // When
            service.procesarModificacionRecall(dto, loteRecall, movimientoVenta, result, currentUser);

            // Then
            assertEquals(1, result.size());
            assertEquals(loteRecall, result.get(0));
            verify(loteRepository).findById(loteRecall.getId());
            verify(movimientoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe procesar recall correctamente para lote sin trazas")
        void procesarModificacionRecall_loteSinTrazas_debeProcesarCorrectamente() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                MovimientoDTO dto = new MovimientoDTO();
                dto.setFechaMovimiento(LocalDate.now());

                User currentUser = crearUsuario();
                Lote lote = crearLote();
                lote.setEstado(EstadoEnum.DISPONIBLE);
                lote.setTrazado(false);

                Bulto bulto1 = crearBulto();
                bulto1.setCantidadActual(new BigDecimal("50"));
                Bulto bulto2 = crearBulto();
                bulto2.setCantidadActual(BigDecimal.ZERO);

                lote.setBultos(List.of(bulto1, bulto2));

                Movimiento movimientoVenta = crearMovimiento();
                Movimiento movimientoRecall = crearMovimiento();
                List<Lote> result = new ArrayList<>();

                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModifRecall(dto, currentUser))
                        .thenReturn(movimientoRecall);
                when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoRecall);
                when(bultoRepository.saveAll(any())).thenReturn(List.of(bulto1, bulto2));
                when(loteRepository.save(any(Lote.class))).thenReturn(lote);
                when(loteRepository.findById(lote.getId())).thenReturn(Optional.of(lote));

                // When
                service.procesarModificacionRecall(dto, lote, movimientoVenta, result, currentUser);

                // Then
                assertEquals(1, result.size());
                assertEquals(EstadoEnum.RECALL, lote.getEstado());
                assertEquals(EstadoEnum.RECALL, bulto1.getEstado()); // Tiene stock
                assertNotEquals(EstadoEnum.RECALL, bulto2.getEstado()); // No tiene stock
                verify(movimientoRepository).save(any(Movimiento.class));
                verify(bultoRepository).saveAll(lote.getBultos());
                verify(loteRepository).save(lote);
            }
        }

        @Test
        @DisplayName("Debe procesar recall correctamente para lote con trazas")
        void procesarModificacionRecall_loteConTrazas_debeProcesarCorrectamente() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                MovimientoDTO dto = new MovimientoDTO();
                dto.setFechaMovimiento(LocalDate.now());

                User currentUser = crearUsuario();
                Lote lote = crearLote();
                lote.setEstado(EstadoEnum.DISPONIBLE);
                lote.setTrazado(true);

                Traza traza1 = crearTraza();
                traza1.setEstado(EstadoEnum.DISPONIBLE);
                Traza traza2 = crearTraza();
                traza2.setEstado(EstadoEnum.VENDIDO);

                Set<Traza> trazas = new HashSet<>();
                trazas.add(traza1);
                trazas.add(traza2);

                Bulto bulto = crearBulto();
                bulto.setTrazas(trazas);
                lote.setBultos(List.of(bulto));

                Movimiento movimientoVenta = crearMovimiento();
                Movimiento movimientoRecall = crearMovimiento();
                List<Lote> result = new ArrayList<>();

                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModifRecall(dto, currentUser))
                        .thenReturn(movimientoRecall);
                when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoRecall);
                when(bultoRepository.saveAll(any())).thenReturn(List.of(bulto));
                when(trazaRepository.saveAll(any())).thenReturn(List.of(traza1));
                when(loteRepository.save(any(Lote.class))).thenReturn(lote);
                when(loteRepository.findById(lote.getId())).thenReturn(Optional.of(lote));

                // When
                service.procesarModificacionRecall(dto, lote, movimientoVenta, result, currentUser);

                // Then
                assertEquals(1, result.size());
                assertEquals(EstadoEnum.RECALL, lote.getEstado());
                assertEquals(EstadoEnum.RECALL, traza1.getEstado()); // Era DISPONIBLE
                assertEquals(EstadoEnum.VENDIDO, traza2.getEstado()); // Era VENDIDO, no cambia
                assertEquals(EstadoEnum.RECALL, bulto.getEstado());
                verify(trazaRepository).saveAll(any());
                verify(movimientoRepository).save(any(Movimiento.class));
                verify(loteRepository).save(lote);
            }
        }

        @Test
        @DisplayName("Debe cancelar análisis en curso cuando existe sin dictamen")
        void procesarModificacionRecall_conAnalisisEnCurso_debeCancelarAnalisis() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                MovimientoDTO dto = new MovimientoDTO();
                dto.setFechaMovimiento(LocalDate.now());

                User currentUser = crearUsuario();
                Lote lote = crearLote();
                lote.setEstado(EstadoEnum.DISPONIBLE);
                lote.setTrazado(false);
                lote.setBultos(new ArrayList<>());

                Analisis analisisEnCurso = new Analisis();
                analisisEnCurso.setId(1L);
                analisisEnCurso.setNroAnalisis("A-001");
                analisisEnCurso.setDictamen(null); // Sin dictamen
                lote.setUltimoAnalisis(analisisEnCurso);

                Movimiento movimientoVenta = crearMovimiento();
                Movimiento movimientoRecall = crearMovimiento();
                List<Lote> result = new ArrayList<>();

                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModifRecall(dto, currentUser))
                        .thenReturn(movimientoRecall);
                when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoRecall);
                when(bultoRepository.saveAll(any())).thenReturn(new ArrayList<>());
                when(analisisRepository.save(analisisEnCurso)).thenReturn(analisisEnCurso);
                when(loteRepository.save(any(Lote.class))).thenReturn(lote);
                when(loteRepository.findById(lote.getId())).thenReturn(Optional.of(lote));

                // When
                service.procesarModificacionRecall(dto, lote, movimientoVenta, result, currentUser);

                // Then
                assertEquals(DictamenEnum.CANCELADO, analisisEnCurso.getDictamen());
                verify(analisisRepository).save(analisisEnCurso);
            }
        }

        @Test
        @DisplayName("No debe cancelar análisis cuando ya tiene dictamen")
        void procesarModificacionRecall_analisisConDictamen_noDebeCancelar() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                MovimientoDTO dto = new MovimientoDTO();
                dto.setFechaMovimiento(LocalDate.now());

                User currentUser = crearUsuario();
                Lote lote = crearLote();
                lote.setEstado(EstadoEnum.DISPONIBLE);
                lote.setTrazado(false);
                lote.setBultos(new ArrayList<>());

                Analisis analisisCompleto = new Analisis();
                analisisCompleto.setId(1L);
                analisisCompleto.setNroAnalisis("A-001");
                analisisCompleto.setDictamen(DictamenEnum.APROBADO); // Ya tiene dictamen
                lote.setUltimoAnalisis(analisisCompleto);

                Movimiento movimientoVenta = crearMovimiento();
                Movimiento movimientoRecall = crearMovimiento();
                List<Lote> result = new ArrayList<>();

                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModifRecall(dto, currentUser))
                        .thenReturn(movimientoRecall);
                when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoRecall);
                when(bultoRepository.saveAll(any())).thenReturn(new ArrayList<>());
                when(loteRepository.save(any(Lote.class))).thenReturn(lote);
                when(loteRepository.findById(lote.getId())).thenReturn(Optional.of(lote));

                // When
                service.procesarModificacionRecall(dto, lote, movimientoVenta, result, currentUser);

                // Then
                assertEquals(DictamenEnum.APROBADO, analisisCompleto.getDictamen()); // No cambió
                verify(analisisRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("No debe cancelar análisis cuando no existe último análisis")
        void procesarModificacionRecall_sinUltimoAnalisis_noDebeCancelar() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                MovimientoDTO dto = new MovimientoDTO();
                dto.setFechaMovimiento(LocalDate.now());

                User currentUser = crearUsuario();
                Lote lote = crearLote();
                lote.setEstado(EstadoEnum.DISPONIBLE);
                lote.setTrazado(false);
                lote.setBultos(new ArrayList<>());
                lote.setUltimoAnalisis(null); // Sin análisis

                Movimiento movimientoVenta = crearMovimiento();
                Movimiento movimientoRecall = crearMovimiento();
                List<Lote> result = new ArrayList<>();

                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModifRecall(dto, currentUser))
                        .thenReturn(movimientoRecall);
                when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoRecall);
                when(bultoRepository.saveAll(any())).thenReturn(new ArrayList<>());
                when(loteRepository.save(any(Lote.class))).thenReturn(lote);
                when(loteRepository.findById(lote.getId())).thenReturn(Optional.of(lote));

                // When
                service.procesarModificacionRecall(dto, lote, movimientoVenta, result, currentUser);

                // Then
                verify(analisisRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Debe configurar movimiento de modificación correctamente")
        void procesarModificacionRecall_configuracionMovimiento_debeSerCorrecta() {
            try (MockedStatic<MovimientoModificacionUtils> mockedStatic = mockStatic(MovimientoModificacionUtils.class)) {
                // Given
                MovimientoDTO dto = new MovimientoDTO();
                dto.setFechaMovimiento(LocalDate.now());

                User currentUser = crearUsuario();
                Lote lote = crearLote();
                lote.setEstado(EstadoEnum.DISPONIBLE);
                lote.setDictamen(DictamenEnum.APROBADO);
                lote.setTrazado(false);
                lote.setBultos(new ArrayList<>());

                Movimiento movimientoVenta = crearMovimiento();
                Movimiento movimientoRecall = crearMovimiento();
                List<Lote> result = new ArrayList<>();

                mockedStatic.when(() -> MovimientoModificacionUtils.createMovimientoModifRecall(dto, currentUser))
                        .thenReturn(movimientoRecall);
                when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
                    Movimiento mov = invocation.getArgument(0);
                    // Verificar que se configuró correctamente
                    assertEquals(DictamenEnum.APROBADO, mov.getDictamenInicial());
                    assertEquals(movimientoVenta, mov.getMovimientoOrigen());
                    assertEquals(lote, mov.getLote());
                    return mov;
                });
                when(bultoRepository.saveAll(any())).thenReturn(new ArrayList<>());
                when(loteRepository.save(any(Lote.class))).thenReturn(lote);
                when(loteRepository.findById(lote.getId())).thenReturn(Optional.of(lote));

                // When
                service.procesarModificacionRecall(dto, lote, movimientoVenta, result, currentUser);

                // Then
                assertTrue(lote.getMovimientos().contains(movimientoRecall));
                verify(movimientoRepository).save(any(Movimiento.class));
            }
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

    private Bulto crearBulto() {
        Bulto bulto = new Bulto();
        bulto.setId(1L);
        bulto.setNroBulto("BULTO-001");
        bulto.setCantidadInicial(new BigDecimal("50"));
        bulto.setCantidadActual(new BigDecimal("50"));
        bulto.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        bulto.setEstado(EstadoEnum.DISPONIBLE);
        bulto.setActivo(true);
        bulto.setTrazas(new HashSet<>());
        return bulto;
    }

    private Traza crearTraza() {
        Traza traza = new Traza();
        traza.setId(1L);
        traza.setNroTraza("TRAZA-001");
        traza.setCantidad(new BigDecimal("10"));
        traza.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);
        traza.setEstado(EstadoEnum.DISPONIBLE);
        traza.setActivo(true);
        return traza;
    }

    private Movimiento crearMovimiento() {
        Movimiento mov = new Movimiento();
        mov.setId(1L);
        mov.setCodigoMovimiento("MOV-001");
        mov.setTipoMovimiento(TipoMovimientoEnum.MODIFICACION);
        mov.setMotivo(MotivoEnum.RECALL);
        mov.setFechaYHoraCreacion(OffsetDateTime.now());
        mov.setActivo(true);
        return mov;
    }
}

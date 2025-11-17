package com.mb.conitrack.service.cu;

import com.mb.conitrack.dto.DetalleMovimientoDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.Bulto;
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
import com.mb.conitrack.utils.MovimientoAltaUtils;
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
@DisplayName("AltaRecallService - Tests")
class AltaRecallServiceTest {

    @InjectMocks
    AltaRecallService service;

    @Mock
    LoteRepository loteRepository;

    @Mock
    MovimientoRepository movimientoRepository;

    @Mock
    BultoRepository bultoRepository;

    @Mock
    TrazaRepository trazaRepository;

    @Nested
    @DisplayName("procesarAltaRecall() - Tests")
    class ProcesarAltaRecallTests {

        @Test
        @DisplayName("Debe procesar alta recall correctamente para lote sin trazas")
        void procesarAltaRecall_loteSinTrazas_debeProcesarCorrectamente() {
            try (MockedStatic<MovimientoAltaUtils> mockedStatic = mockStatic(MovimientoAltaUtils.class)) {
                // Given
                MovimientoDTO dto = crearMovimientoDTOSinTrazas();
                User currentUser = crearUsuario();
                Lote loteOrigen = crearLote();
                loteOrigen.setTrazado(false);

                Movimiento movimientoVenta = crearMovimiento();
                Movimiento movimientoRecall = crearMovimiento();
                List<Lote> result = new ArrayList<>();

                Lote loteRecall = new Lote();
                loteRecall.setId(2L);
                loteRecall.setCodigoLote("LOTE-001_R_1");
                loteRecall.setTrazado(false);
                loteRecall.setBultos(new ArrayList<>());

                Bulto bultoRecall = crearBulto();
                bultoRecall.setCantidadActual(new BigDecimal("10"));
                loteRecall.getBultos().add(bultoRecall);

                when(loteRepository.findLotesByLoteOrigen(loteOrigen.getCodigoLote())).thenReturn(new ArrayList<>());
                when(loteRepository.save(any(Lote.class))).thenReturn(loteRecall);
                mockedStatic.when(() -> MovimientoAltaUtils.createMovimientoAltaRecall(any(), any(), any()))
                        .thenReturn(movimientoRecall);
                when(bultoRepository.save(any(Bulto.class))).thenReturn(bultoRecall);
                when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoRecall);
                when(loteRepository.findById(2L)).thenReturn(Optional.of(loteRecall));

                // When
                service.procesarAltaRecall(dto, loteOrigen, movimientoVenta, result, currentUser);

                // Then
                assertEquals(1, result.size());
                verify(loteRepository, atLeast(2)).save(any(Lote.class));
                verify(movimientoRepository).save(movimientoRecall);
                verify(bultoRepository).save(bultoRecall);
            }
        }

        @Test
        @DisplayName("Debe procesar alta recall correctamente para lote con trazas")
        void procesarAltaRecall_loteConTrazas_debeProcesarCorrectamente() {
            try (MockedStatic<MovimientoAltaUtils> mockedStatic = mockStatic(MovimientoAltaUtils.class)) {
                // Given
                MovimientoDTO dto = crearMovimientoDTOConTrazas();
                User currentUser = crearUsuario();
                Lote loteOrigen = crearLoteConTrazas();

                Movimiento movimientoVenta = crearMovimiento();
                Movimiento movimientoRecall = crearMovimiento();
                movimientoRecall.setDetalles(new ArrayList<>());
                List<Lote> result = new ArrayList<>();

                Lote loteRecall = new Lote();
                loteRecall.setId(2L);
                loteRecall.setCodigoLote("LOTE-001_R_1");
                loteRecall.setTrazado(true);
                loteRecall.setBultos(new ArrayList<>());

                Bulto bultoRecall = crearBulto();
                bultoRecall.setNroBulto(1);
                loteRecall.getBultos().add(bultoRecall);

                when(loteRepository.findLotesByLoteOrigen(loteOrigen.getCodigoLote())).thenReturn(new ArrayList<>());
                when(loteRepository.save(any(Lote.class))).thenReturn(loteRecall);
                mockedStatic.when(() -> MovimientoAltaUtils.createMovimientoAltaRecall(any(), any(), any()))
                        .thenReturn(movimientoRecall);
                when(trazaRepository.saveAll(any())).thenReturn(new ArrayList<>());
                when(bultoRepository.save(any(Bulto.class))).thenReturn(bultoRecall);
                when(movimientoRepository.save(any(Movimiento.class))).thenReturn(movimientoRecall);
                when(loteRepository.findById(2L)).thenReturn(Optional.of(loteRecall));

                // When
                service.procesarAltaRecall(dto, loteOrigen, movimientoVenta, result, currentUser);

                // Then
                assertEquals(1, result.size());
                verify(loteRepository, atLeast(2)).save(any(Lote.class));
                verify(movimientoRepository).save(movimientoRecall);
                verify(trazaRepository).saveAll(any());
            }
        }
    }

    @Nested
    @DisplayName("crearLoteRecall() - Tests")
    class CrearLoteRecallTests {

        @Test
        @DisplayName("Debe crear lote recall para producto sin trazas")
        void crearLoteRecall_productoSinTrazas_debeCrearCorrectamente() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            Lote loteOrigen = crearLote();
            loteOrigen.setTrazado(false);
            loteOrigen.setCodigoLote("LOTE-001");

            when(loteRepository.findLotesByLoteOrigen("LOTE-001")).thenReturn(new ArrayList<>());

            // When
            Lote loteRecall = service.crearLoteRecall(loteOrigen, dto);

            // Then
            assertNotNull(loteRecall);
            assertEquals("LOTE-001_R_1", loteRecall.getCodigoLote());
            assertEquals(EstadoEnum.RECALL, loteRecall.getEstado());
            assertEquals(DictamenEnum.RETIRO_MERCADO, loteRecall.getDictamen());
            assertEquals(UnidadMedidaEnum.UNIDAD, loteRecall.getUnidadMedida());
            assertEquals(false, loteRecall.getTrazado());
            assertEquals(loteOrigen, loteRecall.getLoteOrigen());
            assertNotNull(loteRecall.getBultos());
            assertEquals(1, loteRecall.getBultos().size());
            assertEquals(new BigDecimal("10"), loteRecall.getCantidadInicial());
        }

        @Test
        @DisplayName("Debe crear lote recall para producto con trazas")
        void crearLoteRecall_productoConTrazas_debeCrearCorrectamente() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOConTrazas();
            Lote loteOrigen = crearLote();
            loteOrigen.setTrazado(true);
            loteOrigen.setCodigoLote("LOTE-001");

            when(loteRepository.findLotesByLoteOrigen("LOTE-001")).thenReturn(new ArrayList<>());

            // When
            Lote loteRecall = service.crearLoteRecall(loteOrigen, dto);

            // Then
            assertNotNull(loteRecall);
            assertEquals("LOTE-001_R_1", loteRecall.getCodigoLote());
            assertEquals(EstadoEnum.RECALL, loteRecall.getEstado());
            assertEquals(DictamenEnum.RETIRO_MERCADO, loteRecall.getDictamen());
            assertEquals(true, loteRecall.getTrazado());
            assertEquals(loteOrigen, loteRecall.getLoteOrigen());
            assertNotNull(loteRecall.getBultos());
            assertEquals(1, loteRecall.getBultos().size());
            assertEquals(new BigDecimal("2"), loteRecall.getCantidadInicial()); // 2 trazas
        }

        @Test
        @DisplayName("Debe generar código con sufijo correcto cuando ya existen recalls")
        void crearLoteRecall_recallsExistentes_debeGenerarSufijoIncrementado() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            Lote loteOrigen = crearLote();
            loteOrigen.setCodigoLote("LOTE-001");

            Lote recall1 = new Lote();
            recall1.setCodigoLote("LOTE-001_R_1");
            Lote recall2 = new Lote();
            recall2.setCodigoLote("LOTE-001_R_2");

            when(loteRepository.findLotesByLoteOrigen("LOTE-001")).thenReturn(List.of(recall1, recall2));

            // When
            Lote loteRecall = service.crearLoteRecall(loteOrigen, dto);

            // Then
            assertEquals("LOTE-001_R_3", loteRecall.getCodigoLote());
        }

        @Test
        @DisplayName("Debe omitir detalles con cantidad cero en producto sin trazas")
        void crearLoteRecall_detallesConCantidadCero_debeOmitir() {
            // Given
            MovimientoDTO dto = new MovimientoDTO();
            dto.setFechaYHoraCreacion(OffsetDateTime.now());

            DetalleMovimientoDTO detalle1 = new DetalleMovimientoDTO();
            detalle1.setNroBulto(1);
            detalle1.setCantidad(new BigDecimal("10"));

            DetalleMovimientoDTO detalle2 = new DetalleMovimientoDTO();
            detalle2.setNroBulto(2);
            detalle2.setCantidad(BigDecimal.ZERO); // Cantidad cero

            DetalleMovimientoDTO detalle3 = new DetalleMovimientoDTO();
            detalle3.setNroBulto(3);
            detalle3.setCantidad(null); // Cantidad null

            dto.setDetalleMovimientoDTOs(List.of(detalle1, detalle2, detalle3));

            Lote loteOrigen = crearLote();
            loteOrigen.setTrazado(false);

            when(loteRepository.findLotesByLoteOrigen(loteOrigen.getCodigoLote())).thenReturn(new ArrayList<>());

            // When
            Lote loteRecall = service.crearLoteRecall(loteOrigen, dto);

            // Then
            assertEquals(1, loteRecall.getBultos().size()); // Solo detalle1
            assertEquals(new BigDecimal("10"), loteRecall.getCantidadInicial());
        }

        @Test
        @DisplayName("Debe copiar propiedades del lote original correctamente")
        void crearLoteRecall_propiedadesOriginales_debeCopiar() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            Lote loteOrigen = crearLote();
            loteOrigen.setCodigoLote("LOTE-ORIGINAL");
            loteOrigen.setLoteProveedor("LP-001");
            loteOrigen.setOrdenProduccionOrigen("OP-001");
            loteOrigen.setDetalleConservacion("Conservar en frío");
            loteOrigen.setFechaVencimientoVigente(LocalDate.of(2025, 12, 31));

            when(loteRepository.findLotesByLoteOrigen("LOTE-ORIGINAL")).thenReturn(new ArrayList<>());

            // When
            Lote loteRecall = service.crearLoteRecall(loteOrigen, dto);

            // Then
            assertEquals(loteOrigen.getProducto(), loteRecall.getProducto());
            assertEquals(loteOrigen.getProveedor(), loteRecall.getProveedor());
            assertEquals(loteOrigen.getFabricante(), loteRecall.getFabricante());
            assertEquals(loteOrigen.getPaisOrigen(), loteRecall.getPaisOrigen());
            assertEquals("LP-001", loteRecall.getLoteProveedor());
            assertEquals("OP-001", loteRecall.getOrdenProduccionOrigen());
            assertEquals("Conservar en frío", loteRecall.getDetalleConservacion());
            assertEquals(LocalDate.of(2025, 12, 31), loteRecall.getFechaVencimientoProveedor());
            assertTrue(loteRecall.getActivo());
        }

        @Test
        @DisplayName("Debe crear bultos con estado RECALL y unidad UNIDAD")
        void crearLoteRecall_bultos_debenTenerEstadoRecallYUnidadUnidad() {
            // Given
            MovimientoDTO dto = crearMovimientoDTOSinTrazas();
            Lote loteOrigen = crearLote();
            loteOrigen.setTrazado(false);

            when(loteRepository.findLotesByLoteOrigen(loteOrigen.getCodigoLote())).thenReturn(new ArrayList<>());

            // When
            Lote loteRecall = service.crearLoteRecall(loteOrigen, dto);

            // Then
            for (Bulto bulto : loteRecall.getBultos()) {
                assertEquals(EstadoEnum.RECALL, bulto.getEstado());
                assertEquals(UnidadMedidaEnum.UNIDAD, bulto.getUnidadMedida());
                assertTrue(bulto.getActivo());
                assertEquals(loteRecall, bulto.getLote());
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
        traza1.setNroTraza("T-001");
        traza1.setBulto(bulto);
        traza1.setLote(lote);

        Traza traza2 = crearTraza();
        traza2.setNroTraza("T-002");
        traza2.setBulto(bulto);
        traza2.setLote(lote);

        Set<Traza> trazas = new HashSet<>();
        trazas.add(traza1);
        trazas.add(traza2);
        bulto.setTrazas(trazas);

        lote.getBultos().add(bulto);

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
        return bulto;
    }

    private Traza crearTraza() {
        Traza traza = new Traza();
        traza.setId(1L);
        traza.setNroTraza("T-001");
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
        mov.setTipoMovimiento(TipoMovimientoEnum.ALTA);
        mov.setMotivo(MotivoEnum.RECALL);
        mov.setFechaYHoraCreacion(OffsetDateTime.now());
        mov.setActivo(true);
        mov.setDetalles(new ArrayList<>());
        return mov;
    }

    private MovimientoDTO crearMovimientoDTOSinTrazas() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setFechaMovimiento(LocalDate.now());

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

        TrazaDTO traza1 = new TrazaDTO();
        traza1.setNroBulto(1);
        traza1.setNroTraza("T-001");

        TrazaDTO traza2 = new TrazaDTO();
        traza2.setNroBulto(1);
        traza2.setNroTraza("T-002");

        dto.setTrazaDTOs(List.of(traza1, traza2));
        return dto;
    }
}

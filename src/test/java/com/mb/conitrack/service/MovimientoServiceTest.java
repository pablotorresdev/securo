package com.mb.conitrack.service;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.DetalleMovimiento;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.enums.TipoMovimientoEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para MovimientoService.
 * Cobertura completa de métodos de consulta y cálculo de movimientos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - MovimientoService")
class MovimientoServiceTest {

    @Mock
    private MovimientoRepository movimientoRepository;

    @InjectMocks
    private MovimientoService service;

    private Lote loteTest;
    private Movimiento movimiento1;
    private Movimiento movimiento2;

    @BeforeEach
    void setUp() {
        loteTest = new Lote();
        loteTest.setId(1L);
        loteTest.setCodigoLote("L-TEST-001");

        movimiento1 = crearMovimientoTest("MOV-001", TipoMovimientoEnum.ALTA, MotivoEnum.COMPRA);
        movimiento2 = crearMovimientoTest("MOV-002", TipoMovimientoEnum.BAJA, MotivoEnum.VENTA);
    }

    private Movimiento crearMovimientoTest(String codigoMovimiento, TipoMovimientoEnum tipo, MotivoEnum motivo) {
        Movimiento movimiento = new Movimiento();
        movimiento.setId(1L);
        movimiento.setCodigoMovimiento(codigoMovimiento);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setMotivo(motivo);
        movimiento.setLote(loteTest);
        movimiento.setFecha(LocalDate.now());
        movimiento.setActivo(true);
        movimiento.setDetalles(new HashSet<>());
        return movimiento;
    }

    @Test
    @DisplayName("test_findAllMovimientos_debe_retornarTodosMovimientosActivos")
    void test_findAllMovimientos_debe_retornarTodosMovimientosActivos() {
        // Given
        when(movimientoRepository.findAllByActivoTrue()).thenReturn(Arrays.asList(movimiento1, movimiento2));

        // When
        List<MovimientoDTO> resultado = service.findAllMovimientos();

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        verify(movimientoRepository).findAllByActivoTrue();
    }

    @Test
    @DisplayName("test_findAllMovimientos_sinMovimientos_debe_retornarListaVacia")
    void test_findAllMovimientos_sinMovimientos_debe_retornarListaVacia() {
        // Given
        when(movimientoRepository.findAllByActivoTrue()).thenReturn(Collections.emptyList());

        // When
        List<MovimientoDTO> resultado = service.findAllMovimientos();

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        verify(movimientoRepository).findAllByActivoTrue();
    }

    @Test
    @DisplayName("test_findAllMovimientosAudit_debe_retornarMovimientosParaAuditoria")
    void test_findAllMovimientosAudit_debe_retornarMovimientosParaAuditoria() {
        // Given
        when(movimientoRepository.findAllAudit()).thenReturn(Arrays.asList(movimiento1, movimiento2));

        // When
        List<MovimientoDTO> resultado = service.findAllMovimientosAudit();

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        verify(movimientoRepository).findAllAudit();
    }

    @Test
    @DisplayName("test_findByCodigoLote_debe_retornarMovimientosDelLoteOrdenados")
    void test_findByCodigoLote_debe_retornarMovimientosDelLoteOrdenados() {
        // Given
        when(movimientoRepository.findAllByLoteCodigoLoteOrderByFechaAsc("L-TEST-001"))
                .thenReturn(Arrays.asList(movimiento1, movimiento2));

        // When
        List<MovimientoDTO> resultado = service.findByCodigoLote("L-TEST-001");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        verify(movimientoRepository).findAllByLoteCodigoLoteOrderByFechaAsc("L-TEST-001");
    }

    @Test
    @DisplayName("test_findByCodigoLote_sinMovimientos_debe_retornarListaVacia")
    void test_findByCodigoLote_sinMovimientos_debe_retornarListaVacia() {
        // Given
        when(movimientoRepository.findAllByLoteCodigoLoteOrderByFechaAsc("L-INEXISTENTE"))
                .thenReturn(Collections.emptyList());

        // When
        List<MovimientoDTO> resultado = service.findByCodigoLote("L-INEXISTENTE");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        verify(movimientoRepository).findAllByLoteCodigoLoteOrderByFechaAsc("L-INEXISTENTE");
    }

    @Test
    @DisplayName("test_getMovimientosVentaByCodigolote_debe_retornarSoloMovimientosDeVenta")
    void test_getMovimientosVentaByCodigolote_debe_retornarSoloMovimientosDeVenta() {
        // Given
        when(movimientoRepository.findMovimientosVentaByCodigoLote("L-TEST-001"))
                .thenReturn(Arrays.asList(movimiento2));

        // When
        List<MovimientoDTO> resultado = service.getMovimientosVentaByCodigolote("L-TEST-001");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        verify(movimientoRepository).findMovimientosVentaByCodigoLote("L-TEST-001");
    }

    @Test
    @DisplayName("test_getUltimoMovimientosCodigolote_debe_retornarUltimoMovimiento")
    void test_getUltimoMovimientosCodigolote_debe_retornarUltimoMovimiento() {
        // Given
        when(movimientoRepository.findLatestByCodigoLote("L-TEST-001"))
                .thenReturn(Arrays.asList(movimiento2));

        // When
        MovimientoDTO resultado = service.getUltimoMovimientosCodigolote("L-TEST-001");

        // Then
        assertThat(resultado).isNotNull();
        verify(movimientoRepository).findLatestByCodigoLote("L-TEST-001");
    }

    @Test
    @DisplayName("test_calcularMaximoDevolucionPorBulto_conMovimientoVenta_debe_calcularSaldo")
    void test_calcularMaximoDevolucionPorBulto_conMovimientoVenta_debe_calcularSaldo() {
        // Given
        Bulto bulto1 = new Bulto();
        bulto1.setNroBulto(1);

        DetalleMovimiento detalle1 = new DetalleMovimiento();
        detalle1.setBulto(bulto1);
        detalle1.setCantidad(new BigDecimal("10"));
        detalle1.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        movimiento2.setDetalles(Set.of(detalle1));

        when(movimientoRepository.findMovimientosVentaByCodigoMovimiento("MOV-002"))
                .thenReturn(Optional.of(movimiento2));
        when(movimientoRepository.findByMovimientoOrigen("MOV-002"))
                .thenReturn(Collections.emptyList());

        // When
        List<Integer> resultado = service.calcularMaximoDevolucionPorBulto("MOV-002");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isEqualTo(10);
        verify(movimientoRepository).findMovimientosVentaByCodigoMovimiento("MOV-002");
        verify(movimientoRepository).findByMovimientoOrigen("MOV-002");
    }

    @Test
    @DisplayName("test_calcularMaximoDevolucionPorBulto_movimientoNoExiste_debe_retornarListaVacia")
    void test_calcularMaximoDevolucionPorBulto_movimientoNoExiste_debe_retornarListaVacia() {
        // Given
        when(movimientoRepository.findMovimientosVentaByCodigoMovimiento("MOV-INEXISTENTE"))
                .thenReturn(Optional.empty());

        // When
        List<Integer> resultado = service.calcularMaximoDevolucionPorBulto("MOV-INEXISTENTE");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEmpty();
        verify(movimientoRepository).findMovimientosVentaByCodigoMovimiento("MOV-INEXISTENTE");
    }

    @Test
    @DisplayName("test_calcularMaximoRecallPorBulto_conMovimientoVenta_debe_calcularSaldo")
    void test_calcularMaximoRecallPorBulto_conMovimientoVenta_debe_calcularSaldo() {
        // Given
        Bulto bulto1 = new Bulto();
        bulto1.setNroBulto(1);

        DetalleMovimiento detalle1 = new DetalleMovimiento();
        detalle1.setBulto(bulto1);
        detalle1.setCantidad(new BigDecimal("15"));
        detalle1.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        movimiento2.setDetalles(Set.of(detalle1));

        when(movimientoRepository.findMovimientosVentaByCodigoMovimiento("MOV-002"))
                .thenReturn(Optional.of(movimiento2));
        when(movimientoRepository.findByMovimientoOrigen("MOV-002"))
                .thenReturn(Collections.emptyList());

        // When
        List<Integer> resultado = service.calcularMaximoRecallPorBulto("MOV-002");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isEqualTo(15);
        verify(movimientoRepository).findMovimientosVentaByCodigoMovimiento("MOV-002");
    }

    @Test
    @DisplayName("test_calcularMaximoDevolucionPorBulto_unidadNoEsUNIDAD_debe_lanzarExcepcion")
    void test_calcularMaximoDevolucionPorBulto_unidadNoEsUNIDAD_debe_lanzarExcepcion() {
        // Given
        Bulto bulto1 = new Bulto();
        bulto1.setNroBulto(1);

        DetalleMovimiento detalle1 = new DetalleMovimiento();
        detalle1.setBulto(bulto1);
        detalle1.setCantidad(new BigDecimal("10"));
        detalle1.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO); // No es UNIDAD

        movimiento2.setDetalles(Set.of(detalle1));

        when(movimientoRepository.findMovimientosVentaByCodigoMovimiento("MOV-002"))
                .thenReturn(Optional.of(movimiento2));

        // When & Then
        assertThatThrownBy(() -> service.calcularMaximoDevolucionPorBulto("MOV-002"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("La unidad de medida del movimiento de venta debe ser UNIDAD.");

        verify(movimientoRepository).findMovimientosVentaByCodigoMovimiento("MOV-002");
    }

    @Test
    @DisplayName("test_calcularMaximoDevolucionPorBulto_conDevoluciones_debe_restarDelSaldo")
    void test_calcularMaximoDevolucionPorBulto_conDevoluciones_debe_restarDelSaldo() {
        // Given
        Bulto bulto1 = new Bulto();
        bulto1.setNroBulto(1);
        Bulto bulto2 = new Bulto();
        bulto2.setNroBulto(2);

        // Movimiento de venta
        DetalleMovimiento detalleVenta1 = new DetalleMovimiento();
        detalleVenta1.setBulto(bulto1);
        detalleVenta1.setCantidad(new BigDecimal("10"));
        detalleVenta1.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        DetalleMovimiento detalleVenta2 = new DetalleMovimiento();
        detalleVenta2.setBulto(bulto2);
        detalleVenta2.setCantidad(new BigDecimal("5"));
        detalleVenta2.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        movimiento2.setDetalles(Set.of(detalleVenta1, detalleVenta2));

        // Movimiento de devolución
        Movimiento movDevolucion = new Movimiento();
        movDevolucion.setId(3L);
        movDevolucion.setCodigoMovimiento("MOV-003");

        DetalleMovimiento detalleDevolucion1 = new DetalleMovimiento();
        detalleDevolucion1.setBulto(bulto1);
        detalleDevolucion1.setCantidad(new BigDecimal("3"));
        detalleDevolucion1.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        movDevolucion.setDetalles(Set.of(detalleDevolucion1));

        when(movimientoRepository.findMovimientosVentaByCodigoMovimiento("MOV-002"))
                .thenReturn(Optional.of(movimiento2));
        when(movimientoRepository.findByMovimientoOrigen("MOV-002"))
                .thenReturn(Arrays.asList(movDevolucion));

        // When
        List<Integer> resultado = service.calcularMaximoDevolucionPorBulto("MOV-002");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0)).isEqualTo(7); // 10 - 3 = 7
        assertThat(resultado.get(1)).isEqualTo(5); // Sin devolución
    }

    @Test
    @DisplayName("test_calcularMaximoDevolucionPorBulto_devolucionUnidadIncorrecta_debe_lanzarExcepcion")
    void test_calcularMaximoDevolucionPorBulto_devolucionUnidadIncorrecta_debe_lanzarExcepcion() {
        // Given
        Bulto bulto1 = new Bulto();
        bulto1.setNroBulto(1);

        DetalleMovimiento detalleVenta = new DetalleMovimiento();
        detalleVenta.setBulto(bulto1);
        detalleVenta.setCantidad(new BigDecimal("10"));
        detalleVenta.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        movimiento2.setDetalles(Set.of(detalleVenta));

        // Movimiento de devolución con unidad incorrecta
        Movimiento movDevolucion = new Movimiento();
        DetalleMovimiento detalleDevolucion = new DetalleMovimiento();
        detalleDevolucion.setBulto(bulto1);
        detalleDevolucion.setCantidad(new BigDecimal("3"));
        detalleDevolucion.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO); // Incorrecto

        movDevolucion.setDetalles(Set.of(detalleDevolucion));

        when(movimientoRepository.findMovimientosVentaByCodigoMovimiento("MOV-002"))
                .thenReturn(Optional.of(movimiento2));
        when(movimientoRepository.findByMovimientoOrigen("MOV-002"))
                .thenReturn(Arrays.asList(movDevolucion));

        // When & Then
        assertThatThrownBy(() -> service.calcularMaximoDevolucionPorBulto("MOV-002"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("La unidad de medida del movimiento de devolucion debe ser UNIDAD.");
    }

    @Test
    @DisplayName("test_calcularMaximoDevolucionPorBulto_devolucionCantidadCero_debe_ignorar")
    void test_calcularMaximoDevolucionPorBulto_devolucionCantidadCero_debe_ignorar() {
        // Given
        Bulto bulto1 = new Bulto();
        bulto1.setNroBulto(1);

        DetalleMovimiento detalleVenta = new DetalleMovimiento();
        detalleVenta.setBulto(bulto1);
        detalleVenta.setCantidad(new BigDecimal("10"));
        detalleVenta.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        movimiento2.setDetalles(Set.of(detalleVenta));

        // Movimiento de devolución con cantidad cero
        Movimiento movDevolucion = new Movimiento();
        DetalleMovimiento detalleDevolucion = new DetalleMovimiento();
        detalleDevolucion.setBulto(bulto1);
        detalleDevolucion.setCantidad(BigDecimal.ZERO); // Cantidad cero
        detalleDevolucion.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        movDevolucion.setDetalles(Set.of(detalleDevolucion));

        when(movimientoRepository.findMovimientosVentaByCodigoMovimiento("MOV-002"))
                .thenReturn(Optional.of(movimiento2));
        when(movimientoRepository.findByMovimientoOrigen("MOV-002"))
                .thenReturn(Arrays.asList(movDevolucion));

        // When
        List<Integer> resultado = service.calcularMaximoDevolucionPorBulto("MOV-002");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0)).isEqualTo(10); // No se restó nada
    }

    @Test
    @DisplayName("test_calcularMaximoDevolucionPorBulto_bultosSinConsecutivos_debe_rellenarConCeros")
    void test_calcularMaximoDevolucionPorBulto_bultosSinConsecutivos_debe_rellenarConCeros() {
        // Given
        Bulto bulto1 = new Bulto();
        bulto1.setNroBulto(1);
        Bulto bulto3 = new Bulto();
        bulto3.setNroBulto(3);

        // Movimiento de venta - Bultos 1 y 3 (falta el 2)
        DetalleMovimiento detalleVenta1 = new DetalleMovimiento();
        detalleVenta1.setBulto(bulto1);
        detalleVenta1.setCantidad(new BigDecimal("10"));
        detalleVenta1.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        DetalleMovimiento detalleVenta3 = new DetalleMovimiento();
        detalleVenta3.setBulto(bulto3);
        detalleVenta3.setCantidad(new BigDecimal("5"));
        detalleVenta3.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        movimiento2.setDetalles(Set.of(detalleVenta1, detalleVenta3));

        when(movimientoRepository.findMovimientosVentaByCodigoMovimiento("MOV-002"))
                .thenReturn(Optional.of(movimiento2));
        when(movimientoRepository.findByMovimientoOrigen("MOV-002"))
                .thenReturn(Collections.emptyList());

        // When
        List<Integer> resultado = service.calcularMaximoDevolucionPorBulto("MOV-002");

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(3);
        assertThat(resultado.get(0)).isEqualTo(10); // Bulto 1
        assertThat(resultado.get(1)).isEqualTo(0);  // Bulto 2 (no existe, relleno con 0)
        assertThat(resultado.get(2)).isEqualTo(5);  // Bulto 3
    }

    @Test
    @DisplayName("test_calcularMaximoDevolucionPorBulto_devolucionExcedente_debe_lanzarExcepcion")
    void test_calcularMaximoDevolucionPorBulto_devolucionExcedente_debe_lanzarExcepcion() {
        // Given
        Bulto bulto1 = new Bulto();
        bulto1.setNroBulto(1);

        DetalleMovimiento detalleVenta = new DetalleMovimiento();
        detalleVenta.setBulto(bulto1);
        detalleVenta.setCantidad(new BigDecimal("10"));
        detalleVenta.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        movimiento2.setDetalles(Set.of(detalleVenta));

        // Movimiento de devolución que excede la venta
        Movimiento movDevolucion = new Movimiento();
        DetalleMovimiento detalleDevolucion = new DetalleMovimiento();
        detalleDevolucion.setBulto(bulto1);
        detalleDevolucion.setCantidad(new BigDecimal("15")); // Más de lo vendido!
        detalleDevolucion.setUnidadMedida(UnidadMedidaEnum.UNIDAD);

        movDevolucion.setDetalles(Set.of(detalleDevolucion));

        when(movimientoRepository.findMovimientosVentaByCodigoMovimiento("MOV-002"))
                .thenReturn(Optional.of(movimiento2));
        when(movimientoRepository.findByMovimientoOrigen("MOV-002"))
                .thenReturn(Arrays.asList(movDevolucion));

        // When & Then
        assertThatThrownBy(() -> service.calcularMaximoDevolucionPorBulto("MOV-002"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("La cantidad de devuelta no puede ser negativa.");
    }
}

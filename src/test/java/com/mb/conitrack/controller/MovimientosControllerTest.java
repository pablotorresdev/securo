package com.mb.conitrack.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.service.MovimientoService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("MovimientosController Tests")
class MovimientosControllerTest {

    @Mock
    private MovimientoService movimientoService;

    @InjectMocks
    private MovimientosController controller;

    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        model = new ConcurrentModel();
    }

    @Test
    @DisplayName("cancelar debe redirigir a la página principal")
    void testCancelar() {
        // Act
        String viewName = controller.cancelar();

        // Assert
        assertEquals("redirect:/", viewName);
    }

    @Test
    @DisplayName("listMovimientos debe retornar vista con todos los movimientos")
    void testListMovimientos() {
        // Arrange
        List<MovimientoDTO> movimientos = new ArrayList<>();
        MovimientoDTO mov1 = new MovimientoDTO();
        movimientos.add(mov1);

        when(movimientoService.findAllMovimientos()).thenReturn(movimientos);

        // Act
        String viewName = controller.listMovimientos(model);

        // Assert
        assertEquals("movimientos/list-movimientos", viewName);
        assertEquals(movimientos, model.getAttribute("movimientoDTOs"));
        verify(movimientoService, times(1)).findAllMovimientos();
    }

    @Test
    @DisplayName("listMovimientos debe retornar vista con lista vacía cuando no hay movimientos")
    void testListMovimientos_Empty() {
        // Arrange
        when(movimientoService.findAllMovimientos()).thenReturn(new ArrayList<>());

        // Act
        String viewName = controller.listMovimientos(model);

        // Assert
        assertEquals("movimientos/list-movimientos", viewName);
        assertTrue(((List<?>) model.getAttribute("movimientoDTOs")).isEmpty());
        verify(movimientoService, times(1)).findAllMovimientos();
    }

    @Test
    @DisplayName("listBultosPorLote debe retornar vista con movimientos del lote especificado")
    void testListBultosPorLote() {
        // Arrange
        String codigoLote = "LOTE001";
        List<MovimientoDTO> movimientos = new ArrayList<>();
        MovimientoDTO mov1 = new MovimientoDTO();
        movimientos.add(mov1);

        when(movimientoService.findByCodigoLote(codigoLote)).thenReturn(movimientos);

        // Act
        String viewName = controller.listBultosPorLote(codigoLote, model);

        // Assert
        assertEquals("movimientos/list-movimientos", viewName);
        assertEquals(movimientos, model.getAttribute("movimientoDTOs"));
        verify(movimientoService, times(1)).findByCodigoLote(codigoLote);
    }

    @Test
    @DisplayName("maximosDevolucionPorBultoNoTrazado debe retornar ResponseEntity con lista de máximos")
    void testMaximosDevolucionPorBultoNoTrazado() {
        // Arrange
        String codigoMovimiento = "MOV001";
        List<Integer> maximos = Arrays.asList(10, 20, 30);

        when(movimientoService.calcularMaximoDevolucionPorBulto(codigoMovimiento)).thenReturn(maximos);

        // Act
        ResponseEntity<List<Integer>> response = controller.maximosDevolucionPorBultoNoTrazado(codigoMovimiento);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(maximos, response.getBody());
        verify(movimientoService, times(1)).calcularMaximoDevolucionPorBulto(codigoMovimiento);
    }

    @Test
    @DisplayName("maximosDevolucionPorBultoNoTrazado debe retornar lista vacía cuando no hay máximos")
    void testMaximosDevolucionPorBultoNoTrazado_Empty() {
        // Arrange
        String codigoMovimiento = "MOV999";
        when(movimientoService.calcularMaximoDevolucionPorBulto(codigoMovimiento)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<Integer>> response = controller.maximosDevolucionPorBultoNoTrazado(codigoMovimiento);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
        verify(movimientoService, times(1)).calcularMaximoDevolucionPorBulto(codigoMovimiento);
    }

    @Test
    @DisplayName("maximosRecallPorBultoNoTrazado debe retornar ResponseEntity con lista de máximos")
    void testMaximosRecallPorBultoNoTrazado() {
        // Arrange
        String codigoMovimiento = "MOV002";
        List<Integer> maximos = Arrays.asList(5, 15, 25);

        when(movimientoService.calcularMaximoRecallPorBulto(codigoMovimiento)).thenReturn(maximos);

        // Act
        ResponseEntity<List<Integer>> response = controller.maximosRecallPorBultoNoTrazado(codigoMovimiento);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(maximos, response.getBody());
        verify(movimientoService, times(1)).calcularMaximoRecallPorBulto(codigoMovimiento);
    }

    @Test
    @DisplayName("getMovimientosVentaByCodigolote debe retornar lista de movimientos de venta")
    void testGetMovimientosVentaByCodigolote() {
        // Arrange
        String codInterno = "INT001";
        List<MovimientoDTO> movimientos = new ArrayList<>();
        MovimientoDTO mov1 = new MovimientoDTO();
        movimientos.add(mov1);

        when(movimientoService.getMovimientosVentaByCodigolote(codInterno)).thenReturn(movimientos);

        // Act
        List<MovimientoDTO> result = controller.getMovimientosVentaByCodigolote(codInterno);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(movimientoService, times(1)).getMovimientosVentaByCodigolote(codInterno);
    }

    @Test
    @DisplayName("getMovimientosVentaByCodigolote debe retornar lista vacía cuando no hay movimientos")
    void testGetMovimientosVentaByCodigolote_Empty() {
        // Arrange
        String codInterno = "INT999";
        when(movimientoService.getMovimientosVentaByCodigolote(codInterno)).thenReturn(new ArrayList<>());

        // Act
        List<MovimientoDTO> result = controller.getMovimientosVentaByCodigolote(codInterno);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(movimientoService, times(1)).getMovimientosVentaByCodigolote(codInterno);
    }

    @Test
    @DisplayName("getUltimoMovimientoByCodigolote debe retornar el último movimiento")
    void testGetUltimoMovimientoByCodigolote() {
        // Arrange
        String codigoLote = "LOTE001";
        MovimientoDTO movimiento = new MovimientoDTO();
        movimiento.setCodigoMovimiento("MOV123");

        when(movimientoService.getUltimoMovimientosCodigolote(codigoLote)).thenReturn(movimiento);

        // Act
        MovimientoDTO result = controller.getUltimoMovimientoByCodigolote(codigoLote);

        // Assert
        assertNotNull(result);
        assertEquals("MOV123", result.getCodigoMovimiento());
        verify(movimientoService, times(1)).getUltimoMovimientosCodigolote(codigoLote);
    }

    @Test
    @DisplayName("getUltimoMovimientoByCodigolote debe retornar null cuando no hay movimientos")
    void testGetUltimoMovimientoByCodigolote_NotFound() {
        // Arrange
        String codigoLote = "LOTE999";
        when(movimientoService.getUltimoMovimientosCodigolote(codigoLote)).thenReturn(null);

        // Act
        MovimientoDTO result = controller.getUltimoMovimientoByCodigolote(codigoLote);

        // Assert
        assertNull(result);
        verify(movimientoService, times(1)).getUltimoMovimientosCodigolote(codigoLote);
    }
}

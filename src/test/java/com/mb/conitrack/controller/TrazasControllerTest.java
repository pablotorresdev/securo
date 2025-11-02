package com.mb.conitrack.controller;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.service.TrazaService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("TrazasController Tests")
class TrazasControllerTest {

    @Mock
    private TrazaService trazaService;

    @InjectMocks
    private TrazasController controller;

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
    @DisplayName("listTrazasActivas debe retornar vista con todas las trazas activas")
    void testListTrazasActivas() {
        // Arrange
        List<TrazaDTO> trazas = new ArrayList<>();
        TrazaDTO traza1 = new TrazaDTO();
        trazas.add(traza1);

        when(trazaService.findAllByActivoTrue()).thenReturn(trazas);

        // Act
        String viewName = controller.listTrazasActivas(model);

        // Assert
        assertEquals("trazas/list-trazas", viewName);
        assertEquals(trazas, model.getAttribute("trazaDTOs"));
        verify(trazaService, times(1)).findAllByActivoTrue();
    }

    @Test
    @DisplayName("listTrazasActivas debe retornar vista con lista vacía cuando no hay trazas")
    void testListTrazasActivas_Empty() {
        // Arrange
        when(trazaService.findAllByActivoTrue()).thenReturn(new ArrayList<>());

        // Act
        String viewName = controller.listTrazasActivas(model);

        // Assert
        assertEquals("trazas/list-trazas", viewName);
        assertTrue(((List<?>) model.getAttribute("trazaDTOs")).isEmpty());
        verify(trazaService, times(1)).findAllByActivoTrue();
    }

    @Test
    @DisplayName("listTrazasActivasPorLote debe retornar vista con trazas activas del lote")
    void testListTrazasActivasPorLote() {
        // Arrange
        String codigoLote = "LOTE001";
        List<TrazaDTO> trazas = new ArrayList<>();
        TrazaDTO traza1 = new TrazaDTO();
        trazas.add(traza1);

        when(trazaService.findByCodigoLoteAndActivo(codigoLote)).thenReturn(trazas);

        // Act
        String viewName = controller.listTrazasActivasPorLote(codigoLote, model);

        // Assert
        assertEquals("trazas/list-trazas", viewName);
        assertEquals(trazas, model.getAttribute("trazaDTOs"));
        verify(trazaService, times(1)).findByCodigoLoteAndActivo(codigoLote);
    }

    @Test
    @DisplayName("getTrazasVendidasPorMovimiento debe retornar lista de trazas vendidas")
    void testGetTrazasVendidasPorMovimiento() {
        // Arrange
        String codigoMovimiento = "MOV001";
        List<TrazaDTO> trazas = new ArrayList<>();
        TrazaDTO traza1 = new TrazaDTO();
        trazas.add(traza1);

        when(trazaService.getTrazasVendidasByCodigoMovimiento(codigoMovimiento)).thenReturn(trazas);

        // Act
        List<TrazaDTO> result = controller.getTrazasVendidasPorMovimiento(codigoMovimiento);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(trazaService, times(1)).getTrazasVendidasByCodigoMovimiento(codigoMovimiento);
    }

    @Test
    @DisplayName("getTrazasVendidasPorMovimiento debe retornar lista vacía cuando no hay trazas")
    void testGetTrazasVendidasPorMovimiento_Empty() {
        // Arrange
        String codigoMovimiento = "MOV999";
        when(trazaService.getTrazasVendidasByCodigoMovimiento(codigoMovimiento)).thenReturn(new ArrayList<>());

        // Act
        List<TrazaDTO> result = controller.getTrazasVendidasPorMovimiento(codigoMovimiento);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(trazaService, times(1)).getTrazasVendidasByCodigoMovimiento(codigoMovimiento);
    }

    @Test
    @DisplayName("getTrazasVendidasPorLote debe retornar lista de trazas vendidas del lote")
    void testGetTrazasVendidasPorLote() {
        // Arrange
        String codigoLote = "LOTE002";
        List<TrazaDTO> trazas = new ArrayList<>();
        TrazaDTO traza1 = new TrazaDTO();
        TrazaDTO traza2 = new TrazaDTO();
        trazas.add(traza1);
        trazas.add(traza2);

        when(trazaService.getTrazasVendidasByCodigoLote(codigoLote)).thenReturn(trazas);

        // Act
        List<TrazaDTO> result = controller.getTrazasVendidasPorLote(codigoLote);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(trazaService, times(1)).getTrazasVendidasByCodigoLote(codigoLote);
    }

    @Test
    @DisplayName("getTrazasDisponiblesPorBulto debe retornar lista de trazas disponibles")
    void testGetTrazasDisponiblesPorBulto() {
        // Arrange
        String codigoLote = "LOTE003";
        Integer nroBulto = 1;
        List<TrazaDTO> trazas = new ArrayList<>();
        TrazaDTO traza1 = new TrazaDTO();
        trazas.add(traza1);

        when(trazaService.getTrazasByCodigoLoteAndNroBulto(codigoLote, nroBulto)).thenReturn(trazas);

        // Act
        List<TrazaDTO> result = controller.getTrazasDisponiblesPorBulto(codigoLote, nroBulto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(trazaService, times(1)).getTrazasByCodigoLoteAndNroBulto(codigoLote, nroBulto);
    }

    @Test
    @DisplayName("getTrazasDisponiblesPorBulto debe funcionar con diferentes bultos")
    void testGetTrazasDisponiblesPorBulto_DifferentBultos() {
        // Arrange
        String codigoLote = "LOTE004";
        Integer nroBulto1 = 1;
        Integer nroBulto2 = 2;
        List<TrazaDTO> trazas1 = new ArrayList<>();
        List<TrazaDTO> trazas2 = new ArrayList<>();

        when(trazaService.getTrazasByCodigoLoteAndNroBulto(codigoLote, nroBulto1)).thenReturn(trazas1);
        when(trazaService.getTrazasByCodigoLoteAndNroBulto(codigoLote, nroBulto2)).thenReturn(trazas2);

        // Act
        List<TrazaDTO> result1 = controller.getTrazasDisponiblesPorBulto(codigoLote, nroBulto1);
        List<TrazaDTO> result2 = controller.getTrazasDisponiblesPorBulto(codigoLote, nroBulto2);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        verify(trazaService, times(1)).getTrazasByCodigoLoteAndNroBulto(codigoLote, nroBulto1);
        verify(trazaService, times(1)).getTrazasByCodigoLoteAndNroBulto(codigoLote, nroBulto2);
    }
}

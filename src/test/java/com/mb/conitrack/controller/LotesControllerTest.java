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

import com.mb.conitrack.dto.BultoDTO;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.service.LoteService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("LotesController Tests")
class LotesControllerTest {

    @Mock
    private LoteService loteService;

    @InjectMocks
    private LotesController controller;

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
    @DisplayName("listLotes debe retornar vista con todos los lotes")
    void testListLotes() {
        // Arrange
        List<LoteDTO> lotes = new ArrayList<>();
        LoteDTO lote1 = new LoteDTO();
        lotes.add(lote1);

        when(loteService.findAllLotes()).thenReturn(lotes);

        // Act
        String viewName = controller.listLotes(model);

        // Assert
        assertEquals("lotes/list-lotes", viewName);
        assertEquals(lotes, model.getAttribute("loteDTOs"));
        verify(loteService, times(1)).findAllLotes();
    }

    @Test
    @DisplayName("listLotes debe retornar vista con lista vacía cuando no hay lotes")
    void testListLotes_Empty() {
        // Arrange
        when(loteService.findAllLotes()).thenReturn(new ArrayList<>());

        // Act
        String viewName = controller.listLotes(model);

        // Assert
        assertEquals("lotes/list-lotes", viewName);
        assertTrue(((List<?>) model.getAttribute("loteDTOs")).isEmpty());
        verify(loteService, times(1)).findAllLotes();
    }

    @Test
    @DisplayName("listFechasLotes debe retornar vista con lotes dictaminados con stock")
    void testListFechasLotes() {
        // Arrange
        List<LoteDTO> lotes = new ArrayList<>();
        LoteDTO lote1 = new LoteDTO();
        lotes.add(lote1);

        when(loteService.findLotesDictaminadosConStock()).thenReturn(lotes);

        // Act
        String viewName = controller.listFechasLotes(model);

        // Assert
        assertEquals("lotes/list-fechas-lotes", viewName);
        assertEquals(lotes, model.getAttribute("loteDTOs"));
        verify(loteService, times(1)).findLotesDictaminadosConStock();
    }

    @Test
    @DisplayName("getBultosForMuestreoByCodigoLote debe retornar lista de bultos para muestreo")
    void testGetBultosForMuestreoByCodigoLote() {
        // Arrange
        String codigoLote = "LOTE001";
        List<BultoDTO> bultos = new ArrayList<>();
        BultoDTO bulto1 = new BultoDTO();
        bultos.add(bulto1);

        when(loteService.findBultosForMuestreoByCodigoLote(codigoLote)).thenReturn(bultos);

        // Act
        List<BultoDTO> result = controller.getBultosForMuestreoByCodigoLote(codigoLote);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(loteService, times(1)).findBultosForMuestreoByCodigoLote(codigoLote);
    }

    @Test
    @DisplayName("getBultosForMuestreoByCodigoLote debe retornar lista vacía cuando no hay bultos")
    void testGetBultosForMuestreoByCodigoLote_Empty() {
        // Arrange
        String codigoLote = "LOTE999";
        when(loteService.findBultosForMuestreoByCodigoLote(codigoLote)).thenReturn(new ArrayList<>());

        // Act
        List<BultoDTO> result = controller.getBultosForMuestreoByCodigoLote(codigoLote);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(loteService, times(1)).findBultosForMuestreoByCodigoLote(codigoLote);
    }

    @Test
    @DisplayName("getBultosForAjusteByCodigoLote debe retornar lista de bultos para ajuste")
    void testGetBultosForAjusteByCodigoLote() {
        // Arrange
        String codigoLote = "LOTE002";
        List<BultoDTO> bultos = new ArrayList<>();
        BultoDTO bulto1 = new BultoDTO();
        BultoDTO bulto2 = new BultoDTO();
        bultos.add(bulto1);
        bultos.add(bulto2);

        when(loteService.findBultosForAjusteByCodigoLote(codigoLote)).thenReturn(bultos);

        // Act
        List<BultoDTO> result = controller.getBultosForAjusteByCodigoLote(codigoLote);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(loteService, times(1)).findBultosForAjusteByCodigoLote(codigoLote);
    }

    @Test
    @DisplayName("getBultosForAjusteByCodigoLote debe funcionar con diferentes códigos de lote")
    void testGetBultosForAjusteByCodigoLote_DifferentCodes() {
        // Arrange
        String codigoLote1 = "ABC123";
        String codigoLote2 = "XYZ789";
        List<BultoDTO> bultos1 = new ArrayList<>();
        List<BultoDTO> bultos2 = new ArrayList<>();

        when(loteService.findBultosForAjusteByCodigoLote(codigoLote1)).thenReturn(bultos1);
        when(loteService.findBultosForAjusteByCodigoLote(codigoLote2)).thenReturn(bultos2);

        // Act
        List<BultoDTO> result1 = controller.getBultosForAjusteByCodigoLote(codigoLote1);
        List<BultoDTO> result2 = controller.getBultosForAjusteByCodigoLote(codigoLote2);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        verify(loteService, times(1)).findBultosForAjusteByCodigoLote(codigoLote1);
        verify(loteService, times(1)).findBultosForAjusteByCodigoLote(codigoLote2);
    }
}

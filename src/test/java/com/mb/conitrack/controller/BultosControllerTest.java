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
import com.mb.conitrack.service.BultoService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("BultosController Tests")
class BultosControllerTest {

    @Mock
    private BultoService bultoService;

    @InjectMocks
    private BultosController controller;

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
    @DisplayName("listBultos debe retornar vista con todos los bultos")
    void testListBultos() {
        // Arrange
        List<BultoDTO> bultos = new ArrayList<>();
        BultoDTO bulto1 = new BultoDTO();
        bultos.add(bulto1);

        when(bultoService.findAllBultos()).thenReturn(bultos);

        // Act
        String viewName = controller.listBultos(model);

        // Assert
        assertEquals("bultos/list-bultos", viewName);
        assertEquals(bultos, model.getAttribute("bultoDTOs"));
        verify(bultoService, times(1)).findAllBultos();
    }

    @Test
    @DisplayName("listBultos debe retornar vista con lista vacía cuando no hay bultos")
    void testListBultos_Empty() {
        // Arrange
        when(bultoService.findAllBultos()).thenReturn(new ArrayList<>());

        // Act
        String viewName = controller.listBultos(model);

        // Assert
        assertEquals("bultos/list-bultos", viewName);
        assertTrue(((List<?>) model.getAttribute("bultoDTOs")).isEmpty());
        verify(bultoService, times(1)).findAllBultos();
    }

    @Test
    @DisplayName("listBultosPorLote debe retornar vista con bultos del lote especificado")
    void testListBultosPorLote() {
        // Arrange
        String codigoLote = "LOTE001";
        List<BultoDTO> bultos = new ArrayList<>();
        BultoDTO bulto1 = new BultoDTO();
        bultos.add(bulto1);

        when(bultoService.findByCodigoLote(codigoLote)).thenReturn(bultos);

        // Act
        String viewName = controller.listBultosPorLote(codigoLote, model);

        // Assert
        assertEquals("bultos/list-bultos", viewName);
        assertEquals(bultos, model.getAttribute("bultoDTOs"));
        verify(bultoService, times(1)).findByCodigoLote(codigoLote);
    }

    @Test
    @DisplayName("listBultosPorLote debe funcionar con diferentes códigos de lote")
    void testListBultosPorLote_DifferentCodes() {
        // Arrange
        String codigoLote1 = "ABC123";
        String codigoLote2 = "XYZ789";
        List<BultoDTO> bultos1 = new ArrayList<>();
        List<BultoDTO> bultos2 = new ArrayList<>();

        when(bultoService.findByCodigoLote(codigoLote1)).thenReturn(bultos1);
        when(bultoService.findByCodigoLote(codigoLote2)).thenReturn(bultos2);

        // Act
        String viewName1 = controller.listBultosPorLote(codigoLote1, model);
        String viewName2 = controller.listBultosPorLote(codigoLote2, model);

        // Assert
        assertEquals("bultos/list-bultos", viewName1);
        assertEquals("bultos/list-bultos", viewName2);
        verify(bultoService, times(1)).findByCodigoLote(codigoLote1);
        verify(bultoService, times(1)).findByCodigoLote(codigoLote2);
    }
}

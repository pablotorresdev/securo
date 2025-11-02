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

import com.mb.conitrack.dto.AnalisisDTO;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.service.AnalisisService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AnalisisController Tests")
class AnalisisControllerTest {

    @Mock
    private AnalisisService analisisService;

    @InjectMocks
    private AnalisisController controller;

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
    @DisplayName("listAnalisis debe retornar vista con todos los análisis")
    void testListAnalisis() {
        // Arrange
        List<AnalisisDTO> analisis = new ArrayList<>();
        AnalisisDTO analisis1 = new AnalisisDTO();
        analisis.add(analisis1);

        when(analisisService.findAllAnalisis()).thenReturn(analisis);

        // Act
        String viewName = controller.listAnalisis(model);

        // Assert
        assertEquals("analisis/list-analisis", viewName);
        assertEquals(analisis, model.getAttribute("analisisDTOs"));
        verify(analisisService, times(1)).findAllAnalisis();
    }

    @Test
    @DisplayName("listAnalisis debe retornar vista con lista vacía cuando no hay análisis")
    void testListAnalisis_Empty() {
        // Arrange
        when(analisisService.findAllAnalisis()).thenReturn(new ArrayList<>());

        // Act
        String viewName = controller.listAnalisis(model);

        // Assert
        assertEquals("analisis/list-analisis", viewName);
        assertTrue(((List<?>) model.getAttribute("analisisDTOs")).isEmpty());
        verify(analisisService, times(1)).findAllAnalisis();
    }

    @Test
    @DisplayName("listAnalisisPorLote debe retornar vista con análisis del lote especificado")
    void testListAnalisisPorLote() {
        // Arrange
        String codigoLote = "LOTE001";
        List<AnalisisDTO> analisis = new ArrayList<>();
        AnalisisDTO analisis1 = new AnalisisDTO();
        analisis.add(analisis1);

        when(analisisService.findByCodigoLote(codigoLote)).thenReturn(analisis);

        // Act
        String viewName = controller.listAnalisisPorLote(codigoLote, model);

        // Assert
        assertEquals("analisis/list-analisis", viewName);
        assertEquals(analisis, model.getAttribute("analisisDTOs"));
        verify(analisisService, times(1)).findByCodigoLote(codigoLote);
    }

    @Test
    @DisplayName("analisisDetails debe retornar LoteDTO cuando el análisis existe")
    void testAnalisisDetails_Found() {
        // Arrange
        String nroAnalisis = "AN001";
        Analisis analisis = new Analisis();
        Lote lote = new Lote();
        lote.setId(1L);
        lote.setCodigoLote("LOTE001");
        analisis.setLote(lote);

        when(analisisService.findByNroAnalisis(nroAnalisis)).thenReturn(analisis);

        // Act
        LoteDTO result = controller.analisisDetails(nroAnalisis);

        // Assert
        assertNotNull(result);
        assertEquals("LOTE001", result.getCodigoLote());
        verify(analisisService, times(1)).findByNroAnalisis(nroAnalisis);
    }

    @Test
    @DisplayName("analisisDetails debe retornar LoteDTO vacío cuando el análisis no existe")
    void testAnalisisDetails_NotFound() {
        // Arrange
        String nroAnalisis = "AN999";
        when(analisisService.findByNroAnalisis(nroAnalisis)).thenReturn(null);

        // Act
        LoteDTO result = controller.analisisDetails(nroAnalisis);

        // Assert
        assertNotNull(result);
        assertNull(result.getCodigoLote()); // LoteDTO vacío
        verify(analisisService, times(1)).findByNroAnalisis(nroAnalisis);
    }

    @Test
    @DisplayName("analisisDetails debe funcionar con diferentes números de análisis")
    void testAnalisisDetails_DifferentNumbers() {
        // Arrange
        String nroAnalisis1 = "AN001";
        String nroAnalisis2 = "AN002";
        Analisis analisis1 = new Analisis();
        Analisis analisis2 = new Analisis();
        Lote lote1 = new Lote();
        Lote lote2 = new Lote();
        lote1.setCodigoLote("LOTE001");
        lote2.setCodigoLote("LOTE002");
        analisis1.setLote(lote1);
        analisis2.setLote(lote2);

        when(analisisService.findByNroAnalisis(nroAnalisis1)).thenReturn(analisis1);
        when(analisisService.findByNroAnalisis(nroAnalisis2)).thenReturn(analisis2);

        // Act
        LoteDTO result1 = controller.analisisDetails(nroAnalisis1);
        LoteDTO result2 = controller.analisisDetails(nroAnalisis2);

        // Assert
        assertEquals("LOTE001", result1.getCodigoLote());
        assertEquals("LOTE002", result2.getCodigoLote());
        verify(analisisService, times(1)).findByNroAnalisis(nroAnalisis1);
        verify(analisisService, times(1)).findByNroAnalisis(nroAnalisis2);
    }
}

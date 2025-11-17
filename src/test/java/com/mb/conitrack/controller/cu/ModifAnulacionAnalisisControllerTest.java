package com.mb.conitrack.controller.cu;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.mb.conitrack.dto.AnalisisDTO;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.cu.ModifAnulacionAnalisisService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModifAnulacionAnalisisControllerTest {

    @Spy
    @InjectMocks
    ModifAnulacionAnalisisController controller;

    @Mock
    ModifAnulacionAnalisisService anulacionAnalisisService;

    @Mock
    AnalisisService analisisService;

    Model model;
    RedirectAttributes redirect;
    MovimientoDTO movDto;
    BindingResult binding;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        redirect = new RedirectAttributesModelMap();
        movDto = new MovimientoDTO();
        binding = new BeanPropertyBindingResult(movDto, "movimientoDTO");
    }

    @Test
    void testCancelar() {
        assertEquals("redirect:/", controller.cancelar());
    }

    @Test
    void testShowAnulacionAnalisisForm() {
        List<AnalisisDTO> analisis = List.of(new AnalisisDTO());
        when(analisisService.findAllEnCursoDTOs()).thenReturn(analisis);

        String view = controller.showAnulacionAnalisisForm(movDto, model);

        assertEquals("calidad/anulacion/anulacion-analisis", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
        assertSame(analisis, model.getAttribute("analisisDTOs"));
        assertNotNull(movDto.getFechaMovimiento());
    }

    @Test
    void testAnulacionAnalisis_ConValidacionExitosa() {
        LoteDTO resultDTO = new LoteDTO();
        when(anulacionAnalisisService.validarAnulacionAnalisisInput(movDto, binding)).thenReturn(true);
        when(anulacionAnalisisService.persistirAnulacionAnalisis(any(MovimientoDTO.class))).thenReturn(resultDTO);

        String view = controller.anulacionAnalisis(movDto, binding, model, redirect);

        assertEquals("redirect:/calidad/anulacion/anulacion-analisis-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("loteDTO"));
        assertNotNull(redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testAnulacionAnalisis_ConValidacionFallida() {
        List<AnalisisDTO> analisis = List.of(new AnalisisDTO());
        when(anulacionAnalisisService.validarAnulacionAnalisisInput(movDto, binding)).thenReturn(false);
        when(analisisService.findAllEnCursoDTOs()).thenReturn(analisis);

        String view = controller.anulacionAnalisis(movDto, binding, model, redirect);

        assertEquals("calidad/anulacion/anulacion-analisis", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
    }

    @Test
    void testAnulacionAnalisis_ConErrorEnPersistencia() {
        when(anulacionAnalisisService.validarAnulacionAnalisisInput(movDto, binding)).thenReturn(true);
        when(anulacionAnalisisService.persistirAnulacionAnalisis(any(MovimientoDTO.class))).thenReturn(null);

        String view = controller.anulacionAnalisis(movDto, binding, model, redirect);

        assertEquals("redirect:/calidad/anulacion/anulacion-analisis-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testExitoAnulacionAnalisis() {
        LoteDTO loteDTO = new LoteDTO();

        String view = controller.exitoAnulacionAnalisis(loteDTO);

        assertEquals("calidad/anulacion/anulacion-analisis-ok", view);
    }
}

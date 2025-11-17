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

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.cu.ModifTrazadoLoteService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModifTrazadoLoteControllerTest {

    @Spy
    @InjectMocks
    ModifTrazadoLoteController controller;

    @Mock
    ModifTrazadoLoteService modifTrazadoLoteService;

    @Mock
    LoteService loteService;

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
    void testShowTrazadoLoteForm() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(loteService.findAllForTrazadoLoteDTOs()).thenReturn(lotes);

        String view = controller.showTrazadoLoteForm(movDto, model);

        assertEquals("ventas/trazado/inicio-trazado", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
        assertSame(lotes, model.getAttribute("loteTrazadoDtos"));
    }

    @Test
    void testTrazadoLote_ConValidacionExitosa() {
        LoteDTO resultDTO = new LoteDTO();
        when(modifTrazadoLoteService.validarTrazadoLoteInput(movDto, binding)).thenReturn(true);
        when(modifTrazadoLoteService.persistirTrazadoLote(any(MovimientoDTO.class))).thenReturn(resultDTO);

        String view = controller.trazadoLote(movDto, binding, model, redirect);

        assertEquals("redirect:/ventas/trazado/inicio-trazado-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("loteDTO"));
        assertNotNull(redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testTrazadoLote_ConValidacionFallida() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(modifTrazadoLoteService.validarTrazadoLoteInput(movDto, binding)).thenReturn(false);
        when(loteService.findAllForTrazadoLoteDTOs()).thenReturn(lotes);

        String view = controller.trazadoLote(movDto, binding, model, redirect);

        assertEquals("ventas/trazado/inicio-trazado", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
    }

    @Test
    void testTrazadoLote_ConErrorEnPersistencia() {
        when(modifTrazadoLoteService.validarTrazadoLoteInput(movDto, binding)).thenReturn(true);
        when(modifTrazadoLoteService.persistirTrazadoLote(any(MovimientoDTO.class))).thenReturn(null);

        String view = controller.trazadoLote(movDto, binding, model, redirect);

        assertEquals("redirect:/ventas/trazado/inicio-trazado-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testExitoTrazadoLote() {
        LoteDTO loteDTO = new LoteDTO();

        String view = controller.exitoTrazadoLote(loteDTO);

        assertEquals("ventas/trazado/inicio-trazado-ok", view);
    }
}

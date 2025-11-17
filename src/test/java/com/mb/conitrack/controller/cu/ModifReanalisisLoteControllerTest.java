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
import com.mb.conitrack.service.cu.ModifReanalisisLoteService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModifReanalisisLoteControllerTest {

    @Spy
    @InjectMocks
    ModifReanalisisLoteController controller;

    @Mock
    ModifReanalisisLoteService reanalisisLoteService;

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
    void testShowReanalisisLoteForm() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(loteService.findAllForReanalisisLoteDTOs()).thenReturn(lotes);

        String view = controller.showReanalisisLoteForm(movDto, model);

        assertEquals("calidad/reanalisis/inicio-reanalisis", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
        assertSame(lotes, model.getAttribute("loteReanalisisDTOs"));
    }

    @Test
    void testReanalisisLote_ConValidacionExitosa() {
        LoteDTO resultDTO = new LoteDTO();
        when(reanalisisLoteService.validarReanalisisLoteInput(movDto, binding)).thenReturn(true);
        when(reanalisisLoteService.persistirReanalisisLote(any(MovimientoDTO.class))).thenReturn(resultDTO);

        String view = controller.reanalisisLote(movDto, binding, model, redirect);

        assertEquals("redirect:/calidad/reanalisis/inicio-reanalisis-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("loteDTO"));
        assertNotNull(redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testReanalisisLote_ConValidacionFallida() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(reanalisisLoteService.validarReanalisisLoteInput(movDto, binding)).thenReturn(false);
        when(loteService.findAllForReanalisisLoteDTOs()).thenReturn(lotes);

        String view = controller.reanalisisLote(movDto, binding, model, redirect);

        assertEquals("calidad/reanalisis/inicio-reanalisis", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
    }

    @Test
    void testReanalisisLote_ConErrorEnPersistencia() {
        when(reanalisisLoteService.validarReanalisisLoteInput(movDto, binding)).thenReturn(true);
        when(reanalisisLoteService.persistirReanalisisLote(any(MovimientoDTO.class))).thenReturn(null);

        String view = controller.reanalisisLote(movDto, binding, model, redirect);

        assertEquals("redirect:/calidad/reanalisis/inicio-reanalisis-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testExitoReanalisisLote() {
        LoteDTO loteDTO = new LoteDTO();

        String view = controller.exitoReanalisisLote(loteDTO);

        assertEquals("calidad/reanalisis/inicio-reanalisis-ok", view);
    }
}

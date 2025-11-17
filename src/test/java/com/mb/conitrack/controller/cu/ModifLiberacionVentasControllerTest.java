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
import com.mb.conitrack.service.cu.ModifLiberacionVentasService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModifLiberacionVentasControllerTest {

    @Spy
    @InjectMocks
    ModifLiberacionVentasController controller;

    @Mock
    ModifLiberacionVentasService modifLiberacionVentasService;

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
    void testShowLiberacionProductoForm() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(loteService.findAllForLiberacionProductoDTOs()).thenReturn(lotes);

        String view = controller.showLiberacionProductoForm(movDto, model);

        assertEquals("ventas/liberacion/inicio-liberacion", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
        assertSame(lotes, model.getAttribute("loteLiberacionProdDtos"));
    }

    @Test
    void testLiberacionProducto_ConValidacionExitosa() {
        LoteDTO resultDTO = new LoteDTO();
        when(modifLiberacionVentasService.validarLiberacionProductoInput(movDto, binding)).thenReturn(true);
        when(modifLiberacionVentasService.persistirLiberacionProducto(any(MovimientoDTO.class))).thenReturn(resultDTO);

        String view = controller.liberacionProducto(movDto, binding, model, redirect);

        assertEquals("redirect:/ventas/liberacion/inicio-liberacion-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("loteDTO"));
        assertNotNull(redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testLiberacionProducto_ConValidacionFallida() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(modifLiberacionVentasService.validarLiberacionProductoInput(movDto, binding)).thenReturn(false);
        when(loteService.findAllForLiberacionProductoDTOs()).thenReturn(lotes);

        String view = controller.liberacionProducto(movDto, binding, model, redirect);

        assertEquals("ventas/liberacion/inicio-liberacion", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
    }

    @Test
    void testLiberacionProducto_ConErrorEnPersistencia() {
        when(modifLiberacionVentasService.validarLiberacionProductoInput(movDto, binding)).thenReturn(true);
        when(modifLiberacionVentasService.persistirLiberacionProducto(any(MovimientoDTO.class))).thenReturn(null);

        String view = controller.liberacionProducto(movDto, binding, model, redirect);

        assertEquals("redirect:/ventas/liberacion/inicio-liberacion-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testExitoLiberacionProducto() {
        LoteDTO loteDTO = new LoteDTO();

        String view = controller.exitoLiberacionProducto(loteDTO);

        assertEquals("ventas/liberacion/inicio-liberacion-ok", view);
    }
}

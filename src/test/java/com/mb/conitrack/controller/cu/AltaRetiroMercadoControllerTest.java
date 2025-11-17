package com.mb.conitrack.controller.cu;

import java.util.Arrays;
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
import com.mb.conitrack.service.cu.ModifRetiroMercadoService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AltaRetiroMercadoControllerTest {

    @Spy
    @InjectMocks
    AltaRetiroMercadoController controller;

    @Mock
    ModifRetiroMercadoService retiroMercadoService;

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
    void testShowRetiroMercadoForm() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(loteService.findAllForRecallDTOs()).thenReturn(lotes);

        String view = controller.showRetiroMercadoForm(movDto, model);

        assertEquals("ventas/recall/retiro-mercado", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
        assertSame(lotes, model.getAttribute("lotesRecall"));
    }

    @Test
    void testConfirmRetiroMercado_ConValidacionExitosa() {
        when(retiroMercadoService.validarRetiroMercadoInput(movDto, binding)).thenReturn(true);

        String view = controller.confirmRetiroMercado(movDto, binding, model);

        assertEquals("ventas/recall/retiro-mercado-confirm", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
    }

    @Test
    void testConfirmRetiroMercado_ConValidacionFallida() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(retiroMercadoService.validarRetiroMercadoInput(movDto, binding)).thenReturn(false);
        when(loteService.findAllForRecallDTOs()).thenReturn(lotes);

        String view = controller.confirmRetiroMercado(movDto, binding, model);

        assertEquals("ventas/recall/retiro-mercado", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
    }

    @Test
    void testRetiroMercado_ConValidacionExitosa() {
        LoteDTO loteRecall = new LoteDTO();
        LoteDTO loteVenta = new LoteDTO();
        List<LoteDTO> resultList = Arrays.asList(loteRecall, loteVenta);

        when(retiroMercadoService.validarRetiroMercadoInput(movDto, binding)).thenReturn(true);
        when(retiroMercadoService.persistirRetiroMercado(any(MovimientoDTO.class))).thenReturn(resultList);

        String view = controller.retiroMercado(movDto, binding, model, redirect);

        assertEquals("redirect:/ventas/recall/retiro-mercado-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("loteRecallDTO"));
        assertNotNull(redirect.getFlashAttributes().get("loteVentaDTO"));
        assertNotNull(redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testRetiroMercado_ConValidacionFallida() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(retiroMercadoService.validarRetiroMercadoInput(movDto, binding)).thenReturn(false);
        when(loteService.findAllForRecallDTOs()).thenReturn(lotes);

        String view = controller.retiroMercado(movDto, binding, model, redirect);

        assertEquals("ventas/recall/retiro-mercado", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
    }

    @Test
    void testRetiroMercado_ConErrorEnPersistencia() {
        when(retiroMercadoService.validarRetiroMercadoInput(movDto, binding)).thenReturn(true);
        when(retiroMercadoService.persistirRetiroMercado(any(MovimientoDTO.class))).thenReturn(null);

        String view = controller.retiroMercado(movDto, binding, model, redirect);

        assertEquals("redirect:/ventas/recall/retiro-mercado-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testRetiroMercado_ConListaVacia() {
        List<LoteDTO> emptyList = List.of();
        when(retiroMercadoService.validarRetiroMercadoInput(movDto, binding)).thenReturn(true);
        when(retiroMercadoService.persistirRetiroMercado(any(MovimientoDTO.class))).thenReturn(emptyList);

        String view = controller.retiroMercado(movDto, binding, model, redirect);

        assertEquals("redirect:/ventas/recall/retiro-mercado-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testExitoRetiroMercado() {
        LoteDTO loteDTO = new LoteDTO();

        String view = controller.exitoRetiroMercado(loteDTO);

        assertEquals("ventas/recall/retiro-mercado-ok", view);
    }
}

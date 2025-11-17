package com.mb.conitrack.controller.cu;

import java.util.ArrayList;
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
import com.mb.conitrack.service.cu.BajaAjusteStockService;
import com.mb.conitrack.service.cu.ModifReversoMovimientoService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContingenciasControllerTest {

    @Spy
    @InjectMocks
    ContingenciasController controller;

    @Mock
    ModifReversoMovimientoService reeversoMovimientoService;

    @Mock
    BajaAjusteStockService ajusteStockService;

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

    // ===== Tests para Reverso Movimiento =====

    @Test
    void testShowReversoMovimientoForm() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(loteService.findAllForReversoMovimientoDTOs()).thenReturn(lotes);

        String view = controller.showReversoMovimientoForm(movDto, model);

        assertEquals("contingencias/reverso-movimiento", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
        assertSame(lotes, model.getAttribute("loteReversoDTOs"));
    }

    @Test
    void testReversoMovimiento_ConValidacionExitosa() {
        LoteDTO resultDTO = new LoteDTO();
        when(reeversoMovimientoService.persistirReversoMovmiento(any(MovimientoDTO.class))).thenReturn(resultDTO);

        String view = controller.ReversoMovimiento(movDto, binding, model, redirect);

        assertEquals("redirect:/contingencias/reverso-movimiento-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("loteDTO"));
        assertNotNull(redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testReversoMovimiento_ConErroresDeValidacion() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        binding.reject("error", "Error de validación");
        when(loteService.findAllForReversoMovimientoDTOs()).thenReturn(lotes);

        String view = controller.ReversoMovimiento(movDto, binding, model, redirect);

        assertEquals("contingencias/reverso-movimiento", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
    }

    @Test
    void testReversoMovimiento_ConIllegalStateException() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(loteService.findAllForReversoMovimientoDTOs()).thenReturn(lotes);
        when(reeversoMovimientoService.persistirReversoMovmiento(any(MovimientoDTO.class)))
            .thenThrow(new IllegalStateException("Error de estado"));

        String view = controller.ReversoMovimiento(movDto, binding, model, redirect);

        assertEquals("contingencias/reverso-movimiento", view);
        assertNotNull(binding.getFieldError("codigoLote"));
    }

    @Test
    void testReversoMovimiento_ConErrorEnPersistencia() {
        when(reeversoMovimientoService.persistirReversoMovmiento(any(MovimientoDTO.class))).thenReturn(null);

        String view = controller.ReversoMovimiento(movDto, binding, model, redirect);

        assertEquals("redirect:/contingencias/reverso-movimiento-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testExitoReversoMovimiento() {
        LoteDTO loteDTO = new LoteDTO();

        String view = controller.exitoReversoMovimiento(loteDTO);

        assertEquals("contingencias/reverso-movimiento-ok", view);
    }

    // ===== Tests para Ajuste Stock =====

    @Test
    void testShowAjusteStockForm() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(loteService.findAllForAjusteDTOs()).thenReturn(lotes);

        String view = controller.showAjusteStockForm(movDto, model);

        assertEquals("contingencias/ajuste-stock", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
        assertSame(lotes, model.getAttribute("loteAjusteDTOs"));
    }

    @Test
    void testAjusteStock_ConValidacionExitosa() {
        LoteDTO resultDTO = new LoteDTO();
        movDto.setTrazaDTOs(new ArrayList<>());
        when(ajusteStockService.validarAjusteStockInput(movDto, binding)).thenReturn(true);
        when(ajusteStockService.bajaAjusteStock(any(MovimientoDTO.class))).thenReturn(resultDTO);

        String view = controller.ajusteStock(movDto, binding, model, redirect);

        assertEquals("redirect:/contingencias/ajuste-stock-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("loteDTO"));
        assertNotNull(redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testAjusteStock_ConValidacionFallida() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(ajusteStockService.validarAjusteStockInput(movDto, binding)).thenReturn(false);
        when(loteService.findAllForAjusteDTOs()).thenReturn(lotes);

        String view = controller.ajusteStock(movDto, binding, model, redirect);

        assertEquals("contingencias/ajuste-stock", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
    }

    @Test
    void testAjusteStock_ConErrorEnPersistencia() {
        movDto.setTrazaDTOs(new ArrayList<>());
        when(ajusteStockService.validarAjusteStockInput(movDto, binding)).thenReturn(true);
        when(ajusteStockService.bajaAjusteStock(any(MovimientoDTO.class))).thenReturn(null);

        String view = controller.ajusteStock(movDto, binding, model, redirect);

        assertEquals("redirect:/contingencias/ajuste-stock-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testExitoAjuste() {
        LoteDTO loteDTO = new LoteDTO();

        String view = controller.exitoAjuste(loteDTO);

        assertEquals("contingencias/ajuste-stock-ok", view);
    }
}

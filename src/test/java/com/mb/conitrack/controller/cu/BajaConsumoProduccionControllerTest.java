package com.mb.conitrack.controller.cu;

import java.math.BigDecimal;
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
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.cu.BajaConsumoProduccionService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BajaConsumoProduccionControllerTest {

    @Spy
    @InjectMocks
    BajaConsumoProduccionController controller;

    @Mock
    BajaConsumoProduccionService consumoProduccionService;

    @Mock
    LoteService loteService;

    Model model;
    RedirectAttributes redirect;
    LoteDTO loteDTO;
    BindingResult binding;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        redirect = new RedirectAttributesModelMap();
        loteDTO = new LoteDTO();
        binding = new BeanPropertyBindingResult(loteDTO, "loteDTO");
    }

    @Test
    void testCancelar() {
        assertEquals("redirect:/", controller.cancelar());
    }

    @Test
    void testShowConsumoProduccionForm() {
        List<LoteDTO> lotes = List.of(new LoteDTO(), new LoteDTO());
        when(loteService.findAllForConsumoProduccionDTOs()).thenReturn(lotes);

        String view = controller.showConsumoProduccionForm(loteDTO, model);

        assertEquals("produccion/baja/consumo-produccion", view);
        assertSame(loteDTO, model.getAttribute("loteDTO"));
        assertSame(lotes, model.getAttribute("loteProduccionDTOs"));
    }

    @Test
    void testConsumoProduccion_ConValidacionExitosa() {
        loteDTO.setCodigoLote("LOTE-001");
        loteDTO.setOrdenProduccion("OP-001");
        loteDTO.setCantidadActual(BigDecimal.valueOf(100));

        LoteDTO resultDTO = new LoteDTO();
        resultDTO.setCodigoLote("LOTE-001");
        resultDTO.setOrdenProduccion("OP-001");

        when(consumoProduccionService.validarConsumoProduccionInput(loteDTO, binding)).thenReturn(true);
        when(consumoProduccionService.bajaConsumoProduccion(any(LoteDTO.class))).thenReturn(resultDTO);

        String view = controller.consumoProduccion(loteDTO, binding, model, redirect);

        assertEquals("redirect:/produccion/baja/consumo-produccion-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("loteDTO"));
        assertEquals("Consumo registrado correctamente para la orden OP-001",
            redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testConsumoProduccion_ConValidacionFallida() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(consumoProduccionService.validarConsumoProduccionInput(loteDTO, binding)).thenReturn(false);
        when(loteService.findAllForConsumoProduccionDTOs()).thenReturn(lotes);

        String view = controller.consumoProduccion(loteDTO, binding, model, redirect);

        assertEquals("produccion/baja/consumo-produccion", view);
        assertSame(lotes, model.getAttribute("loteProduccionDTOs"));
    }

    @Test
    void testConsumoProduccion_ConResultadoNull() {
        when(consumoProduccionService.validarConsumoProduccionInput(loteDTO, binding)).thenReturn(true);
        when(consumoProduccionService.bajaConsumoProduccion(any(LoteDTO.class))).thenReturn(null);

        String view = controller.consumoProduccion(loteDTO, binding, model, redirect);

        assertEquals("redirect:/produccion/baja/consumo-produccion-ok", view);
        assertEquals("Hubo un error en el consumo de stock por produccón.",
            redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testExitoConsumoProduccion() {
        assertEquals("produccion/baja/consumo-produccion-ok",
            controller.exitoConsumoProduccion(loteDTO));
    }
}

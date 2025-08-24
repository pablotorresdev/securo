package com.mb.conitrack.controller.cu;

import java.util.List;
import java.util.Map;

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
import com.mb.conitrack.service.cu.BajaDevolucionCompraService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests para BajaDevolucionCompraController
 */
@ExtendWith(MockitoExtension.class)
class BajaDevolucionCompraControllerTest {

    @Spy
    @InjectMocks
    BajaDevolucionCompraController controller;

    @Mock
    BajaDevolucionCompraService devolucionService;

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

    // -------------------- GET /cancelar --------------------

    @Test
    void testCancel() {
        assertEquals("redirect:/", controller.cancelar());
    }

    // -------------------- GET /devolucion-compra --------------------

    @Test
    void testExitoDevolucionCompra() {
        assertEquals("compras/baja/devolucion-compra-ok", controller.exitoDevolucionCompra(new LoteDTO()));
    }

    // -------------------- GET /devolucion-compra-ok --------------------

    // -------------------- POST /devolucion-compra (handler) --------------------

    @Test
    void testShowDevolucionCompraForm_InicializaModeloYRetornaView() {
        // given
        var lista = List.of(new LoteDTO(), new LoteDTO());
        when(loteService.findAllForDevolucionCompraDTOs()).thenReturn(lista);

        // when
        String view = controller.showDevolucionCompraForm(movDto, model);

        // then
        assertEquals("compras/baja/devolucion-compra", view);
        assertSame(lista, model.getAttribute("lotesDevolvibles"));
    }

    // -------------------- validarDevolucionCompra (método no-private) --------------------

}

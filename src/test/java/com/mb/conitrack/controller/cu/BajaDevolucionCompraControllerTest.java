package com.mb.conitrack.controller.cu;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.QueryServiceLote;
import com.mb.conitrack.utils.ControllerUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class BajaDevolucionCompraControllerTest {

    @Spy
    @InjectMocks
    BajaDevolucionCompraController controller;

    @Mock
    LoteService loteService;

    @Mock
    QueryServiceLote queryServiceLote;

    Model model;

    RedirectAttributes redirect;

    MovimientoDTO dto;

    BindingResult binding;

    @Test
    @DisplayName("GET /cancelar -> redirect:/")
    void cancelar() {
        assertEquals("redirect:/", controller.cancelar());
    }

    /* ------------------ cancelar / ok ------------------ */

    @Test
    @DisplayName("GET /devolucion-compra-ok -> vista de éxito")
    void exitoDevolucionCompra() {
        assertEquals("compras/baja/devolucion-compra-ok", controller.exitoDevolucionCompra(new LoteDTO()));
    }

    @Test
    @DisplayName("Pasan Lote y fechaMovimiento, falla validarFechaAnalisisPosteriorIngresoLote -> vuelve al form")
    void fallaFechaAnalisis() {
        Lote lote = new Lote();

        try (MockedStatic<ControllerUtils> ms = mockStatic(ControllerUtils.class)) {
            ControllerUtils utils = mock(ControllerUtils.class);
            ms.when(ControllerUtils::getInstance).thenReturn(utils);

            when(utils.getLoteByCodigoInterno(eq("COD-DEV-001"), eq(binding), eq(queryServiceLote)))
                .thenReturn(lote);
            when(utils.validarFechaMovimientoPosteriorIngresoLote(dto, lote, binding)).thenReturn(true);
            when(utils.validarFechaAnalisisPosteriorIngresoLote(dto, lote, binding)).thenReturn(false);

            doNothing().when(controller).initModelDevolucionCompra(any());

            String view = controller.procesarDevolucionCompra(dto, binding, model, redirect);

            assertEquals("compras/baja/devolucion-compra", view);
            verify(utils).validarFechaMovimientoPosteriorIngresoLote(dto, lote, binding);
            verify(utils).validarFechaAnalisisPosteriorIngresoLote(dto, lote, binding);
            verify(controller).initModelDevolucionCompra(model);
            verify(controller, never()).procesarDevolucionCompra(any(), any(), any());
            assertSame(dto, model.getAttribute("movimientoDTO"));
        }
    }

    @Test
    @DisplayName("Pasa Lote, falla validarFechaMovimientoPosteriorIngresoLote -> vuelve al form")
    void fallaFechaMovimiento() {
        Lote lote = new Lote();

        try (MockedStatic<ControllerUtils> ms = mockStatic(ControllerUtils.class)) {
            ControllerUtils utils = mock(ControllerUtils.class);
            ms.when(ControllerUtils::getInstance).thenReturn(utils);

            when(utils.getLoteByCodigoInterno(eq("COD-DEV-001"), eq(binding), eq(queryServiceLote)))
                .thenReturn(lote);
            when(utils.validarFechaMovimientoPosteriorIngresoLote(dto, lote, binding)).thenReturn(false);

            doNothing().when(controller).initModelDevolucionCompra(any());

            String view = controller.procesarDevolucionCompra(dto, binding, model, redirect);

            assertEquals("compras/baja/devolucion-compra", view);
            verify(utils).validarFechaMovimientoPosteriorIngresoLote(dto, lote, binding);
            verify(utils, never()).validarFechaAnalisisPosteriorIngresoLote(any(), any(), any());
            verify(controller).initModelDevolucionCompra(model);
            verify(controller, never()).procesarDevolucionCompra(any(), any(), any());
            assertSame(dto, model.getAttribute("movimientoDTO"));
        }
    }

    @Test
    @DisplayName("initModelDevolucionCompra agrega 'lotesDevolvibles' desde DTOUtils.fromEntities")
    void initModelDevolucionCompra_ok() {
        List<Lote> entrada = List.of(new Lote());
        List<LoteDTO> salida = List.of(new LoteDTO());
        when(queryServiceLote.findAllForDevolucionCompra()).thenReturn(entrada);

        try (MockedStatic<DTOUtils> ms = mockStatic(DTOUtils.class)) {
            ms.when(() -> DTOUtils.fromLoteEntities(entrada)).thenReturn(salida);

            controller.initModelDevolucionCompra(model);

            assertSame(salida, model.getAttribute("lotesDevolvibles"));
            verify(queryServiceLote).findAllForDevolucionCompra();
            ms.verify(() -> DTOUtils.fromLoteEntities(entrada));
        }
    }

    /* ------------------ init model ------------------ */

    @Test
    @DisplayName("Falla: no encuentra Lote -> vuelve al form e inicializa modelo")
    void noEncuentraLote() {
        try (MockedStatic<ControllerUtils> ms = mockStatic(ControllerUtils.class)) {
            ControllerUtils utils = mock(ControllerUtils.class);
            ms.when(ControllerUtils::getInstance).thenReturn(utils);

            when(utils.getLoteByCodigoInterno(eq("COD-DEV-001"), eq(binding), eq(queryServiceLote)))
                .thenReturn(null);

            // Evitar que haga lógica pesada dentro del init
            doNothing().when(controller).initModelDevolucionCompra(any());

            String view = controller.procesarDevolucionCompra(dto, binding, model, redirect);

            assertEquals("compras/baja/devolucion-compra", view);
            verify(utils).getLoteByCodigoInterno("COD-DEV-001", binding, queryServiceLote);
            verify(controller).initModelDevolucionCompra(model);
            verify(controller, never()).procesarDevolucionCompra(any(), any(), any());
            assertSame(dto, model.getAttribute("movimientoDTO"));
        }
    }

    /* ------------------ POST /devolucion-compra (errores) ------------------ */

    @Test
    @DisplayName("procesarDevolucionCompra: merge null -> agrega loteDTO=null y flash 'error'")
    void procesar_error() {
        MovimientoDTO mov = new MovimientoDTO();
        Lote lote = new Lote();
        RedirectAttributes ra = new RedirectAttributesModelMap();

        Lote persistido = new Lote();
        when(loteService.bajaBultosDevolucionCompra(mov, lote)).thenReturn(persistido);

        try (MockedStatic<DTOUtils> ms = mockStatic(DTOUtils.class)) {
            ms.when(() -> DTOUtils.fromLoteEntity(persistido)).thenReturn(null);

            controller.procesarDevolucionCompra(mov, lote, ra);

            assertNotNull(mov.getFechaYHoraCreacion());
            verify(loteService).bajaBultosDevolucionCompra(mov, lote);
            ms.verify(() -> DTOUtils.fromLoteEntity(persistido));

            Map<String, ?> flash = ra.getFlashAttributes();
            assertTrue(flash.containsKey("loteDTO"));
            assertNull(flash.get("loteDTO"));
            assertEquals("Hubo un error en la devolucion de compra.", flash.get("error"));
            assertFalse(flash.containsKey("success"));
        }
    }

    @Test
    @DisplayName("procesarDevolucionCompra: merge ok -> agrega loteDTO y flash 'success'")
    void procesar_ok() {
        MovimientoDTO mov = new MovimientoDTO();
        Lote lote = new Lote();
        RedirectAttributes ra = new RedirectAttributesModelMap();

        Lote persistido = new Lote();
        LoteDTO merged = new LoteDTO();

        when(loteService.bajaBultosDevolucionCompra(mov, lote)).thenReturn(persistido);

        try (MockedStatic<DTOUtils> ms = mockStatic(DTOUtils.class)) {
            ms.when(() -> DTOUtils.fromLoteEntity(persistido)).thenReturn(merged);

            controller.procesarDevolucionCompra(mov, lote, ra);

            assertNotNull(mov.getFechaYHoraCreacion());
            assertTrue(mov.getFechaYHoraCreacion().isBefore(LocalDateTime.now().plusSeconds(2)));

            verify(loteService).bajaBultosDevolucionCompra(mov, lote);
            ms.verify(() -> DTOUtils.fromLoteEntity(persistido));

            Map<String, ?> flash = ra.getFlashAttributes();
            assertSame(merged, flash.get("loteDTO"));
            assertEquals("Devolucion realizada correctamente.", flash.get("success"));
            assertFalse(flash.containsKey("error"));
        }
    }

    @BeforeEach
    void setup() {
        openMocks(this);
        model = new ExtendedModelMap();
        redirect = new RedirectAttributesModelMap();
        dto = new MovimientoDTO();
        dto.setCodigoInternoLote("COD-DEV-001");
        binding = new BeanPropertyBindingResult(dto, "movimientoDTO");
    }

    /* ------------------ POST /devolucion-compra (éxito) ------------------ */

    @Test
    @DisplayName("GET /devolucion-compra -> lista vacía en el modelo cuando el servicio no trae datos")
    void showDevolucionCompraForm_listaVacia() {
        when(queryServiceLote.findAllForDevolucionCompra()).thenReturn(Collections.emptyList());

        String view = controller.showDevolucionCompraForm(dto, model);

        assertEquals("compras/baja/devolucion-compra", view);
        Object attr = model.getAttribute("lotesDevolvibles");
        assertNotNull(attr);
        assertTrue(attr instanceof List<?>);
        assertTrue(((List<?>)attr).isEmpty());
        verify(queryServiceLote).findAllForDevolucionCompra();
    }

    /* ------------------ procesarDevolucionCompra (unit) ------------------ */

    @Test
    @DisplayName("GET /devolucion-compra: arma modelo con lista y retorna vista")
    void showDevolucionCompraForm_ok() {
        List<Lote> entrada = List.of(new Lote(), new Lote());
        List<LoteDTO> salidaDtos = List.of(new LoteDTO(), new LoteDTO());

        when(queryServiceLote.findAllForDevolucionCompra()).thenReturn(entrada);

        try (MockedStatic<DTOUtils> ms = mockStatic(DTOUtils.class)) {
            ms.when(() -> DTOUtils.fromLoteEntities(entrada)).thenReturn(salidaDtos);

            String view = controller.showDevolucionCompraForm(dto, model);

            assertEquals("compras/baja/devolucion-compra", view);
            assertSame(salidaDtos, model.getAttribute("lotesDevolvibles"));
            verify(queryServiceLote).findAllForDevolucionCompra();
            ms.verify(() -> DTOUtils.fromLoteEntities(entrada));
        }
    }

    @Test
    @DisplayName("Todos OK -> redirige y llama procesarDevolucionCompra")
    void todoOk() {
        Lote lote = new Lote();

        try (MockedStatic<ControllerUtils> ms = mockStatic(ControllerUtils.class)) {
            ControllerUtils utils = mock(ControllerUtils.class);
            ms.when(ControllerUtils::getInstance).thenReturn(utils);

            when(utils.getLoteByCodigoInterno(eq("COD-DEV-001"), eq(binding), eq(queryServiceLote)))
                .thenReturn(lote);
            when(utils.validarFechaMovimientoPosteriorIngresoLote(dto, lote, binding)).thenReturn(true);
            when(utils.validarFechaAnalisisPosteriorIngresoLote(dto, lote, binding)).thenReturn(true);

            doNothing().when(controller).procesarDevolucionCompra(eq(dto), eq(lote), eq(redirect));

            String view = controller.procesarDevolucionCompra(dto, binding, model, redirect);

            assertEquals("redirect:/compras/baja/devolucion-compra-ok", view);
            verify(utils).getLoteByCodigoInterno("COD-DEV-001", binding, queryServiceLote);
            verify(utils).validarFechaMovimientoPosteriorIngresoLote(dto, lote, binding);
            verify(utils).validarFechaAnalisisPosteriorIngresoLote(dto, lote, binding);
            verify(controller, never()).initModelDevolucionCompra(any());
            verify(controller).procesarDevolucionCompra(dto, lote, redirect);
        }
    }

}

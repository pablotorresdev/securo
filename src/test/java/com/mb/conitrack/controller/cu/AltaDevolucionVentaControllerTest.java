package com.mb.conitrack.controller.cu;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.cu.AltaDevolucionVentaService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AltaDevolucionVentaControllerTest {

    @Spy
    @InjectMocks
    AltaDevolucionVentaController controller;

    @Mock
    AltaDevolucionVentaService devolucionVentaService;

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
    void testShowDevolucionVentaForm() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(loteService.findAllForDevolucionDTOs()).thenReturn(lotes);

        String view = controller.showDevolucionVentaForm(movDto, model);

        assertEquals("ventas/alta/devolucion-venta", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
        assertSame(lotes, model.getAttribute("lotesDevolucion"));
    }

    @Test
    void testConfirmarDevolucionVenta_ConValidacionExitosa() {
        movDto.setCodigoLote("L-001");

        Lote lote = new Lote();
        Producto producto = new Producto();
        producto.setNombreGenerico("Producto Test");
        producto.setCodigoProducto("PROD-001");
        lote.setProducto(producto);

        when(devolucionVentaService.validarDevolucionVentaInput(movDto, binding)).thenReturn(true);
        when(loteService.findByCodigoLote(anyString())).thenReturn(Optional.of(lote));

        String view = controller.confirmarDevolucionVenta(movDto, binding, model);

        assertEquals("ventas/alta/devolucion-venta-confirm", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
        assertEquals("Producto Test", movDto.getNombreProducto());
        assertEquals("PROD-001", movDto.getCodigoProducto());
    }

    @Test
    void testConfirmarDevolucionVenta_ConValidacionFallida() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(devolucionVentaService.validarDevolucionVentaInput(movDto, binding)).thenReturn(false);
        when(loteService.findAllForDevolucionDTOs()).thenReturn(lotes);

        String view = controller.confirmarDevolucionVenta(movDto, binding, model);

        assertEquals("ventas/alta/devolucion-venta", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
    }

    @Test
    void testConfirmarDevolucionVenta_SinCodigoLote() {
        when(devolucionVentaService.validarDevolucionVentaInput(movDto, binding)).thenReturn(true);

        String view = controller.confirmarDevolucionVenta(movDto, binding, model);

        assertEquals("ventas/alta/devolucion-venta-confirm", view);
    }

    @Test
    void testDevolucionVenta_ConValidacionExitosa() {
        LoteDTO loteDevuelto = new LoteDTO();
        LoteDTO loteVenta = new LoteDTO();
        List<LoteDTO> resultList = Arrays.asList(loteDevuelto, loteVenta);

        when(devolucionVentaService.validarDevolucionVentaInput(movDto, binding)).thenReturn(true);
        when(devolucionVentaService.persistirDevolucionVenta(any(MovimientoDTO.class))).thenReturn(resultList);

        String view = controller.devolucionVenta(movDto, binding, model, redirect);

        assertEquals("redirect:/ventas/alta/devolucion-venta-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("loteDevueltoDTO"));
        assertNotNull(redirect.getFlashAttributes().get("loteVentaDTO"));
        assertNotNull(redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testDevolucionVenta_ConValidacionFallida() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(devolucionVentaService.validarDevolucionVentaInput(movDto, binding)).thenReturn(false);
        when(loteService.findAllForDevolucionDTOs()).thenReturn(lotes);

        String view = controller.devolucionVenta(movDto, binding, model, redirect);

        assertEquals("ventas/alta/devolucion-venta", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
    }

    @Test
    void testDevolucionVenta_ConErrorEnPersistencia() {
        when(devolucionVentaService.validarDevolucionVentaInput(movDto, binding)).thenReturn(true);
        when(devolucionVentaService.persistirDevolucionVenta(any(MovimientoDTO.class))).thenReturn(null);

        String view = controller.devolucionVenta(movDto, binding, model, redirect);

        assertEquals("redirect:/ventas/alta/devolucion-venta-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testDevolucionVenta_ConListaVacia() {
        List<LoteDTO> emptyList = List.of();
        when(devolucionVentaService.validarDevolucionVentaInput(movDto, binding)).thenReturn(true);
        when(devolucionVentaService.persistirDevolucionVenta(any(MovimientoDTO.class))).thenReturn(emptyList);

        String view = controller.devolucionVenta(movDto, binding, model, redirect);

        assertEquals("redirect:/ventas/alta/devolucion-venta-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testExitoDevolucionVenta() {
        LoteDTO loteDTO = new LoteDTO();

        String view = controller.exitoDevolucionVenta(loteDTO);

        assertEquals("ventas/alta/devolucion-venta-ok", view);
    }
}

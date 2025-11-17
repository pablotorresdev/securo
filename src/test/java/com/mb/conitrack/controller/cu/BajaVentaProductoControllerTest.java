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
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.cu.BajaVentaProductoService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BajaVentaProductoControllerTest {

    @Spy
    @InjectMocks
    BajaVentaProductoController controller;

    @Mock
    BajaVentaProductoService ventaProductoService;

    @Mock
    LoteService loteService;

    Model model;
    RedirectAttributes redirect;
    LoteDTO loteDto;
    BindingResult binding;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        redirect = new RedirectAttributesModelMap();
        loteDto = new LoteDTO();
        binding = new BeanPropertyBindingResult(loteDto, "loteDTO");
    }

    @Test
    void testCancelar() {
        assertEquals("redirect:/", controller.cancelar());
    }

    @Test
    void testShowVentaProductoForm() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(loteService.findAllForVentaProductoDTOs()).thenReturn(lotes);

        String view = controller.showVentaProductoForm(loteDto, model);

        assertEquals("ventas/baja/venta-producto", view);
        assertSame(loteDto, model.getAttribute("loteDTO"));
        assertSame(lotes, model.getAttribute("loteVentaDTOs"));
    }

    @Test
    void testVentaProducto_ConValidacionExitosa() {
        LoteDTO resultDTO = new LoteDTO();
        resultDTO.setNombreProducto("Producto Test");
        loteDto.setNombreProducto("Producto Test");
        loteDto.setTrazaDTOs(new ArrayList<>());

        when(ventaProductoService.validarVentaProductoInput(loteDto, binding)).thenReturn(true);
        when(ventaProductoService.bajaVentaProducto(any(LoteDTO.class))).thenReturn(resultDTO);

        String view = controller.ventaProducto(loteDto, binding, model, redirect);

        assertEquals("redirect:/ventas/baja/venta-producto-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("loteDTO"));
        assertNotNull(redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testVentaProducto_ConValidacionFallida() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        when(ventaProductoService.validarVentaProductoInput(loteDto, binding)).thenReturn(false);
        when(loteService.findAllForVentaProductoDTOs()).thenReturn(lotes);

        String view = controller.ventaProducto(loteDto, binding, model, redirect);

        assertEquals("ventas/baja/venta-producto", view);
        assertSame(loteDto, model.getAttribute("loteDTO"));
    }

    @Test
    void testVentaProducto_ConErrorEnPersistencia() {
        loteDto.setTrazaDTOs(new ArrayList<>());
        when(ventaProductoService.validarVentaProductoInput(loteDto, binding)).thenReturn(true);
        when(ventaProductoService.bajaVentaProducto(any(LoteDTO.class))).thenReturn(null);

        String view = controller.ventaProducto(loteDto, binding, model, redirect);

        assertEquals("redirect:/ventas/baja/venta-producto-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testVentaProducto_ConTrazas() {
        LoteDTO resultDTO = new LoteDTO();
        resultDTO.setNombreProducto("Producto Test");
        loteDto.setNombreProducto("Producto Test");

        // Crear algunas trazas
        List<TrazaDTO> trazas = new ArrayList<>();
        TrazaDTO traza1 = new TrazaDTO();
        traza1.setNroBulto(1);
        traza1.setNroTraza(1L);
        trazas.add(traza1);

        TrazaDTO traza2 = new TrazaDTO();
        traza2.setNroBulto(1);
        traza2.setNroTraza(2L);
        trazas.add(traza2);

        loteDto.setTrazaDTOs(trazas);

        when(ventaProductoService.validarVentaProductoInput(loteDto, binding)).thenReturn(true);
        when(ventaProductoService.bajaVentaProducto(any(LoteDTO.class))).thenReturn(resultDTO);

        String view = controller.ventaProducto(loteDto, binding, model, redirect);

        assertEquals("redirect:/ventas/baja/venta-producto-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("trazaVentaDTOs"));
    }

    @Test
    void testExitoVentaProducto() {
        LoteDTO loteDTO = new LoteDTO();

        String view = controller.exitoVentaProducto(loteDTO);

        assertEquals("ventas/baja/venta-producto-ok", view);
    }
}

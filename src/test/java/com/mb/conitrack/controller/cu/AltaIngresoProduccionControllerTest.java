package com.mb.conitrack.controller.cu;

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
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.service.maestro.ProductoService;
import com.mb.conitrack.service.cu.AltaIngresoProduccionService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AltaIngresoProduccionControllerTest {

    @Spy
    @InjectMocks
    AltaIngresoProduccionController controller;

    @Mock
    AltaIngresoProduccionService ingresoProduccionService;

    @Mock
    ProductoService productoService;

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
    void testShowIngresoProduccion() {
        List<Producto> productos = List.of(new Producto());
        when(productoService.getProductosInternos()).thenReturn(productos);

        String view = controller.showIngresoProduccion(loteDto, model);

        assertEquals("produccion/alta/ingreso-produccion", view);
        assertSame(loteDto, model.getAttribute("loteDTO"));
        assertSame(productos, model.getAttribute("productos"));
        assertNotNull(loteDto.getCantidadesBultos());
        assertNotNull(loteDto.getUnidadMedidaBultos());
    }

    @Test
    void testShowIngresoProduccion_ConListasNull() {
        List<Producto> productos = List.of(new Producto());
        when(productoService.getProductosInternos()).thenReturn(productos);

        // Explicitly set lists to null to cover the null check branches
        loteDto.setCantidadesBultos(null);
        loteDto.setUnidadMedidaBultos(null);

        String view = controller.showIngresoProduccion(loteDto, model);

        assertEquals("produccion/alta/ingreso-produccion", view);
        assertNotNull(loteDto.getCantidadesBultos());
        assertNotNull(loteDto.getUnidadMedidaBultos());
    }

    @Test
    void testConfirmarIngresoProduccion_ConValidacionExitosa() {
        loteDto.setProductoId(1L);
        Producto producto = new Producto();
        producto.setNombreGenerico("Producto Test");
        producto.setCodigoProducto("PROD-001");

        when(ingresoProduccionService.validarIngresoProduccionInput(loteDto, binding)).thenReturn(true);
        when(productoService.findById(anyLong())).thenReturn(Optional.of(producto));

        String view = controller.confirmarIngresoProduccion(loteDto, binding, model);

        assertEquals("produccion/alta/ingreso-produccion-confirm", view);
        assertSame(loteDto, model.getAttribute("loteDTO"));
        assertEquals("Producto Test", loteDto.getNombreProducto());
        assertEquals("PROD-001", loteDto.getCodigoProducto());
    }

    @Test
    void testConfirmarIngresoProduccion_ConValidacionFallida() {
        List<Producto> productos = List.of(new Producto());
        when(ingresoProduccionService.validarIngresoProduccionInput(loteDto, binding)).thenReturn(false);
        when(productoService.getProductosInternos()).thenReturn(productos);

        String view = controller.confirmarIngresoProduccion(loteDto, binding, model);

        assertEquals("produccion/alta/ingreso-produccion", view);
        assertSame(loteDto, model.getAttribute("loteDTO"));
    }

    @Test
    void testConfirmarIngresoProduccion_SinProductoId() {
        when(ingresoProduccionService.validarIngresoProduccionInput(loteDto, binding)).thenReturn(true);

        String view = controller.confirmarIngresoProduccion(loteDto, binding, model);

        assertEquals("produccion/alta/ingreso-produccion-confirm", view);
    }

    @Test
    void testIngresoProduccion_ConValidacionExitosa() {
        LoteDTO resultDTO = new LoteDTO();
        when(ingresoProduccionService.validarIngresoProduccionInput(loteDto, binding)).thenReturn(true);
        when(ingresoProduccionService.altaStockPorProduccion(any(LoteDTO.class))).thenReturn(resultDTO);

        String view = controller.ingresoProduccion(loteDto, binding, model, redirect);

        assertEquals("redirect:/produccion/alta/ingreso-produccion-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("loteDTO"));
        assertNotNull(redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testIngresoProduccion_ConValidacionFallida() {
        List<Producto> productos = List.of(new Producto());
        when(ingresoProduccionService.validarIngresoProduccionInput(loteDto, binding)).thenReturn(false);
        when(productoService.getProductosInternos()).thenReturn(productos);

        String view = controller.ingresoProduccion(loteDto, binding, model, redirect);

        assertEquals("produccion/alta/ingreso-produccion", view);
        assertSame(loteDto, model.getAttribute("loteDTO"));
    }

    @Test
    void testIngresoProduccion_ConErrorEnPersistencia() {
        when(ingresoProduccionService.validarIngresoProduccionInput(loteDto, binding)).thenReturn(true);
        when(ingresoProduccionService.altaStockPorProduccion(any(LoteDTO.class))).thenReturn(null);

        String view = controller.ingresoProduccion(loteDto, binding, model, redirect);

        assertEquals("redirect:/produccion/alta/ingreso-produccion-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testExitoIngresoProduccion() {
        LoteDTO loteDTO = new LoteDTO();

        String view = controller.exitoIngresoProduccion(loteDTO);

        assertEquals("produccion/alta/ingreso-produccion-ok", view);
    }
}

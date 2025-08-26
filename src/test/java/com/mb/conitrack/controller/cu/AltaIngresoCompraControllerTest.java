package com.mb.conitrack.controller.cu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
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
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.cu.AltaIngresoCompraService;
import com.mb.conitrack.service.maestro.ProductoService;
import com.mb.conitrack.service.maestro.ProveedorService;

import static com.mb.conitrack.controller.cu.AbstractCuController.controllerUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AltaIngresoCompraControllerTest {

    @Spy
    @InjectMocks
    AltaIngresoCompraController controller;

    @Mock
    AltaIngresoCompraService altaIngresoCompraService;

    @Mock
    ProductoService productoService;

    @Mock
    ProveedorService proveedorService;

    @Mock
    List<Proveedor> proveedoresMock;

    @Mock
    List<Producto> productosMock;

    Model model;

    RedirectAttributes redirect;

    LoteDTO dto;

    BindingResult binding;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        redirect = new RedirectAttributesModelMap();
        dto = new LoteDTO();
        binding = new BeanPropertyBindingResult(dto, "loteDTO");
    }

    // -------------------- GET/acciones simples --------------------

    @Test
    void testCancel() {
        assertEquals("redirect:/", controller.cancelar());
    }

    @Test
    void testExitoIngresoCompra() {
        assertEquals("compras/alta/ingreso-compra-ok", controller.exitoIngresoCompra(dto));
    }

    // -------------------- procesaringresoCompra --------------------

    @Test
    void testShowIngresoCompra_InicializaListasSiNulas() {
        LoteDTO dto = new LoteDTO();
        dto.setCantidadesBultos(null);
        dto.setUnidadMedidaBultos(null);

        when(proveedorService.getProveedoresExternos()).thenReturn(proveedoresMock);
        when(productoService.getProductosExternos()).thenReturn(productosMock);

        String view = controller.showIngresoCompra(dto, new ExtendedModelMap());

        assertEquals("compras/alta/ingreso-compra", view);
        assertNotNull(dto.getCantidadesBultos());
        assertNotNull(dto.getUnidadMedidaBultos());
    }

    @Test
    void testShowIngresoCompra_RespetaListasInicializadas() {
        // given
        LoteDTO dto = new LoteDTO();
        dto.setCantidadesBultos(new ArrayList<>(List.of(BigDecimal.ONE)));
        dto.setUnidadMedidaBultos(new ArrayList<>(List.of(UnidadMedidaEnum.KILOGRAMO)));
        Model model = new ExtendedModelMap();

        when(proveedorService.getProveedoresExternos()).thenReturn(proveedoresMock);
        when(productoService.getProductosExternos()).thenReturn(productosMock);

        // when
        String view = controller.showIngresoCompra(dto, model);

        // then
        assertEquals("compras/alta/ingreso-compra", view);
        assertEquals(1, dto.getCantidadesBultos().size());
        assertEquals(1, dto.getCantidadesBultos().get(0).intValue());
        assertEquals(UnidadMedidaEnum.KILOGRAMO.getNombre(), dto.getUnidadMedidaBultos().get(0).getNombre());
        assertSame(dto, model.getAttribute("loteDTO"));
        assertSame(productosMock, model.getAttribute("productos"));
        assertSame(proveedoresMock, model.getAttribute("proveedores"));
        assertEquals(controller.getCountryList(), model.getAttribute("paises"));
    }

}

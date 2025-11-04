package com.mb.conitrack.controller.cu;

import java.math.BigDecimal;
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
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.cu.AltaIngresoCompraService;
import com.mb.conitrack.service.maestro.ProductoService;
import com.mb.conitrack.service.maestro.ProveedorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
        assertEquals(altaIngresoCompraService.getCountryList(), model.getAttribute("paises"));
    }

    // -------------------- confirmarIngresoCompra --------------------

    @Test
    void testConfirmarIngresoCompra_ConValidacionExitosa() {
        // given
        dto.setProductoId(1L);
        dto.setProveedorId(2L);
        dto.setFabricanteId(3L);

        Producto producto = new Producto();
        producto.setNombreGenerico("Paracetamol");
        producto.setCodigoProducto("API-001");

        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial("Proveedor Test");

        Proveedor fabricante = new Proveedor();
        fabricante.setRazonSocial("Fabricante Test");

        when(altaIngresoCompraService.validarIngresoCompraInput(dto, binding)).thenReturn(true);
        when(productoService.findById(1L)).thenReturn(java.util.Optional.of(producto));
        when(proveedorService.findById(2L)).thenReturn(java.util.Optional.of(proveedor));
        when(proveedorService.findById(3L)).thenReturn(java.util.Optional.of(fabricante));

        // when
        String view = controller.confirmarIngresoCompra(dto, binding, model);

        // then
        assertEquals("compras/alta/ingreso-compra-confirm", view);
        assertEquals("Paracetamol", dto.getNombreProducto());
        assertEquals("API-001", dto.getCodigoProducto());
        assertEquals("Proveedor Test", dto.getNombreProveedor());
        assertEquals("Fabricante Test", dto.getNombreFabricante());
    }

    @Test
    void testConfirmarIngresoCompra_ConValidacionFallida() {
        // given
        when(altaIngresoCompraService.validarIngresoCompraInput(dto, binding)).thenReturn(false);
        when(proveedorService.getProveedoresExternos()).thenReturn(proveedoresMock);
        when(productoService.getProductosExternos()).thenReturn(productosMock);

        // when
        String view = controller.confirmarIngresoCompra(dto, binding, model);

        // then
        assertEquals("compras/alta/ingreso-compra", view);
    }

    @Test
    void testConfirmarIngresoCompra_ConProductoNoEncontrado() {
        // given
        dto.setProductoId(999L);

        when(altaIngresoCompraService.validarIngresoCompraInput(dto, binding)).thenReturn(true);
        when(productoService.findById(999L)).thenReturn(java.util.Optional.empty());

        // when
        String view = controller.confirmarIngresoCompra(dto, binding, model);

        // then
        assertEquals("compras/alta/ingreso-compra-confirm", view);
        assertEquals(null, dto.getNombreProducto());
    }

    @Test
    void testConfirmarIngresoCompra_SinProveedorNiFabricante() {
        // given - Solo producto, sin proveedor ni fabricante
        dto.setProductoId(1L);
        dto.setProveedorId(null);
        dto.setFabricanteId(null);

        Producto producto = new Producto();
        producto.setNombreGenerico("Producto Test");
        producto.setCodigoProducto("PROD-001");

        when(altaIngresoCompraService.validarIngresoCompraInput(dto, binding)).thenReturn(true);
        when(productoService.findById(1L)).thenReturn(java.util.Optional.of(producto));

        // when
        String view = controller.confirmarIngresoCompra(dto, binding, model);

        // then
        assertEquals("compras/alta/ingreso-compra-confirm", view);
        assertEquals("Producto Test", dto.getNombreProducto());
        assertEquals("PROD-001", dto.getCodigoProducto());
        assertEquals(null, dto.getNombreProveedor());
        assertEquals(null, dto.getNombreFabricante());
    }

    @Test
    void testConfirmarIngresoCompra_TodosLosIdsNulos() {
        // given - Sin producto, proveedor ni fabricante (todos los IDs null)
        dto.setProductoId(null);
        dto.setProveedorId(null);
        dto.setFabricanteId(null);

        when(altaIngresoCompraService.validarIngresoCompraInput(dto, binding)).thenReturn(true);

        // when
        String view = controller.confirmarIngresoCompra(dto, binding, model);

        // then
        assertEquals("compras/alta/ingreso-compra-confirm", view);
        assertEquals(null, dto.getNombreProducto());
        assertEquals(null, dto.getCodigoProducto());
        assertEquals(null, dto.getNombreProveedor());
        assertEquals(null, dto.getNombreFabricante());
    }

    // -------------------- ingresoCompra --------------------

    @Test
    void testIngresoCompra_ConValidacionExitosa() {
        // given
        LoteDTO resultDTO = new LoteDTO();
        resultDTO.setCodigoLote("LOTE-TEST-001");

        when(altaIngresoCompraService.validarIngresoCompraInput(dto, binding)).thenReturn(true);
        when(altaIngresoCompraService.altaStockPorCompra(dto)).thenReturn(resultDTO);

        // when
        String view = controller.ingresoCompra(dto, binding, model, redirect);

        // then
        assertEquals("redirect:/compras/alta/ingreso-compra-ok", view);
        assertEquals(resultDTO, redirect.getFlashAttributes().get("loteDTO"));
        assertEquals("Ingreso de stock por compra exitoso.", redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testIngresoCompra_ConValidacionFallida() {
        // given
        when(altaIngresoCompraService.validarIngresoCompraInput(dto, binding)).thenReturn(false);
        when(proveedorService.getProveedoresExternos()).thenReturn(proveedoresMock);
        when(productoService.getProductosExternos()).thenReturn(productosMock);

        // when
        String view = controller.ingresoCompra(dto, binding, model, redirect);

        // then
        assertEquals("compras/alta/ingreso-compra", view);
    }

    @Test
    void testIngresoCompra_ConErrorEnPersistencia() {
        // given
        when(altaIngresoCompraService.validarIngresoCompraInput(dto, binding)).thenReturn(true);
        when(altaIngresoCompraService.altaStockPorCompra(dto)).thenReturn(null);

        // when
        String view = controller.ingresoCompra(dto, binding, model, redirect);

        // then
        assertEquals("redirect:/compras/alta/ingreso-compra-ok", view);
        assertEquals("Hubo un error en el ingreso de stock por compra.", redirect.getFlashAttributes().get("error"));
    }

}

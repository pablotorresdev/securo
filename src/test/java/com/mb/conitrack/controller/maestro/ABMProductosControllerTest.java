package com.mb.conitrack.controller.maestro;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.service.maestro.ProductoService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ABMProductosController Tests")
class ABMProductosControllerTest {

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private ABMProductosController controller;

    private Model model;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        model = new ConcurrentModel();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    @Test
    @DisplayName("productosPage debe retornar la vista index-productos")
    void testProductosPage() {
        // Act
        String viewName = controller.productosPage();

        // Assert
        assertEquals("productos/index-productos", viewName);
    }

    @Test
    @DisplayName("listProductos debe retornar vista con lista de productos activos")
    void testListProductos() {
        // Arrange
        List<Producto> productos = new ArrayList<>();
        Producto prod1 = new Producto();
        prod1.setCodigoProducto("API-001");
        productos.add(prod1);

        when(productoService.findByActivoTrueOrderByCodigoProductoAsc()).thenReturn(productos);

        // Act
        String viewName = controller.listProductos(model);

        // Assert
        assertEquals("productos/list-productos", viewName);
        assertEquals(productos, model.getAttribute("productos"));
        verify(productoService, times(1)).findByActivoTrueOrderByCodigoProductoAsc();
    }

    @Test
    @DisplayName("showAddProductoForm debe retornar vista con producto nuevo")
    void testShowAddProductoForm() {
        // Arrange
        List<Producto> productosInternos = new ArrayList<>();

        when(productoService.getProductosInternos()).thenReturn(productosInternos);

        // Act
        String viewName = controller.showAddProductoForm(model);

        // Assert
        assertEquals("productos/add-producto", viewName);
        assertNotNull(model.getAttribute("producto"));
        assertEquals(productosInternos, model.getAttribute("productosDestino"));
        verify(productoService, times(1)).getProductosInternos();
    }

    @Test
    @DisplayName("addProducto debe guardar producto cuando no requiere producto destino")
    void testAddProducto_Success_NoProductoDestino() {
        // Arrange
        Producto producto = new Producto();
        producto.setCodigoProducto("EXC-001");
        producto.setTipoProducto(TipoProductoEnum.EXCIPIENTE); // No requiere producto destino
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(false);

        // Act
        String viewName = controller.addProducto(producto, bindingResult, model);

        // Assert
        assertEquals("redirect:/productos/list-productos", viewName);
        assertEquals(Boolean.TRUE, producto.getActivo());
        assertNull(producto.getProductoDestino());
        verify(productoService, times(1)).save(producto);
    }

    @Test
    @DisplayName("addProducto debe guardar producto cuando requiere y tiene producto destino")
    void testAddProducto_Success_ConProductoDestino() {
        // Arrange
        Producto producto = new Producto();
        producto.setCodigoProducto("MF-001");
        producto.setTipoProducto(TipoProductoEnum.ACOND_SECUNDARIO); // Requiere producto destino
        producto.setProductoDestino("API-001");
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(false);

        // Act
        String viewName = controller.addProducto(producto, bindingResult, model);

        // Assert
        assertEquals("redirect:/productos/list-productos", viewName);
        assertEquals(Boolean.TRUE, producto.getActivo());
        assertEquals("API-001", producto.getProductoDestino());
        verify(productoService, times(1)).save(producto);
    }

    @Test
    @DisplayName("addProducto debe rechazar cuando requiere producto destino pero está vacío")
    void testAddProducto_ProductoDestinoRequerido_Vacio() {
        // Arrange
        Producto producto = new Producto();
        producto.setTipoProducto(TipoProductoEnum.SEMIELABORADO); // Requiere producto destino
        producto.setProductoDestino(""); // Vacío
        BindingResult bindingResult = mock(BindingResult.class);
        List<Producto> productosInternos = new ArrayList<>();

        when(bindingResult.hasErrors()).thenReturn(false);
        when(productoService.getProductosInternos()).thenReturn(productosInternos);

        // Act
        String viewName = controller.addProducto(producto, bindingResult, model);

        // Assert
        assertEquals("productos/add-producto", viewName);
        verify(bindingResult, times(1)).rejectValue(eq("productoDestino"), anyString(), anyString());
        assertEquals(productosInternos, model.getAttribute("productosDestino"));
        verify(productoService, never()).save(any());
    }

    @Test
    @DisplayName("addProducto debe retornar formulario cuando hay errores de validación")
    void testAddProducto_ValidationErrors() {
        // Arrange
        Producto producto = new Producto();
        BindingResult bindingResult = mock(BindingResult.class);
        List<Producto> productosInternos = new ArrayList<>();

        when(bindingResult.hasErrors()).thenReturn(true);
        when(productoService.getProductosInternos()).thenReturn(productosInternos);

        // Act
        String viewName = controller.addProducto(producto, bindingResult, model);

        // Assert
        assertEquals("productos/add-producto", viewName);
        assertEquals("Validation failed!", model.getAttribute("error"));
        verify(productoService, never()).save(any());
    }

    @Test
    @DisplayName("showEditProductoForm debe retornar vista cuando producto existe y está activo")
    void testShowEditProductoForm_Found_Active() {
        // Arrange
        Long id = 1L;
        Producto producto = new Producto();
        producto.setId(id);
        producto.setActivo(true);
        List<Producto> productosInternos = new ArrayList<>();

        when(productoService.findById(id)).thenReturn(Optional.of(producto));
        when(productoService.getProductosInternos()).thenReturn(productosInternos);

        // Act
        String viewName = controller.showEditProductoForm(id, model);

        // Assert
        assertEquals("productos/edit-producto", viewName);
        assertEquals(producto, model.getAttribute("producto"));
        assertEquals(productosInternos, model.getAttribute("productosDestino"));
    }

    @Test
    @DisplayName("showEditProductoForm debe redirigir cuando producto no existe")
    void testShowEditProductoForm_NotFound() {
        // Arrange
        Long id = 999L;

        when(productoService.findById(id)).thenReturn(Optional.empty());

        // Act
        String viewName = controller.showEditProductoForm(id, model);

        // Assert
        assertEquals("redirect:/productos/list-productos", viewName);
        assertEquals("Producto no encontrado", model.getAttribute("error"));
    }

    @Test
    @DisplayName("showEditProductoForm debe redirigir cuando producto está inactivo")
    void testShowEditProductoForm_Inactive() {
        // Arrange
        Long id = 1L;
        Producto producto = new Producto();
        producto.setId(id);
        producto.setActivo(false);

        when(productoService.findById(id)).thenReturn(Optional.of(producto));

        // Act
        String viewName = controller.showEditProductoForm(id, model);

        // Assert
        assertEquals("redirect:/productos/list-productos", viewName);
        assertEquals("Producto inactivo", model.getAttribute("error"));
    }

    @Test
    @DisplayName("editProducto debe actualizar producto cuando no hay errores")
    void testEditProducto_Success() {
        // Arrange
        Long id = 1L;
        Producto producto = new Producto();
        producto.setCodigoProducto("EXC-001");
        producto.setTipoProducto(TipoProductoEnum.EXCIPIENTE); // No requiere producto destino
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(false);

        // Act
        String viewName = controller.editProducto(id, producto, bindingResult, model, redirectAttributes);

        // Assert
        assertEquals("redirect:/productos/list-productos", viewName);
        assertEquals(id, producto.getId());
        assertEquals(Boolean.TRUE, producto.getActivo());
        assertEquals("Producto editado correctamente!", redirectAttributes.getFlashAttributes().get("success"));
        verify(productoService, times(1)).save(producto);
    }

    @Test
    @DisplayName("editProducto debe retornar formulario cuando hay errores de validación")
    void testEditProducto_ValidationErrors() {
        // Arrange
        Long id = 1L;
        Producto producto = new Producto();
        BindingResult bindingResult = mock(BindingResult.class);
        List<Producto> productosInternos = new ArrayList<>();

        when(bindingResult.hasErrors()).thenReturn(true);
        when(productoService.getProductosInternos()).thenReturn(productosInternos);

        // Act
        String viewName = controller.editProducto(id, producto, bindingResult, model, redirectAttributes);

        // Assert
        assertEquals("productos/edit-producto", viewName);
        assertEquals(productosInternos, model.getAttribute("productosDestino"));
        verify(productoService, never()).save(any());
    }

    @Test
    @DisplayName("editProducto debe actualizar producto cuando requiere y tiene producto destino")
    void testEditProducto_ConProductoDestino() {
        // Arrange
        Long id = 1L;
        Producto producto = new Producto();
        producto.setCodigoProducto("SEMI-001");
        producto.setTipoProducto(TipoProductoEnum.SEMIELABORADO); // Requiere producto destino
        producto.setProductoDestino("API-001"); // Tiene producto destino válido
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(false);

        // Act
        String viewName = controller.editProducto(id, producto, bindingResult, model, redirectAttributes);

        // Assert
        assertEquals("redirect:/productos/list-productos", viewName);
        assertEquals(id, producto.getId());
        assertEquals(Boolean.TRUE, producto.getActivo());
        assertEquals("API-001", producto.getProductoDestino());
        assertEquals("Producto editado correctamente!", redirectAttributes.getFlashAttributes().get("success"));
        verify(productoService, times(1)).save(producto);
    }

    @Test
    @DisplayName("editProducto debe rechazar cuando requiere producto destino pero está vacío")
    void testEditProducto_ProductoDestinoRequerido_Vacio() {
        // Arrange
        Long id = 1L;
        Producto producto = new Producto();
        producto.setTipoProducto(TipoProductoEnum.GRANEL_CAPSULAS); // Requiere producto destino
        producto.setProductoDestino(null);
        BindingResult bindingResult = mock(BindingResult.class);
        List<Producto> productosInternos = new ArrayList<>();

        when(bindingResult.hasErrors()).thenReturn(false);
        when(productoService.getProductosInternos()).thenReturn(productosInternos);

        // Act
        String viewName = controller.editProducto(id, producto, bindingResult, model, redirectAttributes);

        // Assert
        assertEquals("productos/add-producto", viewName);
        verify(bindingResult, times(1)).rejectValue(eq("productoDestino"), anyString(), anyString());
        verify(productoService, never()).save(any());
    }

    @Test
    @DisplayName("deleteProducto debe hacer borrado lógico cuando producto existe y está activo")
    void testDeleteProducto_Success() {
        // Arrange
        Long id = 1L;
        Producto producto = new Producto();
        producto.setId(id);
        producto.setActivo(true);

        when(productoService.findById(id)).thenReturn(Optional.of(producto));

        // Act
        String viewName = controller.deleteProducto(id, redirectAttributes);

        // Assert
        assertEquals("redirect:/productos/list-productos", viewName);
        assertEquals(Boolean.FALSE, producto.getActivo());
        verify(productoService, times(1)).save(producto);
    }

    @Test
    @DisplayName("deleteProducto debe retornar error cuando producto no existe")
    void testDeleteProducto_NotFound() {
        // Arrange
        Long id = 999L;

        when(productoService.findById(id)).thenReturn(Optional.empty());

        // Act
        String viewName = controller.deleteProducto(id, redirectAttributes);

        // Assert
        assertEquals("redirect:/productos/list-productos", viewName);
        assertEquals("Producto no encontrado", redirectAttributes.getFlashAttributes().get("error"));
        verify(productoService, never()).save(any());
    }

    @Test
    @DisplayName("deleteProducto debe retornar error cuando producto ya está inactivo")
    void testDeleteProducto_AlreadyInactive() {
        // Arrange
        Long id = 1L;
        Producto producto = new Producto();
        producto.setId(id);
        producto.setActivo(false);

        when(productoService.findById(id)).thenReturn(Optional.of(producto));

        // Act
        String viewName = controller.deleteProducto(id, redirectAttributes);

        // Assert
        assertEquals("redirect:/productos/list-productos", viewName);
        assertEquals("Producto ya esta inactivo", redirectAttributes.getFlashAttributes().get("error"));
        verify(productoService, never()).save(any());
    }
}

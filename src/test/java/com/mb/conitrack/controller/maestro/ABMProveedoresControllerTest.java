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

import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.service.maestro.ProveedorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ABMProveedoresController Tests")
class ABMProveedoresControllerTest {

    @Mock
    private ProveedorService proveedorService;

    @InjectMocks
    private ABMProveedoresController controller;

    private Model model;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        model = new ConcurrentModel();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    @Test
    @DisplayName("proveedoresPage debe retornar la vista index-proveedores")
    void testProveedoresPage() {
        // Act
        String viewName = controller.proveedoresPage();

        // Assert
        assertEquals("proveedores/index-proveedores", viewName);
    }

    @Test
    @DisplayName("listProveedores debe retornar vista con lista de proveedores")
    void testListProveedores() {
        // Arrange
        List<Proveedor> proveedores = new ArrayList<>();
        Proveedor prov1 = new Proveedor();
        prov1.setRazonSocial("Proveedor A");
        proveedores.add(prov1);

        when(proveedorService.findAllByOrderByRazonSocialAsc()).thenReturn(proveedores);

        // Act
        String viewName = controller.listProveedores(model);

        // Assert
        assertEquals("proveedores/list-proveedores", viewName);
        assertEquals(proveedores, model.getAttribute("proveedores"));
        verify(proveedorService, times(1)).findAllByOrderByRazonSocialAsc();
    }

    @Test
    @DisplayName("showAddProveedorForm debe retornar vista con proveedor nuevo")
    void testShowAddProveedorForm() {
        // Act
        String viewName = controller.showAddProveedorForm(model);

        // Assert
        assertEquals("proveedores/add-proveedor", viewName);
        assertNotNull(model.getAttribute("proveedor"));
        assertTrue(model.getAttribute("proveedor") instanceof Proveedor);
    }

    @Test
    @DisplayName("addProveedor debe guardar proveedor cuando no hay errores")
    void testAddProveedor_Success() {
        // Arrange
        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial("Nuevo Proveedor");
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(false);

        // Act
        String viewName = controller.addProveedor(proveedor, bindingResult, model);

        // Assert
        assertEquals("redirect:/proveedores/list-proveedores", viewName);
        assertEquals(Boolean.TRUE, proveedor.getActivo());
        verify(proveedorService, times(1)).save(proveedor);
    }

    @Test
    @DisplayName("addProveedor debe retornar formulario cuando hay errores de validación")
    void testAddProveedor_ValidationErrors() {
        // Arrange
        Proveedor proveedor = new Proveedor();
        BindingResult bindingResult = mock(BindingResult.class);
        List<Proveedor> proveedoresExternos = new ArrayList<>();

        when(bindingResult.hasErrors()).thenReturn(true);
        when(proveedorService.getProveedoresExternos()).thenReturn(proveedoresExternos);

        // Act
        String viewName = controller.addProveedor(proveedor, bindingResult, model);

        // Assert
        assertEquals("proveedores/add-proveedor", viewName);
        assertEquals("Validation failed!", model.getAttribute("error"));
        assertEquals(proveedoresExternos, model.getAttribute("proveedores"));
        verify(proveedorService, never()).save(any());
    }

    @Test
    @DisplayName("showEditProveedorForm debe retornar vista con proveedor cuando existe")
    void testShowEditProveedorForm_Found() {
        // Arrange
        Long id = 1L;
        Proveedor proveedor = new Proveedor();
        proveedor.setId(id);
        proveedor.setRazonSocial("Proveedor Test");

        when(proveedorService.findById(id)).thenReturn(Optional.of(proveedor));

        // Act
        String viewName = controller.showEditProveedorForm(id, model);

        // Assert
        assertEquals("proveedores/edit-proveedor", viewName);
        assertEquals(proveedor, model.getAttribute("proveedor"));
        verify(proveedorService, times(1)).findById(id);
    }

    @Test
    @DisplayName("showEditProveedorForm debe redirigir cuando proveedor no existe")
    void testShowEditProveedorForm_NotFound() {
        // Arrange
        Long id = 999L;

        when(proveedorService.findById(id)).thenReturn(Optional.empty());

        // Act
        String viewName = controller.showEditProveedorForm(id, model);

        // Assert
        assertEquals("redirect:/proveedores/list-proveedores", viewName);
        assertEquals("Proveedor not found!", model.getAttribute("error"));
        verify(proveedorService, times(1)).findById(id);
    }

    @Test
    @DisplayName("editProveedor debe actualizar proveedor cuando no hay errores")
    void testEditProveedor_Success() {
        // Arrange
        Long id = 1L;
        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial("Proveedor Actualizado");
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(false);

        // Act
        String viewName = controller.editProveedor(id, proveedor, bindingResult, redirectAttributes);

        // Assert
        assertEquals("redirect:/proveedores/list-proveedores", viewName);
        assertEquals(id, proveedor.getId());
        assertEquals(Boolean.TRUE, proveedor.getActivo());
        assertEquals("Proveedor updated successfully!", redirectAttributes.getFlashAttributes().get("success"));
        verify(proveedorService, times(1)).save(proveedor);
    }

    @Test
    @DisplayName("editProveedor debe retornar formulario cuando hay errores de validación")
    void testEditProveedor_ValidationErrors() {
        // Arrange
        Long id = 1L;
        Proveedor proveedor = new Proveedor();
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        String viewName = controller.editProveedor(id, proveedor, bindingResult, redirectAttributes);

        // Assert
        assertEquals("proveedores/edit-proveedor", viewName);
        verify(proveedorService, never()).save(any());
    }

    @Test
    @DisplayName("deleteProveedor debe hacer borrado lógico cuando proveedor existe")
    void testDeleteProveedor_Success() {
        // Arrange
        Long id = 1L;
        Proveedor proveedor = new Proveedor();
        proveedor.setId(id);
        proveedor.setActivo(true);

        when(proveedorService.findById(id)).thenReturn(Optional.of(proveedor));

        // Act
        String viewName = controller.deleteProveedor(id, redirectAttributes);

        // Assert
        assertEquals("redirect:/proveedores/list-proveedores", viewName);
        assertEquals(Boolean.FALSE, proveedor.getActivo());
        verify(proveedorService, times(1)).findById(id);
        verify(proveedorService, times(1)).save(proveedor);
    }

    @Test
    @DisplayName("deleteProveedor debe retornar error cuando proveedor no existe")
    void testDeleteProveedor_NotFound() {
        // Arrange
        Long id = 999L;

        when(proveedorService.findById(id)).thenReturn(Optional.empty());

        // Act
        String viewName = controller.deleteProveedor(id, redirectAttributes);

        // Assert
        assertEquals("redirect:/proveedores/list-proveedores", viewName);
        assertEquals("Proveedor not found!", redirectAttributes.getFlashAttributes().get("error"));
        verify(proveedorService, times(1)).findById(id);
        verify(proveedorService, never()).save(any());
    }
}

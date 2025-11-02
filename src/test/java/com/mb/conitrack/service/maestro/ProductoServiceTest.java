package com.mb.conitrack.service.maestro;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.repository.maestro.ProductoRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ProductoService Tests")
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("getProductosExternos debe retornar lista de productos externos")
    void testGetProductosExternos() {
        // Arrange
        List<Producto> productosExternos = new ArrayList<>();
        Producto producto1 = new Producto();
        producto1.setId(1L);
        producto1.setCodigoProducto("EXT001");
        producto1.setNombreGenerico("Producto Externo 1");
        producto1.setTipoProducto(TipoProductoEnum.API);
        productosExternos.add(producto1);

        when(productoRepository.findProductosExternos()).thenReturn(productosExternos);

        // Act
        List<Producto> result = productoService.getProductosExternos();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("EXT001", result.get(0).getCodigoProducto());
        assertEquals(TipoProductoEnum.API, result.get(0).getTipoProducto());
        verify(productoRepository, times(1)).findProductosExternos();
    }

    @Test
    @DisplayName("getProductosExternos debe retornar lista vacía cuando no hay productos")
    void testGetProductosExternos_Empty() {
        // Arrange
        when(productoRepository.findProductosExternos()).thenReturn(new ArrayList<>());

        // Act
        List<Producto> result = productoService.getProductosExternos();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(productoRepository, times(1)).findProductosExternos();
    }

    @Test
    @DisplayName("getProductosInternos debe retornar lista de productos internos")
    void testGetProductosInternos() {
        // Arrange
        List<Producto> productosInternos = new ArrayList<>();
        Producto producto1 = new Producto();
        producto1.setId(2L);
        producto1.setCodigoProducto("INT001");
        producto1.setNombreGenerico("Producto Interno 1");
        producto1.setTipoProducto(TipoProductoEnum.SEMIELABORADO);
        productosInternos.add(producto1);

        Producto producto2 = new Producto();
        producto2.setId(3L);
        producto2.setCodigoProducto("INT002");
        producto2.setNombreGenerico("Producto Interno 2");
        producto2.setTipoProducto(TipoProductoEnum.UNIDAD_VENTA);
        productosInternos.add(producto2);

        when(productoRepository.findProductosInternos()).thenReturn(productosInternos);

        // Act
        List<Producto> result = productoService.getProductosInternos();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("INT001", result.get(0).getCodigoProducto());
        assertEquals("INT002", result.get(1).getCodigoProducto());
        verify(productoRepository, times(1)).findProductosInternos();
    }

    @Test
    @DisplayName("getProductosInternos debe retornar lista vacía cuando no hay productos")
    void testGetProductosInternos_Empty() {
        // Arrange
        when(productoRepository.findProductosInternos()).thenReturn(new ArrayList<>());

        // Act
        List<Producto> result = productoService.getProductosInternos();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(productoRepository, times(1)).findProductosInternos();
    }

    @Test
    @DisplayName("findByActivoTrueOrderByCodigoProductoAsc debe retornar productos activos ordenados")
    void testFindByActivoTrueOrderByCodigoProductoAsc() {
        // Arrange
        List<Producto> productosActivos = new ArrayList<>();
        Producto producto1 = new Producto();
        producto1.setId(1L);
        producto1.setCodigoProducto("A001");
        producto1.setNombreGenerico("Producto A");
        producto1.setActivo(true);
        productosActivos.add(producto1);

        Producto producto2 = new Producto();
        producto2.setId(2L);
        producto2.setCodigoProducto("B001");
        producto2.setNombreGenerico("Producto B");
        producto2.setActivo(true);
        productosActivos.add(producto2);

        when(productoRepository.findByActivoTrueOrderByCodigoProductoAsc()).thenReturn(productosActivos);

        // Act
        List<Producto> result = productoService.findByActivoTrueOrderByCodigoProductoAsc();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("A001", result.get(0).getCodigoProducto());
        assertEquals("B001", result.get(1).getCodigoProducto());
        assertTrue(result.get(0).getActivo());
        assertTrue(result.get(1).getActivo());
        verify(productoRepository, times(1)).findByActivoTrueOrderByCodigoProductoAsc();
    }

    @Test
    @DisplayName("findByActivoTrueOrderByCodigoProductoAsc debe retornar lista vacía cuando no hay productos activos")
    void testFindByActivoTrueOrderByCodigoProductoAsc_Empty() {
        // Arrange
        when(productoRepository.findByActivoTrueOrderByCodigoProductoAsc()).thenReturn(new ArrayList<>());

        // Act
        List<Producto> result = productoService.findByActivoTrueOrderByCodigoProductoAsc();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(productoRepository, times(1)).findByActivoTrueOrderByCodigoProductoAsc();
    }

    @Test
    @DisplayName("save debe guardar producto correctamente")
    void testSave() {
        // Arrange
        Producto producto = new Producto();
        producto.setCodigoProducto("TEST001");
        producto.setNombreGenerico("Producto Test");

        Producto savedProducto = new Producto();
        savedProducto.setId(1L);
        savedProducto.setCodigoProducto("TEST001");
        savedProducto.setNombreGenerico("Producto Test");

        when(productoRepository.save(producto)).thenReturn(savedProducto);

        // Act
        Producto result = productoService.save(producto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TEST001", result.getCodigoProducto());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    @DisplayName("findById debe retornar producto cuando existe")
    void testFindById_Exists() {
        // Arrange
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setCodigoProducto("TEST001");
        producto.setNombreGenerico("Producto Test");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Act
        Optional<Producto> result = productoService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("TEST001", result.get().getCodigoProducto());
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById debe retornar Optional.empty cuando no existe")
    void testFindById_NotExists() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Producto> result = productoService.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(productoRepository, times(1)).findById(999L);
    }
}

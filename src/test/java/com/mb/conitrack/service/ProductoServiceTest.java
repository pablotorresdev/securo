package com.mb.conitrack.service;

import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.repository.maestro.ProductoRepository;
import com.mb.conitrack.service.maestro.ProductoService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    ProductoRepository productoRepository;

    @InjectMocks
    ProductoService service;

    /* ---------------- helpers específicos ---------------- */

    // SOLO para tests de externos: activo + NO semi + NO UNIDAD_VENTA
    private Producto prodExterno(String codigo) {
        Producto p = mock(Producto.class);
        when(p.getCodigoProducto()).thenReturn(codigo);
        when(p.getActivo()).thenReturn(true);

        // mock de enum (≠ UNIDAD_VENTA) con esSemiElaborado=false
        TipoProductoEnum tipoMock = mock(TipoProductoEnum.class);
        when(tipoMock.esSemiElaborado()).thenReturn(false);
        when(p.getTipoProducto()).thenReturn(tipoMock);
        return p;
    }

    // SOLO para tests de internos: activo + semi (NO UNIDAD_VENTA)
    private Producto prodSemi(String codigo) {
        Producto p = mock(Producto.class);
        when(p.getCodigoProducto()).thenReturn(codigo);
        when(p.getActivo()).thenReturn(true);

        TipoProductoEnum tipoMock = mock(TipoProductoEnum.class);
        when(tipoMock.esSemiElaborado()).thenReturn(true);
        when(p.getTipoProducto()).thenReturn(tipoMock);
        return p;
    }

    // SOLO para tests de internos: activo + UNIDAD_VENTA
    private Producto prodUnidadVenta(String codigo) {
        Producto p = mock(Producto.class);
        when(p.getCodigoProducto()).thenReturn(codigo);
        when(p.getActivo()).thenReturn(true);
        when(p.getTipoProducto()).thenReturn(TipoProductoEnum.UNIDAD_VENTA);
        return p;
    }

    // MÍNIMO para tests que NO usan tipo/activo (p. ej. findAll sort)
    private Producto prodSoloCodigo(String codigo) {
        Producto p = mock(Producto.class);
        when(p.getCodigoProducto()).thenReturn(codigo);
        // NO stubbeamos getActivo ni getTipoProducto aquí
        return p;
    }

    /* ---------------- tests ---------------- */
    @Test
    @DisplayName("save delega en el repositorio")
    void save_ok() {
        // Podés usar un POJO real, así evitás stubs innecesarios
        Producto p = new Producto();
        when(productoRepository.save(p)).thenReturn(p);

        Producto saved = service.save(p);

        assertSame(p, saved);
        verify(productoRepository).save(p);
        verifyNoMoreInteractions(productoRepository);
    }

    @Test
    @DisplayName("findById delega en el repositorio")
    void findById_ok() {
        Producto p = new Producto();
        when(productoRepository.findById(123L)).thenReturn(Optional.of(p));

        Optional<Producto> opt = service.findById(123L);

        assertTrue(opt.isPresent());
        assertSame(p, opt.get());
        verify(productoRepository).findById(123L);
        verifyNoMoreInteractions(productoRepository);
    }
}

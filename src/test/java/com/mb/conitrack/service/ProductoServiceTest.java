package com.mb.conitrack.service;

import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.repository.maestro.ProductoRepository;
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
        when(p.getCodigoInterno()).thenReturn(codigo);
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
        when(p.getCodigoInterno()).thenReturn(codigo);
        when(p.getActivo()).thenReturn(true);

        TipoProductoEnum tipoMock = mock(TipoProductoEnum.class);
        when(tipoMock.esSemiElaborado()).thenReturn(true);
        when(p.getTipoProducto()).thenReturn(tipoMock);
        return p;
    }

    // SOLO para tests de internos: activo + UNIDAD_VENTA
    private Producto prodUnidadVenta(String codigo) {
        Producto p = mock(Producto.class);
        when(p.getCodigoInterno()).thenReturn(codigo);
        when(p.getActivo()).thenReturn(true);
        when(p.getTipoProducto()).thenReturn(TipoProductoEnum.UNIDAD_VENTA);
        return p;
    }

    // MÍNIMO para tests que NO usan tipo/activo (p. ej. findAll sort)
    private Producto prodSoloCodigo(String codigo) {
        Producto p = mock(Producto.class);
        when(p.getCodigoInterno()).thenReturn(codigo);
        // NO stubbeamos getActivo ni getTipoProducto aquí
        return p;
    }

    /* ---------------- tests ---------------- */

    @Test
    @DisplayName("getProductosExternos: filtra activos externos (no semi, no UNIDAD_VENTA) y ordena por código")
    void getProductosExternos_ok() {
        Producto aUnidad = prodUnidadVenta("A");     // fuera
        Producto bExt    = prodExterno("B");         // dentro
        Producto cInac   = mock(Producto.class);     // inactivo -> fuera
        when(cInac.getCodigoInterno()).thenReturn("C");
        when(cInac.getActivo()).thenReturn(false);

        Producto dSemi   = prodSemi("D");            // fuera
        Producto eExt    = prodExterno("E");         // dentro

        when(productoRepository.findAll()).thenReturn(List.of(eExt, dSemi, cInac, bExt, aUnidad));

        List<Producto> out = service.getProductosExternos();

        assertEquals(2, out.size());
        assertSame(bExt, out.get(0));
        assertSame(eExt, out.get(1));

        verify(productoRepository).findAll();
        verifyNoMoreInteractions(productoRepository);
    }

    @Test
    @DisplayName("getProductosInternos: filtra activos internos (semi o UNIDAD_VENTA) y ordena por código")
    void getProductosInternos_ok() {
        Producto aUnidad = prodUnidadVenta("A"); // dentro
        Producto bSemi   = prodSemi("B");        // dentro
        Producto cExt    = prodExterno("C");     // fuera

        // inactivo UNIDAD_VENTA -> fuera
        Producto dInacUnidad = mock(Producto.class);
        lenient().when(dInacUnidad.getCodigoInterno()).thenReturn("D"); // 👈 evitar UnnecessaryStubbing
        when(dInacUnidad.getActivo()).thenReturn(false);

        when(productoRepository.findAll()).thenReturn(List.of(dInacUnidad, cExt, bSemi, aUnidad));

        List<Producto> out = service.getProductosInternos();

        assertEquals(2, out.size());
        assertSame(aUnidad, out.get(0));
        assertSame(bSemi, out.get(1));

        verify(productoRepository).findAll();
        verifyNoMoreInteractions(productoRepository);
    }

    @Test
    @DisplayName("findAll: trae activos y los ordena por código (repo devuelve lista mutable)")
    void findAll_ok() {
        // aquí SOLO se usa getCodigoInterno para ordenar → NO stubbeamos tipo/activo
        Producto c = prodSoloCodigo("C");
        Producto a = prodSoloCodigo("A");
        Producto b = prodSoloCodigo("B");

        // el servicio asume que son activos porque llama findByActivoTrue()
        when(productoRepository.findByActivoTrue()).thenReturn(new ArrayList<>(List.of(c, a, b)));

        List<Producto> out = service.findAll();

        assertEquals(3, out.size());
        assertSame(a, out.get(0));
        assertSame(b, out.get(1));
        assertSame(c, out.get(2));

        verify(productoRepository).findByActivoTrue();
        verifyNoMoreInteractions(productoRepository);
    }

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

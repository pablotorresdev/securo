package com.mb.conitrack.service;

import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.repository.maestro.ProveedorRepository;
import com.mb.conitrack.service.maestro.ProveedorService;

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
class ProveedorServiceTest {

    @Mock
    ProveedorRepository proveedorRepository;

    @InjectMocks
    ProveedorService service;

    /* ---------------- helpers ---------------- */

    private Proveedor prov(String razonSocial) {
        Proveedor p = new Proveedor();
        p.setRazonSocial(razonSocial);
        return p;
    }

    /* ---------------- tests ---------------- */

    @Test
    @DisplayName("getProveedoresExternos: usa el repo con 'conifarma', ordena por razón social y retorna la misma lista ordenada")
    void getProveedoresExternos_ok() {
        Proveedor c = prov("C Pharma");
        Proveedor a = prov("A Labs");
        Proveedor b = prov("B Quimica");

        // Lista MUTABLE para permitir sort()
        List<Proveedor> entrada = new ArrayList<>(List.of(c, a, b));
        when(proveedorRepository.findProveedoresExternosOrderByRazonSocialAsc())
            .thenReturn(entrada);

        List<Proveedor> out = service.getProveedoresExternos();

        assertEquals(3, out.size());
        assertSame(c, out.get(0));
        assertSame(a, out.get(1));
        assertSame(b, out.get(2));

        verify(proveedorRepository).findProveedoresExternosOrderByRazonSocialAsc();
        verifyNoMoreInteractions(proveedorRepository);
    }

    @Test
    @DisplayName("getProveedoresExternos: lista vacía del repo -> retorna vacía")
    void getProveedoresExternos_vacio() {
        when(proveedorRepository.findProveedoresExternosOrderByRazonSocialAsc())
            .thenReturn(new ArrayList<>());

        List<Proveedor> out = service.getProveedoresExternos();

        assertNotNull(out);
        assertTrue(out.isEmpty());

        verify(proveedorRepository).findProveedoresExternosOrderByRazonSocialAsc();
        verifyNoMoreInteractions(proveedorRepository);
    }

    @Test
    @DisplayName("getConifarma: devuelve el primero de la lista del repo (ignore-case/containing)")
    void getConifarma_ok() {
        Proveedor p1 = prov("Conifarma SRL");
        Proveedor p2 = prov("Conifarma Industrial");

        when(proveedorRepository.findConifarma())
            .thenReturn(Optional.of(p1));

        Proveedor out = service.getConifarma();

        assertSame(p1, out);
        verify(proveedorRepository).findConifarma();
        verifyNoMoreInteractions(proveedorRepository);
    }

    @Test
    @DisplayName("getConifarma: si el repo no encuentra, lanza IllegalArgumentException")
    void getConifarma_noEncontrado() {
        when(proveedorRepository.findConifarma())
            .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.getConifarma());

        assertEquals("No se encontró el proveedor Conifarma", ex.getMessage());
        verify(proveedorRepository).findConifarma();
        verifyNoMoreInteractions(proveedorRepository);
    }

    @Test
    @DisplayName("findAll: trae del repo y ordena por razón social")
    void findAll_ByOrderByRazonSocialAsc_ok() {
        Proveedor z = prov("Zeta");
        Proveedor a = prov("Alfa");
        Proveedor m = prov("Magma");

        when(proveedorRepository.findAllByOrderByRazonSocialAsc())
            .thenReturn(new ArrayList<>(List.of(z, a, m))); // MUTABLE

        List<Proveedor> out = service.findAllByOrderByRazonSocialAsc();

        assertEquals(3, out.size());
        assertSame(z, out.get(0));
        assertSame(a, out.get(1));
        assertSame(m, out.get(2));

        verify(proveedorRepository).findAllByOrderByRazonSocialAsc();
        verifyNoMoreInteractions(proveedorRepository);
    }

    @Test
    @DisplayName("findById: delega en el repositorio")
    void findById_ok() {
        Proveedor p = prov("X");
        when(proveedorRepository.findById(7L)).thenReturn(Optional.of(p));

        Optional<Proveedor> out = service.findById(7L);

        assertTrue(out.isPresent());
        assertSame(p, out.get());
        verify(proveedorRepository).findById(7L);
        verifyNoMoreInteractions(proveedorRepository);
    }

    @Test
    @DisplayName("save: delega en el repositorio")
    void save_ok() {
        Proveedor p = prov("Guardar");
        when(proveedorRepository.save(p)).thenReturn(p);

        Proveedor saved = service.save(p);

        assertSame(p, saved);
        verify(proveedorRepository).save(p);
        verifyNoMoreInteractions(proveedorRepository);
    }
}

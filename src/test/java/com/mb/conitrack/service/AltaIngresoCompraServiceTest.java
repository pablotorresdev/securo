package com.mb.conitrack.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.repository.LoteRepository;
import com.mb.conitrack.repository.maestro.ProductoRepository;
import com.mb.conitrack.repository.maestro.ProveedorRepository;
import com.mb.conitrack.service.cu.AltaIngresoCompraService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AltaIngresoCompraServiceTest {

    @Mock
    LoteRepository loteRepository;

    @Mock
    ProveedorRepository proveedorRepository;

    @Mock
    ProductoRepository productoRepository;

    @Mock
    BultoService bultoService;

    @Mock
    MovimientoService movimientoService;

    @Mock
    SecurityContextService securityContextService;

    @InjectMocks
    AltaIngresoCompraService ingresoCompraService;

    @Test
    @DisplayName("altaStockPorCompra - producto inexistente -> IllegalArgumentException")
    void altaStockPorCompra_sinProducto() {
        // given
        LoteDTO dto = dtoBase();

        // Mock user
        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setId(1L);
        User testUser = new User("testuser", "password", adminRole);
        testUser.setId(1L);
        when(securityContextService.getCurrentUser()).thenReturn(testUser);

        Proveedor prov = new Proveedor();
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(prov));

        when(productoRepository.findById(2L)).thenReturn(Optional.empty());

        // when / then
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> ingresoCompraService.altaStockPorCompra(dto));
        assertEquals("El producto no existe.", ex.getMessage());

        verify(proveedorRepository).findById(1L);
        verify(productoRepository).findById(2L);
        verifyNoInteractions(loteRepository, bultoService, movimientoService);
    }

    @Test
    @DisplayName("altaStockPorCompra - proveedor inexistente -> IllegalArgumentException")
    void altaStockPorCompra_sinProveedor() {
        // given
        LoteDTO dto = dtoBase();

        // Mock user
        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setId(1L);
        User testUser = new User("testuser", "password", adminRole);
        testUser.setId(1L);
        when(securityContextService.getCurrentUser()).thenReturn(testUser);

        when(proveedorRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> ingresoCompraService.altaStockPorCompra(dto));
        assertEquals("El proveedor no existe.", ex.getMessage());

        // No debería avanzar a producto ni a guardar
        verify(proveedorRepository).findById(1L);
        verifyNoInteractions(productoRepository, loteRepository, bultoService, movimientoService);
    }

    LoteDTO dtoBase() {
        LoteDTO dto = new LoteDTO();
        dto.setProveedorId(1L);
        dto.setProductoId(2L);
        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        dto.setBultosTotales(1);
        dto.setCantidadInicial(new BigDecimal("5"));
        dto.setUnidadMedida(UnidadMedidaEnum.GRAMO);
        return dto;
    }

}
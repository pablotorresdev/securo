package com.mb.conitrack.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.ProductoService;
import com.mb.conitrack.service.ProveedorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Pruebas unitarias (sin contexto Spring) para: - showIngresoCompra() - initModelIngresoCompra()
 */
class ComprasControllerUnitTest {

    @Mock
    ProductoService productoService;

    @Mock
    ProveedorService proveedorService;

    @InjectMocks
    ComprasController controller;

    private final List<Proveedor> proveedoresMock = null;

    private final List<Producto> productosMock = null;

    /* -------------------------------------------------
     * Caso 2: LoteDTO con listas ya inicializadas
     * ------------------------------------------------- */
    @Test
    @DisplayName("showIngresoCompra respeta listas ya inicializadas")
    void dtoConListas_noSeSobrescribe() {
        // given
        LoteDTO dto = new LoteDTO();
        dto.setCantidadesBultos(new ArrayList<>(List.of(BigDecimal.ONE)));
        dto.setUnidadMedidaBultos(new ArrayList<>(List.of(UnidadMedidaEnum.KILOGRAMO)));
        Model model = new ExtendedModelMap();

        // when
        controller.showIngresoCompra(dto, model);

        // then
        assertEquals(1, dto.getCantidadesBultos().size());
        assertEquals(1, dto.getUnidadMedidaBultos().size());
        assertSame(dto, model.getAttribute("loteDTO"));        // misma instancia
    }

    @Test
    @DisplayName("showIngresoCompra inicializa listas si vienen nulas")
    void dtoConListas_null() {
        // given
        LoteDTO dto = new LoteDTO();
        dto.setCantidadesBultos(null);
        dto.setUnidadMedidaBultos(null);
        Model model = new ExtendedModelMap();
        // when
        controller.showIngresoCompra(dto, model);

        // then
        assertNotNull(dto.getCantidadesBultos());
        assertNotNull(dto.getUnidadMedidaBultos());
        assertSame(dto, model.getAttribute("loteDTO"));        // misma instancia
    }

    /* -------------------------------------------------
     * Caso 1: LoteDTO con listas nulas
     * ------------------------------------------------- */
    @Test
    @DisplayName("showIngresoCompra crea las listas cuando vienen nulas")
    void dtoNuevo_creaListas() {
        // given
        LoteDTO dto = new LoteDTO();      // listas = null
        Model model = new ExtendedModelMap();

        // when
        String vista = controller.showIngresoCompra(dto, model);

        // then
        assertEquals("compras/ingreso-compra", vista);
        assertSame(dto, model.getAttribute("loteDTO"));
        assertEquals(productosMock, model.getAttribute("productos"));
        assertEquals(proveedoresMock, model.getAttribute("proveedores"));
        assertNotNull(dto.getCantidadesBultos());
        assertNotNull(dto.getUnidadMedidaBultos());
    }

    @BeforeEach
    void setUp() {
        openMocks(this);   // inicializa @Mock y @InjectMocks

        given(proveedorService.getProveedoresExternos()).willReturn(proveedoresMock);
        given(productoService.getProductosExternos()).willReturn(productosMock);
    }

}

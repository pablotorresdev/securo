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

import static com.mb.conitrack.controller.ControllerUtils.getCountryList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Pruebas unitarias (sin contexto Spring) para: - showIngresoCompra() - initModelIngresoCompra()
 */
class ComprasControllerTest {

    @Mock
    ProductoService productoService;

    @Mock
    ProveedorService proveedorService;


    @InjectMocks
    ComprasController controller;

    @Mock
    List<Proveedor> proveedoresMock;

    @Mock
    List<Producto> productosMock;

    @BeforeEach
    void setUp() {
        openMocks(this);   // inicializa @Mock y @InjectMocks

        given(proveedorService.getProveedoresExternos()).willReturn(proveedoresMock);
        given(productoService.getProductosExternos()).willReturn(productosMock);
    }

    /* -------------------------------------------------
     * Caso 2: LoteDTO con listas ya inicializadas
     * ------------------------------------------------- */
    @Test
    @DisplayName("showIngresoCompra respeta listas ya inicializadas")
    void dtoConListas_noSeSobrescribe() {
        // given
        LoteDTO dto = new LoteDTO();
        dto.setCantidadesBultos(new ArrayList<BigDecimal>(List.of(BigDecimal.ONE)));
        dto.setUnidadMedidaBultos(new ArrayList<UnidadMedidaEnum>(List.of(UnidadMedidaEnum.KILOGRAMO)));
        Model model = new ExtendedModelMap();

        // when
        final String s = controller.showIngresoCompra(dto, model);
        assertEquals("compras/ingreso-compra", s);

        // then
        assertEquals(1, dto.getCantidadesBultos().size());
        assertEquals(1, dto.getCantidadesBultos().get(0).intValue());
        assertEquals(UnidadMedidaEnum.KILOGRAMO.getNombre(), dto.getUnidadMedidaBultos().get(0).getNombre());
        assertSame(dto, model.getAttribute("loteDTO"));        // misma instancia
        assertSame(productosMock, model.getAttribute("productos"));        // misma instancia
        assertSame(proveedoresMock, model.getAttribute("proveedores"));        // misma instancia
        assertEquals(getCountryList(), model.getAttribute("paises"));        // misma instancia
    }

    @Test
    @DisplayName("showIngresoCompra inicializa listas si vienen nulas")
    void dtoConListas_null() {
        LoteDTO dto = new LoteDTO();
        dto.setCantidadesBultos(null);
        dto.setUnidadMedidaBultos(null);

        controller.showIngresoCompra(dto, new ExtendedModelMap());

        assertNotNull(dto.getCantidadesBultos());
        assertNotNull(dto.getUnidadMedidaBultos());
    }


}

package com.mb.conitrack.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.ProductoService;
import com.mb.conitrack.service.ProveedorService;

import static com.mb.conitrack.controller.ControllerUtils.getCountryList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Pruebas unitarias (sin contexto Spring) para: - showIngresoCompra() - initModelIngresoCompra()
 */
@ExtendWith(MockitoExtension.class)
class ComprasControllerTest {

    @Spy
    @InjectMocks
    ComprasController controller;

    @Mock
    LoteService loteService;

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

    @Test
    @DisplayName("cancel")
    void cancel() {
        final String s = controller.cancelar();
        assertEquals("redirect:/", s);
    }

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

        when(proveedorService.getProveedoresExternos()).thenReturn(proveedoresMock);
        when(productoService.getProductosExternos()).thenReturn(productosMock);

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

        when(proveedorService.getProveedoresExternos()).thenReturn(proveedoresMock);
        when(productoService.getProductosExternos()).thenReturn(productosMock);

        controller.showIngresoCompra(dto, new ExtendedModelMap());

        assertNotNull(dto.getCantidadesBultos());
        assertNotNull(dto.getUnidadMedidaBultos());
    }

    @Test
    @DisplayName("exitoIngresoCompra")
    void exitoIngresoCompra() {
        final String s = controller.exitoIngresoCompra(dto);
        assertEquals("compras/ingreso-compra-ok", s);
    }

    @Test
    @DisplayName("Falla validateCantidadIngreso -> vuelve al form")
    void fallaPrimerValidador() {
        try (MockedStatic<ControllerUtils> mocked = mockStatic(ControllerUtils.class)) {
            mocked.when(() -> ControllerUtils.validateCantidadIngreso(dto, binding)).thenReturn(false);

            // Evitamos lógicas internas pesadas, solo verificamos que se llamen
            doNothing().when(controller).initModelIngresoCompra(any(), any());
            String view = controller.ingresoCompra(dto, binding, model, redirect);

            assertEquals("compras/ingreso-compra", view);
            mocked.verify(() -> ControllerUtils.validateCantidadIngreso(dto, binding));

            // Se arma el modelo y NO procesa
            verify(controller).initModelIngresoCompra(dto, model);
            verify(controller, never()).procesaringresoCompra(any(), any());
        }
    }

    @Test
    @DisplayName("Pasa 1ro, falla validateFechasProveedor -> vuelve al form")
    void fallaSegundoValidador() {
        try (MockedStatic<ControllerUtils> mocked = mockStatic(ControllerUtils.class)) {
            mocked.when(() -> ControllerUtils.validateCantidadIngreso(dto, binding)).thenReturn(true);
            mocked.when(() -> ControllerUtils.validateFechasProveedor(dto, binding)).thenReturn(false);

            // Evitamos lógicas internas pesadas, solo verificamos que se llamen
            doNothing().when(controller).initModelIngresoCompra(any(), any());
            String view = controller.ingresoCompra(dto, binding, model, redirect);

            assertEquals("compras/ingreso-compra", view);
            mocked.verify(() -> ControllerUtils.validateCantidadIngreso(dto, binding));
            mocked.verify(() -> ControllerUtils.validateFechasProveedor(dto, binding));

            verify(controller).initModelIngresoCompra(dto, model);
            verify(controller, never()).procesaringresoCompra(any(), any());
        }
    }

    @Test
    @DisplayName("Pasan 1ro y 2do, falla validarBultos -> vuelve al form")
    void fallaTercerValidador() {
        try (MockedStatic<ControllerUtils> mocked = mockStatic(ControllerUtils.class)) {
            mocked.when(() -> ControllerUtils.validateCantidadIngreso(dto, binding)).thenReturn(true);
            mocked.when(() -> ControllerUtils.validateFechasProveedor(dto, binding)).thenReturn(true);
            mocked.when(() -> ControllerUtils.validarBultos(dto, binding)).thenReturn(false);

            // Evitamos lógicas internas pesadas, solo verificamos que se llamen
            doNothing().when(controller).initModelIngresoCompra(any(), any());
            String view = controller.ingresoCompra(dto, binding, model, redirect);

            assertEquals("compras/ingreso-compra", view);
            mocked.verify(() -> ControllerUtils.validateCantidadIngreso(dto, binding));
            mocked.verify(() -> ControllerUtils.validateFechasProveedor(dto, binding));
            mocked.verify(() -> ControllerUtils.validarBultos(dto, binding));

            verify(controller).initModelIngresoCompra(dto, model);
            verify(controller, never()).procesaringresoCompra(any(), any());
        }
    }

    @Test
    @DisplayName("procesaringresoCompra: resultDTO == null -> agrega loteDTO=null y error")
    void procesar_error() {
        // given
        LoteDTO entrada = new LoteDTO();
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        Lote devueltoPorService = new Lote();

        when(loteService.altaStockPorCompra(entrada)).thenReturn(devueltoPorService);

        try (MockedStatic<DTOUtils> mocked = mockStatic(DTOUtils.class)) {
            // Forzamos que el merge devuelva null
            mocked.when(() -> DTOUtils.mergeEntities(devueltoPorService)).thenReturn(null);

            // when
            controller.procesaringresoCompra(entrada, redirect);

            // then
            assertNotNull(entrada.getFechaYHoraCreacion(), "Debe setear fecha/hora de creación");

            verify(loteService).altaStockPorCompra(entrada);
            mocked.verify(() -> DTOUtils.mergeEntities(devueltoPorService));

            Map<String, ?> flash = redirect.getFlashAttributes();
            assertTrue(flash.containsKey("loteDTO"));  // lo agrega aunque sea null
            assertNull(flash.get("loteDTO"));
            assertEquals("Hubo un error en el ingreso de stock por compra.", flash.get("error"));
            assertFalse(flash.containsKey("success"));
        }
    }

    @Test
    @DisplayName("procesaringresoCompra: resultDTO != null -> agrega loteDTO y success")
    void procesar_ok() {
        // given
        LoteDTO entrada = new LoteDTO();
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        Lote devueltoPorService = new Lote();
        LoteDTO resultDTO = new LoteDTO();

        when(loteService.altaStockPorCompra(entrada)).thenReturn(devueltoPorService);

        try (MockedStatic<DTOUtils> mocked = mockStatic(DTOUtils.class)) {
            mocked.when(() -> DTOUtils.mergeEntities(devueltoPorService)).thenReturn(resultDTO);

            // when
            controller.procesaringresoCompra(entrada, redirect);

            // then
            // 1) Se setea fecha y hora de creación
            assertNotNull(entrada.getFechaYHoraCreacion(), "Debe setear fecha/hora de creación");

            // 2) Se llamó al servicio y al DTOUtils
            verify(loteService).altaStockPorCompra(entrada);
            mocked.verify(() -> DTOUtils.mergeEntities(devueltoPorService));

            // 3) Flash attributes correctos
            Map<String, ?> flash = redirect.getFlashAttributes();
            assertSame(resultDTO, flash.get("loteDTO"));
            assertEquals("Ingreso de stock por compra exitoso.", flash.get("success"));
            assertFalse(flash.containsKey("error"));
        }
    }

    @BeforeEach
    void setUp() {
        openMocks(this);   // inicializa @Mock y @InjectMocks
        model = new ExtendedModelMap();
        redirect = new RedirectAttributesModelMap();
        dto = new LoteDTO();
        binding = new BeanPropertyBindingResult(dto, "loteDTO");
    }

    @Test
    @DisplayName("Todos OK -> redirige y procesa")
    void todoOk() {
        try (MockedStatic<ControllerUtils> mocked = mockStatic(ControllerUtils.class)) {
            mocked.when(() -> ControllerUtils.validateCantidadIngreso(dto, binding)).thenReturn(true);
            mocked.when(() -> ControllerUtils.validateFechasProveedor(dto, binding)).thenReturn(true);
            mocked.when(() -> ControllerUtils.validarBultos(dto, binding)).thenReturn(true);

            doNothing().when(controller).procesaringresoCompra(any(), any());

            String view = controller.ingresoCompra(dto, binding, model, redirect);

            assertEquals("redirect:/compras/ingreso-compra-ok", view);
            mocked.verify(() -> ControllerUtils.validateCantidadIngreso(dto, binding));
            mocked.verify(() -> ControllerUtils.validateFechasProveedor(dto, binding));
            mocked.verify(() -> ControllerUtils.validarBultos(dto, binding));

            verify(controller, never()).initModelIngresoCompra(any(), any());
            verify(controller).procesaringresoCompra(dto, redirect);
        }
    }

}

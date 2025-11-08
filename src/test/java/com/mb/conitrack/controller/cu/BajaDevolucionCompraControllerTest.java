package com.mb.conitrack.controller.cu;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.cu.BajaDevolucionCompraService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests para BajaDevolucionCompraController - 100% Coverage
 */
@ExtendWith(MockitoExtension.class)
class BajaDevolucionCompraControllerTest {

    @Spy
    @InjectMocks
    BajaDevolucionCompraController controller;

    @Mock
    BajaDevolucionCompraService devolucionService;

    @Mock
    LoteService loteService;

    Model model;

    RedirectAttributes redirect;

    MovimientoDTO movDto;

    BindingResult binding;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        redirect = new RedirectAttributesModelMap();
        movDto = new MovimientoDTO();
        binding = new BeanPropertyBindingResult(movDto, "movimientoDTO");
    }

    // -------------------- GET /cancelar --------------------

    @Test
    void testCancelar() {
        assertEquals("redirect:/", controller.cancelar());
    }

    // -------------------- GET /devolucion-compra --------------------

    @Test
    void testShowDevolucionCompraForm_InicializaModeloYRetornaView() {
        // given
        var lista = List.of(new LoteDTO(), new LoteDTO());
        when(loteService.findAllForDevolucionCompraDTOs()).thenReturn(lista);

        // when
        String view = controller.showDevolucionCompraForm(movDto, model);

        // then
        assertEquals("compras/baja/devolucion-compra", view);
        assertSame(lista, model.getAttribute("lotesDevolvibles"));
        assertSame(movDto, model.getAttribute("movimientoDTO"));
    }

    // -------------------- POST /devolucion-compra/confirm --------------------

    @Test
    void testConfirmarDevolucionCompra_ConValidacionExitosa() {
        // given
        movDto.setCodigoLote("LOTE-001");

        Producto producto = new Producto();
        producto.setNombreGenerico("Paracetamol");
        producto.setCodigoProducto("API-001");

        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial("Proveedor Test");

        Lote lote = new Lote();
        lote.setCodigoLote("LOTE-001");
        lote.setProducto(producto);
        lote.setProveedor(proveedor);
        lote.setCantidadActual(BigDecimal.valueOf(100));
        lote.setUnidadMedida(UnidadMedidaEnum.KILOGRAMO);

        when(devolucionService.validarDevolucionCompraInput(movDto, binding)).thenReturn(true);
        when(loteService.findByCodigoLote("LOTE-001")).thenReturn(Optional.of(lote));

        // when
        String view = controller.confirmarDevolucionCompra(movDto, binding, model);

        // then
        assertEquals("compras/baja/devolucion-compra-confirm", view);
        assertEquals("Paracetamol", movDto.getNombreProducto());
        assertEquals("API-001", movDto.getCodigoProducto());
        assertEquals("Proveedor Test", movDto.getNombreProveedor());
        assertNotNull(model.getAttribute("loteDTO"));
        assertSame(movDto, model.getAttribute("movimientoDTO"));
    }

    @Test
    void testConfirmarDevolucionCompra_ConValidacionFallida() {
        // given
        var lista = List.of(new LoteDTO());
        when(devolucionService.validarDevolucionCompraInput(movDto, binding)).thenReturn(false);
        when(loteService.findAllForDevolucionCompraDTOs()).thenReturn(lista);

        // when
        String view = controller.confirmarDevolucionCompra(movDto, binding, model);

        // then
        assertEquals("compras/baja/devolucion-compra", view);
        assertSame(lista, model.getAttribute("lotesDevolvibles"));
    }

    @Test
    void testConfirmarDevolucionCompra_LoteNoEncontrado() {
        // given
        movDto.setCodigoLote("LOTE-999");
        when(devolucionService.validarDevolucionCompraInput(movDto, binding)).thenReturn(true);
        when(loteService.findByCodigoLote("LOTE-999")).thenReturn(Optional.empty());

        // when
        String view = controller.confirmarDevolucionCompra(movDto, binding, model);

        // then
        assertEquals("compras/baja/devolucion-compra-confirm", view);
        assertNull(movDto.getNombreProducto());
        assertNull(model.getAttribute("loteDTO"));
    }

    // -------------------- POST /devolucion-compra --------------------

    @Test
    void testDevolucionCompra_ConValidacionExitosa() {
        // given
        LoteDTO resultDTO = new LoteDTO();
        resultDTO.setCodigoLote("LOTE-001");

        when(devolucionService.validarDevolucionCompraInput(movDto, binding)).thenReturn(true);
        when(devolucionService.bajaBultosDevolucionCompra(movDto)).thenReturn(resultDTO);

        // when
        String view = controller.devolucionCompra(movDto, binding, model, redirect);

        // then
        assertEquals("redirect:/compras/baja/devolucion-compra-ok", view);
        assertEquals(resultDTO, redirect.getFlashAttributes().get("loteDTO"));
        assertEquals("Devolucion realizada correctamente.", redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testDevolucionCompra_ConValidacionFallida() {
        // given
        var lista = List.of(new LoteDTO());
        when(devolucionService.validarDevolucionCompraInput(movDto, binding)).thenReturn(false);
        when(loteService.findAllForDevolucionCompraDTOs()).thenReturn(lista);

        // when
        String view = controller.devolucionCompra(movDto, binding, model, redirect);

        // then
        assertEquals("compras/baja/devolucion-compra", view);
        assertSame(lista, model.getAttribute("lotesDevolvibles"));
    }

    @Test
    void testDevolucionCompra_ConErrorEnPersistencia() {
        // given
        when(devolucionService.validarDevolucionCompraInput(movDto, binding)).thenReturn(true);
        when(devolucionService.bajaBultosDevolucionCompra(movDto)).thenReturn(null);

        // when
        String view = controller.devolucionCompra(movDto, binding, model, redirect);

        // then
        assertEquals("redirect:/compras/baja/devolucion-compra-ok", view);
        assertNull(redirect.getFlashAttributes().get("loteDTO"));
        assertEquals("Hubo un error en la devolucion de compra.", redirect.getFlashAttributes().get("error"));
    }

    // -------------------- GET /devolucion-compra-ok --------------------

    @Test
    void testExitoDevolucionCompra() {
        LoteDTO loteDTO = new LoteDTO();
        loteDTO.setCodigoLote("LOTE-001");

        assertEquals("compras/baja/devolucion-compra-ok", controller.exitoDevolucionCompra(loteDTO));
    }

    // -------------------- Métodos internos --------------------

    @Test
    void testInitModelDevolucionCompra() {
        // given
        var lista = List.of(new LoteDTO(), new LoteDTO());
        when(loteService.findAllForDevolucionCompraDTOs()).thenReturn(lista);

        // when
        controller.initModelDevolucionCompra(model, movDto);

        // then
        assertSame(lista, model.getAttribute("lotesDevolvibles"));
        assertSame(movDto, model.getAttribute("movimientoDTO"));
        verify(loteService).findAllForDevolucionCompraDTOs();
    }

    @Test
    void testProcesarDevolucionCompra_ConExito() {
        // given
        LoteDTO resultDTO = new LoteDTO();
        resultDTO.setCodigoLote("LOTE-001");
        when(devolucionService.bajaBultosDevolucionCompra(movDto)).thenReturn(resultDTO);

        // when
        controller.procesarDevolucionCompra(movDto, redirect);

        // then
        assertNotNull(movDto.getFechaYHoraCreacion());
        assertEquals(resultDTO, redirect.getFlashAttributes().get("loteDTO"));
        assertEquals("Devolucion realizada correctamente.", redirect.getFlashAttributes().get("success"));
        verify(devolucionService).bajaBultosDevolucionCompra(movDto);
    }

    @Test
    void testProcesarDevolucionCompra_ConError() {
        // given
        when(devolucionService.bajaBultosDevolucionCompra(movDto)).thenReturn(null);

        // when
        controller.procesarDevolucionCompra(movDto, redirect);

        // then
        assertNotNull(movDto.getFechaYHoraCreacion());
        assertNull(redirect.getFlashAttributes().get("loteDTO"));
        assertEquals("Hubo un error en la devolucion de compra.", redirect.getFlashAttributes().get("error"));
    }

}

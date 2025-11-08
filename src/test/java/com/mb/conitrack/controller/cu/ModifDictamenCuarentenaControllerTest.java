package com.mb.conitrack.controller.cu;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.cu.ModifDictamenCuarentenaService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModifDictamenCuarentenaControllerTest {

    @Spy
    @InjectMocks
    ModifDictamenCuarentenaController controller;

    @Mock ModifDictamenCuarentenaService dictamenCuarentenaService;
    @Mock AnalisisService analisisService;
    @Mock LoteService loteService;

    Model model;
    LoteDTO dto;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        dto = new LoteDTO();
    }

    // -------------------- GET /cancelar --------------------
    @Test
    @DisplayName("cancel")
    void testCancel() {
        assertEquals("redirect:/", controller.cancelar());
    }

    // -------------------- GET /cuarentena-ok --------------------
    @Test
    void testExitoDictamenCuarentena() {
        assertEquals("calidad/dictamen/cuarentena-ok", controller.exitoDictamenCuarentena(new LoteDTO()));
    }

    // -------------------- GET /cuarentena --------------------
    @Test
    @DisplayName("GET /cuarentena -> llena modelo con lista de LoteDTO y movimientoDTO")
    void testShowDictamenCuarentenaForm_listaConDatos() {
        MovimientoDTO mov = new MovimientoDTO();
        Model model = new ExtendedModelMap();

        LoteDTO l1 = new LoteDTO();
        LoteDTO l2 = new LoteDTO();
        List<LoteDTO> lista = List.of(l1, l2);
        when(loteService.findAllForCuarentenaDTOs()).thenReturn(lista);

        String view = controller.showDictamenCuarentenaForm(mov, model);

        assertEquals("calidad/dictamen/cuarentena", view);
        assertSame(mov, model.getAttribute("movimientoDTO"));

        // Verificamos contenido (equals), no identidad de referencia:
        Object attr = model.getAttribute("loteCuarentenaDTOs");
        assertNotNull(attr);
        assertTrue(attr instanceof List<?>);
        @SuppressWarnings("unchecked")
        List<LoteDTO> enModelo = (List<LoteDTO>) attr;
        assertEquals(lista.size(), enModelo.size());
        assertEquals(lista, enModelo); // igualdad por contenido (Lombok @Data genera equals)
    }

    @Test
    @DisplayName("GET /cuarentena -> lista vacía")
    void testShowDictamenCuarentenaForm_listaVacia() {
        MovimientoDTO mov = new MovimientoDTO();
        when(loteService.findAllForCuarentenaDTOs()).thenReturn(Collections.emptyList());

        String view = controller.showDictamenCuarentenaForm(mov, model);

        assertEquals("calidad/dictamen/cuarentena", view);
        verify(loteService).findAllForCuarentenaDTOs();
        Object lotesAttr = model.getAttribute("loteCuarentenaDTOs");
        assertNotNull(lotesAttr);
        assertInstanceOf(List.class, lotesAttr);
        assertTrue(((List<?>) lotesAttr).isEmpty());
        assertSame(mov, model.getAttribute("movimientoDTO"));
    }

    @Test
    @DisplayName("initModelDictamencuarentena -> setea atributos")
    void testInitModelDictamencuarentena() {
        MovimientoDTO mov = new MovimientoDTO();
        List<LoteDTO> lista = List.of(new LoteDTO());
        when(loteService.findAllForCuarentenaDTOs()).thenReturn(lista);

        controller.initModelDictamencuarentena(mov, model);

        assertSame(lista, model.getAttribute("loteCuarentenaDTOs"));
        assertSame(mov, model.getAttribute("movimientoDTO"));
    }

    // -------------------- POST /cuarentena (handler) --------------------
    @Test
    @DisplayName("POST falla: ya hay errores en BindingResult → vuelve al form")
    void testProcesarDictamenCuarentena_Handler_BindingErrors() {
        MovimientoDTO mov = new MovimientoDTO();
        BindingResult br = new BeanPropertyBindingResult(mov, "movimientoDTO");
        br.addError(new FieldError("movimientoDTO", "dummy", "x"));
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        // no hace falta mockear nada más; con el error inicial la validación corta
        String view = controller.dictamenCuarentena(mov, br, model, redirect);

        assertEquals("calidad/dictamen/cuarentena", view);
        assertSame(mov, model.getAttribute("movimientoDTO"));
        // init model se ejecuta
        assertNotNull(model.getAttribute("loteCuarentenaDTOs"));
    }

    @Test
    @DisplayName("POST OK: validación exitosa → redirect a cuarentena-ok")
    void testDictamenCuarentena_ValidacionExitosa() {
        MovimientoDTO mov = new MovimientoDTO();
        mov.setCodigoLote("LOTE-001");
        BindingResult br = new BeanPropertyBindingResult(mov, "movimientoDTO");
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        LoteDTO loteDTOResult = new LoteDTO();
        loteDTOResult.setCodigoLote("LOTE-001");

        when(dictamenCuarentenaService.validarDictamenCuarentenaInput(mov, br)).thenReturn(true);
        when(dictamenCuarentenaService.persistirDictamenCuarentena(any(MovimientoDTO.class))).thenReturn(loteDTOResult);

        String view = controller.dictamenCuarentena(mov, br, model, redirect);

        assertEquals("redirect:/calidad/dictamen/cuarentena-ok", view);
        verify(dictamenCuarentenaService).validarDictamenCuarentenaInput(mov, br);
        verify(dictamenCuarentenaService).persistirDictamenCuarentena(any(MovimientoDTO.class));
        assertEquals(loteDTOResult, redirect.getFlashAttributes().get("loteDTO"));
        assertEquals("Cambio de calidad a Cuarentena exitoso", redirect.getFlashAttributes().get("success"));
    }

    // -------------------- POST /cuarentena/confirm --------------------
    @Test
    @DisplayName("POST /cuarentena/confirm: validación falla → vuelve al form")
    void testConfirmarDictamenCuarentena_ValidacionFalla() {
        MovimientoDTO mov = new MovimientoDTO();
        BindingResult br = new BeanPropertyBindingResult(mov, "movimientoDTO");

        when(dictamenCuarentenaService.validarDictamenCuarentenaInput(mov, br)).thenReturn(false);
        when(loteService.findAllForCuarentenaDTOs()).thenReturn(Collections.emptyList());

        String view = controller.confirmarDictamenCuarentena(mov, br, model);

        assertEquals("calidad/dictamen/cuarentena", view);
        verify(dictamenCuarentenaService).validarDictamenCuarentenaInput(mov, br);
        verify(loteService).findAllForCuarentenaDTOs();
        assertSame(mov, model.getAttribute("movimientoDTO"));
    }

    @Test
    @DisplayName("POST /cuarentena/confirm: validación OK → muestra confirmación con lote")
    void testConfirmarDictamenCuarentena_ValidacionExitosa_ConLote() {
        MovimientoDTO mov = new MovimientoDTO();
        mov.setCodigoLote("LOTE-001");
        BindingResult br = new BeanPropertyBindingResult(mov, "movimientoDTO");

        com.mb.conitrack.entity.Lote lote = new com.mb.conitrack.entity.Lote();
        lote.setCodigoLote("LOTE-001");

        com.mb.conitrack.entity.maestro.Producto producto = new com.mb.conitrack.entity.maestro.Producto();
        producto.setNombreGenerico("Producto Test");
        producto.setCodigoProducto("PROD-001");
        lote.setProducto(producto);

        com.mb.conitrack.entity.maestro.Proveedor proveedor = new com.mb.conitrack.entity.maestro.Proveedor();
        proveedor.setRazonSocial("Proveedor Test");
        lote.setProveedor(proveedor);

        when(dictamenCuarentenaService.validarDictamenCuarentenaInput(mov, br)).thenReturn(true);
        when(loteService.findByCodigoLote("LOTE-001")).thenReturn(java.util.Optional.of(lote));

        String view = controller.confirmarDictamenCuarentena(mov, br, model);

        assertEquals("calidad/dictamen/cuarentena-confirm", view);
        verify(dictamenCuarentenaService).validarDictamenCuarentenaInput(mov, br);
        verify(loteService).findByCodigoLote("LOTE-001");

        assertEquals("Producto Test", mov.getNombreProducto());
        assertEquals("PROD-001", mov.getCodigoProducto());
        assertEquals("Proveedor Test", mov.getNombreProveedor());

        assertNotNull(model.getAttribute("loteDTO"));
        assertSame(mov, model.getAttribute("movimientoDTO"));
    }

    @Test
    @DisplayName("POST /cuarentena/confirm: validación OK pero lote no encontrado → solo muestra mov")
    void testConfirmarDictamenCuarentena_ValidacionExitosa_SinLote() {
        MovimientoDTO mov = new MovimientoDTO();
        mov.setCodigoLote("LOTE-999");
        BindingResult br = new BeanPropertyBindingResult(mov, "movimientoDTO");

        when(dictamenCuarentenaService.validarDictamenCuarentenaInput(mov, br)).thenReturn(true);
        when(loteService.findByCodigoLote("LOTE-999")).thenReturn(java.util.Optional.empty());

        String view = controller.confirmarDictamenCuarentena(mov, br, model);

        assertEquals("calidad/dictamen/cuarentena-confirm", view);
        verify(dictamenCuarentenaService).validarDictamenCuarentenaInput(mov, br);
        verify(loteService).findByCodigoLote("LOTE-999");
        assertSame(mov, model.getAttribute("movimientoDTO"));
    }

    // -------------------- procesarDictamenCuarentena helper --------------------
    @Test
    @DisplayName("procesarDictamenCuarentena: éxito → flash success")
    void testProcesarDictamenCuarentena_Exito() {
        MovimientoDTO mov = new MovimientoDTO();
        mov.setCodigoLote("LOTE-001");
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        LoteDTO resultado = new LoteDTO();
        resultado.setCodigoLote("LOTE-001");

        when(dictamenCuarentenaService.persistirDictamenCuarentena(any(MovimientoDTO.class))).thenReturn(resultado);

        controller.procesarDictamenCuarentena(mov, redirect);

        verify(dictamenCuarentenaService).persistirDictamenCuarentena(any(MovimientoDTO.class));
        assertEquals(resultado, redirect.getFlashAttributes().get("loteDTO"));
        assertEquals("Cambio de calidad a Cuarentena exitoso", redirect.getFlashAttributes().get("success"));
        assertNull(redirect.getFlashAttributes().get("error"));
        assertNotNull(mov.getFechaYHoraCreacion());
    }

    @Test
    @DisplayName("procesarDictamenCuarentena: error (null) → flash error")
    void testProcesarDictamenCuarentena_Error() {
        MovimientoDTO mov = new MovimientoDTO();
        mov.setCodigoLote("LOTE-001");
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        when(dictamenCuarentenaService.persistirDictamenCuarentena(any(MovimientoDTO.class))).thenReturn(null);

        controller.procesarDictamenCuarentena(mov, redirect);

        verify(dictamenCuarentenaService).persistirDictamenCuarentena(any(MovimientoDTO.class));
        assertNull(redirect.getFlashAttributes().get("loteDTO"));
        assertEquals("Hubo un error al realizar el cambio de calidad a Cuarentena.", redirect.getFlashAttributes().get("error"));
        assertNull(redirect.getFlashAttributes().get("success"));
    }

}

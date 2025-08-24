package com.mb.conitrack.controller.cu;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.cu.ModifDictamenCuarentenaService;
import com.mb.conitrack.utils.ControllerUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

}

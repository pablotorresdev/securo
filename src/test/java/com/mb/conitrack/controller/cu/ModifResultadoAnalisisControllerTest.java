package com.mb.conitrack.controller.cu;

import java.util.ArrayList;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.cu.ModifResultadoAnalisisService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class ModifResultadoAnalisisControllerTest {

    @Spy
    @InjectMocks
    ModifResultadoAnalisisController controller;

    @Mock
    ModifResultadoAnalisisService resultadoAnalisisService;

    @Mock
    LoteService loteService;

    @Mock
    AnalisisService analisisService;

    Model model;

    RedirectAttributes redirect;

    MovimientoDTO dto;

    BindingResult binding;

    private static List<Analisis> analisis(int n) {
        List<Analisis> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(new Analisis());
        }
        return list;
    }

    /* ------------ helpers ------------ */

    private static List<LoteDTO> loteDTOs(int n) {
        List<LoteDTO> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(new LoteDTO());
        }
        return list;
    }

    private static List<Lote> lotes(int n) {
        List<Lote> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(new Lote());
        }
        return list;
    }

    @Test
    @DisplayName("GET /cancelar -> redirect:/")
    void cancelar() {
        assertEquals("redirect:/", controller.cancelar());
    }

    /* ------------ GET cancelar / ok / form ------------ */

    @Test
    @DisplayName("GET /resultado-analisis-ok")
    void exitoResultadoAnalisis() {
        assertEquals(
            "calidad/analisis/resultado-analisis-ok",
            controller.exitoResultadoAnalisis(new LoteDTO()));
    }

    /* ------------ POST: errores de validación en cadena ------------ */

    @BeforeEach
    void setup() {
        openMocks(this);
        model = new ExtendedModelMap();
        redirect = new RedirectAttributesModelMap();
        dto = new MovimientoDTO();
        // IMPORTANTE: seteamos un código no nulo para evitar mismatch con anyString()
        dto.setCodigoLote("X");
        binding = new BeanPropertyBindingResult(dto, "movimientoDTO");
    }

}

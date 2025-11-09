package com.mb.conitrack.controller.cu;

import java.util.List;

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

import com.mb.conitrack.dto.AnalisisDTO;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.cu.ModifResultadoAnalisisService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
    MovimientoDTO movDto;
    BindingResult binding;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        redirect = new RedirectAttributesModelMap();
        movDto = new MovimientoDTO();
        binding = new BeanPropertyBindingResult(movDto, "movimientoDTO");
    }

    @Test
    void testCancelar() {
        assertEquals("redirect:/", controller.cancelar());
    }

    @Test
    void testShowResultadoAnalisisForm() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        List<AnalisisDTO> analisis = List.of(new AnalisisDTO());
        when(loteService.findAllForResultadoAnalisisDTOs()).thenReturn(lotes);
        when(analisisService.findAllEnCursoForLotesCuarentenaDTOs()).thenReturn(analisis);

        String view = controller.showResultadoAnalisisForm(movDto, model);

        assertEquals("calidad/analisis/resultado-analisis", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
        assertSame(lotes, model.getAttribute("loteResultadoAnalisisDTOs"));
        assertSame(analisis, model.getAttribute("analisisDTOs"));
        assertNotNull(model.getAttribute("resultados"));
    }

    @Test
    void testResultadoAnalisis_ConValidacionExitosa() {
        LoteDTO resultDTO = new LoteDTO();
        movDto.setDictamenFinal(com.mb.conitrack.enums.DictamenEnum.APROBADO);
        when(resultadoAnalisisService.validarResultadoAnalisisInput(movDto, binding)).thenReturn(true);
        when(resultadoAnalisisService.persistirResultadoAnalisis(any(MovimientoDTO.class))).thenReturn(resultDTO);

        String view = controller.resultadoAnalisis(movDto, binding, model, redirect);

        assertEquals("redirect:/calidad/analisis/resultado-analisis-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("loteDTO"));
        assertNotNull(redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testResultadoAnalisis_ConValidacionFallida() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        List<AnalisisDTO> analisis = List.of(new AnalisisDTO());
        when(resultadoAnalisisService.validarResultadoAnalisisInput(movDto, binding)).thenReturn(false);
        when(loteService.findAllForResultadoAnalisisDTOs()).thenReturn(lotes);
        when(analisisService.findAllEnCursoForLotesCuarentenaDTOs()).thenReturn(analisis);

        String view = controller.resultadoAnalisis(movDto, binding, model, redirect);

        assertEquals("calidad/analisis/resultado-analisis", view);
        assertSame(lotes, model.getAttribute("loteResultadoAnalisisDTOs"));
    }

    @Test
    void testResultadoAnalisis_ConResultadoNull() {
        when(resultadoAnalisisService.validarResultadoAnalisisInput(movDto, binding)).thenReturn(true);
        when(resultadoAnalisisService.persistirResultadoAnalisis(any(MovimientoDTO.class))).thenReturn(null);

        String view = controller.resultadoAnalisis(movDto, binding, model, redirect);

        assertEquals("redirect:/calidad/analisis/resultado-analisis-ok", view);
        assertEquals("Hubo un error con el cambio de dictamen.", redirect.getFlashAttributes().get("error"));
    }

    @Test
    void testExitoResultadoAnalisis() {
        assertEquals("calidad/analisis/resultado-analisis-ok", controller.exitoResultadoAnalisis(new LoteDTO()));
    }

    @Test
    void testInitModelResultadoAnalisis() {
        List<LoteDTO> lotes = List.of(new LoteDTO());
        List<AnalisisDTO> analisis = List.of(new AnalisisDTO());
        when(loteService.findAllForResultadoAnalisisDTOs()).thenReturn(lotes);
        when(analisisService.findAllEnCursoForLotesCuarentenaDTOs()).thenReturn(analisis);

        controller.initModelResultadoAnalisis(movDto, model);

        assertSame(movDto, model.getAttribute("movimientoDTO"));
        assertNotNull(movDto.getFechaMovimiento());

        @SuppressWarnings("unchecked")
        List<DictamenEnum> resultados = (List<DictamenEnum>) model.getAttribute("resultados");
        assertEquals(2, resultados.size());
    }

    @Test
    void testProcesarResultadoAnalisis() {
        LoteDTO resultDTO = new LoteDTO();
        movDto.setDictamenFinal(com.mb.conitrack.enums.DictamenEnum.RECHAZADO);
        when(resultadoAnalisisService.persistirResultadoAnalisis(any(MovimientoDTO.class))).thenReturn(resultDTO);

        controller.procesarResultadoAnalisis(movDto, redirect);

        assertNotNull(redirect.getFlashAttributes().get("loteDTO"));
        assertNotNull(redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testProcesarResultadoAnalisis_ConResultadoNull() {
        when(resultadoAnalisisService.persistirResultadoAnalisis(any(MovimientoDTO.class))).thenReturn(null);

        controller.procesarResultadoAnalisis(movDto, redirect);

        assertEquals("Hubo un error con el cambio de dictamen.", redirect.getFlashAttributes().get("error"));
    }
}

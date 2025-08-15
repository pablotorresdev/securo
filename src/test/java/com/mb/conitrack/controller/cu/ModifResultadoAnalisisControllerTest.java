package com.mb.conitrack.controller.cu;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

import com.mb.conitrack.dto.AnalisisDTO;
import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.utils.ControllerUtils;

import static com.mb.conitrack.dto.DTOUtils.fromAnalisisEntities;
import static com.mb.conitrack.dto.DTOUtils.getLotesDtosByCodigoInterno;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class ModifResultadoAnalisisControllerTest {

    @Spy
    @InjectMocks
    ModifResultadoAnalisisController controller;

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

    @Test
    @DisplayName("Falla validarContraFechasProveedor -> vuelve al form")
    void fallaContraFechasProveedor() {
        when(loteService.findAllForResultadoAnalisis()).thenReturn(Collections.emptyList());
        when(analisisService.findAllEnCursoForLotesCuarentena()).thenReturn(Collections.emptyList());

        try (
            MockedStatic<ControllerUtils> cu = mockStatic(ControllerUtils.class);
            MockedStatic<DTOUtils> du = mockStatic(DTOUtils.class)) {

            ControllerUtils utils = mock(ControllerUtils.class);
            cu.when(ControllerUtils::getInstance).thenReturn(utils);

            cu.when(() -> ControllerUtils.validarDatosMandatoriosResultadoAnalisisInput(dto, binding))
                .thenReturn(true);
            cu.when(() -> ControllerUtils.validarDatosResultadoAnalisisAprobadoInput(dto, binding))
                .thenReturn(true);

            when(utils.populateLoteListByCodigoInterno(anyList(), nullable(String.class), eq(binding), eq(loteService)))
                .thenAnswer(inv -> {
                    ((List<Lote>)inv.getArgument(0)).add(new Lote());
                    return true;
                });

            cu.when(() -> ControllerUtils.validarExisteMuestreoParaAnalisis(eq(dto), anyList(), eq(binding)))
                .thenReturn(true);

            when(utils.validarFechaMovimientoPosteriorIngresoLote(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(true);
            when(utils.validarFechaAnalisisPosteriorIngresoLote(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(true);

            cu.when(() -> ControllerUtils.validarContraFechasProveedor(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(false);

            du.when(() -> getLotesDtosByCodigoInterno(anyList())).thenReturn(Collections.emptyList());
            du.when(() -> fromAnalisisEntities(anyList())).thenReturn(Collections.emptyList());

            String view = controller.procesarResultadoAnalisis(dto, binding, model, redirect);
            assertEquals("calidad/analisis/resultado-analisis", view);
        }
    }

    @Test
    @DisplayName("Falla validarDatosResultadoAnalisisAprobadoInput -> vuelve al form")
    void fallaDatosAprobado() {
        when(loteService.findAllForResultadoAnalisis()).thenReturn(Collections.emptyList());
        when(analisisService.findAllEnCursoForLotesCuarentena()).thenReturn(Collections.emptyList());

        try (
            MockedStatic<ControllerUtils> cu = mockStatic(ControllerUtils.class);
            MockedStatic<DTOUtils> du = mockStatic(DTOUtils.class)) {

            cu.when(() -> ControllerUtils.validarDatosMandatoriosResultadoAnalisisInput(dto, binding))
                .thenReturn(true);
            cu.when(() -> ControllerUtils.validarDatosResultadoAnalisisAprobadoInput(dto, binding))
                .thenReturn(false);

            du.when(() -> getLotesDtosByCodigoInterno(anyList())).thenReturn(Collections.emptyList());
            du.when(() -> fromAnalisisEntities(anyList())).thenReturn(Collections.emptyList());

            String view = controller.procesarResultadoAnalisis(dto, binding, model, redirect);
            assertEquals("calidad/analisis/resultado-analisis", view);
        }
    }

    /* ------------ POST: errores de validación en cadena ------------ */

    @Test
    @DisplayName("Falla validarDatosMandatoriosResultadoAnalisisInput -> vuelve al form")
    void fallaDatosMandatorios() {
        when(loteService.findAllForResultadoAnalisis()).thenReturn(Collections.emptyList());
        when(analisisService.findAllEnCursoForLotesCuarentena()).thenReturn(Collections.emptyList());

        try (
            MockedStatic<ControllerUtils> cu = mockStatic(ControllerUtils.class);
            MockedStatic<DTOUtils> du = mockStatic(DTOUtils.class)) {

            cu.when(() -> ControllerUtils.validarDatosMandatoriosResultadoAnalisisInput(eq(dto), eq(binding)))
                .thenReturn(false);
            du.when(() -> getLotesDtosByCodigoInterno(anyList())).thenReturn(Collections.emptyList());
            du.when(() -> fromAnalisisEntities(anyList())).thenReturn(Collections.emptyList());

            String view = controller.procesarResultadoAnalisis(dto, binding, model, redirect);
            assertEquals("calidad/analisis/resultado-analisis", view);
        }
    }

    @Test
    @DisplayName("Falla validarExisteMuestreoParaAnalisis -> vuelve al form")
    void fallaExisteMuestreo() {
        when(loteService.findAllForResultadoAnalisis()).thenReturn(Collections.emptyList());
        when(analisisService.findAllEnCursoForLotesCuarentena()).thenReturn(Collections.emptyList());

        try (
            MockedStatic<ControllerUtils> cu = mockStatic(ControllerUtils.class);
            MockedStatic<DTOUtils> du = mockStatic(DTOUtils.class)) {

            ControllerUtils utils = mock(ControllerUtils.class);
            cu.when(ControllerUtils::getInstance).thenReturn(utils);

            cu.when(() -> ControllerUtils.validarDatosMandatoriosResultadoAnalisisInput(dto, binding))
                .thenReturn(true);
            cu.when(() -> ControllerUtils.validarDatosResultadoAnalisisAprobadoInput(dto, binding))
                .thenReturn(true);

            when(utils.populateLoteListByCodigoInterno(anyList(), nullable(String.class), eq(binding), eq(loteService)))
                .thenAnswer(inv -> {
                    ((List<Lote>)inv.getArgument(0)).add(new Lote());
                    return true;
                });

            cu.when(() -> ControllerUtils.validarExisteMuestreoParaAnalisis(eq(dto), anyList(), eq(binding)))
                .thenReturn(false);

            du.when(() -> getLotesDtosByCodigoInterno(anyList())).thenReturn(Collections.emptyList());
            du.when(() -> fromAnalisisEntities(anyList())).thenReturn(Collections.emptyList());

            String view = controller.procesarResultadoAnalisis(dto, binding, model, redirect);
            assertEquals("calidad/analisis/resultado-analisis", view);
        }
    }

    @Test
    @DisplayName("Falla validarFechaAnalisisPosteriorIngresoLote -> vuelve al form")
    void fallaFechaAnalisis() {
        when(loteService.findAllForResultadoAnalisis()).thenReturn(Collections.emptyList());
        when(analisisService.findAllEnCursoForLotesCuarentena()).thenReturn(Collections.emptyList());

        try (
            MockedStatic<ControllerUtils> cu = mockStatic(ControllerUtils.class);
            MockedStatic<DTOUtils> du = mockStatic(DTOUtils.class)) {

            ControllerUtils utils = mock(ControllerUtils.class);
            cu.when(ControllerUtils::getInstance).thenReturn(utils);

            cu.when(() -> ControllerUtils.validarDatosMandatoriosResultadoAnalisisInput(dto, binding))
                .thenReturn(true);
            cu.when(() -> ControllerUtils.validarDatosResultadoAnalisisAprobadoInput(dto, binding))
                .thenReturn(true);

            when(utils.populateLoteListByCodigoInterno(anyList(), nullable(String.class), eq(binding), eq(loteService)))
                .thenAnswer(inv -> {
                    ((List<Lote>)inv.getArgument(0)).add(new Lote());
                    return true;
                });

            cu.when(() -> ControllerUtils.validarExisteMuestreoParaAnalisis(eq(dto), anyList(), eq(binding)))
                .thenReturn(true);

            when(utils.validarFechaMovimientoPosteriorIngresoLote(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(true);
            when(utils.validarFechaAnalisisPosteriorIngresoLote(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(false);

            du.when(() -> getLotesDtosByCodigoInterno(anyList())).thenReturn(Collections.emptyList());
            du.when(() -> fromAnalisisEntities(anyList())).thenReturn(Collections.emptyList());

            String view = controller.procesarResultadoAnalisis(dto, binding, model, redirect);
            assertEquals("calidad/analisis/resultado-analisis", view);
        }
    }

    @Test
    @DisplayName("Falla validarFechaMovimientoPosteriorIngresoLote -> vuelve al form")
    void fallaFechaMovimiento() {
        when(loteService.findAllForResultadoAnalisis()).thenReturn(Collections.emptyList());
        when(analisisService.findAllEnCursoForLotesCuarentena()).thenReturn(Collections.emptyList());

        try (
            MockedStatic<ControllerUtils> cu = mockStatic(ControllerUtils.class);
            MockedStatic<DTOUtils> du = mockStatic(DTOUtils.class)) {

            ControllerUtils utils = mock(ControllerUtils.class);
            cu.when(ControllerUtils::getInstance).thenReturn(utils);

            cu.when(() -> ControllerUtils.validarDatosMandatoriosResultadoAnalisisInput(dto, binding))
                .thenReturn(true);
            cu.when(() -> ControllerUtils.validarDatosResultadoAnalisisAprobadoInput(dto, binding))
                .thenReturn(true);

            when(utils.populateLoteListByCodigoInterno(anyList(), nullable(String.class), eq(binding), eq(loteService)))
                .thenAnswer(inv -> {
                    ((List<Lote>)inv.getArgument(0)).add(new Lote());
                    return true;
                });

            cu.when(() -> ControllerUtils.validarExisteMuestreoParaAnalisis(eq(dto), anyList(), eq(binding)))
                .thenReturn(true);

            when(utils.validarFechaMovimientoPosteriorIngresoLote(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(false);

            du.when(() -> getLotesDtosByCodigoInterno(anyList())).thenReturn(Collections.emptyList());
            du.when(() -> fromAnalisisEntities(anyList())).thenReturn(Collections.emptyList());

            String view = controller.procesarResultadoAnalisis(dto, binding, model, redirect);
            assertEquals("calidad/analisis/resultado-analisis", view);
        }
    }

    @Test
    @DisplayName("Falla populateLoteListByCodigoInterno -> vuelve al form")
    void fallaPopulateLoteList() {
        when(loteService.findAllForResultadoAnalisis()).thenReturn(Collections.emptyList());
        when(analisisService.findAllEnCursoForLotesCuarentena()).thenReturn(Collections.emptyList());

        try (
            MockedStatic<ControllerUtils> cu = mockStatic(ControllerUtils.class);
            MockedStatic<DTOUtils> du = mockStatic(DTOUtils.class)) {

            ControllerUtils utils = mock(ControllerUtils.class);
            cu.when(ControllerUtils::getInstance).thenReturn(utils);

            cu.when(() -> ControllerUtils.validarDatosMandatoriosResultadoAnalisisInput(dto, binding))
                .thenReturn(true);
            cu.when(() -> ControllerUtils.validarDatosResultadoAnalisisAprobadoInput(dto, binding))
                .thenReturn(true);

            // Usar nullable(String.class) por si el código fuese null
            when(utils.populateLoteListByCodigoInterno(anyList(), nullable(String.class), eq(binding), eq(loteService)))
                .thenReturn(false);

            du.when(() -> getLotesDtosByCodigoInterno(anyList())).thenReturn(Collections.emptyList());
            du.when(() -> fromAnalisisEntities(anyList())).thenReturn(Collections.emptyList());

            String view = controller.procesarResultadoAnalisis(dto, binding, model, redirect);
            assertEquals("calidad/analisis/resultado-analisis", view);
        }
    }

    @Test
    @DisplayName("Falla validarValorTitulo -> vuelve al form")
    void fallaValorTitulo() {
        when(loteService.findAllForResultadoAnalisis()).thenReturn(Collections.emptyList());
        when(analisisService.findAllEnCursoForLotesCuarentena()).thenReturn(Collections.emptyList());

        try (
            MockedStatic<ControllerUtils> cu = mockStatic(ControllerUtils.class);
            MockedStatic<DTOUtils> du = mockStatic(DTOUtils.class)) {

            ControllerUtils utils = mock(ControllerUtils.class);
            cu.when(ControllerUtils::getInstance).thenReturn(utils);

            cu.when(() -> ControllerUtils.validarDatosMandatoriosResultadoAnalisisInput(dto, binding))
                .thenReturn(true);
            cu.when(() -> ControllerUtils.validarDatosResultadoAnalisisAprobadoInput(dto, binding))
                .thenReturn(true);

            when(utils.populateLoteListByCodigoInterno(anyList(), nullable(String.class), eq(binding), eq(loteService)))
                .thenAnswer(inv -> {
                    ((List<Lote>)inv.getArgument(0)).add(new Lote());
                    return true;
                });

            cu.when(() -> ControllerUtils.validarExisteMuestreoParaAnalisis(eq(dto), anyList(), eq(binding)))
                .thenReturn(true);

            when(utils.validarFechaMovimientoPosteriorIngresoLote(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(true);
            when(utils.validarFechaAnalisisPosteriorIngresoLote(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(true);
            cu.when(() -> ControllerUtils.validarContraFechasProveedor(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(true);

            when(utils.validarValorTitulo(eq(dto), anyList(), eq(binding))).thenReturn(false);

            du.when(() -> getLotesDtosByCodigoInterno(anyList())).thenReturn(Collections.emptyList());
            du.when(() -> fromAnalisisEntities(anyList())).thenReturn(Collections.emptyList());

            String view = controller.procesarResultadoAnalisis(dto, binding, model, redirect);
            assertEquals("calidad/analisis/resultado-analisis", view);
        }
    }

    @Test
    @DisplayName("BindingResult con errores -> vuelve al form e inicializa modelo")
    void procesar_ResultadoAnalisis_bindingErrors() {
        binding.reject("any", "err");
        when(loteService.findAllForResultadoAnalisis()).thenReturn(Collections.emptyList());
        when(analisisService.findAllEnCursoForLotesCuarentena()).thenReturn(Collections.emptyList());

        try (MockedStatic<DTOUtils> ms = mockStatic(DTOUtils.class)) {
            ms.when(() -> getLotesDtosByCodigoInterno(anyList())).thenReturn(Collections.emptyList());
            ms.when(() -> fromAnalisisEntities(anyList())).thenReturn(Collections.emptyList());

            String view = controller.procesarResultadoAnalisis(dto, binding, model, redirect);

            assertEquals("calidad/analisis/resultado-analisis", view);
            assertSame(dto, model.getAttribute("movimientoDTO"));
            assertNotNull(model.getAttribute("loteDTOs"));
            assertNotNull(model.getAttribute("analisisDTOs"));
        }
    }

    @Test
    @DisplayName("Todos OK con dictamen RECHAZADO")
    void procesar_ResultadoAnalisis_ok_RECHAZADO() {
        dto.setDictamenFinal(DictamenEnum.RECHAZADO);

        try (
            MockedStatic<ControllerUtils> cu = mockStatic(ControllerUtils.class);
            MockedStatic<DTOUtils> du = mockStatic(DTOUtils.class)) {

            ControllerUtils utils = mock(ControllerUtils.class);
            cu.when(ControllerUtils::getInstance).thenReturn(utils);

            cu.when(() -> ControllerUtils.validarDatosMandatoriosResultadoAnalisisInput(dto, binding)).thenReturn(true);
            cu.when(() -> ControllerUtils.validarDatosResultadoAnalisisAprobadoInput(dto, binding)).thenReturn(true);
            when(utils.populateLoteListByCodigoInterno(anyList(), nullable(String.class), eq(binding), eq(loteService)))
                .thenAnswer(inv -> {
                    ((List<Lote>)inv.getArgument(0)).add(new Lote());
                    return true;
                });
            cu.when(() -> ControllerUtils.validarExisteMuestreoParaAnalisis(eq(dto), anyList(), eq(binding)))
                .thenReturn(true);
            when(utils.validarFechaMovimientoPosteriorIngresoLote(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(true);
            when(utils.validarFechaAnalisisPosteriorIngresoLote(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(true);
            cu.when(() -> ControllerUtils.validarContraFechasProveedor(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(true);

            Lote persistido = new Lote();
            when(loteService.persistirResultadoAnalisis(dto)).thenReturn(persistido);
            du.when(() -> DTOUtils.fromLoteEntity(persistido)).thenReturn(new LoteDTO());

            String view = controller.procesarResultadoAnalisis(dto, binding, model, redirect);

            assertEquals("redirect:/calidad/analisis/resultado-analisis-ok", view);
            Map<String, ?> flash = redirect.getFlashAttributes();
            assertTrue(flash.containsKey("loteDTO"));
            assertNotNull(flash.get("loteDTO"));
            assertEquals(
                "Cambio de dictamen a RECHAZADO exitoso",
                flash.get("success"));
            assertTrue(flash.containsKey("success"));
            verify(utils, never()).validarValorTitulo(any(), anyList(), any());
        }
    }

    @Test
    @DisplayName("Todos OK pero merge null -> redirige con error")
    void procesar_ResultadoAnalisis_ok_mergeNull() {
        dto.setDictamenFinal(DictamenEnum.RECIBIDO);

        try (
            MockedStatic<ControllerUtils> cu = mockStatic(ControllerUtils.class);
            MockedStatic<DTOUtils> du = mockStatic(DTOUtils.class)) {

            ControllerUtils utils = mock(ControllerUtils.class);
            cu.when(ControllerUtils::getInstance).thenReturn(utils);

            cu.when(() -> ControllerUtils.validarDatosMandatoriosResultadoAnalisisInput(dto, binding)).thenReturn(true);
            cu.when(() -> ControllerUtils.validarDatosResultadoAnalisisAprobadoInput(dto, binding)).thenReturn(true);
            when(utils.populateLoteListByCodigoInterno(anyList(), nullable(String.class), eq(binding), eq(loteService)))
                .thenAnswer(inv -> {
                    ((List<Lote>)inv.getArgument(0)).add(new Lote());
                    return true;
                });
            cu.when(() -> ControllerUtils.validarExisteMuestreoParaAnalisis(eq(dto), anyList(), eq(binding)))
                .thenReturn(true);
            when(utils.validarFechaMovimientoPosteriorIngresoLote(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(true);
            when(utils.validarFechaAnalisisPosteriorIngresoLote(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(true);
            cu.when(() -> ControllerUtils.validarContraFechasProveedor(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(true);
            when(utils.validarValorTitulo(eq(dto), anyList(), eq(binding))).thenReturn(true);

            Lote persistido = new Lote();
            when(loteService.persistirResultadoAnalisis(dto)).thenReturn(persistido);
            du.when(() -> DTOUtils.fromLoteEntity(persistido)).thenReturn(null);

            String view = controller.procesarResultadoAnalisis(dto, binding, model, redirect);

            assertEquals("redirect:/calidad/analisis/resultado-analisis-ok", view);
            Map<String, ?> flash = redirect.getFlashAttributes();
            assertTrue(flash.containsKey("loteDTO"));
            assertNull(flash.get("loteDTO"));
            assertEquals("Hubo un error con el cambio de dictamen.", flash.get("error"));
            assertFalse(flash.containsKey("success"));
        }
    }

    /* ------------ POST: éxito ------------ */

    @Test
    @DisplayName("Todos OK -> redirige y setea success (merge OK)")
    void procesar_ResultadoAnalisis_ok_success() {
        dto.setDictamenFinal(DictamenEnum.APROBADO);

        try (
            MockedStatic<ControllerUtils> cu = mockStatic(ControllerUtils.class);
            MockedStatic<DTOUtils> du = mockStatic(DTOUtils.class)) {

            ControllerUtils utils = mock(ControllerUtils.class);
            cu.when(ControllerUtils::getInstance).thenReturn(utils);

            cu.when(() -> ControllerUtils.validarDatosMandatoriosResultadoAnalisisInput(dto, binding)).thenReturn(true);
            cu.when(() -> ControllerUtils.validarDatosResultadoAnalisisAprobadoInput(dto, binding)).thenReturn(true);
            when(utils.populateLoteListByCodigoInterno(anyList(), nullable(String.class), eq(binding), eq(loteService)))
                .thenAnswer(inv -> {
                    ((List<Lote>)inv.getArgument(0)).add(new Lote());
                    return true;
                });
            cu.when(() -> ControllerUtils.validarExisteMuestreoParaAnalisis(eq(dto), anyList(), eq(binding)))
                .thenReturn(true);
            when(utils.validarFechaMovimientoPosteriorIngresoLote(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(true);
            when(utils.validarFechaAnalisisPosteriorIngresoLote(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(true);
            cu.when(() -> ControllerUtils.validarContraFechasProveedor(eq(dto), any(Lote.class), eq(binding)))
                .thenReturn(true);
            when(utils.validarValorTitulo(eq(dto), anyList(), eq(binding))).thenReturn(true);

            Lote persistido = new Lote();
            LoteDTO merged = new LoteDTO();
            when(loteService.persistirResultadoAnalisis(dto)).thenReturn(persistido);
            du.when(() -> DTOUtils.fromLoteEntity(persistido)).thenReturn(merged);

            String view = controller.procesarResultadoAnalisis(dto, binding, model, redirect);

            assertEquals("redirect:/calidad/analisis/resultado-analisis-ok", view);
            assertNotNull(dto.getFechaYHoraCreacion());
            assertTrue(dto.getFechaYHoraCreacion().isBefore(LocalDateTime.now().plusSeconds(2)));

            verify(loteService).persistirResultadoAnalisis(dto);
            du.verify(() -> DTOUtils.fromLoteEntity(persistido));

            Map<String, ?> flash = redirect.getFlashAttributes();
            assertSame(merged, flash.get("loteDTO"));
            assertEquals("Cambio de dictamen a APROBADO exitoso", flash.get("success"));
            assertFalse(flash.containsKey("error"));
        }
    }

    @BeforeEach
    void setup() {
        openMocks(this);
        model = new ExtendedModelMap();
        redirect = new RedirectAttributesModelMap();
        dto = new MovimientoDTO();
        // IMPORTANTE: seteamos un código no nulo para evitar mismatch con anyString()
        dto.setCodigoInternoLote("X");
        binding = new BeanPropertyBindingResult(dto, "movimientoDTO");
    }

    @Test
    @DisplayName("GET /resultado-analisis -> inicializa modelo y retorna vista")
    void showResultadoAnalisisForm_ok() {
        List<Lote> entradaLotes = lotes(2);
        List<Analisis> entradaAnalisis = analisis(3);
        List<LoteDTO> salidaLotes = List.of(new LoteDTO(), new LoteDTO());
        List<AnalisisDTO> salidaAnalisis = List.of(new AnalisisDTO());

        when(loteService.findAllForResultadoAnalisis()).thenReturn(entradaLotes);
        when(analisisService.findAllEnCursoForLotesCuarentena()).thenReturn(entradaAnalisis);

        try (MockedStatic<DTOUtils> ms = mockStatic(DTOUtils.class)) {
            ms.when(() -> getLotesDtosByCodigoInterno(entradaLotes)).thenReturn(salidaLotes);
            ms.when(() -> fromAnalisisEntities(entradaAnalisis)).thenReturn(salidaAnalisis);

            String view = controller.showResultadoAnalisisForm(dto, model);

            assertEquals("calidad/analisis/resultado-analisis", view);
            assertSame(dto, model.getAttribute("movimientoDTO"));
            assertSame(salidaLotes, model.getAttribute("loteDTOs"));
            assertSame(salidaAnalisis, model.getAttribute("analisisDTOs"));

            Object resultados = model.getAttribute("resultados");
            assertNotNull(resultados);
            assertTrue(resultados instanceof List<?>);
            @SuppressWarnings("unchecked")
            List<DictamenEnum> res = (List<DictamenEnum>)resultados;
            assertEquals(2, res.size());
            assertTrue(res.contains(DictamenEnum.APROBADO));
            assertTrue(res.contains(DictamenEnum.RECHAZADO));

            assertNotNull(dto.getFechaMovimiento());

            verify(loteService).findAllForResultadoAnalisis();
            verify(analisisService).findAllEnCursoForLotesCuarentena();
            ms.verify(() -> getLotesDtosByCodigoInterno(entradaLotes));
            ms.verify(() -> fromAnalisisEntities(entradaAnalisis));
        }
    }

}

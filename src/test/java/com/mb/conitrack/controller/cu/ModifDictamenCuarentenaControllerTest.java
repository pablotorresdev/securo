package com.mb.conitrack.controller.cu;

import java.util.Collections;
import java.util.List;

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
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.ProductoService;
import com.mb.conitrack.service.ProveedorService;
import com.mb.conitrack.utils.ControllerUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class ModifDictamenCuarentenaControllerTest {

    @Spy
    @InjectMocks
    ModifDictamenCuarentenaController controller;

    @Mock
    LoteService loteService;

    @Mock
    AnalisisService analisisService;;

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

    @Test
    @DisplayName("dictamenCuarentena - error: mergeEntities devuelve null → flash 'error'")
    void dictamenCuarentena_error() {
        // given
        MovimientoDTO dto = new MovimientoDTO();
        Lote lote = new Lote();
        RedirectAttributes redirect = mock(RedirectAttributes.class);

        when(loteService.persistirDictamenCuarentena(eq(dto), eq(lote))).thenReturn(lote);

        try (MockedStatic<DTOUtils> mocked = mockStatic(DTOUtils.class)) {
            mocked.when(() -> DTOUtils.mergeEntities(lote)).thenReturn(null);

            // when
            controller.dictamenCuarentena(dto, lote, redirect);

            // then
            assertNotNull(dto.getFechaYHoraCreacion());
            verify(loteService).persistirDictamenCuarentena(dto, lote);
            mocked.verify(() -> DTOUtils.mergeEntities(lote));

            // flash attributes
            verify(redirect).addFlashAttribute(eq("loteDTO"), isNull());
            verify(redirect).addFlashAttribute(
                eq("error"),
                eq("Hubo un error al realizar el cambio de calidad a Cuarentena."));
            verifyNoMoreInteractions(redirect);
        }
    }

    @Test
    @DisplayName("dictamenCuarentena - éxito: mergeEntities devuelve dto → flash 'success'")
    void dictamenCuarentena_ok() {
        MovimientoDTO dto = new MovimientoDTO();
        final Lote lote = new Lote();
        RedirectAttributes redirect = mock(RedirectAttributes.class);
        LoteDTO merged = new LoteDTO();

        when(loteService.persistirDictamenCuarentena(eq(dto), eq(lote))).thenReturn(lote);

        try (MockedStatic<DTOUtils> mocked = mockStatic(DTOUtils.class)) {
            mocked.when(() -> DTOUtils.mergeEntities(lote)).thenReturn(merged);

            controller.dictamenCuarentena(dto, lote, redirect);

            assertNotNull(dto.getFechaYHoraCreacion());
            verify(loteService).persistirDictamenCuarentena(dto, lote);
            mocked.verify(() -> DTOUtils.mergeEntities(lote));

            verify(redirect).addFlashAttribute("loteDTO", merged);
            verify(redirect).addFlashAttribute("success", "Cambio de calidad a Cuarentena exitoso");
            verifyNoMoreInteractions(redirect);
        }
    }

    @Test
    @DisplayName("exitoDictamenCuarentena")
    void exitoDictamenCuarentena() {
        final String s = controller.exitoDictamenCuarentena(dto);
        assertEquals("calidad/dictamen/cuarentena-ok", s);
    }

    @Test
    void exitoMuestreo() {
    }

    @Test
    void exitoReanalisisProducto() {
    }

    @Test
    void exitoResultadoAnalisis() {
    }

    @Test
    @DisplayName("Falla 1er validador -> vuelve al form y llama initModel")
    void fallaPrimerValidador() {
        MovimientoDTO dto = new MovimientoDTO();
        BindingResult br = new BeanPropertyBindingResult(dto, "movimientoDTO");
        Model model = new ExtendedModelMap();
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        doNothing().when(controller).initModelDictamencuarentena(any(), any());

        try (MockedStatic<ControllerUtils> mocked = mockStatic(ControllerUtils.class)) {
            ControllerUtils utilsMock = mock(ControllerUtils.class);
            mocked.when(ControllerUtils::getInstance).thenReturn(utilsMock);

            when(utilsMock.validarNroAnalisisNotNull(dto, br)).thenReturn(false);

            String view = controller.procesarDictamenCuarentena(dto, br, model, redirect);

            assertEquals("calidad/dictamen/cuarentena", view);
            verify(utilsMock).validarNroAnalisisNotNull(dto, br);
            verify(controller).initModelDictamencuarentena(dto, model);
            verify(controller, never()).dictamenCuarentena(any(), any(), any());
            assertSame(dto, model.getAttribute("movimientoDTO"));
        }
    }

    @Test
    @DisplayName("Pasa 1°, falla 2° -> vuelve al form")
    void fallaSegundoValidador() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoInternoLote("X");
        BindingResult br = new BeanPropertyBindingResult(dto, "movimientoDTO");
        Model model = new ExtendedModelMap();
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        doNothing().when(controller).initModelDictamencuarentena(any(), any());

        try (MockedStatic<ControllerUtils> mocked = mockStatic(ControllerUtils.class)) {
            ControllerUtils utilsMock = mock(ControllerUtils.class);
            mocked.when(ControllerUtils::getInstance).thenReturn(utilsMock);

            when(utilsMock.validarNroAnalisisNotNull(dto, br)).thenReturn(true);
            when(utilsMock.validarNroAnalisisUnico(eq(dto), eq(br), eq(analisisService))).thenReturn(false);

            String view = controller.procesarDictamenCuarentena(dto, br, model, redirect);

            assertEquals("calidad/dictamen/cuarentena", view);
            verify(utilsMock).validarNroAnalisisNotNull(dto, br);
            verify(utilsMock).validarNroAnalisisUnico(eq(dto), eq(br), eq(analisisService));
            verify(controller).initModelDictamencuarentena(dto, model);
            verify(controller, never()).dictamenCuarentena(any(), any(), any());
            assertSame(dto, model.getAttribute("movimientoDTO"));
        }
    }

    @Test
    @DisplayName("Pasan 1° y 2°, falla 3° -> vuelve al form")
    void fallaTercerValidador() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoInternoLote("X");
        BindingResult br = new BeanPropertyBindingResult(dto, "movimientoDTO");
        Model model = new ExtendedModelMap();
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        doNothing().when(controller).initModelDictamencuarentena(any(), any());

        try (MockedStatic<ControllerUtils> mocked = mockStatic(ControllerUtils.class)) {
            ControllerUtils utilsMock = mock(ControllerUtils.class);
            mocked.when(ControllerUtils::getInstance).thenReturn(utilsMock);

            when(utilsMock.validarNroAnalisisNotNull(dto, br)).thenReturn(true);
            when(utilsMock.validarNroAnalisisUnico(eq(dto), eq(br), eq(analisisService))).thenReturn(true);
            when(utilsMock.getLoteByCodigoInterno(eq("X"), eq(br), eq(loteService)))
                .thenAnswer(inv -> {
                    return null;                   // el estático debe devolver boolean
                });

            String view = controller.procesarDictamenCuarentena(dto, br, model, redirect);

            assertEquals("calidad/dictamen/cuarentena", view);
            verify(utilsMock).validarNroAnalisisNotNull(dto, br);
            verify(utilsMock).validarNroAnalisisUnico(eq(dto), eq(br), eq(analisisService));
            verify(utilsMock).getLoteByCodigoInterno(eq("X"), eq(br), eq(loteService));
            verify(controller).initModelDictamencuarentena(dto, model);
            verify(controller, never()).dictamenCuarentena(any(), any(), any());
            assertSame(dto, model.getAttribute("movimientoDTO"));
        }
    }

    @Test
    @DisplayName("Pasan 1°, 2°, 3° falla 4° -> vuelve al form")
    void fallaCuartoValidador() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoInternoLote("X");
        BindingResult br = new BeanPropertyBindingResult(dto, "movimientoDTO");
        Model model = new ExtendedModelMap();
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        doNothing().when(controller).initModelDictamencuarentena(any(), any());

        try (MockedStatic<ControllerUtils> mocked = mockStatic(ControllerUtils.class)) {

            ControllerUtils utilsMock = mock(ControllerUtils.class);
            mocked.when(ControllerUtils::getInstance).thenReturn(utilsMock);

            when(utilsMock.validarNroAnalisisNotNull(dto, br)).thenReturn(true);
            when(utilsMock.validarNroAnalisisUnico(eq(dto), eq(br), eq(analisisService))).thenReturn(true);
            when(utilsMock.getLoteByCodigoInterno(eq("X"), eq(br), eq(loteService)))
                .thenAnswer(inv -> {
                    return new Lote();                   // el estático debe devolver boolean
                });
            // 3° falla
            when(utilsMock.validarFechaMovimientoPosteriorIngresoLote(eq(dto), any(Lote.class), eq(br)))
                .thenReturn(false);

            String view = controller.procesarDictamenCuarentena(dto, br, model, redirect);

            assertEquals("calidad/dictamen/cuarentena", view);
            verify(utilsMock).validarNroAnalisisNotNull(dto, br);
            verify(utilsMock).getLoteByCodigoInterno(eq("X"), eq(br), eq(loteService));
            verify(utilsMock).validarNroAnalisisUnico(eq(dto), eq(br), eq(analisisService));
            verify(utilsMock).validarFechaMovimientoPosteriorIngresoLote(eq(dto), any(Lote.class), eq(br));
            verify(controller).initModelDictamencuarentena(dto, model);
            verify(controller, never()).dictamenCuarentena(any(), any(), any());
            assertSame(dto, model.getAttribute("movimientoDTO"));
        }
    }

    @Test
    @DisplayName("Pasan 1°, 2°, 3°, 4° falla 5° -> vuelve al form")
    void fallaQuintoValidador() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoInternoLote("X");
        BindingResult br = new BeanPropertyBindingResult(dto, "movimientoDTO");
        Model model = new ExtendedModelMap();
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        doNothing().when(controller).initModelDictamencuarentena(any(), any());

        try (MockedStatic<ControllerUtils> mocked = mockStatic(ControllerUtils.class)) {

            ControllerUtils utilsMock = mock(ControllerUtils.class);
            mocked.when(ControllerUtils::getInstance).thenReturn(utilsMock);
            when(utilsMock.validarNroAnalisisNotNull(dto, br)).thenReturn(true);
            when(utilsMock.validarNroAnalisisUnico(eq(dto), eq(br), eq(analisisService))).thenReturn(true);
            when(utilsMock.getLoteByCodigoInterno(eq("X"), eq(br), eq(loteService)))
                .thenAnswer(inv -> {
                    return new Lote();                   // el estático debe devolver boolean
                });
            // 3° falla
            when(utilsMock.validarFechaMovimientoPosteriorIngresoLote(eq(dto), any(Lote.class), eq(br)))
                .thenReturn(true);
            when(utilsMock.validarFechaAnalisisPosteriorIngresoLote(eq(dto), any(Lote.class), eq(br)))
                .thenReturn(false);

            String view = controller.procesarDictamenCuarentena(dto, br, model, redirect);

            assertEquals("calidad/dictamen/cuarentena", view);
            verify(utilsMock).validarNroAnalisisNotNull(dto, br);
            verify(utilsMock).validarNroAnalisisUnico(eq(dto), eq(br), eq(analisisService));
            verify(utilsMock).getLoteByCodigoInterno(eq("X"), eq(br), eq(loteService));
            verify(utilsMock).validarFechaMovimientoPosteriorIngresoLote(eq(dto), any(Lote.class), eq(br));
            verify(utilsMock).validarFechaAnalisisPosteriorIngresoLote(eq(dto), any(Lote.class), eq(br));
            verify(controller).initModelDictamencuarentena(dto, model);
            verify(controller, never()).dictamenCuarentena(any(), any(), any());
            assertSame(dto, model.getAttribute("movimientoDTO"));
        }
    }

    @Test
    void procesarDictamenCuarentena() {
    }

    @Test
    void procesarMuestreoBulto() {
    }

    @Test
    void procesarReanalisisProducto() {
    }

    @Test
    void procesarResultadoAnalisis() {
    }

    @BeforeEach
    void setUp() {
        openMocks(this);   // inicializa @Mock y @InjectMocks
        dto = new LoteDTO();
        model = new ExtendedModelMap();
    }

    @Test
    void showDictamenCuarentenaForm() {
    }

    @Test
    @DisplayName("GET /cuarentena -> convierte lotes a DTOs y los agrega al model (lista con datos)")
    void showDictamenCuarentenaForm_listaConDatos() {
        // given
        MovimientoDTO dto = new MovimientoDTO();
        Model model = new ExtendedModelMap();

        Lote l1 = new Lote();
        Lote l2 = new Lote();
        given(loteService.findAllForCuarentena()).willReturn(List.of(l1, l2));

        List<LoteDTO> fakeDtos = List.of(new LoteDTO(), new LoteDTO());

        // mock estático de DTOUtils.getLotesDtosByCodigoInterno(...)
        try (MockedStatic<DTOUtils> mocked = mockStatic(DTOUtils.class)) {
            mocked.when(() -> DTOUtils.getLotesDtosByCodigoInterno(anyList()))
                .thenReturn(fakeDtos);

            // when
            String view = controller.showDictamenCuarentenaForm(dto, model);

            // then
            assertEquals("calidad/dictamen/cuarentena", view);
            assertSame(fakeDtos, model.getAttribute("lotesForCuarentena"));
            assertSame(dto, model.getAttribute("movimientoDTO"));

            // Verificamos que se haya llamado al método estático con alguna lista
            mocked.verify(() -> DTOUtils.getLotesDtosByCodigoInterno(anyList()));
        }
    }

    @Test
    @DisplayName("GET /cuarentena -> retorna vista y pone DTO y lista vacía en el model")
    void showDictamenCuarentenaForm_listaVacia() {
        // given
        MovimientoDTO dto = new MovimientoDTO();
        given(loteService.findAllForCuarentena()).willReturn(Collections.emptyList());

        // when
        String view = controller.showDictamenCuarentenaForm(dto, model);

        // then
        assertEquals("calidad/dictamen/cuarentena", view);

        // se llama al servicio
        verify(loteService).findAllForCuarentena();

        // model con atributos esperados
        Object lotesAttr = model.getAttribute("lotesForCuarentena");
        assertNotNull(lotesAttr);
        assertTrue(lotesAttr instanceof List<?>);
        assertTrue(((List<?>)lotesAttr).isEmpty());

        assertSame(dto, model.getAttribute("movimientoDTO"));
    }

    @Test
    void showMuestreoBultoForm() {
    }

    @Test
    void showReanalisisProductoForm() {
    }

    @Test
    void showResultadoAnalisisForm() {
    }

    @Test
    @DisplayName("Todos OK -> redirige y llama dictamenCuarentena")
    void todoOk() {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setCodigoInternoLote("X");
        BindingResult br = new BeanPropertyBindingResult(dto, "movimientoDTO");
        Model model = new ExtendedModelMap();
        RedirectAttributes redirect = new RedirectAttributesModelMap();

        doNothing().when(controller).dictamenCuarentena(eq(dto), any(Lote.class), eq(redirect));

        try (MockedStatic<ControllerUtils> mocked = mockStatic(ControllerUtils.class)) {
            ControllerUtils utilsMock = mock(ControllerUtils.class);
            mocked.when(ControllerUtils::getInstance).thenReturn(utilsMock);
            when(utilsMock.validarNroAnalisisNotNull(dto, br)).thenReturn(true);
            when(utilsMock.validarNroAnalisisUnico(eq(dto), eq(br), eq(analisisService))).thenReturn(true);
            when(utilsMock.getLoteByCodigoInterno(eq("X"), eq(br), eq(loteService)))
                .thenAnswer(inv -> {
                    return new Lote();                   // el estático debe devolver boolean
                });
            when(utilsMock.validarFechaMovimientoPosteriorIngresoLote(eq(dto), any(Lote.class), eq(br)))
                .thenReturn(true);
            when(utilsMock.validarFechaAnalisisPosteriorIngresoLote(eq(dto), any(Lote.class), eq(br)))
                .thenReturn(true);

            String view = controller.procesarDictamenCuarentena(dto, br, model, redirect);

            assertEquals("redirect:/calidad/dictamen/cuarentena-ok", view);
            verify(utilsMock).validarNroAnalisisNotNull(dto, br);
            verify(utilsMock).validarNroAnalisisUnico(eq(dto), eq(br), eq(analisisService));
            verify(utilsMock).getLoteByCodigoInterno(eq("X"), eq(br), eq(loteService));
            verify(utilsMock).validarFechaMovimientoPosteriorIngresoLote(eq(dto), any(Lote.class), eq(br));
            verify(utilsMock).validarFechaAnalisisPosteriorIngresoLote(eq(dto), any(Lote.class), eq(br));
            verify(controller, never()).initModelDictamencuarentena(any(), any());
            verify(controller).dictamenCuarentena(eq(dto), any(Lote.class), eq(redirect));
        }
    }

}
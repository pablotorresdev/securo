package com.mb.conitrack.controller.cu;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.utils.ControllerUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;


@ExtendWith(MockitoExtension.class)
class BajaMuestreoBultoControllerTest {

    @Spy
    @InjectMocks
    BajaMuestreoBultoController controller;

    @Mock
    LoteService loteService;

    Model model;
    RedirectAttributes redirect;
    MovimientoDTO dto;
    BindingResult binding;

    @BeforeEach
    void setup() {
        openMocks(this);   // inicializa @Mock y @InjectMocks
        model = new ExtendedModelMap();
        redirect = new RedirectAttributesModelMap();
        dto = new MovimientoDTO();
        dto.setCodigoInternoLote("COD-123");
        dto.setNroBulto("2");
        dto.setFechaMovimiento(LocalDate.now());
        binding = new BeanPropertyBindingResult(dto, "movimientoDTO");
    }

    /* ------------------ helpers ------------------ */

    private Lote loteConBultos(int cantBultos) {
        Lote lote = new Lote();
        List<Bulto> bultos = new ArrayList<>();
        for (int i = 1; i <= cantBultos; i++) {
            Bulto b = new Bulto();
            b.setNroBulto(i);
            b.setLote(lote);
            bultos.add(b);
        }
        lote.setBultos(bultos);
        lote.setFechaIngreso(LocalDate.now().minusDays(10));
        return lote;
    }

    /* ------------------ cancelar / ok ------------------ */

    @Test
    @DisplayName("GET /cancelar -> redirect:/")
    void cancelar() {
        assertEquals("redirect:/", controller.cancelar());
    }

    @Test
    @DisplayName("GET /muestreo-bulto: arma modelo y retorna vista del form")
    void showMuestreoBultoForm_ok() {
        List<Lote> entrada = List.of(new Lote(), new Lote());
        List<LoteDTO> salidaDtos = List.of(new LoteDTO());

        when(loteService.findAllForMuestreo()).thenReturn(entrada);

        try (MockedStatic<DTOUtils> mocked = mockStatic(DTOUtils.class)) {
            mocked.when(() -> DTOUtils.fromLoteEntities(entrada)).thenReturn(salidaDtos);

            String view = controller.showMuestreoBultoForm(dto, model);

            assertEquals("calidad/baja/muestreo-bulto", view);
            assertSame(dto, model.getAttribute("movimientoDTO"));
            assertSame(salidaDtos, model.getAttribute("lotesMuestreables"));
            verify(loteService).findAllForMuestreo();
            mocked.verify(() -> DTOUtils.fromLoteEntities(entrada));
        }
    }

    @Test
    @DisplayName("GET /muestreo-bulto-ok -> vista de éxito")
    void exitoMuestreo() {
        assertEquals("calidad/baja/muestreo-bulto-ok", controller.exitoMuestreo(new LoteDTO()));
    }

    /* ------------------ POST /muestreo-bulto (errores) ------------------ */

    @Test
    @DisplayName("Falla: validarNroAnalisisNotNull=false -> vuelve al form, no llama servicio")
    void fallaPrimerValidador() {
        try (MockedStatic<ControllerUtils> ms = mockStatic(ControllerUtils.class)) {
            ControllerUtils utils = mock(ControllerUtils.class);
            ms.when(ControllerUtils::getInstance).thenReturn(utils);
            when(utils.validarNroAnalisisNotNull(dto, binding)).thenReturn(false);

            // 👉 Evita que se ejecute la lógica interna que usa loteService
            doNothing().when(controller).initModelMuestreoBulto(any(), any());

            String view = controller.procesarMuestreoBulto(dto, binding, model, redirect);

            assertEquals("calidad/baja/muestreo-bulto", view);
            assertSame(dto, model.getAttribute("movimientoDTO"));
            verify(utils).validarNroAnalisisNotNull(dto, binding);
            verify(controller).initModelMuestreoBulto(dto, model);
            verifyNoInteractions(loteService); // ahora sí, válido
        }
    }

    @Test
    @DisplayName("Falla: no encuentra Lote -> vuelve al form, no sigue validaciones")
    void noEncuentraLote() {
        try (MockedStatic<ControllerUtils> ms = mockStatic(ControllerUtils.class)) {
            ControllerUtils utils = mock(ControllerUtils.class);
            ms.when(ControllerUtils::getInstance).thenReturn(utils);

            when(utils.validarNroAnalisisNotNull(dto, binding)).thenReturn(true);
            when(loteService.findLoteByCodigoInterno("COD-123")).thenReturn(Optional.empty());

            String view = controller.procesarMuestreoBulto(dto, binding, model, redirect);

            assertEquals("calidad/baja/muestreo-bulto", view);
            assertSame(dto, model.getAttribute("movimientoDTO"));

            verify(utils).validarNroAnalisisNotNull(dto, binding);
            verify(loteService).findLoteByCodigoInterno("COD-123");
            verify(utils, never()).validarFechaMovimientoPosteriorIngresoLote(any(), any(), any());
            verify(utils, never()).validarFechaAnalisisPosteriorIngresoLote(any(), any(), any());
            verify(utils, never()).validarCantidadesMovimiento(any(), any(Bulto.class), any());
        }
    }

    @Test
    @DisplayName("Falla: fechaMovimiento anterior al ingreso -> vuelve al form")
    void fechaMovimientoInvalida() {
        Lote lote = loteConBultos(2);

        try (MockedStatic<ControllerUtils> ms = mockStatic(ControllerUtils.class)) {
            ControllerUtils utils = mock(ControllerUtils.class);
            ms.when(ControllerUtils::getInstance).thenReturn(utils);

            when(utils.validarNroAnalisisNotNull(dto, binding)).thenReturn(true);
            when(loteService.findLoteByCodigoInterno("COD-123")).thenReturn(Optional.of(lote));
            when(utils.validarFechaMovimientoPosteriorIngresoLote(dto, lote, binding)).thenReturn(false);

            String view = controller.procesarMuestreoBulto(dto, binding, model, redirect);

            assertEquals("calidad/baja/muestreo-bulto", view);
            assertSame(dto, model.getAttribute("movimientoDTO"));

            verify(utils).validarNroAnalisisNotNull(dto, binding);
            verify(utils).validarFechaMovimientoPosteriorIngresoLote(dto, lote, binding);
            verify(utils, never()).validarFechaAnalisisPosteriorIngresoLote(any(), any(), any());
            verify(utils, never()).validarCantidadesMovimiento(any(), any(Bulto.class), any());
            verify(loteService, never()).bajaMuestreo(any(), any());
        }
    }

    @Test
    @DisplayName("Falla: fecha de análisis anterior al ingreso -> vuelve al form")
    void fechaAnalisisInvalida() {
        Lote lote = loteConBultos(2);

        try (MockedStatic<ControllerUtils> ms = mockStatic(ControllerUtils.class)) {
            ControllerUtils utils = mock(ControllerUtils.class);
            ms.when(ControllerUtils::getInstance).thenReturn(utils);

            when(utils.validarNroAnalisisNotNull(dto, binding)).thenReturn(true);
            when(loteService.findLoteByCodigoInterno("COD-123")).thenReturn(Optional.of(lote));
            when(utils.validarFechaMovimientoPosteriorIngresoLote(dto, lote, binding)).thenReturn(true);
            when(utils.validarFechaAnalisisPosteriorIngresoLote(dto, lote, binding)).thenReturn(false);

            String view = controller.procesarMuestreoBulto(dto, binding, model, redirect);

            assertEquals("calidad/baja/muestreo-bulto", view);
            verify(utils).validarFechaAnalisisPosteriorIngresoLote(dto, lote, binding);
            verify(utils, never()).validarCantidadesMovimiento(any(), any(Bulto.class), any());
            verify(loteService, never()).bajaMuestreo(any(), any());
        }
    }

    @Test
    @DisplayName("Falla: nroBulto inexistente en el lote -> vuelve al form")
    void bultoInexistente() {
        Lote lote = loteConBultos(1); // dto pide "2"

        try (MockedStatic<ControllerUtils> ms = mockStatic(ControllerUtils.class)) {
            ControllerUtils utils = mock(ControllerUtils.class);
            ms.when(ControllerUtils::getInstance).thenReturn(utils);

            when(utils.validarNroAnalisisNotNull(dto, binding)).thenReturn(true);
            when(loteService.findLoteByCodigoInterno("COD-123")).thenReturn(Optional.of(lote));
            when(utils.validarFechaMovimientoPosteriorIngresoLote(dto, lote, binding)).thenReturn(true);
            when(utils.validarFechaAnalisisPosteriorIngresoLote(dto, lote, binding)).thenReturn(true);

            String view = controller.procesarMuestreoBulto(dto, binding, model, redirect);

            assertEquals("calidad/baja/muestreo-bulto", view);
            verify(utils, never()).validarCantidadesMovimiento(any(), any(Bulto.class), any());
            verify(loteService, never()).bajaMuestreo(any(), any());
        }
    }

    @Test
    @DisplayName("Falla: validarCantidadesMovimiento=false -> vuelve al form")
    void cantidadesInvalidas() {
        Lote lote = loteConBultos(2);
        Bulto esperado = lote.getBultos().get(1);

        try (MockedStatic<ControllerUtils> ms = mockStatic(ControllerUtils.class)) {
            ControllerUtils utils = mock(ControllerUtils.class);
            ms.when(ControllerUtils::getInstance).thenReturn(utils);

            when(utils.validarNroAnalisisNotNull(dto, binding)).thenReturn(true);
            when(loteService.findLoteByCodigoInterno("COD-123")).thenReturn(Optional.of(lote));
            when(utils.validarFechaMovimientoPosteriorIngresoLote(dto, lote, binding)).thenReturn(true);
            when(utils.validarFechaAnalisisPosteriorIngresoLote(dto, lote, binding)).thenReturn(true);
            when(utils.validarCantidadesMovimiento(dto, esperado, binding)).thenReturn(false);

            String view = controller.procesarMuestreoBulto(dto, binding, model, redirect);

            assertEquals("calidad/baja/muestreo-bulto", view);
            verify(utils).validarCantidadesMovimiento(dto, esperado, binding);
            verify(loteService, never()).bajaMuestreo(any(), any());
        }
    }

    /* ------------------ POST /muestreo-bulto (éxito) ------------------ */

    @Test
    @DisplayName("OK: procesa muestreo, setea fecha y redirige con success")
    void muestreo_ok() {
        Lote lote = loteConBultos(2);
        Bulto bulto = lote.getBultos().get(1);
        Lote persistido = new Lote();
        LoteDTO resultDTO = new LoteDTO();

        try (MockedStatic<ControllerUtils> ms = mockStatic(ControllerUtils.class);
            MockedStatic<DTOUtils> msDto = mockStatic(DTOUtils.class)) {

            ControllerUtils utils = mock(ControllerUtils.class);
            ms.when(ControllerUtils::getInstance).thenReturn(utils);

            when(utils.validarNroAnalisisNotNull(dto, binding)).thenReturn(true);
            when(loteService.findLoteByCodigoInterno("COD-123")).thenReturn(Optional.of(lote));
            when(utils.validarFechaMovimientoPosteriorIngresoLote(dto, lote, binding)).thenReturn(true);
            when(utils.validarFechaAnalisisPosteriorIngresoLote(dto, lote, binding)).thenReturn(true);
            when(utils.validarCantidadesMovimiento(dto, bulto, binding)).thenReturn(true);

            when(loteService.bajaMuestreo(dto, bulto)).thenReturn(persistido);
            msDto.when(() -> DTOUtils.mergeLoteEntities(List.of(persistido))).thenReturn(resultDTO);

            String view = controller.procesarMuestreoBulto(dto, binding, model, redirect);

            assertEquals("redirect:/calidad/baja/muestreo-bulto-ok", view);
            // fecha de creación seteada en muestreoBulto()
            assertNotNull(dto.getFechaYHoraCreacion());
            assertTrue(dto.getFechaYHoraCreacion().isBefore(LocalDateTime.now().plusSeconds(2)));

            verify(loteService).bajaMuestreo(dto, bulto);
            msDto.verify(() -> DTOUtils.mergeLoteEntities(List.of(persistido)));

            Map<String, ?> flash = redirect.getFlashAttributes();
            assertSame(resultDTO, flash.get("loteDTO"));
            assertTrue(flash.containsKey("trazasMuestreo")); // puede ser null o lista, pero se agrega
            assertEquals("Muestreo registrado correctamente.", flash.get("success"));
            assertFalse(flash.containsKey("error"));
        }
    }

    @Test
    @DisplayName("OK pero merge devuelve null -> redirige con error")
    void muestreo_mergeNull() {
        Lote lote = loteConBultos(2);
        Bulto bulto = lote.getBultos().get(1);
        Lote persistido = new Lote();

        try (MockedStatic<ControllerUtils> ms = mockStatic(ControllerUtils.class);
            MockedStatic<DTOUtils> msDto = mockStatic(DTOUtils.class)) {

            ControllerUtils utils = mock(ControllerUtils.class);
            ms.when(ControllerUtils::getInstance).thenReturn(utils);

            when(utils.validarNroAnalisisNotNull(dto, binding)).thenReturn(true);
            when(loteService.findLoteByCodigoInterno("COD-123")).thenReturn(Optional.of(lote));
            when(utils.validarFechaMovimientoPosteriorIngresoLote(dto, lote, binding)).thenReturn(true);
            when(utils.validarFechaAnalisisPosteriorIngresoLote(dto, lote, binding)).thenReturn(true);
            when(utils.validarCantidadesMovimiento(dto, bulto, binding)).thenReturn(true);

            when(loteService.bajaMuestreo(dto, bulto)).thenReturn(persistido);
            msDto.when(() -> DTOUtils.mergeLoteEntities(List.of(persistido))).thenReturn(null);

            String view = controller.procesarMuestreoBulto(dto, binding, model, redirect);

            assertEquals("redirect:/calidad/baja/muestreo-bulto-ok", view);

            Map<String, ?> flash = redirect.getFlashAttributes();
            assertTrue(flash.containsKey("loteDTO"));
            assertNull(flash.get("loteDTO"));
            assertEquals("Hubo un error persistiendo el muestreo.", flash.get("error"));
            assertFalse(flash.containsKey("success"));
        }
    }

}
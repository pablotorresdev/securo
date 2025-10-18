package com.mb.conitrack.controller.cu;

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

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.service.BultoService;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.cu.BajaMuestreoBultoService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BajaMuestreoBultoControllerTest {

    @Spy
    @InjectMocks
    BajaMuestreoBultoController controller;

    @Mock
    BajaMuestreoBultoService muestreoBultoService;

    @Mock
    LoteService loteService;

    @Mock
    BultoService bultoService;

    Model model;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
    }

    // ------------------ GET /cancelar ------------------
    @Test
    @DisplayName("cancel -> redirige a /")
    void testCancel() {
        assertEquals("redirect:/", controller.cancelar());
    }

    // ------------------ GET /muestreo-trazable-ok ------------------
    @Test
    @DisplayName("exitoMuestreo -> retorna vista OK")
    void testExitoMuestreoTrazable() {
        assertEquals("calidad/baja/muestreo-trazable-ok", controller.exitoMuestreoTrazable(new LoteDTO()));
    }

    @Test
    @DisplayName("initModelMuestreoBulto -> setea atributos en el model")
    void testInitModelMuestreoTrazable() {
        MovimientoDTO mov = new MovimientoDTO();
        List<LoteDTO> lista = List.of(new LoteDTO());
        when(loteService.findAllForMuestreoTrazableDTOs()).thenReturn(lista);

        controller.initModelMuestreoTrazable(mov, model);

        assertSame(lista, model.getAttribute("loteMuestreoDTOs"));
        assertSame(mov, model.getAttribute("movimientoDTO"));
    }

    // ------------------ POST /muestreo-trazable (handler) ------------------
    @Test
    @DisplayName("GET /muestreo-trazable -> llena modelo y retorna vista")
    void testShowMuestreoTrazableForm() {
        MovimientoDTO mov = new MovimientoDTO();
        List<LoteDTO> lista = List.of(new LoteDTO(), new LoteDTO());
        when(loteService.findAllForMuestreoTrazableDTOs()).thenReturn(lista);

        String view = controller.showMuestreoTrazableForm(mov, model);

        assertEquals("calidad/baja/muestreo-trazable", view);
        assertSame(mov, model.getAttribute("movimientoDTO"));
        assertSame(lista, model.getAttribute("loteMuestreoDTOs"));
    }

}

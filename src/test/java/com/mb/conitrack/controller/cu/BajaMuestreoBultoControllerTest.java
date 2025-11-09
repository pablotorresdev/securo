package com.mb.conitrack.controller.cu;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.cu.BajaMuestreoBultoService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests para BajaMuestreoBultoController - 100% Coverage
 * CU3: Muestreo Trazable y Muestreo Multi Bulto
 */
@ExtendWith(MockitoExtension.class)
class BajaMuestreoBultoControllerTest {

    @Spy
    @InjectMocks
    BajaMuestreoBultoController controller;

    @Mock
    BajaMuestreoBultoService muestreoBultoService;

    @Mock
    LoteService loteService;

    Model model;
    RedirectAttributes redirect;
    MovimientoDTO movDto;
    LoteDTO loteDTO;
    BindingResult bindingMovimiento;
    BindingResult bindingLote;

    @BeforeEach
    void setUp() {
        model = new ExtendedModelMap();
        redirect = new RedirectAttributesModelMap();
        movDto = new MovimientoDTO();
        loteDTO = new LoteDTO();
        bindingMovimiento = new BeanPropertyBindingResult(movDto, "movimientoDTO");
        bindingLote = new BeanPropertyBindingResult(loteDTO, "loteDTO");
    }

    // -------------------- GET /cancelar --------------------

    @Test
    void testCancelar() {
        assertEquals("redirect:/", controller.cancelar());
    }

    // -------------------- GET /muestreo-trazable --------------------

    @Test
    void testShowMuestreoTrazableForm() {
        // given
        List<LoteDTO> lista = List.of(new LoteDTO(), new LoteDTO());
        when(loteService.findAllForMuestreoTrazableDTOs()).thenReturn(lista);

        // when
        String view = controller.showMuestreoTrazableForm(movDto, model);

        // then
        assertEquals("calidad/baja/muestreo-trazable", view);
        assertSame(movDto, model.getAttribute("movimientoDTO"));
        assertSame(lista, model.getAttribute("loteMuestreoDTOs"));
    }

    // -------------------- POST /muestreo-trazable --------------------

    @Test
    void testMuestreoTrazable_ConValidacionExitosa() {
        // given
        movDto.setCodigoLote("LOTE-001");
        movDto.setNroBulto("1");
        movDto.setCantidad(BigDecimal.TEN);

        LoteDTO resultDTO = new LoteDTO();
        resultDTO.setCodigoLote("LOTE-001");

        when(muestreoBultoService.validarMuestreoTrazableInput(movDto, bindingMovimiento)).thenReturn(true);
        when(muestreoBultoService.bajaMuestreoTrazable(any(MovimientoDTO.class))).thenReturn(resultDTO);

        // when
        String view = controller.muestreoTrazable(movDto, bindingMovimiento, model, redirect);

        // then
        assertEquals("redirect:/calidad/baja/muestreo-trazable-ok", view);
        assertNotNull(redirect.getFlashAttributes().get("loteDTO"));
        assertEquals("Muestreo registrado correctamente.", redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testMuestreoTrazable_ConValidacionFallida() {
        // given
        List<LoteDTO> lista = List.of(new LoteDTO());
        when(muestreoBultoService.validarMuestreoTrazableInput(movDto, bindingMovimiento)).thenReturn(false);
        when(loteService.findAllForMuestreoTrazableDTOs()).thenReturn(lista);

        // when
        String view = controller.muestreoTrazable(movDto, bindingMovimiento, model, redirect);

        // then
        assertEquals("calidad/baja/muestreo-trazable", view);
        assertSame(lista, model.getAttribute("loteMuestreoDTOs"));
    }

    @Test
    void testMuestreoTrazable_ConResultadoNull() {
        // given
        when(muestreoBultoService.validarMuestreoTrazableInput(movDto, bindingMovimiento)).thenReturn(true);
        when(muestreoBultoService.bajaMuestreoTrazable(any(MovimientoDTO.class))).thenReturn(null);

        // when
        String view = controller.muestreoTrazable(movDto, bindingMovimiento, model, redirect);

        // then
        assertEquals("redirect:/calidad/baja/muestreo-trazable-ok", view);
        assertEquals("Hubo un error persistiendo el muestreo.", redirect.getFlashAttributes().get("error"));
    }

    // -------------------- GET /muestreo-trazable-ok --------------------

    @Test
    void testExitoMuestreoTrazable() {
        assertEquals("calidad/baja/muestreo-trazable-ok", controller.exitoMuestreoTrazable(loteDTO));
    }

    // -------------------- GET /muestreo-multi-bulto --------------------

    @Test
    void testShowMuestreoMultiBultoForm() {
        // given
        List<LoteDTO> lista = List.of(new LoteDTO(), new LoteDTO());
        when(loteService.findAllForMuestreoMultiBultoDTOs()).thenReturn(lista);

        // when
        String view = controller.showMuestreoMultiBultoForm(loteDTO, model);

        // then
        assertEquals("calidad/baja/muestreo-multi-bulto", view);
        assertSame(loteDTO, model.getAttribute("loteDTO"));
        assertSame(lista, model.getAttribute("loteMuestreoMultiBultoDTOs"));
    }

    // -------------------- POST /muestreo-multi-bulto/confirm --------------------

    @Test
    void testConfirmarMuestreoMultiBulto_ConValidacionExitosa() {
        // given
        loteDTO.setCodigoLote("LOTE-001");
        loteDTO.setFechaEgreso(LocalDate.now());
        loteDTO.setObservaciones("Test");
        loteDTO.setCantidadesBultos(List.of(BigDecimal.TEN, BigDecimal.valueOf(20)));
        loteDTO.setUnidadMedidaBultos(List.of(UnidadMedidaEnum.KILOGRAMO, UnidadMedidaEnum.KILOGRAMO));
        loteDTO.setNroBultoList(List.of(1, 2));

        String nroAnalisis = "AN-001";

        LoteDTO loteCompleto = new LoteDTO();
        loteCompleto.setCodigoLote("LOTE-001");
        loteCompleto.setNombreProducto("Producto Test");
        loteCompleto.setCodigoProducto("PROD-001");

        when(muestreoBultoService.validarmuestreoMultiBultoInput(loteDTO, bindingLote)).thenReturn(true);
        when(loteService.findDTOByCodigoLote("LOTE-001")).thenReturn(Optional.of(loteCompleto));

        // when
        String view = controller.confirmarMuestreoMultiBulto(loteDTO, nroAnalisis, bindingLote, model);

        // then
        assertEquals("calidad/baja/muestreo-multi-bulto-confirm", view);
        assertEquals("AN-001", model.getAttribute("nroAnalisis"));
        assertNotNull(model.getAttribute("loteDTO"));

        // Verificar que se copiaron los datos del form
        LoteDTO modelLote = (LoteDTO) model.getAttribute("loteDTO");
        assertEquals(loteDTO.getFechaEgreso(), modelLote.getFechaEgreso());
        assertEquals(loteDTO.getObservaciones(), modelLote.getObservaciones());
        assertSame(loteDTO.getCantidadesBultos(), modelLote.getCantidadesBultos());
        assertSame(loteDTO.getUnidadMedidaBultos(), modelLote.getUnidadMedidaBultos());
        assertSame(loteDTO.getNroBultoList(), modelLote.getNroBultoList());
    }

    @Test
    void testConfirmarMuestreoMultiBulto_ConValidacionFallida() {
        // given
        List<LoteDTO> lista = List.of(new LoteDTO());
        when(muestreoBultoService.validarmuestreoMultiBultoInput(loteDTO, bindingLote)).thenReturn(false);
        when(loteService.findAllForMuestreoMultiBultoDTOs()).thenReturn(lista);

        // when
        String view = controller.confirmarMuestreoMultiBulto(loteDTO, "AN-001", bindingLote, model);

        // then
        assertEquals("calidad/baja/muestreo-multi-bulto", view);
        assertSame(lista, model.getAttribute("loteMuestreoMultiBultoDTOs"));
    }

    @Test
    void testConfirmarMuestreoMultiBulto_LoteNoEncontrado() {
        // given
        loteDTO.setCodigoLote("LOTE-999");
        when(muestreoBultoService.validarmuestreoMultiBultoInput(loteDTO, bindingLote)).thenReturn(true);
        when(loteService.findDTOByCodigoLote("LOTE-999")).thenReturn(Optional.empty());

        // when
        String view = controller.confirmarMuestreoMultiBulto(loteDTO, "AN-001", bindingLote, model);

        // then
        assertEquals("calidad/baja/muestreo-multi-bulto-confirm", view);
        assertEquals("AN-001", model.getAttribute("nroAnalisis"));
        // No debe haber loteDTO en el modelo ya que el Optional está vacío
    }

    // -------------------- POST /muestreo-multi-bulto --------------------

    @Test
    void testMuestreoMultiBulto_ConValidacionExitosa() {
        // given
        loteDTO.setCodigoLote("LOTE-001");
        loteDTO.setCantidadesBultos(new ArrayList<>(List.of(BigDecimal.TEN, BigDecimal.valueOf(20))));
        loteDTO.setUnidadMedidaBultos(new ArrayList<>(List.of(UnidadMedidaEnum.KILOGRAMO, UnidadMedidaEnum.KILOGRAMO)));
        loteDTO.setNroBultoList(List.of(1, 2));

        LoteDTO resultDTO = new LoteDTO();
        resultDTO.setCodigoLote("LOTE-001");
        // El servicio sobrescribe cantidadesBultos con las cantidades actuales (después del muestreo)
        resultDTO.setCantidadesBultos(new ArrayList<>(List.of(BigDecimal.valueOf(90), BigDecimal.valueOf(80))));
        resultDTO.setUnidadMedidaBultos(new ArrayList<>(List.of(UnidadMedidaEnum.KILOGRAMO, UnidadMedidaEnum.KILOGRAMO)));

        when(muestreoBultoService.bajamuestreoMultiBulto(any(LoteDTO.class))).thenReturn(resultDTO);

        // when
        String view = controller.muestreoMultiBulto(loteDTO, "AN-001", bindingLote, model, redirect);

        // then
        assertEquals("redirect:/calidad/baja/muestreo-multi-bulto-ok", view);

        LoteDTO flashLote = (LoteDTO) redirect.getFlashAttributes().get("loteDTO");
        assertNotNull(flashLote);

        // Verificar que se restauraron las cantidades muestreadas originales
        assertEquals(BigDecimal.TEN, flashLote.getCantidadesBultos().get(0));
        assertEquals(BigDecimal.valueOf(20), flashLote.getCantidadesBultos().get(1));

        assertEquals("Muestreo registrado correctamente.", redirect.getFlashAttributes().get("success"));
    }

    @Test
    void testMuestreoMultiBulto_ConBindingErrors() {
        // given
        bindingLote.reject("error.test", "Error de validación");

        List<LoteDTO> lista = List.of(new LoteDTO());
        when(loteService.findAllForMuestreoMultiBultoDTOs()).thenReturn(lista);

        // when
        String view = controller.muestreoMultiBulto(loteDTO, "AN-001", bindingLote, model, redirect);

        // then
        assertEquals("calidad/baja/muestreo-multi-bulto", view);
        assertSame(lista, model.getAttribute("loteMuestreoMultiBultoDTOs"));
    }

    @Test
    void testMuestreoMultiBulto_ConResultadoNull() {
        // given
        loteDTO.setCantidadesBultos(new ArrayList<>(List.of(BigDecimal.TEN)));
        loteDTO.setUnidadMedidaBultos(new ArrayList<>(List.of(UnidadMedidaEnum.KILOGRAMO)));

        when(muestreoBultoService.bajamuestreoMultiBulto(any(LoteDTO.class))).thenReturn(null);

        // when
        String view = controller.muestreoMultiBulto(loteDTO, "AN-001", bindingLote, model, redirect);

        // then
        assertEquals("redirect:/calidad/baja/muestreo-multi-bulto-ok", view);
        assertEquals("Hubo un error persistiendo el muestreo.", redirect.getFlashAttributes().get("error"));
    }

    // -------------------- GET /muestreo-multi-bulto-ok --------------------

    @Test
    void testExitoMuestreoMultiBulto() {
        assertEquals("calidad/baja/muestreo-multi-bulto-ok", controller.exitomuestreoMultiBulto(loteDTO));
    }

    // -------------------- Métodos de inicialización --------------------

    @Test
    void testInitModelMuestreoTrazable() {
        // given
        List<LoteDTO> lista = List.of(new LoteDTO(), new LoteDTO());
        when(loteService.findAllForMuestreoTrazableDTOs()).thenReturn(lista);

        // when
        controller.initModelMuestreoTrazable(movDto, model);

        // then
        assertSame(lista, model.getAttribute("loteMuestreoDTOs"));
        assertSame(movDto, model.getAttribute("movimientoDTO"));
        verify(loteService).findAllForMuestreoTrazableDTOs();
    }

    @Test
    void testProcesarMuestreoTrazable() {
        // given
        movDto.setCodigoLote("LOTE-001");
        movDto.setNroBulto("1");
        movDto.setTrazaDTOs(List.of(new TrazaDTO()));

        LoteDTO resultDTO = new LoteDTO();
        resultDTO.setCodigoLote("LOTE-001");

        when(muestreoBultoService.bajaMuestreoTrazable(any(MovimientoDTO.class))).thenReturn(resultDTO);

        // when
        controller.procesarMuestreoTrazable(movDto, redirect);

        // then
        assertNotNull(redirect.getFlashAttributes().get("loteDTO"));
        assertEquals("1", redirect.getFlashAttributes().get("bultoMuestreo"));
        assertNotNull(redirect.getFlashAttributes().get("trazaMuestreoDTOs"));
        assertEquals("Muestreo registrado correctamente.", redirect.getFlashAttributes().get("success"));
        verify(muestreoBultoService).bajaMuestreoTrazable(any(MovimientoDTO.class));
    }

    @Test
    void testProcesarMuestreoTrazable_ConResultadoNull() {
        // given
        when(muestreoBultoService.bajaMuestreoTrazable(any(MovimientoDTO.class))).thenReturn(null);

        // when
        controller.procesarMuestreoTrazable(movDto, redirect);

        // then
        assertEquals("Hubo un error persistiendo el muestreo.", redirect.getFlashAttributes().get("error"));
    }
}

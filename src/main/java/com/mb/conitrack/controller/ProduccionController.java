package com.mb.conitrack.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.validation.AltaProduccion;
import com.mb.conitrack.dto.validation.BajaProduccion;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.ProductoService;

import static com.mb.conitrack.controller.ControllerUtils.populateAvailableLoteListByCodigoInterno;
import static com.mb.conitrack.controller.ControllerUtils.validarBultos;
import static com.mb.conitrack.controller.ControllerUtils.validarCantidadesPorMedidas;
import static com.mb.conitrack.controller.ControllerUtils.validarFechaEgresoLoteDtoPosteriorLote;
import static com.mb.conitrack.controller.ControllerUtils.validateCantidadIngreso;
import static com.mb.conitrack.dto.DTOUtils.getLotesDtosByCodigoInterno;

@Controller
@RequestMapping("/produccion")
public class ProduccionController {

    //TODO: Sistema FIFO (fecha reanalisis/vencimiento) para lotes que compartan el mismo producto

    @Autowired
    private ProductoService productoService;

    @Autowired
    private LoteService loteService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    // CU7: Baja por Consumo Producción *****************************************************
    // @PreAuthorize("hasAuthority('ROLE_SUPERVISOR_PLANTA')")
    @GetMapping("/consumo-produccion")
    public String showConsumoProduccionForm(@ModelAttribute LoteDTO loteDTO, Model model) {

        initModelConsumoProduccion(loteDTO, model);
        return "produccion/consumo-produccion";
    }

    @PostMapping("/consumo-produccion")
    public String procesarConsumoProduccion(
        @Validated(BajaProduccion.class) @ModelAttribute LoteDTO loteDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes
    ) {

        if (!validarConsumoProduccionInput(loteDTO, bindingResult)) {
            initModelConsumoProduccion(loteDTO, model);
            return "produccion/consumo-produccion";
        }

        consumoProduccion(loteDTO, redirectAttributes);
        return "redirect:/produccion/consumo-produccion-ok";
    }

    @GetMapping("/consumo-produccion-ok")
    public String exitoConsumoProduccion(
        @ModelAttribute LoteDTO loteDTO) {
        return "produccion/consumo-produccion-ok";
    }

    // CU10 Ingreso por produccion interna *****************************************************
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/ingreso-produccion")
    public String showIngresoProduccion(@ModelAttribute("loteDTO") LoteDTO loteDTO, Model model) {

        //TODO: validar que la traza solo se pueda ingresar en unidad de venta
        initModelIngresoProduccion(model, loteDTO);
        return "produccion/ingreso-produccion";
    }

    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @PostMapping("/ingreso-produccion")
    public String procesarIngresoProduccion(
        @Validated(AltaProduccion.class) @ModelAttribute("loteDTO") LoteDTO loteDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        if (!validarIngresoProduccionInput(loteDTO, bindingResult)) {
            initModelIngresoProduccion(model, loteDTO);
            return "produccion/ingreso-produccion";
        }

        procesarIngresoProduccion(loteDTO, redirectAttributes);
        return "redirect:/produccion/ingreso-produccion-ok";
    }

    @GetMapping("/ingreso-produccion-ok")
    public String exitoIngresoProduccion(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "produccion/ingreso-produccion-ok"; // Template Thymeleaf
    }

    // CU12: Liberación de Unidad de Venta *****************************************************
    // @PreAuthorize("hasAuthority('ROLE_GERENTE_GARANTIA')")
    @GetMapping("/cu12")
    public String liberacionUnidadVenta() {
        return "/";
    }

    // CU13: Baja por Venta de Producto Propio
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/cu13")
    public String ventaProducto() {
        return "/";
    }

    private void consumoProduccion(final LoteDTO loteDTO, final RedirectAttributes redirectAttributes) {
        loteDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO resultDTO = DTOUtils.mergeEntities(loteService.registrarConsumoProduccion(loteDTO));

        redirectAttributes.addFlashAttribute("success", "Consumo registrado correctamente para la orden " + loteDTO.getOrdenProduccion());
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null ? "Consumo registrado correctamente para la orden " + loteDTO.getOrdenProduccion() : "Hubo un error en el consumo de stock por produccón.");
    }

    private void initModelConsumoProduccion(final LoteDTO loteDTO, final Model model) {
        List<LoteDTO> lotesProduccion = getLotesDtosByCodigoInterno(loteService.findAllForConsumoProduccion());
        model.addAttribute("lotesProduccion", lotesProduccion);
        model.addAttribute("loteDTO", loteDTO); //  ← mantiene lo que el usuario ingresó
    }

    private void initModelIngresoProduccion(final Model model, final LoteDTO loteDTO) {
        model.addAttribute("productos", productoService.getProductosInternos());

        if (loteDTO.getCantidadesBultos() == null) {
            loteDTO.setCantidadesBultos(new ArrayList<>());
        }
        if (loteDTO.getUnidadMedidaBultos() == null) {
            loteDTO.setUnidadMedidaBultos(new ArrayList<>());
        }
        model.addAttribute("loteDTO", loteDTO);
    }

    private void procesarIngresoProduccion(final LoteDTO loteDTO, final RedirectAttributes redirectAttributes) {
        loteDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO resultDTO = DTOUtils.mergeEntities(loteService.ingresarStockPorProduccion(loteDTO));

        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null ? "Ingreso de stock por produccion exitoso." : "Hubo un error en el ingreso de stock por produccón.");
    }

    private boolean validarConsumoProduccionInput(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        //TODO: caso donde el lote 2/3 se haya usado, pero el 1/3 no ni el 3/3
        final List<Lote> lotes = new ArrayList<>();
        return populateAvailableLoteListByCodigoInterno(lotes, loteDTO.getCodigoInterno(), bindingResult, loteService)
            && validarFechaEgresoLoteDtoPosteriorLote(loteDTO, lotes.get(0), bindingResult)
            && validarCantidadesPorMedidas(loteDTO, lotes, bindingResult);
    }

    private boolean validarIngresoProduccionInput(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        return validateCantidadIngreso(loteDTO, bindingResult)
            && validarBultos(loteDTO, bindingResult)
            && validarTraza(loteDTO, bindingResult);
    }

    private boolean validarTraza(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        //TODO: validar que la traza solo se aplique a unidad de venta
        if (loteDTO.getTrazaInicial() != null) {
            if (loteDTO.getUnidadMedida() != UnidadMedidaEnum.UNIDAD) {
                bindingResult.rejectValue("trazaInicial", "", "El número de traza solo aplica a unidades de venta");
                return false;
            }
            final Long maxNroTraza = loteService.findMaxNroTraza(loteDTO.getProductoId());
            if (maxNroTraza > 0 && loteDTO.getTrazaInicial() <= maxNroTraza) {
                bindingResult.rejectValue("trazaInicial", "", "El número de traza debe ser mayor al último registrado. " + maxNroTraza);
                return false;
            }
        }
        return true;
    }

}

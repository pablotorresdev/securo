package com.mb.conitrack.controller.cu;

import java.time.LocalDateTime;
import java.util.ArrayList;

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
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.ProductoService;
import com.mb.conitrack.service.TrazaService;

@Controller
@RequestMapping("/produccion/alta")
public class AltaIngresoProduccionController extends AbstractCuController {

    //TODO: Sistema FIFO (fecha reanalisis/vencimiento) para lotes que compartan el mismo producto

    @Autowired
    private ProductoService productoService;

    @Autowired
    private LoteService loteService;

    @Autowired
    private TrazaService trazaService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    // CU10 Ingreso por produccion interna *****************************************************
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/ingreso-produccion")
    public String showIngresoProduccion(@ModelAttribute("loteDTO") LoteDTO loteDTO, Model model) {
        //TODO: validar que la traza solo se pueda ingresar en unidad de venta
        initModelIngresoProduccion(loteDTO, model);
        return "produccion/alta/ingreso-produccion";
    }

    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @PostMapping("/ingreso-produccion")
    public String procesarIngresoProduccion(
        @Validated(AltaProduccion.class) @ModelAttribute("loteDTO") LoteDTO loteDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!validarIngresoProduccionInput(loteDTO, bindingResult)) {
            initModelIngresoProduccion(loteDTO, model);
            return "produccion/alta/ingreso-produccion";
        }

        procesarIngresoProduccion(loteDTO, redirectAttributes);
        return "redirect:/produccion/alta/ingreso-produccion-ok";
    }

    @GetMapping("/ingreso-produccion-ok")
    public String exitoIngresoProduccion(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "produccion/alta/ingreso-produccion-ok"; // Template Thymeleaf
    }

    private void initModelIngresoProduccion(final LoteDTO loteDTO, final Model model) {
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
        final LoteDTO resultDTO = DTOUtils.fromLoteEntity(loteService.altaStockPorProduccion(loteDTO));

        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null
                ? "Ingreso de stock por produccion exitoso."
                : "Hubo un error en el ingreso de stock por produccón.");
    }

    private boolean validarIngresoProduccionInput(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        boolean success = controllerUtils().validateCantidadIngreso(loteDTO, bindingResult);
        success = success && controllerUtils().validarBultos(loteDTO, bindingResult);
        success = success && validarTraza(loteDTO, bindingResult);
        return success;
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
            final Long maxNroTraza = trazaService.findMaxNroTraza(loteDTO.getProductoId());
            if (maxNroTraza > 0 && loteDTO.getTrazaInicial() <= maxNroTraza) {
                bindingResult.rejectValue(
                    "trazaInicial",
                    "",
                    "El número de traza debe ser mayor al último registrado. " + maxNroTraza);
                return false;
            }
        }
        return true;
    }

}

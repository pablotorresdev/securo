package com.mb.conitrack.controller.cu;

import java.time.OffsetDateTime;
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
import com.mb.conitrack.service.cu.AltaIngresoProduccionService;
import com.mb.conitrack.service.maestro.ProductoService;

@Controller
@RequestMapping("/produccion/alta")
public class AltaIngresoProduccionController extends AbstractCuController {

    //TODO: Sistema FIFO (fecha reanalisis/vencimiento) para lotes que compartan el mismo producto

    @Autowired
    private ProductoService productoService;

    @Autowired
    private AltaIngresoProduccionService ingresoProduccionService;

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
    public String ingresoProduccion(
        @Validated(AltaProduccion.class) @ModelAttribute("loteDTO") LoteDTO loteDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!ingresoProduccionService.validarIngresoProduccionInput(loteDTO, bindingResult)) {
            initModelIngresoProduccion(loteDTO, model);
            return "produccion/alta/ingreso-produccion";
        }

        ingresoProduccion(loteDTO, redirectAttributes);
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

    private void ingresoProduccion(final LoteDTO loteDTO, final RedirectAttributes redirectAttributes) {

        loteDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO resultDTO = ingresoProduccionService.altaStockPorProduccion(loteDTO);

        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null
                ? "Ingreso de stock por produccion exitoso."
                : "Hubo un error en el ingreso de stock por produccón.");
    }

}

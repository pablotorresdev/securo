package com.mb.conitrack.controller.cu;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.validation.AltaProduccion;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.cu.AltaIngresoProduccionService;

@Controller
@RequestMapping("/produccion/alta")
public class AltaIngresoProduccionController extends AbstractCuController {

    @Autowired
    private AltaIngresoProduccionService ingresoProduccionService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    // CU20 Ingreso por produccion interna *****************************************************
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/ingreso-produccion")
    public String showIngresoProduccion(@ModelAttribute("loteDTO") LoteDTO loteDTO, Model model) {
        initModelIngresoProduccion(loteDTO, model);
        return "produccion/alta/ingreso-produccion";
    }

    /** Muestra pantalla de confirmación con preview de datos antes de guardar. */
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @PostMapping("/ingreso-produccion/confirm")
    public String confirmarIngresoProduccion(
        @Validated(AltaProduccion.class) @ModelAttribute("loteDTO") LoteDTO loteDTO,
        BindingResult bindingResult,
        Model model) {

        if (!ingresoProduccionService.validarIngresoProduccionInput(loteDTO, bindingResult)) {
            initModelIngresoProduccion(loteDTO, model);
            return "produccion/alta/ingreso-produccion";
        }

        // Resolver nombres desde IDs para mostrar en la confirmación
        resolverNombresParaConfirmacion(loteDTO);

        model.addAttribute("loteDTO", loteDTO);
        return "produccion/alta/ingreso-produccion-confirm";
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

        procesarIngresoProduccion(loteDTO, redirectAttributes);
        return "redirect:/produccion/alta/ingreso-produccion-ok";
    }

    /** Resuelve nombres de producto desde IDs para mostrar en confirmación. */
    private void resolverNombresParaConfirmacion(LoteDTO loteDTO) {
        // Resolver nombre del producto
        if (loteDTO.getProductoId() != null) {
            productoService.findById(loteDTO.getProductoId()).ifPresent(producto -> {
                loteDTO.setNombreProducto(producto.getNombreGenerico());
                loteDTO.setCodigoProducto(producto.getCodigoProducto());
            });
        }
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

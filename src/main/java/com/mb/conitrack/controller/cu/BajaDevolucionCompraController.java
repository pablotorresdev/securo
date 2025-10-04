package com.mb.conitrack.controller.cu;

import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.service.cu.BajaDevolucionCompraService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/compras/baja")
public class BajaDevolucionCompraController extends AbstractCuController {

    @Autowired
    private BajaDevolucionCompraService devolucionCompraService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    // CU4: Baja por Devolución Compra *****************************************************
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/devolucion-compra")
    public String showDevolucionCompraForm(@ModelAttribute("movimientoDTO") MovimientoDTO movimientoDTO, Model model) {
        initModelDevolucionCompra(model, movimientoDTO);
        return "compras/baja/devolucion-compra";
    }

    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @PostMapping("/devolucion-compra")
    public String devolucionCompra(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!devolucionCompraService.validarDevolucionCompraInput(movimientoDTO, bindingResult)) {
            initModelDevolucionCompra(model, movimientoDTO);
            return "compras/baja/devolucion-compra";
        }

        procesarDevolucionCompra(movimientoDTO, redirectAttributes);
        return "redirect:/compras/baja/devolucion-compra-ok";
    }

    @GetMapping("/devolucion-compra-ok")
    public String exitoDevolucionCompra(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "compras/baja/devolucion-compra-ok";
    }

    void initModelDevolucionCompra(final Model model, MovimientoDTO movimientoDTO) {
        model.addAttribute("lotesDevolvibles", loteService.findAllForDevolucionCompraDTOs());
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    void procesarDevolucionCompra(final MovimientoDTO movimientoDTO, final RedirectAttributes redirectAttributes) {

        movimientoDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO loteDTO = devolucionCompraService.bajaBultosDevolucionCompra(movimientoDTO);

        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null ? "Devolucion realizada correctamente." : "Hubo un error en la devolucion de compra.");
    }

}

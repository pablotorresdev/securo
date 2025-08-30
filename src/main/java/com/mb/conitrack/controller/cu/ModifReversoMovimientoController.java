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
import com.mb.conitrack.service.cu.ModifReversoMovimientoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("ajustes")
public class ModifReversoMovimientoController extends AbstractCuController {

    @Autowired
    private ModifReversoMovimientoService reeversoMovimientoService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    //***************************** CU2 Reverso Lote a Movimiento************************************
    // CU2: Reverso Lote a Movimiento
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_CALIDAD')")
    @GetMapping("/reverso-movimiento")
    public String showReversoMovimientoForm(@ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        //TODO: implementar el filtro correcto en base a calidad y Analisis (Fecha, calidad)
        initModelReversoMovimiento(movimientoDTO, model);
        return "ajustes/reverso-movimiento";
    }

    @PostMapping("/reverso-movimiento")
    public String ReversoMovimiento(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!reeversoMovimientoService.validarReversoMovmientoInput(movimientoDTO, bindingResult)) {
            initModelReversoMovimiento(movimientoDTO, model);
            return "ajustes/reverso-movimiento";
        }

        procesarReversoMovimiento(movimientoDTO, redirectAttributes);
        return "redirect:/ajustes/reverso-movimiento-ok";
    }

    @GetMapping("/reverso-movimiento-ok")
    public String exitoReversoMovimiento(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "ajustes/reverso-movimiento-ok";
    }

    void initModelReversoMovimiento(final MovimientoDTO movimientoDTO, final Model model) {
        model.addAttribute("loteReversoDTOs", loteService.findAllActivo());
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    void procesarReversoMovimiento(
        final MovimientoDTO dto,
        final RedirectAttributes redirectAttributes) {

        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO loteDTO = reeversoMovimientoService.persistirReversoMovmiento(dto);

        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null
                ? "Reverso de Movimiento exitoso"
                : "Hubo un error al realizar el Reverso de Movimiento.");
    }

}

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
import com.mb.conitrack.service.cu.ModifAnulacionAnalisisService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/calidad/anulacion")
public class ModifAnulacionAnalisisController extends AbstractCuController {

    @Autowired
    private ModifAnulacionAnalisisService anulacionAnalisisService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    // @PreAuthorize("hasAuthority('ROLE_CONTROL_CALIDAD')")
    @GetMapping("/anulacion-analisis")
    public String showAnulacionAnalisisForm(@ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        initModelAnulacionAnalisis(movimientoDTO, model);
        return "calidad/anulacion/anulacion-analisis";
    }

    @PostMapping("/anulacion-analisis")
    public String anulacionAnalisis(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!anulacionAnalisisService.validarAnulacionAnalisisInput(movimientoDTO, bindingResult)) {
            initModelAnulacionAnalisis(movimientoDTO, model);
            return "calidad/anulacion/anulacion-analisis";
        }

        procesarAnulacionAnalisis(movimientoDTO, redirectAttributes);
        return "redirect:/calidad/anulacion/anulacion-analisis-ok";
    }

    @GetMapping("/anulacion-analisis-ok")
    public String exitoAnulacionAnalisis(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "calidad/anulacion/anulacion-analisis-ok";
    }

    void initModelAnulacionAnalisis(final MovimientoDTO movimientoDTO, final Model model) {
        movimientoDTO.setFechaMovimiento(OffsetDateTime.now().toLocalDate());
        model.addAttribute("movimientoDTO", movimientoDTO);
        model.addAttribute("analisisDTOs", analisisService.findAllEnCursoDTOs());
    }

    void procesarAnulacionAnalisis(final MovimientoDTO movimientoDTO, final RedirectAttributes redirectAttributes) {

        movimientoDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO loteDTO = anulacionAnalisisService.persistirAnulacionAnalisis(movimientoDTO);

        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null
                ? ("Anulacion de analisis exitoso")
                : "Hubo un error en la anulacion del analisis.");
    }

}

package com.mb.conitrack.controller.cu;

import java.time.OffsetDateTime;
import java.util.List;

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
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.cu.ModifResultadoAnalisisService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/calidad/analisis")
public class ModifResultadoAnalisisController extends AbstractCuController {

    @Autowired
    private ModifResultadoAnalisisService resultadoAnalisisService;


    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    // CU5: Resultado QA Aprobado
    // CU6: Resultado QA Rechazado
    // @PreAuthorize("hasAuthority('ROLE_CONTROL_CALIDAD')")
    @GetMapping("/resultado-analisis")
    public String showResultadoAnalisisForm(@ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        initModelResultadoAnalisis(movimientoDTO, model);
        return "calidad/analisis/resultado-analisis";
    }

    @PostMapping("/resultado-analisis")
    public String resultadoAnalisis(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!resultadoAnalisisService.validarResultadoAnalisisInput(movimientoDTO, bindingResult)) {
            initModelResultadoAnalisis(movimientoDTO, model);
            return "calidad/analisis/resultado-analisis";
        }

        procesarResultadoAnalisis(movimientoDTO, redirectAttributes);
        return "redirect:/calidad/analisis/resultado-analisis-ok";
    }

    @GetMapping("/resultado-analisis-ok")
    public String exitoResultadoAnalisis(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "calidad/analisis/resultado-analisis-ok";
    }

    void initModelResultadoAnalisis(final MovimientoDTO movimientoDTO, final Model model) {
        movimientoDTO.setFechaMovimiento(OffsetDateTime.now().toLocalDate());
        model.addAttribute("movimientoDTO", movimientoDTO);
        model.addAttribute("loteResultadoAnalisisDTOs", loteService.findAllForResultadoAnalisisDTOs());
        model.addAttribute("analisisDTOs", analisisService.findAllEnCursoForLotesCuarentenaDTOs());
        model.addAttribute("resultados", List.of(DictamenEnum.APROBADO, DictamenEnum.RECHAZADO));
    }

    void procesarResultadoAnalisis(final MovimientoDTO movimientoDTO, final RedirectAttributes redirectAttributes) {

        movimientoDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO loteDTO = resultadoAnalisisService.persistirResultadoAnalisis(movimientoDTO);

        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null
                ? ("Cambio de dictamen a " + movimientoDTO.getDictamenFinal() + " exitoso")
                : "Hubo un error con el cambio de dictamen.");
    }

}

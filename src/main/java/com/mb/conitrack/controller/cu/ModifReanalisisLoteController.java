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
import com.mb.conitrack.service.cu.ModifReanalisisLoteService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/calidad/reanalisis")
public class ModifReanalisisLoteController extends AbstractCuController {

    @Autowired
    private ModifReanalisisLoteService reanalisisLoteService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    //***************************** CU8 Reanalisis de Producto Aprobado************************************
    // CU8: Reanalisis de Producto Aprobado
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_CALIDAD')")
    @GetMapping("/inicio-reanalisis")
    public String showReanalisisLoteForm(@ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        initModelReanalisisLote(movimientoDTO, model);
        return "calidad/reanalisis/inicio-reanalisis";
    }

    @PostMapping("/inicio-reanalisis")
    public String reanalisisLote(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!reanalisisLoteService.validarReanalisisLoteInput(movimientoDTO, bindingResult)) {
            initModelReanalisisLote(movimientoDTO, model);
            return "calidad/reanalisis/inicio-reanalisis";
        }

        procesarReanalisisLote(movimientoDTO, redirectAttributes);
        return "redirect:/calidad/reanalisis/inicio-reanalisis-ok";
    }

    @GetMapping("/inicio-reanalisis-ok")
    public String exitoReanalisisLote(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "calidad/reanalisis/inicio-reanalisis-ok";
    }

    private void initModelReanalisisLote(final MovimientoDTO movimientoDTO, final Model model) {
        //TODO: implementar el filtro correcto en base a calidad y Analisis (Fecha, calidad)
        model.addAttribute("loteReanalisisDTOs", loteService.findAllForReanalisisLoteDTOs());
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    private void procesarReanalisisLote(final MovimientoDTO dto, final RedirectAttributes redirectAttributes) {

        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO loteDTO = reanalisisLoteService.persistirReanalisisLote(dto);
        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);

        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null
                ? "Anilisis asignado con éxito: " + loteDTO.getUltimoNroAnalisisDto()
                : "Hubo un error al asignar el analisis.");
    }

}

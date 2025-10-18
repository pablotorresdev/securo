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
import com.mb.conitrack.service.cu.ModifTrazadoLoteService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/ventas/trazado")
public class ModifTrazadoLoteController extends AbstractCuController {

    @Autowired
    private ModifTrazadoLoteService modifTrazadoLoteService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    //***************************** CU27 Trazado lote************************************
    // CU27: Trazado Lote
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_ventas')")
    @GetMapping("/inicio-trazado")
    public String showTrazadoLoteForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        initModelTrazadoLote(movimientoDTO, model);
        return "ventas/trazado/inicio-trazado";
    }

    @PostMapping("/inicio-trazado")
    public String trazadoLote(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!modifTrazadoLoteService.validarTrazadoLoteInput(movimientoDTO, bindingResult)) {
            initModelTrazadoLote(movimientoDTO, model);
            return "ventas/trazado/inicio-trazado";
        }

        procesarTrazadoLote(movimientoDTO, redirectAttributes);
        return "redirect:/ventas/trazado/inicio-trazado-ok";
    }

    @GetMapping("/inicio-trazado-ok")
    public String exitoTrazadoLote(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "ventas/trazado/inicio-trazado-ok";
    }

    private void initModelTrazadoLote(final MovimientoDTO movimientoDTO, final Model model) {
        final List<LoteDTO> loteTrazadoDtos = loteService.findAllForTrazadoLoteDTOs();
        model.addAttribute("loteTrazadoDtos", loteTrazadoDtos);
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    private void procesarTrazadoLote(
        final MovimientoDTO dto,
        final RedirectAttributes redirectAttributes) {

        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO loteDTO = modifTrazadoLoteService.persistirTrazadoLote(dto);

        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null
                ? "Trazado de Lote exitoso"
                : "Hubo un error con el trazado del Lote");
    }

}

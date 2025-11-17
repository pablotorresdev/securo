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
import com.mb.conitrack.service.cu.ModifRetiroMercadoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/ventas/recall")
public class AltaRetiroMercadoController extends AbstractCuController {

    @Autowired
    private ModifRetiroMercadoService retiroMercadoService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    // @PreAuthorize("hasAuthority('ROLE_GERENTE_GARANTIA')")
    @GetMapping("/retiro-mercado")
    public String showRetiroMercadoForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        initModelRetiroMercado(movimientoDTO, model);
        return "ventas/recall/retiro-mercado";
    }

    @PostMapping("/retiro-mercado/confirm")
    public String confirmRetiroMercado(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model) {

        if (!retiroMercadoService.validarRetiroMercadoInput(movimientoDTO, bindingResult)) {
            initModelRetiroMercado(movimientoDTO, model);
            model.addAttribute("movimientoDTO", movimientoDTO);
            return "ventas/recall/retiro-mercado";
        }

        model.addAttribute("movimientoDTO", movimientoDTO);
        return "ventas/recall/retiro-mercado-confirm";
    }

    @PostMapping("/retiro-mercado")
    public String retiroMercado(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!retiroMercadoService.validarRetiroMercadoInput(movimientoDTO, bindingResult)) {
            initModelRetiroMercado(movimientoDTO, model);
            model.addAttribute("movimientoDTO", movimientoDTO);
            return "ventas/recall/retiro-mercado";
        }
        prcesarRetiroMercado(movimientoDTO, redirectAttributes);
        return "redirect:/ventas/recall/retiro-mercado-ok";
    }

    @GetMapping("/retiro-mercado-ok")
    public String exitoRetiroMercado(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "ventas/recall/retiro-mercado-ok";
    }

    private void initModelRetiroMercado(final MovimientoDTO movimientoDTO, final Model model) {
        model.addAttribute("lotesRecall", loteService.findAllForRecallDTOs());
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    private void prcesarRetiroMercado(
        final @Valid MovimientoDTO movimientoDTO,
        final RedirectAttributes redirectAttributes) {

        movimientoDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        final List<LoteDTO> resultDTO = retiroMercadoService.persistirRetiroMercado(movimientoDTO);

        if (resultDTO != null && !resultDTO.isEmpty()) {
            redirectAttributes.addFlashAttribute("loteRecallDTO", resultDTO.get(0));
            redirectAttributes.addFlashAttribute("loteVentaDTO", resultDTO.get(1));
            redirectAttributes.addFlashAttribute("movimientoDTO", movimientoDTO);
            redirectAttributes.addFlashAttribute("success", "Ingreso de stock por devolución exitoso.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Hubo un error en el ingreso de stock por devolución.");
        }
    }

}

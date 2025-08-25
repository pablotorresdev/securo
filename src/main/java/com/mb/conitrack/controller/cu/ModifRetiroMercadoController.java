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
import com.mb.conitrack.service.cu.ModifRetiroMercadoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/ventas/recall")
public class ModifRetiroMercadoController extends AbstractCuController {

    @Autowired
    private ModifRetiroMercadoService retiroMercadoService;

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    //***************************** CU3 Muestreo************************************
    // CU13: Devolución de cliente
    // @PreAuthorize("hasAuthority('ROLE_GERENTE_GARANTIA')")
    @GetMapping("/retiro-mercado")
    public String showRetiroMercadoForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        //TODO: implementar el filtro correcto en base a ventas y Analisis (Fecha, ventas)
        initModelRetiroMercado(movimientoDTO, model);
        return "ventas/recall/retiro-mercado";
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
            return "ventas/devolucion/retiro-mercado";
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
        model.addAttribute("lotesRecall", loteService.findAllForDevolucionOrRecallDTOs());
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    private void prcesarRetiroMercado(
        final @Valid MovimientoDTO movimientoDTO,
        final RedirectAttributes redirectAttributes) {

        movimientoDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO resultDTO = retiroMercadoService.persistirRetiroMercado(movimientoDTO);

        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute("movimientoDTO", movimientoDTO);
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null
                ? "Ingreso de stock por devolución exitoso."
                : "Hubo un error en el ingreso de stock por devolución.");
    }

}

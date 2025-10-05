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
import com.mb.conitrack.service.cu.BajaAjusteStockService;
import com.mb.conitrack.service.cu.ModifReversoMovimientoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/contingencias")
public class ContingenciasController extends AbstractCuController {

    @Autowired
    private ModifReversoMovimientoService reeversoMovimientoService;

    @Autowired
    private BajaAjusteStockService ajusteStockService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    //***************************** CU2 Reverso Movimiento************************************
    // CU25: Reverso Movimiento
    // @PreAuthorize("???')")
    @GetMapping("/reverso-movimiento")
    public String showReversoMovimientoForm(@ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        initModelReversoMovimiento(movimientoDTO, model);
        return "contingencias/reverso-movimiento";
    }

    @PostMapping("/reverso-movimiento")
    public String ReversoMovimiento(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!reeversoMovimientoService.validarReversoMovmientoInput(movimientoDTO, bindingResult)) {
            initModelReversoMovimiento(movimientoDTO, model);
            return "contingencias/reverso-movimiento";
        }
        try {
            procesarReversoMovimiento(movimientoDTO, redirectAttributes);
        } catch (IllegalStateException e) {
            bindingResult.rejectValue(
                "codigoLote",
                "",
                "El lote origen tiene una devolucion asociada, no se puede reversar el movimiento.");
            initModelReversoMovimiento(movimientoDTO, model);
            return "contingencias/reverso-movimiento";
        }

        return "redirect:/contingencias/reverso-movimiento-ok";
    }

    @GetMapping("/reverso-movimiento-ok")
    public String exitoReversoMovimiento(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "contingencias/reverso-movimiento-ok";
    }

    //***************************** CU26 Ajuste Stock************************************
    // CU26: Ajuste Stock
    // @PreAuthorize("???')")
    @GetMapping("/ajuste-stock")
    public String showAjusteStockForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        initModelAjusteStock(movimientoDTO, model);
        return "contingencias/ajuste-stock";
    }

    @PostMapping("/ajuste-stock")
    public String ajusteStock(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!ajusteStockService.validarAjusteStockInput(movimientoDTO, bindingResult)) {
            initModelAjusteStock(movimientoDTO, model);
            return "contingencias/ajuste-stock";
        }

        procesarAjusteStock(movimientoDTO, redirectAttributes);
        return "redirect:/contingencias/ajuste-stock-ok";
    }

    @GetMapping("/ajuste-stock-ok")
    public String exitoAjuste(
        @ModelAttribute LoteDTO loteDTO) {
        return "contingencias/ajuste-stock-ok";
    }

    void initModelAjusteStock(final MovimientoDTO movimientoDTO, final Model model) {
        model.addAttribute("loteAjusteDTOs", loteService.findAllForAjusteDTOs());
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    void initModelReversoMovimiento(final MovimientoDTO movimientoDTO, final Model model) {
        model.addAttribute("loteReversoDTOs", loteService.findAllForReversoMovimientoDTOs());
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    void procesarAjusteStock(
        final MovimientoDTO movimientoDTO,
        final RedirectAttributes redirectAttributes) {

        movimientoDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO loteDTO = ajusteStockService.bajaAjusteStock(movimientoDTO);

        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute("trazaAjusteDTOs", movimientoDTO.getTrazaDTOs());
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null ? "Ajuste registrado correctamente." : "Hubo un error persistiendo el ajuste.");
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

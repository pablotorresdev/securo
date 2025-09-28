package com.mb.conitrack.controller.cu;

import java.time.OffsetDateTime;

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
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.service.cu.BajaMuestreoBultoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/calidad/baja")
public class BajaMuestreoBultoController extends AbstractCuController {

    @Autowired
    private BajaMuestreoBultoService muestreoBultoService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_CALIDAD')")
    @GetMapping("/muestreo-bulto")
    public String showMuestreoBultoForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        initModelMuestreoBulto(movimientoDTO, model);
        return "calidad/baja/muestreo-bulto";
    }

    @PostMapping("/muestreo-bulto")
    //TODO: soportar multimuestreo (x cant x cada bulto)
    public String muestreoBulto(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!muestreoBultoService.validarMuestreoBultoInput(movimientoDTO, bindingResult)) {
            initModelMuestreoBulto(movimientoDTO, model);
            return "calidad/baja/muestreo-bulto";
        }

        procesarMuestreoBulto(movimientoDTO, redirectAttributes);
        return "redirect:/calidad/baja/muestreo-bulto-ok";
    }

    @GetMapping("/muestreo-bulto-ok")
    public String exitoMuestreo(
        @ModelAttribute LoteDTO loteDTO) {
        return "calidad/baja/muestreo-bulto-ok";
    }

    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_CALIDAD')")
    @GetMapping("/muestreo-multi-bulto")
    public String showMuestreoMultiBultoForm(
        @ModelAttribute LoteDTO loteDTO, Model model) {
        initModelmuestreoMultiBulto(loteDTO, model);
        return "calidad/baja/muestreo-multi-bulto";
    }

    @PostMapping("/muestreo-multi-bulto")
    public String muestreoMultiBulto(
        @Validated @ModelAttribute LoteDTO loteDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!muestreoBultoService.validarmuestreoMultiBultoInput(loteDTO, bindingResult)) {
            initModelmuestreoMultiBulto(loteDTO, model);
            return "calidad/baja/muestreo-multi-bulto";
        }

        procesarmuestreoMultiBulto(loteDTO, redirectAttributes);
        return "redirect:/calidad/baja/muestreo-multi-bulto-ok";
    }

    //*************** MUESTREO MULTI BULTO ***********************

    @GetMapping("/muestreo-multi-bulto-ok")
    public String exitomuestreoMultiBulto(
        @ModelAttribute LoteDTO loteDTO) {
        return "calidad/baja/muestreo-multi-bulto-ok";
    }

    void initModelMuestreoBulto(final MovimientoDTO movimientoDTO, final Model model) {
        model.addAttribute("loteMuestreoDTOs", loteService.findAllForMuestreoDTOs());
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    void procesarMuestreoBulto(
        final MovimientoDTO movimientoDTO,
        final RedirectAttributes redirectAttributes) {

        movimientoDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO loteDTO = muestreoBultoService.bajaMuestreo(movimientoDTO);

        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute("bultoMuestreo", movimientoDTO.getNroBulto());
        redirectAttributes.addFlashAttribute("trazaMuestreoDTOs", movimientoDTO.getTrazaDTOs());
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null ? "Muestreo registrado correctamente." : "Hubo un error persistiendo el muestreo.");
    }

    private void initModelmuestreoMultiBulto(final LoteDTO loteDTO, final Model model) {
        model.addAttribute("loteMuestreoMultiBultoDTOs", loteService.findAllForMuestreoMultiBultoDTOs());
        model.addAttribute("loteDTO", loteDTO); //  ← mantiene lo que el usuario ingresó
    }

    private void procesarmuestreoMultiBulto(final LoteDTO loteDTO, final RedirectAttributes redirectAttributes) {

        loteDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO resultDTO = muestreoBultoService.bajamuestreoMultiBulto(loteDTO);

        //TODO: se puede remover esto?
        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null ? "Muestreo registrado correctamente." : "Hubo un error persistiendo el muestreo.");
    }

}

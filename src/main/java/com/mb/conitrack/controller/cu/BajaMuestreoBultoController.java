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
    @GetMapping("/muestreo-trazable")
    public String showMuestreoTrazableForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        initModelMuestreoTrazable(movimientoDTO, model);
        return "calidad/baja/muestreo-trazable";
    }

    @PostMapping("/muestreo-trazable")
    public String muestreoTrazable(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!muestreoBultoService.validarMuestreoTrazableInput(movimientoDTO, bindingResult)) {
            initModelMuestreoTrazable(movimientoDTO, model);
            return "calidad/baja/muestreo-trazable";
        }

        procesarMuestreoTrazable(movimientoDTO, redirectAttributes);
        return "redirect:/calidad/baja/muestreo-trazable-ok";
    }

    @GetMapping("/muestreo-trazable-ok")
    public String exitoMuestreoTrazable(
        @ModelAttribute LoteDTO loteDTO) {
        return "calidad/baja/muestreo-trazable-ok";
    }

    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_CALIDAD')")
    @GetMapping("/muestreo-multi-bulto")
    public String showMuestreoMultiBultoForm(
        @ModelAttribute LoteDTO loteDTO, Model model) {
        initModelmuestreoMultiBulto(loteDTO, model);
        return "calidad/baja/muestreo-multi-bulto";
    }

    @PostMapping("/muestreo-multi-bulto/confirm")
    public String confirmarMuestreoMultiBulto(
        @Validated @ModelAttribute LoteDTO loteDTO,
        @ModelAttribute("nroAnalisis") String nroAnalisis,
        BindingResult bindingResult,
        Model model) {

        if (!muestreoBultoService.validarmuestreoMultiBultoInput(loteDTO, bindingResult)) {
            initModelmuestreoMultiBulto(loteDTO, model);
            return "calidad/baja/muestreo-multi-bulto";
        }

        // Obtener el lote completo de la base de datos para mostrar toda la información
        loteService.findDTOByCodigoLote(loteDTO.getCodigoLote()).ifPresent(loteCompleto -> {
            // Copiar los datos del form al lote completo
            loteCompleto.setFechaEgreso(loteDTO.getFechaEgreso());
            loteCompleto.setObservaciones(loteDTO.getObservaciones());
            loteCompleto.setCantidadesBultos(loteDTO.getCantidadesBultos());
            loteCompleto.setUnidadMedidaBultos(loteDTO.getUnidadMedidaBultos());
            loteCompleto.setNroBultoList(loteDTO.getNroBultoList());

            model.addAttribute("loteDTO", loteCompleto);
        });

        model.addAttribute("nroAnalisis", nroAnalisis);

        return "calidad/baja/muestreo-multi-bulto-confirm";
    }

    @PostMapping("/muestreo-multi-bulto")
    public String muestreoMultiBulto(
        @Validated @ModelAttribute LoteDTO loteDTO,
        @ModelAttribute("nroAnalisis") String nroAnalisis,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        // Validación adicional si es necesario
        if (bindingResult.hasErrors()) {
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

    void initModelMuestreoTrazable(final MovimientoDTO movimientoDTO, final Model model) {
        model.addAttribute("loteMuestreoDTOs", loteService.findAllForMuestreoTrazableDTOs());
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    void procesarMuestreoTrazable(
        final MovimientoDTO movimientoDTO,
        final RedirectAttributes redirectAttributes) {

        movimientoDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO loteDTO = muestreoBultoService.bajaMuestreoTrazable(movimientoDTO);

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

        // Preservar las cantidades muestreadas originales antes de que se pierdan
        final java.util.List<java.math.BigDecimal> cantidadesMuestreadas = new java.util.ArrayList<>(loteDTO.getCantidadesBultos());
        final java.util.List<com.mb.conitrack.enums.UnidadMedidaEnum> unidadesMuestreadas = new java.util.ArrayList<>(loteDTO.getUnidadMedidaBultos());

        loteDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO resultDTO = muestreoBultoService.bajamuestreoMultiBulto(loteDTO);

        // Restaurar las cantidades muestreadas para mostrar en la pantalla de éxito
        if (resultDTO != null) {
            resultDTO.setCantidadesBultos(cantidadesMuestreadas);
            resultDTO.setUnidadMedidaBultos(unidadesMuestreadas);
        }

        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null ? "Muestreo registrado correctamente." : "Hubo un error persistiendo el muestreo.");
    }

}

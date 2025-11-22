package com.mb.conitrack.controller.cu;

import java.time.OffsetDateTime;
import java.util.List;

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
import com.mb.conitrack.dto.validation.BajaProduccion;
import com.mb.conitrack.service.cu.BajaConsumoProduccionService;

@Controller
@RequestMapping("/produccion/baja")
public class BajaConsumoProduccionController extends AbstractCuController {

    @Autowired
    private BajaConsumoProduccionService consumoProduccionService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    // CU7: Baja por Consumo Producción *****************************************************
    // @PreAuthorize("hasAuthority('ROLE_SUPERVISOR_PLANTA')")
    @GetMapping("/consumo-produccion")
    public String showConsumoProduccionForm(@ModelAttribute LoteDTO loteDTO, Model model) {
        initModelConsumoProduccion(loteDTO, model);
        return "produccion/baja/consumo-produccion";
    }

    @PostMapping("/consumo-produccion")
    public String consumoProduccion(
        @Validated(BajaProduccion.class) @ModelAttribute LoteDTO loteDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!consumoProduccionService.validarConsumoProduccionInput(loteDTO, bindingResult)) {
            initModelConsumoProduccion(loteDTO, model);
            return "produccion/baja/consumo-produccion";
        }

        procesarConsumoProduccion(loteDTO, redirectAttributes);
        return "redirect:/produccion/baja/consumo-produccion-ok";
    }

    @GetMapping("/consumo-produccion-ok")
    public String exitoConsumoProduccion(
        @ModelAttribute LoteDTO loteDTO) {
        return "produccion/baja/consumo-produccion-ok";
    }

    void initModelConsumoProduccion(final LoteDTO loteDTO, final Model model) {
        List<LoteDTO> loteProduccionDTOs = loteService.findAllForConsumoProduccionDTOs();
        model.addAttribute("loteProduccionDTOs", loteProduccionDTOs);
        model.addAttribute("loteDTO", loteDTO); //  ← mantiene lo que el usuario ingresó
    }

    void procesarConsumoProduccion(final LoteDTO loteDTO, final RedirectAttributes redirectAttributes) {

        loteDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO resultDTO = consumoProduccionService.bajaConsumoProduccion(loteDTO);

        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null
                ? "Consumo registrado correctamente para la orden " + loteDTO.getOrdenProduccion()
                : "Hubo un error en el consumo de stock por produccón.");
    }

}

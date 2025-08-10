package com.mb.conitrack.controller.cu;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.validation.BajaProduccion;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.utils.ControllerUtils;

import static com.mb.conitrack.dto.DTOUtils.getLotesDtosByCodigoInterno;

@Controller
@RequestMapping("/produccion/baja")
public class BajaConsumoProduccionController {

    //TODO: Sistema FIFO (fecha reanalisis/vencimiento) para lotes que compartan el mismo producto

    @Autowired
    private LoteService loteService;

    private static ControllerUtils xontrollerUtils() {
        return ControllerUtils.getInstance();
    }

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
    public String procesarConsumoProduccion(
        @Validated(BajaProduccion.class) @ModelAttribute LoteDTO loteDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!validarConsumoProduccionInput(loteDTO, bindingResult)) {
            initModelConsumoProduccion(loteDTO, model);
            return "produccion/baja/consumo-produccion";
        }

        consumoProduccion(loteDTO, redirectAttributes);
        return "redirect:/produccion/baja/consumo-produccion-ok";
    }

    @GetMapping("/consumo-produccion-ok")
    public String exitoConsumoProduccion(
        @ModelAttribute LoteDTO loteDTO) {
        return "produccion/baja/consumo-produccion-ok";
    }

    private void consumoProduccion(final LoteDTO loteDTO, final RedirectAttributes redirectAttributes) {
        loteDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO resultDTO = DTOUtils.mergeEntities(loteService.bajaConsumoProduccion(loteDTO));

        //TODO: se puede remover esto?
        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null
                ? "Consumo registrado correctamente para la orden " + loteDTO.getOrdenProduccion()
                : "Hubo un error en el consumo de stock por produccón.");
    }

    private void initModelConsumoProduccion(final LoteDTO loteDTO, final Model model) {
        List<LoteDTO> lotesProduccion = getLotesDtosByCodigoInterno(loteService.findAllForConsumoProduccion());
        model.addAttribute("lotesProduccion", lotesProduccion);
        model.addAttribute("loteDTO", loteDTO); //  ← mantiene lo que el usuario ingresó
    }

    private boolean validarConsumoProduccionInput(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        //TODO: caso donde el lote 2/3 se haya usado, pero el 1/3 no ni el 3/3
        final List<Lote> lotes = new ArrayList<>();
        boolean success = xontrollerUtils().populateAvailableLoteListByCodigoInterno(
            lotes,
            loteDTO.getCodigoInternoLote(),
            bindingResult,
            loteService);
        success = success &&
            xontrollerUtils().validarFechaEgresoLoteDtoPosteriorLote(loteDTO, lotes.get(0), bindingResult);
        success = success &&
            xontrollerUtils().validarCantidadesPorMedidas(loteDTO, lotes, bindingResult);
        return success;
    }

}

package com.mb.conitrack.controller.cu;

import java.time.LocalDateTime;
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

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.utils.ControllerUtils;

import jakarta.validation.Valid;

import static com.mb.conitrack.dto.DTOUtils.getLotesDtosByCodigoInterno;

@Controller
@RequestMapping("/calidad/dictamen")
public class ModifDictamenCuarentenaController {

    @Autowired
    private LoteService loteService;

    @Autowired
    private AnalisisService analisisService;

    private static ControllerUtils controllerUtils() {
        return ControllerUtils.getInstance();
    }

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    //***************************** CU2 Dictamen Lote a Cuarentena************************************
    // CU2: Dictamen Lote a Cuarentena
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_CALIDAD')")
    @GetMapping("/cuarentena")
    public String showDictamenCuarentenaForm(@ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        //TODO: implementar el filtro correcto en base a calidad y Analisis (Fecha, calidad)
        initModelDictamencuarentena(movimientoDTO, model);
        return "calidad/dictamen/cuarentena";
    }

    @PostMapping("/cuarentena")
    public String procesarDictamenCuarentena(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {
        Lote lote = null;
        boolean success = controllerUtils().validarNroAnalisisNotNull(movimientoDTO, bindingResult);
        success = success && controllerUtils()
            .validarNroAnalisisUnico(movimientoDTO, bindingResult, analisisService);
        if (success) {
            lote = controllerUtils().getLoteByCodigoInterno(
                movimientoDTO.getCodigoInternoLote(),
                bindingResult,
                loteService);
        }
        success = success && lote != null;
        success = success && controllerUtils()
            .validarFechaMovimientoPosteriorIngresoLote(movimientoDTO, lote, bindingResult);
        success = success && controllerUtils()
            .validarFechaAnalisisPosteriorIngresoLote(movimientoDTO, lote, bindingResult);

        if (!success) {
            initModelDictamencuarentena(movimientoDTO, model);
            model.addAttribute("movimientoDTO", movimientoDTO);
            return "calidad/dictamen/cuarentena";
        }

        dictamenCuarentena(movimientoDTO, lote, redirectAttributes);
        return "redirect:/calidad/dictamen/cuarentena-ok";
    }

    @GetMapping("/cuarentena-ok")
    public String exitoDictamenCuarentena(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "calidad/dictamen/cuarentena-ok";
    }

    void dictamenCuarentena(
        final MovimientoDTO dto,
        final Lote lote,
        final RedirectAttributes redirectAttributes) {
        dto.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO loteDTO = DTOUtils.fromLoteEntity(loteService.persistirDictamenCuarentena(dto, lote));
        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);

        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null
                ? "Cambio de calidad a Cuarentena exitoso"
                : "Hubo un error al realizar el cambio de calidad a Cuarentena.");
    }

    void initModelDictamencuarentena(final MovimientoDTO movimientoDTO, final Model model) {
        final List<LoteDTO> lotesDtos = getLotesDtosByCodigoInterno(loteService.findAllForCuarentena());
        model.addAttribute("lotesForCuarentena", lotesDtos);
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

}

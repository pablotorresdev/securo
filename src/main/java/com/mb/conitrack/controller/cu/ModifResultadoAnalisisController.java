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

import com.mb.conitrack.dto.AnalisisDTO;
import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.utils.ControllerUtils;

import jakarta.validation.Valid;

import static com.mb.conitrack.dto.DTOUtils.fromAnalisisEntities;
import static com.mb.conitrack.dto.DTOUtils.fromLoteEntities;

@Controller
@RequestMapping("/calidad/analisis")
public class ModifResultadoAnalisisController {

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

    //***************************** CU5/6 Resultado Analisis************************************
    // CU5: Resultado QA Aprobado
    // CU6: Resultado QA Rechazado
    // @PreAuthorize("hasAuthority('ROLE_CONTROL_CALIDAD')")
    @GetMapping("/resultado-analisis")
    public String showResultadoAnalisisForm(@ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        //TODO: implementar el filtro correcto en base a Dictamen y Analisis (Fecha, Dictamen)
        //TODO: pasar a DTO
        initModelResultadoAnalisis(movimientoDTO, model);
        return "calidad/analisis/resultado-analisis";
    }

    @PostMapping("/resultado-analisis")
    public String procesarResultadoAnalisis(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {
        if (!validarResultadoAnalisisInput(movimientoDTO, bindingResult)) {
            initModelResultadoAnalisis(movimientoDTO, model);
            return "calidad/analisis/resultado-analisis";
        }

        resultadoAnalisis(movimientoDTO, redirectAttributes);
        return "redirect:/calidad/analisis/resultado-analisis-ok";
    }

    @GetMapping("/resultado-analisis-ok")
    public String exitoResultadoAnalisis(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "calidad/analisis/resultado-analisis-ok";
    }

    private void initModelResultadoAnalisis(final MovimientoDTO movimientoDTO, final Model model) {
        final List<LoteDTO> lotesDTOs = fromLoteEntities(loteService.findAllForResultadoAnalisis());
        List<AnalisisDTO> analisisDTOs = fromAnalisisEntities(analisisService.findAllEnCursoForLotesCuarentena());
        movimientoDTO.setFechaMovimiento(LocalDateTime.now().toLocalDate());
        model.addAttribute("movimientoDTO", movimientoDTO);
        model.addAttribute("loteDTOs", lotesDTOs);
        model.addAttribute("analisisDTOs", analisisDTOs);
        model.addAttribute("resultados", List.of(DictamenEnum.APROBADO, DictamenEnum.RECHAZADO));
    }

    private void resultadoAnalisis(final MovimientoDTO movimientoDTO, final RedirectAttributes redirectAttributes) {
        movimientoDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO loteDTO = DTOUtils.fromLoteEntity(loteService.persistirResultadoAnalisis(movimientoDTO));

        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null
                ? ("Cambio de dictamen a " + movimientoDTO.getDictamenFinal() + " exitoso")
                : "Hubo un error con el cambio de dictamen.");
    }

    private boolean validarResultadoAnalisisInput(
        final MovimientoDTO movimientoDTO,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        Lote lote = null;
        boolean success = controllerUtils().validarDatosMandatoriosResultadoAnalisisInput(movimientoDTO, bindingResult);
        success = success && controllerUtils().validarDatosResultadoAnalisisAprobadoInput(movimientoDTO, bindingResult);

        if (success) {
            lote = controllerUtils().getLoteByCodigoInterno(
                movimientoDTO.getCodigoInternoLote(),
                bindingResult,
                loteService);
        }
        success = success && lote != null;
        success = success && controllerUtils().validarExisteMuestreoParaAnalisis(movimientoDTO, lote, bindingResult);
        success = success &&
            controllerUtils()
                .validarFechaMovimientoPosteriorIngresoLote(movimientoDTO, lote, bindingResult);
        success = success &&
            controllerUtils()
                .validarFechaAnalisisPosteriorIngresoLote(movimientoDTO, lote, bindingResult);
        success = success &&
            controllerUtils().validarContraFechasProveedor(movimientoDTO, lote, bindingResult);
        if (DictamenEnum.RECHAZADO != movimientoDTO.getDictamenFinal()) {
            success = success && controllerUtils().validarValorTitulo(movimientoDTO, lote, bindingResult);
        }
        return success;
    }

}

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

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.LoteService;

import jakarta.validation.Valid;

import static com.mb.conitrack.dto.DTOUtils.fromLoteEntities;

@Controller
@RequestMapping("/calidad/reanalisis")
public class ModifReanalisisProductoController extends AbstractCuController {

    @Autowired
    private LoteService loteService;

    @Autowired
    private AnalisisService analisisService;

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    //***************************** CUZ Reanalisis de Producto Aprobado************************************
    // CUZ: Reanalisis de Producto Aprobado
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_CALIDAD')")
    @GetMapping("/inicio-reanalisis")
    public String showReanalisisProductoForm(@ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        initModelReanalisisProducto(movimientoDTO, model);
        return "calidad/reanalisis/inicio-reanalisis";
    }

    @PostMapping("/inicio-reanalisis")
    public String procesarReanalisisProducto(
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
        success = success &&
            controllerUtils().validarFechaMovimientoPosteriorIngresoLote(
                movimientoDTO,
                lote,
                bindingResult);
        success = success &&
            controllerUtils().validarFechaAnalisisPosteriorIngresoLote(movimientoDTO, lote, bindingResult);

        if (!success) {
            initModelReanalisisProducto(movimientoDTO, model);
            model.addAttribute("movimientoDTO", movimientoDTO);
            return "calidad/reanalisis/inicio-reanalisis";
        }

        reanalisisProducto(movimientoDTO, lote, redirectAttributes);
        return "redirect:/calidad/reanalisis/inicio-reanalisis-ok";
    }

    @GetMapping("/inicio-reanalisis-ok")
    public String exitoReanalisisProducto(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "calidad/reanalisis/inicio-reanalisis-ok";
    }

    private void initModelReanalisisProducto(final MovimientoDTO movimientoDTO, final Model model) {
        //TODO: implementar el filtro correcto en base a calidad y Analisis (Fecha, calidad)
        final List<LoteDTO> lotesDtos = fromLoteEntities(loteService.findAllForReanalisisProducto());
        model.addAttribute("loteDTOs", lotesDtos);
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    private void reanalisisProducto(
        final MovimientoDTO dto,
        final Lote lote,
        final RedirectAttributes redirectAttributes) {
        dto.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO loteDTO = dtoUtils().fromLoteEntity(loteService.persistirReanalisisProducto(dto, lote));
        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null
                ? "Anilisis asignado con éxito: " + loteDTO.getUltimoNroAnalisisDto()
                : "Hubo un error al asignar el analisis.");
    }

}

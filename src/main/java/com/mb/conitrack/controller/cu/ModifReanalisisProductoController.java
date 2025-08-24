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
import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.cu.ModifReanalisisProductoService;

import jakarta.validation.Valid;

import static com.mb.conitrack.dto.DTOUtils.fromLoteEntities;

@Controller
@RequestMapping("/calidad/reanalisis")
public class ModifReanalisisProductoController extends AbstractCuController {

    @Autowired
    private ModifReanalisisProductoService reanalisisProductoService;

    @Autowired
    private AnalisisService analisisService;

    @Autowired
    private LoteService loteService;

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

        if (!reanalisisProductoService.validarReanalisisProducto(movimientoDTO, bindingResult)) {
            initModelReanalisisProducto(movimientoDTO, model);
            model.addAttribute("movimientoDTO", movimientoDTO);
            return "calidad/reanalisis/inicio-reanalisis";
        }

        procesarReanalisisProducto(movimientoDTO, redirectAttributes);
        return "redirect:/calidad/reanalisis/inicio-reanalisis-ok";
    }

    @GetMapping("/inicio-reanalisis-ok")
    public String exitoReanalisisProducto(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "calidad/reanalisis/inicio-reanalisis-ok";
    }

    private void initModelReanalisisProducto(final MovimientoDTO movimientoDTO, final Model model) {
        //TODO: implementar el filtro correcto en base a calidad y Analisis (Fecha, calidad)
        final List<LoteDTO> lotesDtos = loteService.findAllForReanalisisProductoDTOs();
        model.addAttribute("loteReanalisisDTOs", lotesDtos);
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    private void procesarReanalisisProducto(
        final MovimientoDTO dto,
        final RedirectAttributes redirectAttributes) {

        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO loteDTO = reanalisisProductoService.persistirReanalisisProducto(dto);
        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);

        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null
                ? "Anilisis asignado con éxito: " + loteDTO.getUltimoNroAnalisisDto()
                : "Hubo un error al asignar el analisis.");
    }

}

package com.mb.conitrack.controller.cu;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.QueryServiceLote;
import com.mb.conitrack.utils.ControllerUtils;

import jakarta.validation.Valid;

import static com.mb.conitrack.controller.cu.AbstractCuController.controllerUtils;
import static com.mb.conitrack.dto.DTOUtils.fromLoteEntities;

@Controller
@RequestMapping("/ventas/liberacion")
public class ModifLiberacionVentasController {

    @Autowired
    private LoteService loteService;

    @Autowired
    private QueryServiceLote queryServiceLote;

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    //***************************** CU1 LIBERACION************************************
    // CU11: Dictamen Lote a liberacion
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_ventas')")
    @GetMapping("/inicio-liberacion")
    public String showLiberacionProductoForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        //TODO: implementar el filtro correcto en base a ventas y Analisis (Fecha, ventas)
        initModelLiberacionProducto(movimientoDTO, model);
        return "ventas/liberacion/inicio-liberacion";
    }

    @PostMapping("/inicio-liberacion")
    public String procesarLiberacionProducto(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {


        Lote lote = controllerUtils().getLoteByCodigoInterno(
            movimientoDTO.getCodigoInternoLote(),
            bindingResult,
            queryServiceLote);

        boolean success = lote != null;
        success = success && ControllerUtils.getInstance()
            .validarFechaMovimientoPosteriorIngresoLote(movimientoDTO, lote, bindingResult);
        success = success &&  ControllerUtils.getInstance()
            .validarFechaAnalisisPosteriorIngresoLote(movimientoDTO, lote, bindingResult);

        if (!success) {
            initModelLiberacionProducto(movimientoDTO, model);
            model.addAttribute("movimientoDTO", movimientoDTO);
            return "ventas/liberacion/inicio-liberacion";
        }

        liberacionProducto(movimientoDTO, lote, redirectAttributes);
        return "redirect:/ventas/liberacion/inicio-liberacion-ok";
    }

    @GetMapping("/inicio-liberacion-ok")
    public String exitoLiberacionProducto(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "ventas/liberacion/inicio-liberacion-ok";
    }

    private void initModelLiberacionProducto(final MovimientoDTO movimientoDTO, final Model model) {
        final List<LoteDTO> loteLiberacionProdDtos = fromLoteEntities(queryServiceLote.findAllForLiberacionProducto());
        //TODO: unificar nombres de atributos
        model.addAttribute("loteLiberacionProdDtos", loteLiberacionProdDtos);
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    private void liberacionProducto(
        final MovimientoDTO dto,
        final Lote lote,
        final RedirectAttributes redirectAttributes) {
        dto.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO loteDTO = DTOUtils.fromLoteEntity(loteService.persistirLiberacionProducto(dto, lote));
        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null
                ? "Liberación de Producto exitosa"
                : "Hubo un error con la liberación del Producto");
    }

}

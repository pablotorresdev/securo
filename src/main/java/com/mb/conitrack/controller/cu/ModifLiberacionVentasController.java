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
import com.mb.conitrack.service.cu.ModifLiberacionVentasService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/ventas/liberacion")
public class ModifLiberacionVentasController extends AbstractCuController {

    @Autowired
    private ModifLiberacionVentasService modifLiberacionVentasService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    //***************************** CU21 LIBERACION************************************
    // CU21: Dictamen Lote a liberacion
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_ventas')")
    @GetMapping("/inicio-liberacion")
    public String showLiberacionProductoForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        initModelLiberacionProducto(movimientoDTO, model);
        return "ventas/liberacion/inicio-liberacion";
    }

    @PostMapping("/inicio-liberacion")
    public String liberacionProducto(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!modifLiberacionVentasService.validarLiberacionProductoInput(movimientoDTO, bindingResult)) {
            initModelLiberacionProducto(movimientoDTO, model);
            return "ventas/liberacion/inicio-liberacion";
        }

        procesarLiberacionProducto(movimientoDTO, redirectAttributes);
        return "redirect:/ventas/liberacion/inicio-liberacion-ok";
    }

    @GetMapping("/inicio-liberacion-ok")
    public String exitoLiberacionProducto(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "ventas/liberacion/inicio-liberacion-ok";
    }

    private void initModelLiberacionProducto(final MovimientoDTO movimientoDTO, final Model model) {
        final List<LoteDTO> loteLiberacionProdDtos = loteService.findAllForLiberacionProductoDTOs();
        model.addAttribute("loteLiberacionProdDtos", loteLiberacionProdDtos);
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    private void procesarLiberacionProducto(
        final MovimientoDTO dto,
        final RedirectAttributes redirectAttributes) {

        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO loteDTO = modifLiberacionVentasService.persistirLiberacionProducto(dto);

        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null
                ? "Liberación de Producto exitosa"
                : "Hubo un error con la liberación del Producto");
    }

}

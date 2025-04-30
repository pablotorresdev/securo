package com.mb.conitrack.controller;

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

import jakarta.validation.Valid;

import static com.mb.conitrack.controller.ControllerUtils.populateLoteListByCodigoInterno;
import static com.mb.conitrack.controller.ControllerUtils.validarFechaMovimientoPosteriorLote;
import static com.mb.conitrack.dto.DTOUtils.getLotesDtosByCodigoInterno;

@Controller
@RequestMapping("/ventas")
public class VentasController {

    @Autowired
    private LoteService loteService;

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    //***************************** CU3 Muestreo************************************
    // CU2: Dictamen Lote a liberacion
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_ventas')")
    @GetMapping("/liberacion-producto")
    public String showliberacionProductoForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        //TODO: implementar el filtro correcto en base a ventas y Analisis (Fecha, ventas)
        initModelLiberacionProducto(movimientoDTO, model);
        return "ventas/liberacion-producto";
    }

    @PostMapping("/liberacion-producto")
    public String procesarLiberacionProducto(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        final List<Lote> lotesList = new ArrayList<>();
        boolean success = populateLoteListByCodigoInterno(
            lotesList,
            movimientoDTO.getCodigoInterno(),
            bindingResult,
            loteService)
            && validarFechaMovimientoPosteriorLote(movimientoDTO, lotesList.get(0), bindingResult);

        if (!success) {
            initModelLiberacionProducto(movimientoDTO, model);
            model.addAttribute("movimientoDTO", movimientoDTO);
            return "ventas/liberacion-producto";
        }

        liberacionProducto(movimientoDTO, lotesList, redirectAttributes);
        return "redirect:/ventas/liberacion-producto-ok";
    }

    @GetMapping("/liberacion-producto-ok")
    public String exitoLiberacionProducto(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "ventas/liberacion-producto-ok";
    }

    private void initModelLiberacionProducto(final MovimientoDTO movimientoDTO, final Model model) {
        final List<LoteDTO> lotesDtos = getLotesDtosByCodigoInterno(loteService.findAllForLiberacionProducto());
        //TODO: unificar nombres de atributos
        model.addAttribute("lotesDtos", lotesDtos);
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    private void liberacionProducto(
        final MovimientoDTO dto,
        final List<Lote> lotesList,
        final RedirectAttributes redirectAttributes) {
        dto.setFechaYHoraCreacion(LocalDateTime.now());
        final List<Lote> lotes = loteService.persistirLiberacionProducto(dto, lotesList);
        final LoteDTO loteDTO = DTOUtils.mergeEntities(lotes);
        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null
                ? "Liberación de Producto exitosa"
                : "Hubo un error con la liberación del Producto");
    }

}

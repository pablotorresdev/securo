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

import static com.mb.conitrack.controller.ControllerUtils.populateAvailableLoteListByCodigoInterno;
import static com.mb.conitrack.controller.ControllerUtils.populateLoteListByCodigoInterno;
import static com.mb.conitrack.controller.ControllerUtils.validarCantidadesPorMedidas;
import static com.mb.conitrack.controller.ControllerUtils.validarFechaEgresoLoteDtoPosteriorLote;
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
    public String showLiberacionProductoForm(
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

    //***************************** CU12 Venta de Producto Propio************************************
    // CU2: Venta de Producto Propio
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/venta-producto")
    public String showVentaProductoForm(
        @ModelAttribute LoteDTO loteDTO, Model model) {
        initModelVentaProducto(loteDTO, model);
        return "ventas/venta-producto";
    }

    @PostMapping("/venta-producto")
    public String procesarVentaProducto(
        @Valid @ModelAttribute LoteDTO loteDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!validarVentaProductoInput(loteDTO, bindingResult)) {
            initModelVentaProducto(loteDTO, model);
            return "ventas/venta-producto";
        }

        ventaProducto(loteDTO, redirectAttributes);
        return "redirect:/ventas/venta-producto-ok";
    }

    @GetMapping("/venta-producto-ok")
    public String exitoVentaProducto(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "ventas/venta-producto-ok";
    }

    private void initModelVentaProducto(final LoteDTO loteDTO, final Model model) {
        List<LoteDTO> lotesVenta = getLotesDtosByCodigoInterno(loteService.findAllForVentaProducto());
        model.addAttribute("lotesVenta", lotesVenta);
        model.addAttribute("loteDTO", loteDTO);
    }

    private boolean validarVentaProductoInput(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        //TODO: analizar validacion para ventas, ahora se copio la de consumo produccion
        final List<Lote> lotes = new ArrayList<>();
        return populateAvailableLoteListByCodigoInterno(lotes, loteDTO.getCodigoInterno(), bindingResult, loteService)
            && validarFechaEgresoLoteDtoPosteriorLote(loteDTO, lotes.get(0), bindingResult)
            && validarCantidadesPorMedidas(loteDTO, lotes, bindingResult);
    }

    private void ventaProducto(final LoteDTO loteDTO, final RedirectAttributes redirectAttributes) {
        loteDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO resultDTO = DTOUtils.mergeEntities(loteService.registrarVentaProducto(loteDTO));

        //TODO: se puede remover esto?
        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null
                ? "Venta de producto " + loteDTO.getNombreProducto() + " exitosa"
                : "Hubo un error en la venta de producto.");
    }

}

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
import com.mb.conitrack.utils.ControllerUtils;

import jakarta.validation.Valid;

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

    //***************************** CU1 LIBERACION************************************
    // CU11: Dictamen Lote a liberacion
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
        boolean success = ControllerUtils.getInstance().populateLoteListByCodigoInterno(
            lotesList,
            movimientoDTO.getCodigoInternoLote(),
            bindingResult,
            loteService)
            &&
            ControllerUtils.getInstance()
                .validarFechaMovimientoPosteriorLote(movimientoDTO, lotesList.get(0), bindingResult);

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

    //***************************** CU12 Venta de Producto Propio************************************
    // CU12: Venta de Producto Propio
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

    //***************************** CU3 Muestreo************************************
    // CU13: Devolución de cliente
    // @PreAuthorize("hasAuthority('ROLE_GERENTE_GARANTIA')")
    @GetMapping("/devolucion-venta")
    public String showDevolucionVentaForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        //TODO: implementar el filtro correcto en base a ventas y Analisis (Fecha, ventas)
        initModelDevolucionVenta(movimientoDTO, model);
        return "ventas/devolucion-venta";
    }

    @PostMapping("/devolucion-venta")
    public String procesarDevolucionVenta(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        final boolean success = validateCantidadDevolucion(movimientoDTO, bindingResult);

        if (!success) {
            initModelDevolucionVenta(movimientoDTO, model);
            model.addAttribute("movimientoDTO", movimientoDTO);
            return "ventas/devolucion-venta";
        }
        devolucionVenta(movimientoDTO, redirectAttributes);
        return "redirect:/ventas/devolucion-venta-ok";
    }

    @GetMapping("/devolucion-venta-ok")
    public String exitoDevolucionVenta(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "ventas/devolucion-venta-ok";
    }

    private void devolucionVenta(
        final @Valid MovimientoDTO movimientoDTO,
        final RedirectAttributes redirectAttributes) {

        movimientoDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO resultDTO = DTOUtils.mergeEntities(loteService.altaStockDevolucionVenta(movimientoDTO));

        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null
                ? "Ingreso de stock por devolución exitoso."
                : "Hubo un error en el ingreso de stock por  devolución.");
    }

    private void initModelDevolucionVenta(final MovimientoDTO movimientoDTO, final Model model) {
        List<LoteDTO> lotesDevolucion = getLotesDtosByCodigoInterno(loteService.findAllForDevolucionVenta());
        model.addAttribute("lotesDevolucion", lotesDevolucion);
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    private void initModelLiberacionProducto(final MovimientoDTO movimientoDTO, final Model model) {
        final List<LoteDTO> lotesDtos = getLotesDtosByCodigoInterno(loteService.findAllForLiberacionProducto());
        //TODO: unificar nombres de atributos
        model.addAttribute("lotesDtos", lotesDtos);
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    private void initModelVentaProducto(final LoteDTO loteDTO, final Model model) {
        List<LoteDTO> lotesVenta = getLotesDtosByCodigoInterno(loteService.findAllForVentaProducto());
        model.addAttribute("lotesVenta", lotesVenta);
        model.addAttribute("loteDTO", loteDTO);
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

    private boolean validarVentaProductoInput(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        //TODO: analizar validacion para ventas, ahora se copio la de consumo produccion
        final List<Lote> lotes = new ArrayList<>();
        return ControllerUtils.getInstance().populateAvailableLoteListByCodigoInterno(
            lotes,
            loteDTO.getCodigoInternoLote(),
            bindingResult,
            loteService)
            &&
            ControllerUtils.getInstance().validarFechaEgresoLoteDtoPosteriorLote(loteDTO, lotes.get(0), bindingResult)
            &&
            ControllerUtils.getInstance().validarCantidadesPorMedidas(loteDTO, lotes, bindingResult);
    }

    private boolean validateCantidadDevolucion(
        final @Valid MovimientoDTO movimientoDTO,
        final BindingResult bindingResult) {
        return true;
    }

    private void ventaProducto(final LoteDTO loteDTO, final RedirectAttributes redirectAttributes) {
        loteDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO resultDTO = DTOUtils.mergeEntities(loteService.bajaVentaProducto(loteDTO));

        //TODO: se puede remover esto?
        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute("trazasMuestreo", loteDTO.getTrazaDTOs());
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null
                ? "Venta de producto " + loteDTO.getNombreProducto() + " exitosa"
                : "Hubo un error en la venta de producto.");
    }

}

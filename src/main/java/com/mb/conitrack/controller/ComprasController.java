package com.mb.conitrack.controller;

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
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.dto.validation.AltaCompra;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.ProductoService;
import com.mb.conitrack.service.ProveedorService;

import jakarta.validation.Valid;

import static com.mb.conitrack.controller.ControllerUtils.getCountryList;
import static com.mb.conitrack.controller.ControllerUtils.populateLoteListByCodigoInterno;
import static com.mb.conitrack.controller.ControllerUtils.validarBultos;
import static com.mb.conitrack.controller.ControllerUtils.validarFechaMovimientoPosteriorLote;
import static com.mb.conitrack.controller.ControllerUtils.validateCantidadIngreso;
import static com.mb.conitrack.controller.ControllerUtils.validateFechasProveedor;
import static com.mb.conitrack.dto.DTOUtils.getLotesDtosByCodigoInterno;

@Controller
@RequestMapping("/compras")
public class ComprasController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private LoteService loteService;

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    // CU1 Ingreso por compra *****************************************************
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/ingreso-compra")
    public String showIngresoCompra(@ModelAttribute("loteDTO") LoteDTO loteDTO, Model model) {
        initModelIngresoCompra(loteDTO, model);
        return "compras/ingreso-compra";
    }

    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @PostMapping("/ingreso-compra")
    public String ingresoCompra(
        @Validated(AltaCompra.class) @ModelAttribute("loteDTO") LoteDTO loteDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        final boolean success = validateCantidadIngreso(loteDTO, bindingResult)
            && validateFechasProveedor(loteDTO, bindingResult)
            && validarBultos(loteDTO, bindingResult);

        if (!success) {
            initModelIngresoCompra(loteDTO, model);
            return "compras/ingreso-compra";
        }

        ingresoCompra(loteDTO, redirectAttributes);
        return "redirect:/compras/ingreso-compra-ok";
    }

    @GetMapping("/ingreso-compra-ok")
    public String exitoIngresoCompra(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "compras/ingreso-compra-ok"; // Template Thymeleaf
    }

    // CU4: Baja por Devolución Compra *****************************************************
    //TODO: En el caso que existieran más de un bulto, el sistema solicitará ingresar las cantidades individuales para cada bulto, bultos completos o devolución completa.
    // Esto afectara a cada bulto independientemente o a todo el lote, respectivamente.
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/devolucion-compra")
    public String showDevolucionCompraForm(
        @ModelAttribute("movimientoDTO") MovimientoDTO movimientoDTO, Model model) {
        initModelDevolucionCompra(model);
        return "compras/devolucion-compra";
    }

    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @PostMapping("/devolucion-compra")
    public String procesarDevolucionCompra(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        final List<Lote> lotesList = new ArrayList<>();
        if (!(populateLoteListByCodigoInterno(lotesList, movimientoDTO.getCodigoInterno(), bindingResult, loteService) &&
            validarFechaMovimientoPosteriorLote(movimientoDTO, lotesList.get(0), bindingResult))) {
            initModelDevolucionCompra(model);
            model.addAttribute("movimientoDTO", movimientoDTO);
            return "compras/devolucion-compra";
        }

        devolucionCompra(movimientoDTO, lotesList, redirectAttributes);
        return "redirect:/compras/devolucion-compra-ok";
    }

    @GetMapping("/devolucion-compra-ok")
    public String exitoMuestreo(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "compras/devolucion-compra-ok";
    }

    private void devolucionCompra(final MovimientoDTO movimientoDTO, final List<Lote> lotesList, final RedirectAttributes redirectAttributes) {
        movimientoDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO resultDTO = DTOUtils.mergeEntities(loteService.persistirDevolucionCompra(movimientoDTO, lotesList));
        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null ? "Ingreso de stock por compra exitoso." : "Hubo un error en el ingreso de stock por compra.");
        redirectAttributes.addFlashAttribute("success", "Devolucion realizada correctamente.");
    }

    private void ingresoCompra(final LoteDTO loteDTO, final RedirectAttributes redirectAttributes) {
        loteDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO resultDTO = DTOUtils.mergeEntities(loteService.ingresarStockPorCompra(loteDTO));
        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null ? "Ingreso de stock por compra exitoso." : "Hubo un error en el ingreso de stock por compra.");
    }

    private void initModelDevolucionCompra(final Model model) {
        model.addAttribute("lotesDevolvibles", getLotesDtosByCodigoInterno(loteService.findAllForDevolucionCompra()));
    }

    private void initModelIngresoCompra(final LoteDTO loteDTO, final Model model) {
        model.addAttribute("productos", productoService.getProductosExternos());
        model.addAttribute("proveedores", proveedorService.getProveedoresExternos());

        if (loteDTO.getCantidadesBultos() == null) {
            loteDTO.setCantidadesBultos(new ArrayList<>());
        }
        if (loteDTO.getUnidadMedidaBultos() == null) {
            loteDTO.setUnidadMedidaBultos(new ArrayList<>());
        }
        model.addAttribute("loteDTO", loteDTO);
        model.addAttribute("paises", getCountryList());
    }

}

package com.mb.conitrack.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

import static com.mb.conitrack.controller.ControllerUtils.validarBultos;
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

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    // CU1 Ingreso por compra *****************************************************
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/ingreso-compra")
    public String showIngresoCompra(@ModelAttribute("loteDTO") LoteDTO loteDTO, Model model) {
        setupModelIngresoCompra(model, loteDTO);
        return "compras/ingreso-compra";
    }

    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @PostMapping("/ingreso-compra")
    public String ingresoCompra(
        @Validated(AltaCompra.class) @ModelAttribute("loteDTO") LoteDTO loteDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        validateCantidadIngreso(loteDTO, bindingResult);
        validateFechasProveedor(loteDTO, bindingResult);
        validarBultos(loteDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            setupModelIngresoCompra(model, loteDTO);
            return "compras/ingreso-compra";
        }

        ingresoCompra(loteDTO, redirectAttributes);
        return "redirect:/compras/ingreso-compra-ok";
    }

    @GetMapping("/ingreso-compra-ok")
    public String exitoIngresoCompra(
        @ModelAttribute("newLoteDTO") LoteDTO loteDTO, Model model) {
        if (loteDTO.getNombreProducto() == null) {
            return "redirect:/compras/cancelar";
        }

        model.addAttribute("loteDTO", loteDTO);
        model.addAttribute("movimientos", loteDTO.getMovimientoDTOs());

        return "compras/ingreso-compra-ok"; // Template Thymeleaf
    }

    // CU4: Baja por Devolución Compra *****************************************************
    //TODO: En el caso que existieran más de un bulto, el sistema solicitará ingresar las cantidades individuales
    // para cada bulto, bultos completos o devolución completa.
    // Esto afectara a cada bulto independientemente o a todo el lote, respectivamente.
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/devolucion-compra")
    public String showDevolucionCompraForm(
        @ModelAttribute("movimientoDTO") MovimientoDTO movimientoDTO, Model model) {
        model.addAttribute("lotesDevolvibles", getLotesDtosByCodigoInterno(loteService.findAllForDevolucionCompra()));
        return "compras/devolucion-compra";
    }

    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @PostMapping("/devolucion-compra")
    public String procesarDevolucionCompra(
        @Valid @ModelAttribute MovimientoDTO dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("lotesDevolvibles", getLotesDtosByCodigoInterno(loteService.findAllForDevolucionCompra()));
            return "compras/devolucion-compra";
        }

        dto.setFechaYHoraCreacion(LocalDateTime.now());
        final List<Lote> lotes = loteService.persistirDevolucionCompra(dto, dto.getCodigoInterno());

        if (lotes.isEmpty()) {
            model.addAttribute("lotesDevolvibles", getLotesDtosByCodigoInterno(loteService.findAllForDevolucionCompra()));
            bindingResult.reject("error", "Error al persistir la devoluciono.");
            return "compras/devolucion-compra";
        }

        redirectAttributes.addFlashAttribute("loteDTO", DTOUtils.fromEntities(lotes));
        redirectAttributes.addFlashAttribute("success", "Devolucion realizada correctamente.");
        return "redirect:/compras/devolucion-compra-ok";
    }

    @GetMapping("/devolucion-compra-ok")
    public String exitoMuestreo(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "compras/devolucion-compra-ok";
    }

    private void ingresoCompra(final LoteDTO loteDTO, final RedirectAttributes redirectAttributes) {
        loteDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final List<Lote> lotes = loteService.ingresarStockPorCompra(loteDTO);
        redirectAttributes.addFlashAttribute("newLoteDTO", DTOUtils.fromEntities(lotes));
        redirectAttributes.addFlashAttribute("success", "Ingreso de stock por compra exitoso.");
    }

    private void setupModelIngresoCompra(final Model model, final LoteDTO loteDTO) {
        model.addAttribute("productos", productoService.getProductosExternos());
        model.addAttribute("proveedores", proveedorService.getProveedoresExternos());

        if (loteDTO.getCantidadesBultos() == null) {
            loteDTO.setCantidadesBultos(new ArrayList<>());
        }
        if (loteDTO.getUnidadMedidaBultos() == null) {
            loteDTO.setUnidadMedidaBultos(new ArrayList<>());
        }
        model.addAttribute("loteDTO", loteDTO);

        String[] countryCodes = Locale.getISOCountries();
        List<String> countries = new ArrayList<>();
        for (String code : countryCodes) {
            Locale locale = new Locale("", code);
            countries.add(locale.getDisplayCountry());
        }
        countries.sort(String::compareTo);
        model.addAttribute("paises", countries);
    }

}

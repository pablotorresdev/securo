package com.mb.conitrack.controller.cu;

import java.time.OffsetDateTime;
import java.util.ArrayList;

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

import java.util.Arrays;
import java.util.stream.Collectors;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.validation.AltaCompra;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.service.cu.AltaIngresoCompraService;

/** CU1 - Controller para alta de lote por ingreso de compra externa. */
@Controller
@RequestMapping("/compras/alta")
public class AltaIngresoCompraController extends AbstractCuController {

    @Autowired
    private AltaIngresoCompraService ingresoCompraService;

    /** Cancela operación y redirige a home. */
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    /** Muestra formulario de ingreso de compra (productos, proveedores, países). */
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/ingreso-compra")
    public String showIngresoCompra(@ModelAttribute("loteDTO") LoteDTO loteDTO, Model model) {
        initModelIngresoCompra(loteDTO, model);
        return "compras/alta/ingreso-compra";
    }

    /** Muestra pantalla de confirmación con preview de datos antes de guardar. */
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @PostMapping("/ingreso-compra/confirm")
    public String confirmarIngresoCompra(
        @Validated(AltaCompra.class) @ModelAttribute("loteDTO") LoteDTO loteDTO,
        BindingResult bindingResult,
        Model model) {

        if (!ingresoCompraService.validarIngresoCompraInput(loteDTO, bindingResult)) {
            initModelIngresoCompra(loteDTO, model);
            return "compras/alta/ingreso-compra";
        }

        // Resolver nombres desde IDs para mostrar en la confirmación
        resolverNombresParaConfirmacion(loteDTO);

        model.addAttribute("loteDTO", loteDTO);
        return "compras/alta/ingreso-compra-confirm";
    }

    /** Procesa formulario: valida datos, crea lote con bultos y movimiento ALTA/COMPRA. */
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @PostMapping("/ingreso-compra")
    public String ingresoCompra(
        @Validated(AltaCompra.class) @ModelAttribute("loteDTO") LoteDTO loteDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!ingresoCompraService.validarIngresoCompraInput(loteDTO, bindingResult)) {
            initModelIngresoCompra(loteDTO, model);
            return "compras/alta/ingreso-compra";
        }

        procesarIngresoCompra(loteDTO, redirectAttributes);
        return "redirect:/compras/alta/ingreso-compra-ok";
    }

    /** Resuelve nombres de producto, proveedor y fabricante desde IDs para mostrar en confirmación. */
    private void resolverNombresParaConfirmacion(LoteDTO loteDTO) {
        // Resolver nombre del producto
        if (loteDTO.getProductoId() != null) {
            productoService.findById(loteDTO.getProductoId()).ifPresent(producto -> {
                loteDTO.setNombreProducto(producto.getNombreGenerico());
                loteDTO.setCodigoProducto(producto.getCodigoProducto());
            });
        }

        // Resolver nombre del proveedor
        if (loteDTO.getProveedorId() != null) {
            proveedorService.findById(loteDTO.getProveedorId()).ifPresent(proveedor -> {
                loteDTO.setNombreProveedor(proveedor.getRazonSocial());
            });
        }

        // Resolver nombre del fabricante (también es un proveedor)
        if (loteDTO.getFabricanteId() != null) {
            proveedorService.findById(loteDTO.getFabricanteId()).ifPresent(fabricante -> {
                loteDTO.setNombreFabricante(fabricante.getRazonSocial());
            });
        }
    }

    /** Muestra página de éxito con detalles del lote creado. */
    @GetMapping("/ingreso-compra-ok")
    public String exitoIngresoCompra(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "compras/alta/ingreso-compra-ok"; // Template Thymeleaf
    }

    /** Inicializa modelo con productos externos, proveedores y países. */
    void initModelIngresoCompra(final LoteDTO loteDTO, final Model model) {
        model.addAttribute("productos", productoService.getProductosExternos());
        model.addAttribute("proveedores", proveedorService.getProveedoresExternos());

        // Filtrar tipos de producto: excluir SEMIELABORADO y UNIDAD_VENTA
        var tiposProductoFiltrados = Arrays.stream(TipoProductoEnum.values())
            .filter(tipo -> !tipo.esSemiElaborado() && tipo != TipoProductoEnum.UNIDAD_VENTA)
            .collect(Collectors.toList());
        model.addAttribute("tiposProducto", tiposProductoFiltrados);

        if (loteDTO.getCantidadesBultos() == null) {
            loteDTO.setCantidadesBultos(new ArrayList<>());
        }
        if (loteDTO.getUnidadMedidaBultos() == null) {
            loteDTO.setUnidadMedidaBultos(new ArrayList<>());
        }
        model.addAttribute("loteDTO", loteDTO);
        model.addAttribute("paises", ingresoCompraService.getCountryList());
    }

    /** Persiste lote, bultos y movimiento en transacción. Genera código y timestamp. */
    void procesarIngresoCompra(final LoteDTO loteDTO, final RedirectAttributes redirectAttributes) {

        loteDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO resultDTO = ingresoCompraService.altaStockPorCompra(loteDTO);

        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null
                ? "Ingreso de stock por compra exitoso."
                : "Hubo un error en el ingreso de stock por compra.");
    }

}

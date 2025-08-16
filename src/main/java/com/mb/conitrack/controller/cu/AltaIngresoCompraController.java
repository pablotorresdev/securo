package com.mb.conitrack.controller.cu;

import java.time.LocalDateTime;
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

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.validation.AltaCompra;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.ProductoService;
import com.mb.conitrack.service.ProveedorService;
import com.mb.conitrack.utils.ControllerUtils;

@Controller
@RequestMapping("/compras/alta")
public class AltaIngresoCompraController extends AbstractCuController {

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
        initModelIngresoCompra(loteDTO, model);
        return "compras/alta/ingreso-compra";
    }

    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @PostMapping("/ingreso-compra")
    public String ingresoCompra(
        @Validated(AltaCompra.class) @ModelAttribute("loteDTO") LoteDTO loteDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!validarIngresoCompra(loteDTO, bindingResult)) {
            initModelIngresoCompra(loteDTO, model);
            return "compras/alta/ingreso-compra";
        }

        procesaringresoCompra(loteDTO, redirectAttributes);
        return "redirect:/compras/alta/ingreso-compra-ok";
    }

    private static boolean validarIngresoCompra(final LoteDTO loteDTO, final BindingResult bindingResult) {
        boolean success = controllerUtils().validateCantidadIngreso(loteDTO, bindingResult);
        success = success && controllerUtils().validateFechasProveedor(loteDTO, bindingResult);
        success = success && controllerUtils().validarBultos(loteDTO, bindingResult);
        return success;
    }

    @GetMapping("/ingreso-compra-ok")
    public String exitoIngresoCompra(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "compras/alta/ingreso-compra-ok"; // Template Thymeleaf
    }

    void initModelIngresoCompra(final LoteDTO loteDTO, final Model model) {
        model.addAttribute("productos", productoService.getProductosExternos());
        model.addAttribute("proveedores", proveedorService.getProveedoresExternos());

        if (loteDTO.getCantidadesBultos() == null) {
            loteDTO.setCantidadesBultos(new ArrayList<>());
        }
        if (loteDTO.getUnidadMedidaBultos() == null) {
            loteDTO.setUnidadMedidaBultos(new ArrayList<>());
        }
        model.addAttribute("loteDTO", loteDTO);
        model.addAttribute("paises", ControllerUtils.getCountryList());
    }

    void procesaringresoCompra(final LoteDTO loteDTO, final RedirectAttributes redirectAttributes) {
        loteDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO resultDTO = dtoUtils().fromLoteEntity(loteService.altaStockPorCompra(loteDTO));
        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null
                ? "Ingreso de stock por compra exitoso."
                : "Hubo un error en el ingreso de stock por compra.");
    }

}

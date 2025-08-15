package com.mb.conitrack.controller.cu;

import java.time.LocalDateTime;
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
import com.mb.conitrack.service.LoteService;

import jakarta.validation.Valid;

import static com.mb.conitrack.dto.DTOUtils.fromLoteEntities;

@Controller
@RequestMapping("/ventas/alta")
public class AltaDevolucionVentaController {

    @Autowired
    private LoteService loteService;

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    //***************************** CU3 Muestreo************************************
    // CU13: Devolución de cliente
    // @PreAuthorize("hasAuthority('ROLE_GERENTE_GARANTIA')")
    @GetMapping("/devolucion-venta")
    public String showDevolucionVentaForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        //TODO: implementar el filtro correcto en base a ventas y Analisis (Fecha, ventas)
        initModelDevolucionVenta(movimientoDTO, model);
        return "ventas/alta/devolucion-venta";
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
            return "ventas/alta/devolucion-venta";
        }
        devolucionVenta(movimientoDTO, redirectAttributes);
        return "redirect:/ventas/alta/devolucion-venta-ok";
    }

    @GetMapping("/devolucion-venta-ok")
    public String exitoDevolucionVenta(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "ventas/alta/devolucion-venta-ok";
    }

    private void devolucionVenta(
        final @Valid MovimientoDTO movimientoDTO,
        final RedirectAttributes redirectAttributes) {

        movimientoDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO resultDTO = DTOUtils.mergeLoteEntities(loteService.altaStockDevolucionVenta(movimientoDTO));

        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null
                ? "Ingreso de stock por devolución exitoso."
                : "Hubo un error en el ingreso de stock por  devolución.");
    }

    private void initModelDevolucionVenta(final MovimientoDTO movimientoDTO, final Model model) {
        List<LoteDTO> lotesDevolucion = fromLoteEntities(loteService.findAllForDevolucionVenta());
        model.addAttribute("lotesDevolucion", lotesDevolucion);
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    private boolean validateCantidadDevolucion(
        final @Valid MovimientoDTO movimientoDTO,
        final BindingResult bindingResult) {
        return true;
    }

}

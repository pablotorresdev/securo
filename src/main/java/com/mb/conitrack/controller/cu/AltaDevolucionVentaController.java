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
import com.mb.conitrack.service.cu.AltaDevolucionVentaService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/ventas/alta")
public class AltaDevolucionVentaController extends AbstractCuController {

    @Autowired
    private AltaDevolucionVentaService devolucionVentaService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    //***************************** CU23 DEvolucion Cliente************************************
    // CU23: Devolución de cliente
    // @PreAuthorize("hasAuthority('ROLE_GERENTE_GARANTIA')")
    @GetMapping("/devolucion-venta")
    public String showDevolucionVentaForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        initModelDevolucionVenta(movimientoDTO, model);
        return "ventas/alta/devolucion-venta";
    }

    @PostMapping("/devolucion-venta")
    public String devolucionVenta(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!devolucionVentaService.validarDevolucionVentaInput(movimientoDTO, bindingResult)) {
            initModelDevolucionVenta(movimientoDTO, model);
            model.addAttribute("movimientoDTO", movimientoDTO);
            return "ventas/alta/devolucion-venta";
        }
        procesarDevolucionVenta(movimientoDTO, redirectAttributes);
        return "redirect:/ventas/alta/devolucion-venta-ok";
    }

    @GetMapping("/devolucion-venta-ok")
    public String exitoDevolucionVenta(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "ventas/alta/devolucion-venta-ok";
    }

    private void initModelDevolucionVenta(final MovimientoDTO movimientoDTO, final Model model) {
        model.addAttribute("lotesDevolucion", loteService.findAllForDevolucionOrRecallDTOs());
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    private void procesarDevolucionVenta(
        final @Valid MovimientoDTO movimientoDTO,
        final RedirectAttributes redirectAttributes) {

        movimientoDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        final List<LoteDTO> resultDTO = devolucionVentaService.persistirDevolucionVenta(movimientoDTO);

        redirectAttributes.addFlashAttribute("loteDevueltoDTO", resultDTO.get(0));
        redirectAttributes.addFlashAttribute("loteVentaDTO", resultDTO.get(1));
        redirectAttributes.addFlashAttribute("movimientoDTO", movimientoDTO);
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null
                ? "Ingreso de stock por devolución exitoso."
                : "Hubo un error en el ingreso de stock por devolución.");
    }

}

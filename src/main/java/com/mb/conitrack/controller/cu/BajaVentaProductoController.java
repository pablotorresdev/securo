package com.mb.conitrack.controller.cu;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

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
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.service.cu.BajaVentaProductoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/ventas/baja")
public class BajaVentaProductoController extends AbstractCuController {

    @Autowired
    private BajaVentaProductoService ventaProductoService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/venta-producto")
    public String showVentaProductoForm(
        @ModelAttribute LoteDTO loteDTO, Model model) {
        initModelVentaProducto(loteDTO, model);
        return "ventas/baja/venta-producto";
    }

    @PostMapping("/venta-producto")
    public String ventaProducto(
        @Valid @ModelAttribute LoteDTO loteDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!ventaProductoService.validarVentaProductoInput(loteDTO, bindingResult)) {
            initModelVentaProducto(loteDTO, model);
            return "ventas/baja/venta-producto";
        }

        procesarVentaProducto(loteDTO, redirectAttributes);
        return "redirect:/ventas/baja/venta-producto-ok";
    }

    @GetMapping("/venta-producto-ok")
    public String exitoVentaProducto(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "ventas/baja/venta-producto-ok";
    }

    private Map<Integer, List<Long>> getTrazaPorBultoDTOs(final LoteDTO loteDTO) {
        Map<Integer, List<Long>> trazasVentaPorBulto =
            loteDTO.getTrazaDTOs().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    TrazaDTO::getNroBulto,
                    java.util.TreeMap::new, // TreeMap para que los bultos salgan 1,2,3...
                    java.util.stream.Collectors.mapping(
                        TrazaDTO::getNroTraza,
                        java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> {
                                list.sort(java.util.Comparator.naturalOrder());
                                return list;
                            }
                        )
                    )
                ));
        return trazasVentaPorBulto;
    }

    private void initModelVentaProducto(final LoteDTO loteDTO, final Model model) {
        model.addAttribute("loteVentaDTOs", loteService.findAllForVentaProductoDTOs());
        model.addAttribute("loteDTO", loteDTO);
    }

    private void procesarVentaProducto(final LoteDTO loteDTO, final RedirectAttributes redirectAttributes) {
        loteDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO resultDTO = ventaProductoService.bajaVentaProducto(loteDTO);

        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute("trazaVentaDTOs", getTrazaPorBultoDTOs(loteDTO));
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null
                ? "Venta de producto " + loteDTO.getNombreProducto() + " exitosa"
                : "Hubo un error en la venta de producto.");
    }

}

package com.mb.conitrack.controller.cu;

import java.time.LocalDateTime;
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

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.QueryServiceLote;

import jakarta.validation.Valid;

import static com.mb.conitrack.dto.DTOUtils.fromLoteEntities;

@Controller
@RequestMapping("/ventas/baja")
public class BajaVentaProductoController extends AbstractCuController {

    @Autowired
    private LoteService loteService;

    @Autowired
    private QueryServiceLote queryServiceLote;

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    //***************************** CU12 Venta de Producto Propio************************************
    // CU12: Venta de Producto Propio
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/venta-producto")
    public String showVentaProductoForm(
        @ModelAttribute LoteDTO loteDTO, Model model) {
        initModelVentaProducto(loteDTO, model);
        return "ventas/baja/venta-producto";
    }

    @PostMapping("/venta-producto")
    public String procesarVentaProducto(
        @Valid @ModelAttribute LoteDTO loteDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!validarVentaProductoInput(loteDTO, bindingResult)) {
            initModelVentaProducto(loteDTO, model);
            return "ventas/baja/venta-producto";
        }

        ventaProducto(loteDTO, redirectAttributes);
        return "redirect:/ventas/baja/venta-producto-ok";
    }

    @GetMapping("/venta-producto-ok")
    public String exitoVentaProducto(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "ventas/baja/venta-producto-ok";
    }

    private void initModelVentaProducto(final LoteDTO loteDTO, final Model model) {
        List<LoteDTO> loteVentaDTOs = fromLoteEntities(queryServiceLote.findAllForVentaProducto());
        model.addAttribute("loteVentaDTOs", loteVentaDTOs);
        model.addAttribute("loteDTO", loteDTO);
    }

    private boolean validarVentaProductoInput(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        Lote lote = controllerUtils().getLoteByCodigoInterno(
            loteDTO.getCodigoInternoLote(),
            bindingResult,
            queryServiceLote);

        boolean success = lote != null;
        success = success && controllerUtils().validarUnidadMedidaVenta(loteDTO, lote, bindingResult);
        success = success && controllerUtils().validarFechaEgresoLoteDtoPosteriorLote(loteDTO, lote, bindingResult);
        return success && controllerUtils().validarCantidadesPorMedidas(loteDTO, lote, bindingResult);
    }

    private void ventaProducto(final LoteDTO loteDTO, final RedirectAttributes redirectAttributes) {
        loteDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO resultDTO = DTOUtils.fromLoteEntity(loteService.bajaVentaProducto(loteDTO));

        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute("trazaVentaDTOs", getTrazaPorBultoDTOs(loteDTO));
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null
                ? "Venta de producto " + loteDTO.getNombreProducto() + " exitosa"
                : "Hubo un error en la venta de producto.");
    }

    private static Map<Integer, List<Long>> getTrazaPorBultoDTOs(final LoteDTO loteDTO) {

        Map<Integer, List<Long>> trazasVentaPorBulto =
            loteDTO.getTrazaDTOs().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    TrazaDTO::getNroBulto,
                    java.util.TreeMap::new, // TreeMap para que los bultos salgan 1,2,3...
                    java.util.stream.Collectors.mapping(
                        TrazaDTO::getNroTraza,
                        java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> { list.sort(java.util.Comparator.naturalOrder()); return list; }
                        )
                    )
                ));

        return trazasVentaPorBulto;
    }

}

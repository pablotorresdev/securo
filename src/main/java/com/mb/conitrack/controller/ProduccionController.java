package com.mb.conitrack.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.service.LoteService;

import static com.mb.conitrack.dto.DTOUtils.getLotesDtosByCodigoInterno;

@Controller
@RequestMapping("/produccion")
public class ProduccionController {

    @Autowired
    private LoteService loteService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    // CU7: Baja por Consumo Producción
    // @PreAuthorize("hasAuthority('ROLE_SUPERVISOR_PLANTA')")
    @GetMapping("/consumo-produccion")
    public String showConsumoProduccionForm(@ModelAttribute LoteDTO loteDTO, Model model) {
        List<LoteDTO> lotesProduccion = getLotesDtosByCodigoInterno(loteService.findAllForConsumoProduccion());
        model.addAttribute("lotesProduccion", lotesProduccion);
        model.addAttribute("loteDTO", loteDTO);
        return "produccion/consumo-produccion";
    }

    // CU7: Baja por Consumo Producción
    // @PreAuthorize("hasAuthority('ROLE_SUPERVISOR_PLANTA')")
    @PostMapping("/consumo-produccion")
    public String procesarConsumoProduccion(@ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        return "calidad/muestreo-bulto";
    }

    // CU11: Ingreso de Stock por Producción Interna
    // @PreAuthorize("hasAuthority('ROLE_DT')")
    @GetMapping("/cu11")
    public String ingresoProduccionInterna() {
        return "/";
    }

    // CU12: Liberación de Unidad de Venta
    // @PreAuthorize("hasAuthority('ROLE_GERENTE_GARANTIA')")
    @GetMapping("/cu12")
    public String liberacionUnidadVenta() {
        return "/";
    }

    // CU13: Baja por Venta de Producto Propio
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/cu13")
    public String ventaProducto() {
        return "/";
    }

}

package com.mb.conitrack.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.service.LoteService;

import jakarta.validation.Valid;

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

    /** CU‑7 : Baja por Consumo‑Producción
     *  – Valida TODO antes de persistir el movimiento                            */
    @PostMapping("/consumo-produccion")
    public String procesarConsumoProduccion(@Valid @ModelAttribute LoteDTO loteDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes ra) {

        if (loteDTO.getCodigoInterno() == null || loteDTO.getCodigoInterno().isBlank()) {
            bindingResult.rejectValue("codigoInterno", "", "Debe seleccionar un lote");
        }

        if (loteDTO.getFechaEgreso() == null) {
            bindingResult.rejectValue("fechaEgreso", "", "La fecha de consumo es obligatoria");
        } else if (loteDTO.getFechaEgreso().isAfter(LocalDate.now())) {
            bindingResult.rejectValue("fechaEgreso", "", "La fecha de consumo no puede ser futura");
        }

        if (loteDTO.getOrdenProduccion() == null || loteDTO.getOrdenProduccion().isBlank()) {
            bindingResult.rejectValue("ordenProduccion", "", "La orden de producción es obligatoria");
        }

        /* ───────────────────────── 2. Validaciones de bultos / cantidades ───────────────────────── */
        // Busca el lote y la disponibilidad real
        List<Lote> lotes = null;
        if (!bindingResult.hasErrors()) {
            lotes = loteService.findLoteListByCodigoInterno(loteDTO.getCodigoInterno());
            if (lotes.isEmpty()) {
                bindingResult.rejectValue("codigoInterno", "", "Lote inexistente");
            }
        }

        // Debe venir al menos un movimiento
        List<MovimientoDTO> movs = loteDTO.getMovimientoDTOs();
        if (movs == null || movs.isEmpty()) {
            bindingResult.rejectValue("cantidadesBultos", "", "Debe ingresar las cantidades a consumir");
        }

//        if (!bindingResult.hasErrors()) {
//
//            /* conversión y sumatoria de stock consumido */
//            BigDecimal totalConsumidoSTD = BigDecimal.ZERO;
//
//            for (int i = 0; i < movs.size(); i++) {
//                MovimientoDTO m = movs.get(i);
//
//                // campo vacíos
//                if (m.getCantidad() == null || m.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
//                    bindingResult.rejectValue("movimientoDTOs[" + i + "].cantidad", "",
//                        "La cantidad debe ser mayor a cero");
//                    continue;
//                }
//                if (m.getUnidadMedida() == null) {
//                    bindingResult.rejectValue("movimientoDTOs[" + i + "].unidadMedida", "",
//                        "Debe indicar la unidad");
//                    continue;
//                }
//
//                /* unidad válida para el producto */
//                final Object UnidadMedidaEnum;
//                if (!UnidadMedidaEnum.esSubUnidadDe(m.getUnidadMedida(), lote.getUnidadMedida())) {
//                    bindingResult.rejectValue("movimientoDTOs[" + i + "].unidadMedida", "",
//                        "Unidad no compatible con la del lote");
//                }
//
//                /* el nroBulto debe existir */
//                int nro = m.getNroBulto() != null ? Integer.parseInt(m.getNroBulto()) : -1;
//                if (nro < 1 || nro > lote.getBultosActuales()) {
//                    bindingResult.rejectValue("movimientoDTOs[" + i + "].nroBulto", "",
//                        "Bulto inexistente");
//                }
//
//                /* no superar stock disponible de ese bulto */
//                BigDecimal dispSTD = lote.getCantidadesBultos().get(nro - 1);  // misma UM std
//                BigDecimal consSTD = m.getCantidad()
//                    .multiply(BigDecimal.valueOf(m.getUnidadMedida().factorTo(lote.getUnidadMedida())));
//                if (consSTD.compareTo(dispSTD) > 0) {
//                    bindingResult.rejectValue("cantidadesBultos", "",
//                        "La cantidad ingresada supera el stock del bulto " + nro);
//                }
//
//                totalConsumidoSTD = totalConsumidoSTD.add(consSTD);
//            }
//
//            /* no superar el stock total del lote */
//            if (lote != null && totalConsumidoSTD.compareTo(lote.getCantidadActual()) > 0) {
//                bindingResult.rejectValue("cantidadesBultos", "",
//                    "La suma de las cantidades supera el stock disponible del lote");
//            }
//        }

        /* ───────────────────────── 3. Si hay errores vuelve a la vista ───────────────────────── */
        if (bindingResult.hasErrors()) {
            List<LoteDTO> lotesProduccion = getLotesDtosByCodigoInterno(
                loteService.findAllForConsumoProduccion());
            model.addAttribute("lotesProduccion", lotesProduccion);
            model.addAttribute("loteDTO", loteDTO); //  ← mantiene lo que el usuario ingresó
            return "produccion/consumo-produccion";
        }

        /* ───────────────────────── 4. Persistencia (service) ───────────────────────── */
//        loteService.registrarConsumoProduccion(loteDTO);   // ← método que descuente stock y guarde movimientos

        ra.addFlashAttribute("success",
            "Consumo registrado correctamente para la orden " + loteDTO.getOrdenProduccion());
        return "redirect:/produccion/consumo-produccion-ok";
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

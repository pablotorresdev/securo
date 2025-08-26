package com.mb.conitrack.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.enums.MotivoEnum;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.MovimientoService;

/**
 * CU3
 */
@Controller
@RequestMapping("/movimientos")
public class MovimientosController {

    @Autowired
    private LoteService loteService;

    @Autowired
    private MovimientoService movimientoService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    @GetMapping("/list-movimientos")
    @Transactional(readOnly = true)
    public String listMovimientos(Model model) {
        model.addAttribute("movimientoDTOs", movimientoService.findAllMovimientosAudit());
        return "movimientos/list-movimientos";
    }

    @GetMapping("/codigoLote/{codigoLote}")
    @Transactional(readOnly = true)
    public String listBultosPorLote(@PathVariable("codigoLote") String codigoLote, Model model) {
        model.addAttribute("movimientoDTOs", movimientoService.findByCodigoLote(codigoLote));
        return "movimientos/list-movimientos";
    }






    //****************************

    @GetMapping("/list-muestreos")
    public String listMuestreos(Model model) {
        model.addAttribute("movimientos", movimientoService.findAllOrderByFechaAscNullsLast());
        return "movimientos/list-movimientos"; //
    }


    //TODO: ver de refactorear a bodyresponse para unificar
    @GetMapping("/loteId/{loteId}")
    public String listMovimientosPorLote(@PathVariable("loteId") Long loteId, Model model) {
        // Se asume que findById() recupera el lote con sus movimientos (por ejemplo, con fetch join)
        final List<Movimiento> movimientos = loteService.findLoteById(loteId).getMovimientos();
        movimientos.sort(Comparator
            .comparing(Movimiento::getFecha));
        model.addAttribute("movimientos", movimientos);
        return "movimientos/list-movimientos"; // Corresponde a movimientos-lote.html
    }

    @GetMapping("/ventas/movimientos-venta/{codInterno}")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<MovimientoDTO> getMovimientosByCodigolote(
        @PathVariable("codInterno") String codInterno) {

        return loteService.findLoteByCodigoLote(codInterno)
            .map(lote -> lote.getMovimientos().stream()
                .filter(Movimiento::getActivo)
                .filter(m -> m.getMotivo() == MotivoEnum.VENTA)
                .filter(m -> m.getDetalles() != null && m.getDetalles().stream()
                    .anyMatch(d -> d.getTrazas() != null && d.getTrazas().stream()
                        .anyMatch(t -> t.getEstado() == EstadoEnum.VENDIDO)))
                .sorted(Comparator.comparing(Movimiento::getFecha))
                .map(DTOUtils::fromMovimientoEntity)
                .toList()
            )
            .orElse(List.of());
    }

    @GetMapping("/ventas/trazas-vendidas/{codInterno}")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<TrazaDTO> getTrazasVendidasPorMovimiento(@PathVariable("codInterno") String codInterno) {

        Movimiento mov = movimientoService.findMovimientoByCodigoMovimiento(codInterno)
            .orElseThrow(() -> new IllegalArgumentException("Movimiento no existe: " + codInterno));

        // Detalle → Traza; sólo estado VENDIDO; sin duplicados
        return mov.getDetalles().stream()
            .flatMap(d -> d.getTrazas().stream())
            .filter(t -> t.getEstado() == EstadoEnum.VENDIDO)
            .collect(java.util.stream.Collectors.toMap(
                Traza::getId, t -> t, (a, b) -> a)) // dedup por ID
            .values().stream()
            .sorted(
                java.util.Comparator
                    .comparing((Traza t) -> t.getBulto().getNroBulto())
                    .thenComparing(Traza::getNroTraza)
            )
            .map(DTOUtils::fromTrazaEntity) // mapea a tu TrazaDTO
            .toList();
    }

}



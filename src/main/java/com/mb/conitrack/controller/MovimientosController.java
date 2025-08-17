package com.mb.conitrack.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.service.MovimientoService;
import com.mb.conitrack.service.QueryServiceLote;
import com.mb.conitrack.service.QueryServiceMovimiento;

/**
 * CU3
 */
@Controller
@RequestMapping("/movimientos")
public class MovimientosController {

    @Autowired
    private MovimientoService movimientoService;

    @Autowired
    private QueryServiceLote queryServiceLote;

    @Autowired
    private QueryServiceMovimiento queryServiceMovimiento;

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    @GetMapping("/list-muestreos")
    public String listMuestreos(Model model) {
        model.addAttribute("movimientos", queryServiceMovimiento.findAllMuestreos());
        return "movimientos/list-movimientos"; //
    }

    @GetMapping("/list-movimientos")
    public String listMovimientos(Model model) {
        model.addAttribute("movimientos", queryServiceMovimiento.findAll());
        return "movimientos/list-movimientos"; //.html
    }

    //TODO: ver de refactorear a bodyresponse para unificar
    @GetMapping("/loteId/{loteId}")
    public String listMovimientosPorLote(@PathVariable("loteId") Long loteId, Model model) {
        // Se asume que findById() recupera el lote con sus movimientos (por ejemplo, con fetch join)
        final List<Movimiento> movimientos = queryServiceLote.findLoteBultoById(loteId).getMovimientos();
        movimientos.sort(Comparator
            .comparing(Movimiento::getFecha));
        model.addAttribute("movimientos", movimientos);
        return "movimientos/list-movimientos"; // Corresponde a movimientos-lote.html
    }

}



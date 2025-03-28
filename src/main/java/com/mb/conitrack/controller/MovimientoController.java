package com.mb.conitrack.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.MovimientoService;

@Controller
@RequestMapping("/movimientos")
public class MovimientoController {

    @Autowired
    private MovimientoService movimientoService;

    @Autowired
    private LoteService loteService;

    @GetMapping("/list-movimientos")
    public String listProveedores(Model model) {
        model.addAttribute("movimientos", movimientoService.findAll());
        return "movimientos/list-movimientos"; //.html
    }

    @GetMapping("/lote/{loteId}")
    public String listMovimientosPorLote(@PathVariable("loteId") Long loteId, Model model) {
        // Se asume que findById() recupera el lote con sus movimientos (por ejemplo, con fetch join)
        final List<Movimiento> movimientos = loteService.findById(loteId).getMovimientos();
        movimientos.sort(Comparator
            .comparing(Movimiento::getFecha));
        model.addAttribute("movimientos", movimientos);
        return "movimientos/list-movimientos"; // Corresponde a movimientos-lote.html
    }
}


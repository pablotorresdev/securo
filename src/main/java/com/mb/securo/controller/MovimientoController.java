package com.mb.securo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mb.securo.entity.Lote;
import com.mb.securo.entity.Movimiento;
import com.mb.securo.service.LoteService;
import com.mb.securo.service.MovimientoService;

@Controller
@RequestMapping("/movimientos")
public class MovimientoController {

    @Autowired
    private MovimientoService movimientoService;

    @Autowired
    private LoteService loteService;

    @GetMapping("/list-movimientos")
    public String listContactos(Model model) {
        List<Movimiento> movimientos = movimientoService.findAll();
        model.addAttribute("movimientos", movimientos);
        return "movimientos/list-movimientos"; //.html
    }

    @GetMapping("/lote/{loteId}")
    public String listMovimientosPorLote(@PathVariable("loteId") Long loteId, Model model) {
        // Se asume que findById() recupera el lote con sus movimientos (por ejemplo, con fetch join)
        model.addAttribute("movimientos", loteService.findById(loteId).getMovimientos());
        return "movimientos/list-movimientos"; // Corresponde a movimientos-lote.html
    }
}


package com.mb.securo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
}


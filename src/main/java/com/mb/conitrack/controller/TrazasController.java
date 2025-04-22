package com.mb.conitrack.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.TrazaService;

/**
 * CU3
 */
@Controller
@RequestMapping("/trazas")
public class TrazasController {

    @Autowired
    private TrazaService trazaService;

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    @GetMapping("/list-trazas")
    public String listTrazas(Model model) {
        model.addAttribute("trazas", trazaService.findAll());
        return "trazas/list-trazas"; //.html
    }

    //TODO: ver de refactorear a bodyresponse para unificar
    @GetMapping("/loteId/{loteId}")
    public String listTrazasPorLote(@PathVariable("loteId") Long loteId, Model model) {
        // Se asume que findById() recupera el lote con sus trazas (por ejemplo, con fetch join)
        final List<Traza> trazas = trazaService.findByLoteId(loteId);
        model.addAttribute("trazas", trazas);
        return "trazas/list-trazas"; // Corresponde a trazas-lote.html
    }

}



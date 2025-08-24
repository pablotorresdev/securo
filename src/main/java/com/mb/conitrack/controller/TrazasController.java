package com.mb.conitrack.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mb.conitrack.service.TrazaService;

@Controller
@RequestMapping("/trazas")
public class TrazasController {

    @Autowired
    private TrazaService trazaService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    @GetMapping("/list-trazas")
    public String listTrazas(Model model) {
        model.addAttribute("trazas", trazaService.findAllByOrderByNroTrazaAsc());
        return "trazas/list-trazas"; //.html
    }

    //TODO: ver de refactorear a bodyresponse para unificar
    @GetMapping("/loteId/{loteId}")
    public String listTrazasPorLote(@PathVariable("loteId") Long loteId, Model model) {
        model.addAttribute("trazas", trazaService.findByLoteId(loteId));
        return "trazas/list-trazas"; // Corresponde a trazas-lote.html
    }

}



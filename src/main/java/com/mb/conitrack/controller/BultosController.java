package com.mb.conitrack.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mb.conitrack.service.BultoService;

import static com.mb.conitrack.dto.DTOUtils.fromBultoEntities;

@Controller
@RequestMapping("/bultos")
public class BultosController {

    @Autowired
    private BultoService bultoService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    @GetMapping("/list-bultos")
    public String listBultos(Model model) {
        model.addAttribute("bultos", bultoService.findAllByOrderByIdAsc());
        return "bultos/list-bultos";
    }

    //TODO: ver de refactorear a bodyresponse para unificar
    @GetMapping("/loteId/{loteId}")
    public String listBultosPorLote(@PathVariable("loteId") Long loteId, Model model) {
        model.addAttribute("bultos", bultoService.findByLoteId(loteId));
        return "bultos/list-bultos";
    }

    @GetMapping("/loteId/{codigoLote}")
    public String listBultosPorLote(@PathVariable("codigoLote") String codigoLote, Model model) {
        model.addAttribute("bultos", fromBultoEntities(bultoService.findActivosByLoteCodigoLote(codigoLote)));
        return "bultos/list-bultos";
    }

}



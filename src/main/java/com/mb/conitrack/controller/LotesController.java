package com.mb.conitrack.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.service.LoteService;

/**
 * CU1, CU4
 */
@Controller
@RequestMapping("/lotes")
public class LotesController {

    @Autowired
    private LoteService loteService;

    @GetMapping("/list-lotes")
    public String listLotes(Model model) {
        model.addAttribute("lotes", loteService.findAllSortByDateAndNroBulto());
        return "lotes/list-lotes";
    }

    @GetMapping("/codigoInterno/{codigoInterno}")
    @ResponseBody
    public List<Lote> getLoteByCodigoInterno(@PathVariable("codigoInterno") String codigoInterno) {
        return loteService.findLoteListByCodigoInterno(codigoInterno);
    }

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

}



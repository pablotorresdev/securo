package com.mb.conitrack.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.service.BultoService;

import static com.mb.conitrack.dto.DTOUtils.fromBultoEntities;

/**
 * CU3
 */
@Controller
@RequestMapping("/bultos")
public class BultosController {

    @Autowired
    private BultoService bultoService;

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    @GetMapping("/list-bultos")
    public String listBultos(Model model) {
        model.addAttribute("bultos", bultoService.findAll());
        return "bultos/list-bultos"; //.html
    }

    //TODO: ver de refactorear a bodyresponse para unificar
    @GetMapping("/loteId/{loteId}")
    public String listBultosPorLote(@PathVariable("loteId") Long loteId, Model model) {
        // Se asume que findById() recupera el lote con sus bultos (por ejemplo, con fetch join)
        final List<Bulto> bultos = bultoService.findByLoteId(loteId);
        model.addAttribute("bultos", bultos);
        return "bultos/list-bultos"; // Corresponde a bultos-lote.html
    }

    @GetMapping("/loteId/{codigoInternoLote}")
    public String listBultosPorLote(@PathVariable("codigoInternoLote") String codigoInternoLote, Model model) {

        final List<Bulto> bultos = bultoService.findByCodigoInternoLote(codigoInternoLote);
        List<Bulto> bultosByCodigoInternoLote = bultos.stream()
            .filter(Bulto::getActivo)
            .filter(bulto -> codigoInternoLote.equals(bulto.getLote().getCodigoInterno()))
            .sorted(Comparator
                .comparing(Bulto::getNroBulto)).toList();

        model.addAttribute("bultos", fromBultoEntities(bultosByCodigoInternoLote));
        return "bultos/list-bultos"; // Corresponde a bultos-lote.html
    }

}



package com.mb.conitrack.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.SessionStatus;

import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.LoteService;

@Controller
@RequestMapping("/analisis")
public class AnalisisController {

    @Autowired
    private AnalisisService analisisService;

    @Autowired
    private LoteService loteService;

    @GetMapping("/cancelar")
    public String cancelar(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/";
    }

    @GetMapping("/list-analisis")
    public String listAnalisis(Model model) {
        model.addAttribute("analisis", analisisService.findAll());
        return "analisis/list-analisis"; //.html
    }

    @GetMapping("/lote/{loteId}")
    public String listAnalisisPorLote(@PathVariable("loteId") Long loteId, Model model) {
        final Lote loteBultoById = loteService.findLoteBultoById(loteId);
        final List<Analisis> analisis = loteBultoById.getAnalisisList();
        analisis.sort(Comparator
            .comparing(Analisis::getFechaAnalisis));
        model.addAttribute("analisis", analisis);
        return "analisis/list-analisis"; // Corresponde a analisis-lote.html
    }
}


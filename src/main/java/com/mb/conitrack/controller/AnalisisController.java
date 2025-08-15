package com.mb.conitrack.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mb.conitrack.dto.AnalisisDTO;
import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.LoteService;

import static com.mb.conitrack.dto.DTOUtils.fromAnalisisEntities;

@Controller
@RequestMapping("/analisis")
public class AnalisisController {

    @Autowired
    private AnalisisService analisisService;

    @Autowired
    private LoteService loteService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    @GetMapping("/list-analisis")
    public String listAnalisis(Model model) {
        final List<AnalisisDTO> analisisDTOs = fromAnalisisEntities(analisisService.findAll());
        model.addAttribute("analisisDTOs", analisisDTOs);
        return "analisis/list-analisis"; //.html
    }

    @GetMapping("/nroAnalisis/{nroAnalisis}")
    @ResponseBody
    public LoteDTO analisisDetails(@PathVariable("nroAnalisis") String nroAnalisis) {
        final Analisis analisis = analisisService.findByNroAnalisis(nroAnalisis);
        if (analisis == null) {
            return new LoteDTO();
        }
        return DTOUtils.fromLoteEntity(analisis.getLote());
    }

    @GetMapping("/loteId/{loteId}")
    public String listAnalisisPorLote(@PathVariable("loteId") Long loteId, Model model) {

        final Lote loteBultoById = loteService.findLoteBultoById(loteId);
        if (loteBultoById == null) {
            return "redirect:/lotes/list-lotes";
        }

        List<Analisis> analisisList = loteBultoById.getAnalisisList().stream().filter(Analisis::getActivo).sorted(Comparator
            .comparing(Analisis::getFechaYHoraCreacion)).toList();

        List<AnalisisDTO> analisisDTOs = fromAnalisisEntities(analisisList);

        model.addAttribute("analisisDTOs", analisisDTOs);
        return "analisis/list-analisis"; // Corresponde a analisis-lote.html
    }

}


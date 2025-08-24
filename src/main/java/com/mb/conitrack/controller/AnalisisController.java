package com.mb.conitrack.controller;

import java.util.List;
import java.util.Optional;

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
import static java.util.Comparator.comparing;

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

        final Lote loteById = loteService.findLoteById(loteId);
        if (loteById == null) {
            return "redirect:/lotes/list-lotes";
        }

        List<Analisis> analisisList = loteById.getAnalisisList()
            .stream()
            .filter(Analisis::getActivo)
            .sorted(comparing(Analisis::getFechaYHoraCreacion))
            .toList();

        model.addAttribute("analisisDTOs", fromAnalisisEntities(analisisList));
        return "analisis/list-analisis";
    }

    @GetMapping("/codigoLote/{codigoLote}")
    public String listAnalisisPorLote(@PathVariable("codigoLote") String codigoLote, Model model) {

        final Optional<Lote> lote = loteService.findLoteByCodigoLote(codigoLote);
        if (lote.isEmpty()) {
            return "redirect:/lotes/list-lotes";
        }

        List<Analisis> analisisList = lote.get()
            .getAnalisisList()
            .stream()
            .filter(Analisis::getActivo)
            .sorted(comparing(Analisis::getFechaYHoraCreacion))
            .toList();

        model.addAttribute("analisisDTOs", fromAnalisisEntities(analisisList));
        return "analisis/list-analisis"; // Corresponde a analisis-lote.html
    }

}


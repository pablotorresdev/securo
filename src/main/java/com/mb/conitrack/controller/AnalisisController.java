package com.mb.conitrack.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.service.AnalisisService;

@Controller
@RequestMapping("/analisis")
public class AnalisisController {

    @Autowired
    private AnalisisService analisisService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    @GetMapping("/list-analisis")
    public String listAnalisis(Model model) {
        model.addAttribute("analisisDTOs", analisisService.findAllAnalisis());
        return "analisis/list-analisis"; //.html
    }

    @GetMapping("/codigoLote/{codigoLote}")
    @Transactional(readOnly = true)
    public String listAnalisisPorLote(@PathVariable("codigoLote") String codigoLote, Model model) {
        model.addAttribute("analisisDTOs", analisisService.findByCodigoLote(codigoLote));
        return "analisis/list-analisis"; // Corresponde a analisis-lote.html
    }

    //***********CU5 MODIFICACION: Resultado QA: Aprobado************
    //***********CU6 MODIFICACION: Resultado QA: Rechazado***********
    @GetMapping("/nroAnalisis/{nroAnalisis}")
    @ResponseBody
    @Transactional(readOnly = true)
    public LoteDTO analisisDetails(@PathVariable("nroAnalisis") String nroAnalisis) {
        final Analisis analisis = analisisService.findByNroAnalisis(nroAnalisis);
        if (analisis == null) {
            return new LoteDTO();
        }
        return DTOUtils.fromLoteEntity(analisis.getLote());
    }

}


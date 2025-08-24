package com.mb.conitrack.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mb.conitrack.dto.BultoDTO;
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

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    @GetMapping("/list-lotes")
    public String listLotes(Model model) {
        model.addAttribute("lotes", loteService.findAllSortByDateAndCodigoLoteAudit());
        return "lotes/list-lotes";
    }

    @GetMapping("/list-fechas-lotes")
    public String listFechasLotes(Model model) {
        model.addAttribute("loteDTOs", loteService.findLotesDictaminadosConStock());
        return "lotes/list-fechas-lotes";
    }

    @GetMapping("/codigoLote/{codigoLote}")
    @ResponseBody
    public List<Lote> getLoteByCodigoLote(@PathVariable("codigoLote") String codigoLote) {
        return loteService.findLoteListByCodigoLote(codigoLote);
    }

    @GetMapping("/codigoLote/muestreo/{codigoLote}")
    @ResponseBody
    public List<BultoDTO> getBultosForMuestreoByCodigoLote(
        @PathVariable String codigoLote) {
        return loteService.findBultosForMuestreoByCodigoLote(codigoLote);
    }

}


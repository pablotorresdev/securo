package com.mb.conitrack.controller;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mb.conitrack.dto.BultoDTO;
import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.DictamenEnum;
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
        model.addAttribute("lotes", loteService.findAllSortByDateAndNroBultoAudit());
        return "lotes/list-lotes";
    }

    @GetMapping("/list-fechas-lotes")
    public String listFechasLotes(Model model) {
        model.addAttribute("lotes", loteService.findAllLotesDictaminados());
        return "lotes/list-fechas-lotes";
    }

    @GetMapping("/codigoInterno/{codigoInternoLote}")
    @ResponseBody
    public List<Lote> getLoteByCodigoInterno(@PathVariable("codigoInternoLote") String codigoInternoLote) {
        return loteService.findLoteListByCodigoInterno(codigoInternoLote);
    }
//
//    @GetMapping("/codigoInterno/muestreo/{codigoInternoLote}")
//    @ResponseBody
//    public List<Lote> getLoteForMuestreoByCodigoInterno(@PathVariable("codigoInternoLote") String codigoInternoLote) {
//        return loteService.findLoteListByCodigoInterno(codigoInternoLote).stream()
//            .filter(lote -> DictamenEnum.RECIBIDO != lote.getDictamen())
//            .filter(lote -> lote.getAnalisisList().stream()
//                .anyMatch(analisis -> analisis.getNroAnalisis() != null))
//            .filter(lote -> lote.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
//            .sorted(Comparator.comparing(Lote::getNroBulto))
//            .toList();
//    }

    @GetMapping("/codigoInterno/muestreo/{codigoInternoLote}")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<BultoDTO> getBultosForMuestreoByCodigoInterno(
        @PathVariable String codigoInternoLote) {

        return loteService.findLoteByCodigoInterno(codigoInternoLote)
            .stream()
            .filter(Lote::getActivo)
            .filter(l -> l.getDictamen() != DictamenEnum.RECIBIDO)
            .filter(l -> l.getAnalisisList().stream().anyMatch(a -> a.getNroAnalisis() != null))
            .flatMap(l -> l.getBultos().stream()
                .filter(Bulto::getActivo)
                .filter(b -> b.getCantidadActual() != null && b.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
                .map(DTOUtils::fromEntity))
            .sorted(Comparator.comparing(BultoDTO::getNroBulto))
            .toList();
    }

}


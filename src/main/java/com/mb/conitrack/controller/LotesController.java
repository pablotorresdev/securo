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
import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Traza;
import com.mb.conitrack.enums.EstadoEnum;
import com.mb.conitrack.service.LoteService;

import static java.util.Comparator.comparing;

/**
 * CU1, CU4
 */
@Controller
@RequestMapping("/lotes")
public class LotesController {

    @Autowired
    private LoteService loteService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    @GetMapping("/list-lotes")
    public String listLotes(Model model) {
        model.addAttribute("loteDTOs", loteService.findAllLotesAudit());
        return "lotes/list-lotes";
    }

    @GetMapping("/list-fechas-lotes")
    public String listFechasLotes(Model model) {
        model.addAttribute("loteDTOs", loteService.findLotesDictaminadosConStock());
        return "lotes/list-fechas-lotes";
    }

    @GetMapping("/codigoLote/muestreo/{codigoLote}")
    @ResponseBody
    public List<BultoDTO> getBultosForMuestreoByCodigoLote(
        @PathVariable String codigoLote) {
        return loteService.findBultosForMuestreoByCodigoLote(codigoLote);
    }

    @GetMapping("/ventas/trazas-vendidas/{codInterno}")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<TrazaDTO> getTrazasVendidasPorLote(@PathVariable("codInterno") String codInterno) {

        Lote lote = loteService.findLoteByCodigoLote(codInterno)
            .orElseThrow(() -> new IllegalArgumentException("Lote no existe: " + codInterno));
        return lote.getTrazas().stream()
            .filter(t -> t.getEstado() == EstadoEnum.VENDIDO)
            .sorted(comparing(Traza::getNroTraza))
            .map(DTOUtils::fromTrazaEntity)
            .toList();
    }

}


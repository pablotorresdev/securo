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

import com.mb.conitrack.dto.TrazaDTO;
import com.mb.conitrack.service.TrazaService;

@Controller
@RequestMapping("/trazas")
public class TrazasController {

    @Autowired
    private TrazaService trazaService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    @GetMapping("/list-trazas")
    public String listTrazas(Model model) {
        model.addAttribute("trazaDTOs", trazaService.findAllTrazaAudit());
        return "trazas/list-trazas";
    }

    //TODO: ver de refactorear a bodyresponse para unificar
    @GetMapping("/codigoLote/{codigoLote}")
    public String listTrazasPorLote(@PathVariable("codigoLote") String codigoLote, Model model) {
        model.addAttribute("trazaDTOs", trazaService.findByCodigoLote(codigoLote));
        return "trazas/list-trazas";
    }

    //***********CU13 MODIF: DEVOLUCION VENTA***********
    @GetMapping("/trazas-vendidas/movimiento/{codigoMovimiento}")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<TrazaDTO> getTrazasVendidasPorMovimiento(@PathVariable("codigoMovimiento") String codigoMovimiento) {
        return trazaService.getTrazasVendidasByCodigoMovimiento(codigoMovimiento);
    }

    //***********CU14 MODIFICACION: RETIRO MERCADO***********
    @GetMapping("/trazas-vendidas/codigoLote/{codigoLote}")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<TrazaDTO> getTrazasVendidasPorLote(@PathVariable("codigoLote") String codigoLote) {
        return trazaService.getTrazasVendidasByCodigoLote(codigoLote);
    }

}



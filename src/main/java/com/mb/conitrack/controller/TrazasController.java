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
    public String listTrazasActivas(Model model) {
        model.addAttribute("trazaDTOs", trazaService.findAllByActivoTrue());
        return "trazas/list-trazas";
    }

    @GetMapping("/codigoLote/{codigoLote}")
    public String listTrazasActivasPorLote(@PathVariable("codigoLote") String codigoLote, Model model) {
        model.addAttribute("trazaDTOs", trazaService.findByCodigoLoteAndActivo(codigoLote));
        return "trazas/list-trazas";
    }

    //***********CU23 MODIF: DEVOLUCION VENTA***********
    @GetMapping("/trazas-vendidas/movimiento/{codigoMovimiento}")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<TrazaDTO> getTrazasVendidasPorMovimiento(@PathVariable("codigoMovimiento") String codigoMovimiento) {
        return trazaService.getTrazasVendidasByCodigoMovimiento(codigoMovimiento);
    }

    //***********CU24 MODIFICACION: RETIRO MERCADO***********
    @GetMapping("/trazas-vendidas/codigoLote/{codigoLote}")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<TrazaDTO> getTrazasVendidasPorLote(@PathVariable("codigoLote") String codigoLote) {
        return trazaService.getTrazasVendidasByCodigoLote(codigoLote);
    }

    @GetMapping("/disponibles/{codigoLote}/{nroBulto}")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<TrazaDTO> getTrazasDisponiblesPorBulto(@PathVariable String codigoLote,
        @PathVariable Integer nroBulto) {
        return trazaService.getTrazasByCodigoLoteAndNroBulto(codigoLote, nroBulto);
     }

}



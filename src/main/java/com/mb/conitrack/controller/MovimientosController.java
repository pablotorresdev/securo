package com.mb.conitrack.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.service.MovimientoService;

@Controller
@RequestMapping("/movimientos")
public class MovimientosController {

    @Autowired
    private MovimientoService movimientoService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    @GetMapping("/list-movimientos")
    @Transactional(readOnly = true)
    public String listMovimientos(Model model) {
        model.addAttribute("movimientoDTOs", movimientoService.findAllMovimientos());
        return "movimientos/list-movimientos";
    }

    @GetMapping("/codigoLote/{codigoLote}")
    @Transactional(readOnly = true)
    public String listBultosPorLote(@PathVariable("codigoLote") String codigoLote, Model model) {
        model.addAttribute("movimientoDTOs", movimientoService.findByCodigoLote(codigoLote));
        return "movimientos/list-movimientos";
    }


    /** Devuelve array: índice i => nroBulto = i+1, valor = máximo en UNIDAD (0 si no hay). */
    @GetMapping("/devolucion/maximos/{codigoMovimiento}")
    public ResponseEntity<List<Integer>> maximosPorBultoNoTrazado(
        @PathVariable String codigoMovimiento) {

        List<Integer> maximos = movimientoService.calcularMaximoDevolucionPorBulto(codigoMovimiento);
        return ResponseEntity.ok(maximos);
    }

    //***********CU23 MODIF: DEVOLUCION VENTA***********
    @GetMapping("/movimientos-venta/{codInterno}")
    @ResponseBody
    @Transactional(readOnly = true)
    public List<MovimientoDTO> getMovimientosVentaByCodigolote(
        @PathVariable("codInterno") String codInterno) {
        return movimientoService.getMovimientosVentaByCodigolote(codInterno);
    }

    //***********CU23 MODIF: DEVOLUCION VENTA***********

    @GetMapping("/ultimo-movimiento/{codigoLote}")
    @ResponseBody
    @Transactional(readOnly = true)
    public MovimientoDTO getUltimoMovimientoByCodigolote(
        @PathVariable("codigoLote") String codigoLote) {
        return movimientoService.getUltimoMovimientosCodigolote(codigoLote);
    }

}



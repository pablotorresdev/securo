package com.mb.securo.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mb.securo.entity.Lote;
import com.mb.securo.service.LoteService;

@RestController
@RequestMapping("/api/lotes")
public class LoteController {

    @Autowired
    private LoteService loteService;

    @PostMapping("/ingreso-compra")
    public ResponseEntity<Lote> ingresarStockPorCompra(@RequestBody Lote lote) {
        Lote nuevoLote = loteService.ingresarStockPorCompra(lote);
        return ResponseEntity.created(URI.create("/api/lotes/" + nuevoLote.getIdLote())).body(nuevoLote);
    }

}


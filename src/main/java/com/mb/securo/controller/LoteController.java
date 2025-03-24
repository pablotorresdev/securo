package com.mb.securo.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mb.securo.dto.LoteRequestDTO;
import com.mb.securo.entity.Lote;
import com.mb.securo.service.LoteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Lote Controller", description = "Endpoints para la gestión de lotes")
@RestController
@RequestMapping("/api/lotes")
public class LoteController {

    @Autowired
    private LoteService loteService;

//    @Operation(summary = "Ingreso de stock por compra", description = "Registra el ingreso de un nuevo lote proveniente de un proveedor externo (CU1)")
//    @PostMapping("/ingreso-compra")
//    public ResponseEntity<Lote> ingresarStockPorCompra(@RequestBody Lote lote) {
//        Lote nuevoLote = loteService.ingresarStockPorCompra(lote);
//        return ResponseEntity.created(URI.create("/api/lotes/" + nuevoLote.getIdLote())).body(nuevoLote);
//    }

    @Operation(summary = "Ingreso de stock por compra", description = "Registra el ingreso de un nuevo lote proveniente de un proveedor externo (CU1)")
    @PostMapping("/ingreso-compra")
    public ResponseEntity<Lote> ingresarStockPorCompra(@Valid @RequestBody LoteRequestDTO loteRequestDTO) {
        Lote nuevoLote = loteService.ingresarStockPorCompra(loteRequestDTO);
        return ResponseEntity.created(URI.create("/api/lotes/" + nuevoLote.getIdLote())).body(nuevoLote);
    }
}

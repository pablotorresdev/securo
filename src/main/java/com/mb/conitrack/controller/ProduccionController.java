package com.mb.conitrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/produccion")
public class ProduccionController {
    
    // CU7: Baja por Consumo Producción
    // @PreAuthorize("hasAuthority('ROLE_SUPERVISOR_PLANTA')")
    @GetMapping("/cu7")
    public String consumoProduccion() {
        return "/";
    }

    // CU11: Ingreso de Stock por Producción Interna
    // @PreAuthorize("hasAuthority('ROLE_DT')")
    @GetMapping("/cu11")
    public String ingresoProduccionInterna() {
        return "/";
    }

    // CU12: Liberación de Unidad de Venta
    // @PreAuthorize("hasAuthority('ROLE_GERENTE_GARANTIA')")
    @GetMapping("/cu12")
    public String liberacionUnidadVenta() {
        return "/";
    }

    // CU13: Baja por Venta de Producto Propio
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/cu13")
    public String ventaProducto() {
        return "/";
    }

}

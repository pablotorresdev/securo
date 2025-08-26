package com.mb.conitrack.controller.cu;

import org.springframework.beans.factory.annotation.Autowired;

import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.maestro.ProductoService;
import com.mb.conitrack.service.maestro.ProveedorService;

public abstract class AbstractCuController {

    @Autowired
    LoteService loteService;

    @Autowired
    ProductoService productoService;

    @Autowired
    ProveedorService proveedorService;

    @Autowired
    AnalisisService analisisService;

}

package com.mb.conitrack.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mb.conitrack.enums.UnidadMedidaEnum;

@Controller
@RequestMapping("/enums")
public class EnumsController {

    @ResponseBody
    @GetMapping("/unidades-compatibles")
    public List<UnidadMedidaEnum> getUnidadesCompatibles(@RequestParam("unidad") UnidadMedidaEnum unidad) {
        return UnidadMedidaEnum.getUnidadesCompatibles(unidad);
    }

    @ResponseBody
    @GetMapping("/subunidades")
    public List<UnidadMedidaEnum> getSubUnidades(@RequestParam("unidad") UnidadMedidaEnum unidad) {
        final List<UnidadMedidaEnum> unidadesCompatibles = UnidadMedidaEnum.getUnidadesCompatibles(unidad);
        unidadesCompatibles.removeIf(u -> u.getFactorConversion() > unidad.getFactorConversion());
        return unidadesCompatibles;
    }

}


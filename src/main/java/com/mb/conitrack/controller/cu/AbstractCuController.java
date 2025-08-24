package com.mb.conitrack.controller.cu;

import org.springframework.beans.factory.annotation.Autowired;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.utils.ControllerUtils;

public abstract class AbstractCuController {

    @Autowired
    LoteService loteService;

    static ControllerUtils controllerUtils() {
        return ControllerUtils.getInstance();
    }

    static DTOUtils dtoUtils() {
        return DTOUtils.getInstance();
    }

}

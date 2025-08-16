package com.mb.conitrack.controller.cu;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.utils.ControllerUtils;

public abstract class AbstractCuController {

    static ControllerUtils controllerUtils() {
        return ControllerUtils.getInstance();
    }

    static DTOUtils dtoUtils() {
        return DTOUtils.getInstance();
    }

}

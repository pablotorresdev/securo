package com.mb.conitrack.controller.cu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.utils.ControllerUtils;

public abstract class AbstractCuController {

    @Autowired
    LoteService loteService;


    List<String> getCountryList() {
        String[] countryCodes = Locale.getISOCountries();
        List<String> countries = new ArrayList<>();
        for (String code : countryCodes) {
            Locale locale = new Locale("", code);
            countries.add(locale.getDisplayCountry());
        }
        countries.sort(String::compareTo);
        return countries;
    }

    static ControllerUtils controllerUtils() {
        return ControllerUtils.getInstance();
    }

    static DTOUtils dtoUtils() {
        return DTOUtils.getInstance();
    }

}

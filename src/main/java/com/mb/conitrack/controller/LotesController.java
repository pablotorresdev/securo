package com.mb.conitrack.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.ProductoService;
import com.mb.conitrack.service.ProveedorService;

import jakarta.validation.Valid;

import static com.mb.conitrack.enums.UnidadMedidaEnum.getUnidadesConvertibles;

/**
 * CU1, CU4
 */
@Controller
@RequestMapping("/lotes")
public class LotesController {

    @Autowired
    private LoteService loteService;

    @GetMapping("/list-lotes")
    public String listLotes(Model model) {
        model.addAttribute("lotes", loteService.findAllSortByDateAndNroBulto());
        return "lotes/list-lotes";
    }

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/";
    }

    //TODO:Pasar a Lote Controller
    @GetMapping("/id/{loteId}")
    public String listAnalisisPorLote(@PathVariable("loteId") Long loteId, Model model) {
        final Lote loteBultoById = loteService.findLoteBultoById(loteId);
        final List<Analisis> analisis = loteBultoById.getAnalisisList();
        analisis.sort(Comparator
            .comparing(Analisis::getFechaYHoraCreacion));
        model.addAttribute("analisis", analisis);
        return "analisis/list-analisis"; // Corresponde a analisis-lote.html
    }

}



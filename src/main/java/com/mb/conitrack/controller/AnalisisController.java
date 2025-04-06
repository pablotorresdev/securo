package com.mb.conitrack.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.LoteService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/analisis")
public class AnalisisController {

    @Autowired
    private AnalisisService analisisService;

    @Autowired
    private LoteService loteService;

    @GetMapping("/cancelar")
    public String cancelar(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/";
    }

    @GetMapping("/list-analisis")
    public String listAnalisis(Model model) {
        model.addAttribute("analisis", analisisService.findAll());
        return "analisis/list-analisis"; //.html
    }


    @GetMapping("/nroAnalisis/{nroAnalisis}")
    @ResponseBody
    public LoteDTO analisisDetails(@PathVariable("nroAnalisis") String nroAnalisis) {
        final Analisis analisis = analisisService.findByNroAnalisis(nroAnalisis);
        return DTOUtils.fromEntities(analisis.getLotes());
    }

}


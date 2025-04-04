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
@SessionAttributes("movimientoDTO")
public class AnalisisController {

    @Autowired
    private AnalisisService analisisService;

    @Autowired
    private LoteService loteService;

    @ModelAttribute("movimientoDTO")
    public MovimientoDTO getMovimientoDTO() {
        final MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaMovimiento(LocalDate.now());
        return dto;
    }

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

    @GetMapping("/lote/{loteId}")
    public String listAnalisisPorLote(@PathVariable("loteId") Long loteId, Model model) {
        final Lote loteBultoById = loteService.findLoteBultoById(loteId);
        final List<Analisis> analisis = loteBultoById.getAnalisisList();
        analisis.sort(Comparator
            .comparing(Analisis::getFechaYHoraCreacion));
        model.addAttribute("analisis", analisis);
        return "analisis/list-analisis"; // Corresponde a analisis-lote.html
    }

    @GetMapping("/resultado")
    public String showResultadoForm(
        @ModelAttribute("movimientoDTO") MovimientoDTO movimientoDTO, Model model) {
        //TODO: implementar el filtro correcto en base a Dictamen y Analisis (Fecha, Dictamen)
        //TODO: pasar a DTO
        model.addAttribute("lotesForResultado", loteService.findAllForResultadoAnalisis());
        model.addAttribute("analisisEnCurso", analisisService.findAllEnCurso());
        model.addAttribute("resultados", List.of(DictamenEnum.APROBADO, DictamenEnum.RECHAZADO));
        return "dictamen/resultado";
    }

    @PostMapping("/resultado")
    public String procesarResultado(
        @Valid @ModelAttribute MovimientoDTO dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes, SessionStatus sessionStatus) {

        final List<Lote> lotesList = loteService.findLoteListById(dto.getLoteId());

        if (lotesList.isEmpty()) {
            bindingResult.reject("loteId", "Lote bloqueado.");
            return "dictamen/resultado";
        }

        if (dto.getFechaAnalisis() == null && dto.getFechaReanalisis() == null) {
            bindingResult.rejectValue("nroAnalisis", "Debe ingresar una fecha de Analisis o Re Analisis");
        }

        if (dto.getDictamenFinal() == null) {
            bindingResult.rejectValue("nroAnalisis", "Debe ingresar un Resultado");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("movimientoDTO", dto);
            return "dictamen/resultado";
        }

        dto.setFechaYHoraCreacion(LocalDateTime.now());
        final List<Lote> lotes = loteService.persistirDictamenResultado(lotesList, dto);

        redirectAttributes.addFlashAttribute("loteDTO", DTOUtils.fromEntities(lotes));
        redirectAttributes.addFlashAttribute("success", "Cambio de dictamen a " + dto.getDictamenFinal() + " exitoso");
        sessionStatus.setComplete();
        return "redirect:/dictamen/exito-resultado";
    }

}


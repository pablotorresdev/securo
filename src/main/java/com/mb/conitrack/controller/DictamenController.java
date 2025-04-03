package com.mb.conitrack.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.service.LoteService;

import jakarta.validation.Valid;

/**
 * CU2
 */
@Controller
@RequestMapping("/dictamen")
@SessionAttributes("loteDTO,movimientoDTO")
public class DictamenController {

    @Autowired
    private LoteService loteService;

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/";
    }

    @ModelAttribute("loteDTO")
    public LoteDTO getLoteDTO() {
        final LoteDTO dto = new LoteDTO();
        dto.setFechaIngreso(LocalDate.now());
        return dto;
    }

    @ModelAttribute("movimientoDTO")
    public MovimientoDTO getMovimientoDTO() {
        final MovimientoDTO dto = new MovimientoDTO();
        dto.setFechaMovimiento(LocalDate.now());
        return dto;
    }

    @GetMapping("/cuarentena")
    public String showCuarentenaForm(
        @ModelAttribute("movimientoDTO") MovimientoDTO movimientoDTO, Model model) {

        //TODO: implementar el filtro correcto en base a Dictamen y Analisis (Fecha, Dictamen)
        List<Lote> lotesForCuarentena = loteService.findAllForCuarentena();
        model.addAttribute("lotesForCuarentena", lotesForCuarentena);
        return "dictamen/cuarentena";
    }

    @PostMapping("/cuarentena")
    public String procesarCuarentena(
        @Valid @ModelAttribute MovimientoDTO dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes, SessionStatus sessionStatus) {

        final List<Lote> lotesList = loteService.findLoteListById(dto.getLoteId());

        if (lotesList.isEmpty()) {
            bindingResult.reject("loteId", "Lote bloqueado.");
            return "dictamen/cuarentena";
        }

        validarValidarNroAnalisis(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("movimientoDTO", dto);
            return "dictamen/cuarentena";
        }

        dto.setFechaYHoraCreacion(LocalDateTime.now());
        final List<Lote> lotes = loteService.persistirDictamenCuarentena(lotesList, dto);

        redirectAttributes.addFlashAttribute("loteDTO", LoteDTO.fromEntities(lotes));
        redirectAttributes.addFlashAttribute("success", "Cambio de dictamen a Cuarentena exitoso");
        sessionStatus.setComplete();
        return "redirect:/dictamen/exito-cuarentena";
    }

    private void validarValidarNroAnalisis(final MovimientoDTO dto, final BindingResult bindingResult) {
        if (dto.getNroReAnalisis() == null && dto.getNroAnalisis() == null) {
            bindingResult.rejectValue("nroAnalisis", "Debe ingresar un nro de Analisis o Re Analisis");
        }
    }

    @GetMapping("/exito-cuarentena")
    public String exitoCuarentena(
        @ModelAttribute("loteDTO") LoteDTO loteDTO,
        Model model) {
        return "dictamen/exito-cuarentena";
    }

}

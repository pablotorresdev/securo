package com.mb.conitrack.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.entity.Movimiento;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.MovimientoService;

import jakarta.validation.Valid;

/**
 * CU3
 */
@Controller
@RequestMapping("/movimientos")
@SessionAttributes("loteDTO,movimientoDTO")
public class MovimientosController {

    @Autowired
    private LoteService loteService;

    @Autowired
    private MovimientoService movimientoService;

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
        dto.setFechaAnalisis(LocalDate.now());
        return dto;
    }

    @GetMapping("/list-muestreos")
    public String listMuestreos(Model model) {
        model.addAttribute("movimientos", movimientoService.findAllMuestreos());
        return "movimientos/list-movimientos"; //
    }

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/";
    }

    @GetMapping("/list-movimientos")
    public String listMovimientos(Model model) {
        model.addAttribute("movimientos", movimientoService.findAll());
        return "movimientos/list-movimientos"; //.html
    }

    @GetMapping("/lote/{loteId}")
    public String listMovimientosPorLote(@PathVariable("loteId") Long loteId, Model model) {
        // Se asume que findById() recupera el lote con sus movimientos (por ejemplo, con fetch join)
        final List<Movimiento> movimientos = loteService.findLoteBultoById(loteId).getMovimientos();
        movimientos.sort(Comparator
            .comparing(Movimiento::getFecha));
        model.addAttribute("movimientos", movimientos);
        return "movimientos/list-movimientos"; // Corresponde a movimientos-lote.html
    }

    //***************************** CU3 Muestreo

    @GetMapping("/muestreo-lote")
    public String showMuestreoForm(
        @ModelAttribute("movimientoDTO") MovimientoDTO movimientoDTO, Model model) {
        List<Lote> lotesMuestreables = loteService.findAllForMuestreo();
        model.addAttribute("lotesMuestreables", lotesMuestreables);
        return "movimientos/muestreo-lote";
    }

    @PostMapping("/muestreo-lote")
    public String procesarMuestreo(
        @Valid @ModelAttribute MovimientoDTO dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes, SessionStatus sessionStatus) {

        Lote lote = loteService.findLoteBultoById(dto.getLoteId());

        if (!lote.getActivo()) {
            bindingResult.reject("loteId", "Lote bloqueado.");
            return "movimientos/muestreo-lote";
        }

        validarDictamenActual(bindingResult, lote);
        validarCantidadesMovimiento(dto, lote, bindingResult);
        validarValidarNroAnalisis(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("lote", lote);
            return "movimientos/muestreo-lote";
        }

        dto.setFechaYHoraCreacion(LocalDateTime.now());
        final Optional<Lote> newLote = loteService.persistirMuestreo(dto, lote);

        if (newLote.isEmpty()) {
            bindingResult.reject("error", "Error al persistir el muestreo.");
            return "movimientos/muestreo-lote";
        }

        LoteDTO loteDTO = DTOUtils.fromEntities(List.of(newLote.get()));
        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute("success", "Muestreo registrado correctamente.");
        sessionStatus.setComplete();
        return "redirect:/movimientos/exito-muestreo-lote";
    }

    private static void validarDictamenActual(final BindingResult bindingResult, final Lote lote) {
        if (DictamenEnum.VENCIDO.equals(lote.getDictamen())) {
            bindingResult.reject("estado", "El lote no está en un estado válido para muestreo: VENCIDO");
        }
    }

    private static void validarCantidadesMovimiento(final MovimientoDTO dto, final Lote lote, final BindingResult bindingResult) {
        final List<UnidadMedidaEnum> unidadesPorTipo = UnidadMedidaEnum.getUnidadesPorTipo(lote.getUnidadMedida());

        if (!unidadesPorTipo.contains(dto.getUnidadMedida())) {
            bindingResult.rejectValue("unidadMedida", "Unidad no compatible con el producto.");
            return;
        }

        if (dto.getCantidad() == null || dto.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            bindingResult.rejectValue("cantidad", "La cantidad debe ser mayor a 0.");
            return;
        }

        BigDecimal cantidadConvertida = dto.getCantidad().multiply(BigDecimal.valueOf(dto.getUnidadMedida().getFactorConversion() / lote.getUnidadMedida().getFactorConversion()));

        if (cantidadConvertida.compareTo(lote.getCantidadActual()) > 0) {
            bindingResult.rejectValue("cantidad", "La cantidad excede el stock disponible del lote.");
        }
    }

    private void validarValidarNroAnalisis(final MovimientoDTO dto, final BindingResult bindingResult) {
        if (dto.getNroReanalisis() == null && dto.getNroAnalisis() == null) {
            bindingResult.rejectValue("nroAnalisis", "Debe ingresar un nro de Analisis o Re Analisis");
        }
    }

    @GetMapping("/exito-muestreo-lote")
    public String exitoMuestreo(
        @ModelAttribute("loteDTO") LoteDTO loteDTO,
        Model model) {
        return "movimientos/exito-muestreo-lote";
    }

}



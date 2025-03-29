package com.mb.conitrack.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.MovimientoService;
import com.mb.conitrack.service.ProductoService;
import com.mb.conitrack.service.ProveedorService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/muestreos")
@SessionAttributes("loteDTO, movimientoDTO")
public class MuestreoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProveedorService proveedorService;

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
        return dto;
    }

    // paths standard
    @GetMapping("/list-lotes")
    public String listLotes(Model model) {
        model.addAttribute("lotes", loteService.findAll());
        return "lotes/list-lotes";
    }

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelarIngreso(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/";
    }

    //***************************** CU2 Ingreso por compra MultiBulto

    @GetMapping("/retiro-muestreo")
    public String showRetiroMuestreoForm(
        @ModelAttribute("movimientoDTO") MovimientoDTO movimientoDTO,
        Model model) {
        // TODO: Listar lotes con estados permitidos
        List<Lote> lotesValidos = loteService.findAll();

        model.addAttribute("lotes", lotesValidos);
        return "lotes/retiro-muestreo";
    }

    @PostMapping("/retiro-muestreo")
    public String procesarMuestreo(
        @Valid @ModelAttribute MovimientoDTO dto,
        BindingResult bindingResult,
        Model model, RedirectAttributes redirectAttributes) {

        Lote lote = loteService.findById(dto.getLoteId());

        if (!lote.getActivo()) {
            bindingResult.reject("loteId", "Lote bloqueado.");
            return "movimientos/retiro-muestreo";
        }

        validarDictamenActual(bindingResult, lote);
        validarCantidadesMovimiento(dto, lote, bindingResult);
        validarDatosObligatorios(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("lote", lote);
            return "movimientos/retiro-muestreo";
        }

        // Realizar baja
        movimientoService.registrarMuestreo(dto);

        // Cambiar dictamen si corresponde
        if (DictamenEnum.RECIBIDO.equals(lote.getDictamen())) {
            //TODO: revisar como implementar esto
            loteService.actualizarDictamen(lote, DictamenEnum.CUARENTENA);
        }

        redirectAttributes.addFlashAttribute("success", "Muestreo registrado correctamente.");
        return "redirect:/";
    }

    private static void validarDictamenActual(final BindingResult bindingResult, final Lote lote) {
        if ("VENCIDO".equalsIgnoreCase(lote.getDictamen().name())) {
            bindingResult.reject("estado", "El lote no está en un estado válido para muestreo: VENCIDO");
        }
    }

    private static void validarCantidadesMovimiento(final MovimientoDTO dto, final Lote lote, final BindingResult bindingResult) {
        final List<UnidadMedidaEnum> unidadesPorTipo = UnidadMedidaEnum.getUnidadesPorTipo(lote.getUnidadMedida());

        // La unidad de medida tiene que ser de igual o menor factor de conversion a la del lote

        if (!unidadesPorTipo.contains(dto.getUnidadMedida())) {
            bindingResult.rejectValue("unidadMedida", "Unidad no compatible con el producto.");
        }

        if (dto.getCantidad().compareTo(BigDecimal.ZERO) <= 0 ||
            dto.getCantidad().compareTo(lote.getCantidadActual()) > 0) {
            bindingResult.rejectValue("cantidad", "Cantidad inválida.");
        }
        ;
    }

    private static void validarDatosObligatorios(final MovimientoDTO dto, final BindingResult bindingResult) {
        if (dto.getNroAnalisis() == null && dto.getNroReAnalisis() == null) {
            bindingResult.rejectValue("nroAnalisis", "Debe ingresar un nro de Analisis o Re Analisis");
        }
    }

}



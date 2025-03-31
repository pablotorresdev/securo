package com.mb.conitrack.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.MovimientoService;
import com.mb.conitrack.service.ProductoService;
import com.mb.conitrack.service.ProveedorService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/muestreos")
@SessionAttributes("loteDTO,movimientoDTO")
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
    public String cancelarIngreso(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/";
    }

    //***************************** CU2 Ingreso por compra MultiBulto

    @GetMapping("/muestreo-lote")
    public String showRetiroMuestreoForm(
        @ModelAttribute("movimientoDTO") MovimientoDTO movimientoDTO, Model model) {
        // TODO: Listar lotes con estados permitidos
        List<Lote> lotesMuestreables = loteService.findAllMuestreable();

        model.addAttribute("lotes", lotesMuestreables);
        return "movimientos/muestreo-lote";
    }

    @PostMapping("/muestreo-lote")
    public String procesarMuestreo(
        @Valid @ModelAttribute MovimientoDTO dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes, SessionStatus sessionStatus) {

        Lote lote = loteService.findById(dto.getLoteId());

        if (!lote.getActivo()) {
            bindingResult.reject("loteId", "Lote bloqueado.");
            return "movimientos/muestreo-lote";
        }

        validarDictamenActual(bindingResult, lote);
        validarCantidadesMovimiento(dto, lote, bindingResult);
        validarDatosObligatorios(dto, bindingResult);
        validarValidarNroAnalisis(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("lote", lote);
            return "movimientos/muestreo-lote";
        }

        // Realizar baja
        movimientoService.persistirMuestreo(dto, lote);

        // Cambiar dictamen si corresponde
        if (DictamenEnum.RECIBIDO.equals(lote.getDictamen())) {
            final List<Lote> allBultosById = loteService.findAllByIdLoteAndActivoTrue(lote.getIdLote());
            persistirCuarentena(dto, allBultosById);
        }

        redirectAttributes.addFlashAttribute("success", "Muestreo registrado correctamente.");
        sessionStatus.setComplete();
        return "redirect:/";
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

    private static void validarDatosObligatorios(final MovimientoDTO dto, final BindingResult bindingResult) {
        if (dto.getNroAnalisis() == null && dto.getNroReAnalisis() == null) {
            bindingResult.rejectValue("nroAnalisis", "Debe ingresar un nro de Analisis o Re Analisis");
        }
    }

    private void validarValidarNroAnalisis(final MovimientoDTO dto, final BindingResult bindingResult) {
        if (dto.getNroReAnalisis() == null && dto.getNroAnalisis() == null) {
            bindingResult.rejectValue("nroAnalisis", "Debe ingresar un nro de Analisis o Re Analisis");
        }
    }

    @Transactional
    void persistirCuarentena(final MovimientoDTO dto, final List<Lote> allBultosById) {
        for (Lote lote : allBultosById) {
            movimientoService.persistirCambioDictamenPorMuestreo(dto, lote);
        }
    }

}



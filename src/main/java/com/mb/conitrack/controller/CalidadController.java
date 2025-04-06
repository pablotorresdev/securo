package com.mb.conitrack.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.entity.Analisis;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.DictamenEnum;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.AnalisisService;
import com.mb.conitrack.service.LoteService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/calidad")
public class CalidadController {

    @Autowired
    private LoteService loteService;

    @Autowired
    private AnalisisService analisisService;

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    //***************************** CU3 Muestreo************************************
    // CU2: Dictamen Lote a Cuarentena
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_CALIDAD')")
    @GetMapping("/cuarentena")
    public String showDictamenCuarentenaForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        //TODO: implementar el filtro correcto en base a calidad y Analisis (Fecha, calidad)
        model.addAttribute("lotesForCuarentena", loteService.findAllForCuarentena());
        model.addAttribute("movimientoDTO", movimientoDTO);

        return "calidad/cuarentena";
    }

    @PostMapping("/cuarentena")
    public String procesarDictamenCuarentena(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        final List<Lote> lotesList = loteService.findLoteListById(movimientoDTO.getLoteId());

        if (lotesList.isEmpty()) {
            bindingResult.reject("loteId", "Lote bloqueado.");
            return "calidad/cuarentena";
        }

        validarValidarNroAnalisis(movimientoDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("movimientoDTO", movimientoDTO);
            return "calidad/cuarentena";
        }

        calidadCuarentena(movimientoDTO, redirectAttributes, lotesList);
        return "redirect:/calidad/cuarentena-ok";
    }

    @GetMapping("/cuarentena-ok")
    public String exitoDictamenCuarentena(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "calidad/cuarentena-ok";
    }

    //***************************** CU3 Muestreo************************************
    // CU3: Baja por Muestreo
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_CALIDAD')")
    @GetMapping("/muestreo-bulto")
    public String showMuestreoBultoForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        List<Lote> lotesMuestreables = loteService.findAllForMuestreo();
        model.addAttribute("lotesMuestreables", lotesMuestreables);
        model.addAttribute("movimientoDTO", movimientoDTO);
        return "calidad/muestreo-bulto";
    }

    @PostMapping("/muestreo-bulto")
    public String procesarMuestreoBulto(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        Lote lote = loteService.findLoteBultoById(movimientoDTO.getLoteId());

        if (!lote.getActivo()) {
            bindingResult.reject("loteId", "Lote bloqueado.");
            return "calidad/muestreo-bulto";
        }

        validarcalidadActual(bindingResult, lote);
        validarCantidadesMovimiento(movimientoDTO, lote, bindingResult);
        validarValidarNroAnalisis(movimientoDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("movimientoDTO", movimientoDTO);
            return "calidad/muestreo-bulto";
        }

        movimientoDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final Optional<Lote> newLote = loteService.persistirMuestreo(movimientoDTO, lote);

        if (newLote.isEmpty()) {
            bindingResult.reject("error", "Error al persistir el muestreo.");
            return "muestreo-bulto";
        }

        LoteDTO loteDTO = DTOUtils.fromEntities(List.of(newLote.get()));
        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute("success", "Muestreo registrado correctamente.");

        return "redirect:/calidad/muestreo-bulto-ok";
    }

    @GetMapping("/muestreo-bulto-ok")
    public String exitoMuestreo(
        @ModelAttribute LoteDTO loteDTO) {
        return "calidad/muestreo-bulto-ok";
    }

    //***************************** CU5/6 Resultado Analisis************************************
    // CU5: Resultado QA Aprobado
    // CU6: Resultado QA Rechazado
    // @PreAuthorize("hasAuthority('ROLE_CONTROL_CALIDAD')")
    @GetMapping("/resultado-analisis")
    public String showResultadoAnalisisForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        //TODO: implementar el filtro correcto en base a Dictamen y Analisis (Fecha, Dictamen)
        //TODO: pasar a DTO
        setupModelResultadoAnalisis(movimientoDTO, model);
        return "calidad/resultado-analisis";
    }

    private void setupModelResultadoAnalisis(final MovimientoDTO movimientoDTO, final Model model) {
        List<Lote> lotes = loteService.findAllForResultadoAnalisis();
        List<Analisis> analisis = analisisService.findAllEnCurso();

        movimientoDTO.setFechaMovimiento(LocalDateTime.now().toLocalDate());
        model.addAttribute("movimientoDTO", movimientoDTO);
        model.addAttribute("lotesForResultado", lotes);
        model.addAttribute("analisisEnCurso", analisis);
        model.addAttribute("resultados", List.of(DictamenEnum.APROBADO, DictamenEnum.RECHAZADO));
    }

    @PostMapping("/resultado-analisis")
    public String procesarResultadoAnalisis(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes, SessionStatus sessionStatus) {

        if (movimientoDTO.getNroAnalisis() == null) {
            bindingResult.rejectValue("nroAnalisis", "El Nro de Analisis es obligatorio");
        }
        if (movimientoDTO.getDictamenFinal() == null) {
            bindingResult.rejectValue("dictamenFinal", "Debe ingresar un Resultado");
        }

        if (DictamenEnum.APROBADO == movimientoDTO.getDictamenFinal()) {
            if (movimientoDTO.getFechaVencimiento() == null && movimientoDTO.getFechaReanalisis() == null) {
                bindingResult.rejectValue("fechaVencimiento", "Debe ingresar una fecha de Re Analisis o Vencimiento");
            }
        }

        if (movimientoDTO.getFechaRealizadoAnalisis() == null) {
            bindingResult.rejectValue("fechaRealizadoAnalisis", "Debe ingresar la fecha en la que se realizó el análisis");
        } else {
            //validar no anterior a fecha de ingreso
        }

        if (movimientoDTO.getTitulo() == null) {
            bindingResult.rejectValue("titulo", "Debe ingresar el valor de titulo del Analisis");
        }

        if (bindingResult.hasErrors()) {
            setupModelResultadoAnalisis(movimientoDTO, model);
            return "calidad/resultado-analisis";
        }

        movimientoDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final List<Lote> lotes = loteService.persistirResultadoAnalisis(movimientoDTO);

        if (lotes.isEmpty()) {
            bindingResult.reject("loteId", "Error al persistir el resultado del Analisis.");
            setupModelResultadoAnalisis(movimientoDTO, model);
            return "calidad/resultado-analisis";
        }

        redirectAttributes.addFlashAttribute("loteDTO", DTOUtils.fromEntities(lotes));
        redirectAttributes.addFlashAttribute("success", "Cambio de dictamen a " + movimientoDTO.getDictamenFinal() + " exitoso");
        sessionStatus.setComplete();
        return "redirect:/calidad/resultado-analisis-ok";
    }

    @GetMapping("/resultado-analisis-ok")
    public String exitoResultadoAnalisis(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "calidad/resultado-analisis-ok";
    }

    // CU8: Reanálisis manual
    // @PreAuthorize("hasAuthority('ROLE_CONTROL_CALIDAD')")
    @GetMapping("/cu8")
    public String reanalisisManual() {
        return "";
    }

    // CU18: Alta de Producto
    // @PreAuthorize("hasAuthority('ROLE_CONTROL_CALIDAD')")
    @GetMapping("/cu18")
    public String altaProducto() {
        return "";
    }

    private void validarcalidadActual(final BindingResult bindingResult, final Lote lote) {
        if (DictamenEnum.VENCIDO.equals(lote.getDictamen())) {
            bindingResult.reject("estado", "El lote no está en un estado válido para muestreo: VENCIDO");
        }
    }

    private void validarCantidadesMovimiento(final MovimientoDTO dto, final Lote lote, final BindingResult bindingResult) {
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

    private void calidadCuarentena(final MovimientoDTO dto, final RedirectAttributes redirectAttributes, final List<Lote> lotesList) {
        dto.setFechaYHoraCreacion(LocalDateTime.now());
        final List<Lote> lotes = loteService.persistirDictamenCuarentena(lotesList, dto);

        redirectAttributes.addFlashAttribute("loteDTO", DTOUtils.fromEntities(lotes));
        redirectAttributes.addFlashAttribute("success", "Cambio de calidad a Cuarentena exitoso");
    }

    private void validarValidarNroAnalisis(final MovimientoDTO dto, final BindingResult bindingResult) {
        if (dto.getNroReanalisis() == null && dto.getNroAnalisis() == null) {
            bindingResult.rejectValue("nroAnalisis", "Debe ingresar un nro de Analisis o Re Analisis");
        }
    }

}

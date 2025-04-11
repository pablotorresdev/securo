package com.mb.conitrack.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.util.StringUtils;

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

import static com.mb.conitrack.dto.DTOUtils.getLotesDtosByCodigoInterno;

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
        final List<LoteDTO> lotesDtos = getLotesDtosByCodigoInterno(loteService.findAllForCuarentena());
        model.addAttribute("lotesForCuarentena", lotesDtos);
        model.addAttribute("movimientoDTO", movimientoDTO);

        return "calidad/cuarentena";
    }

    @PostMapping("/cuarentena")
    public String procesarDictamenCuarentena(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        final List<Lote> lotesList = loteService.findLoteListByCodigoInterno(movimientoDTO.getCodigoInterno());

        if (lotesList.isEmpty()) {
            bindingResult.reject("codigoInterno", "Lote bloqueado.");
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

    @PostMapping("/resultado-analisis")
    public String procesarResultadoAnalisis(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        validarResultadoAnalisis(movimientoDTO, bindingResult);

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



    private void validarDatosResultadoAnalisisComunes(final MovimientoDTO movimientoDTO, final BindingResult bindingResult) {
        // Verificamos que nroAnalisis no sea vacío
        if (StringUtils.isEmpty(movimientoDTO.getNroAnalisis())) {
            bindingResult.rejectValue("nroAnalisis", "", "El Nro de Análisis es obligatorio");
        }

        // Dictamen Final no nulo
        if (movimientoDTO.getDictamenFinal() == null) {
            bindingResult.rejectValue("dictamenFinal", "", "Debe ingresar un Resultado");
        }

        // Fecha Realizado Análisis no nula
        if (movimientoDTO.getFechaRealizadoAnalisis() == null) {
            bindingResult.rejectValue("fechaRealizadoAnalisis", "", "Debe ingresar la fecha en la que se realizó el análisis");
        }
    }

    private void validarDatosResultadoAnalisisAprobado(final MovimientoDTO movimientoDTO, final BindingResult bindingResult) {
        // Al menos una de las fechas de reanálisis o vencimiento debe ser ingresada
        if (movimientoDTO.getFechaVencimiento() == null && movimientoDTO.getFechaReanalisis() == null) {
            bindingResult.rejectValue("fechaVencimiento", "", "Debe ingresar una fecha de Re Análisis o Vencimiento");
        }

        // La fecha de reanálisis no puede ser posterior a la fecha de vencimiento
        if (movimientoDTO.getFechaVencimiento() != null &&
            movimientoDTO.getFechaReanalisis() != null &&
            movimientoDTO.getFechaReanalisis().isAfter(movimientoDTO.getFechaVencimiento())) {
            bindingResult.rejectValue("fechaReanalisis", "", "La fecha de vencimiento no puede ser anterior a la fecha de reanálisis");
        }

        // El título es obligatorio y no puede ser mayor al 100%
        if (movimientoDTO.getTitulo() == null) {
            bindingResult.rejectValue("titulo", "", "Debe ingresar el valor de título del Análisis");
        } else if (movimientoDTO.getTitulo().compareTo(BigDecimal.valueOf(100)) > 0) {
            bindingResult.rejectValue("titulo", "", "El título no puede ser mayor al 100%");
        }
    }

    private void setupModelResultadoAnalisis(final MovimientoDTO movimientoDTO, final Model model) {
        final List<LoteDTO> lotesDtos = getLotesDtosByCodigoInterno(loteService.findAllForResultadoAnalisis());

        List<Analisis> analisis = analisisService.findAllEnCurso();

        movimientoDTO.setFechaMovimiento(LocalDateTime.now().toLocalDate());
        model.addAttribute("movimientoDTO", movimientoDTO);
        model.addAttribute("lotesForResultado", lotesDtos);
        model.addAttribute("analisisEnCurso", analisis);
        model.addAttribute("resultados", List.of(DictamenEnum.APROBADO, DictamenEnum.RECHAZADO));
    }

    private void validarResultadoAnalisis(final MovimientoDTO movimientoDTO, final BindingResult bindingResult) {

        validarDatosResultadoAnalisisComunes(movimientoDTO, bindingResult);

        // Si dictamen es APROBADO, hacemos validaciones adicionales
        if (DictamenEnum.APROBADO == movimientoDTO.getDictamenFinal()) {
            validarDatosResultadoAnalisisAprobado(movimientoDTO, bindingResult);
            if (bindingResult.hasErrors()) {
                return;
            }

            validarDatosResultadoAnalisisAprobadoContraLote(movimientoDTO, bindingResult);
        } else {
            // Si el dictamen es RECHAZADO, se limpian ciertos campos
            movimientoDTO.setTitulo(null);
            movimientoDTO.setFechaReanalisis(null);
            movimientoDTO.setFechaVencimiento(null);
        }
    }

    private void validarDatosResultadoAnalisisAprobadoContraLote(final MovimientoDTO movimientoDTO, final BindingResult bindingResult) {
        // A partir de aquí, validaciones personalizadas en base a los datos del Lote
        final Lote loteBulto = loteService.findLoteListByCodigoInterno(movimientoDTO.getCodigoInterno())
            .stream()
            .findFirst()
            .orElse(null);

        if (loteBulto == null) {
            // Si por algún motivo no se encuentra el lote, marcamos error y detenemos
            bindingResult.rejectValue("loteId", "", "No se encontró el Lote asociado");
            return;
        }

        // (A) Validar la existencia de un Movimiento de BAJA, motivo MUESTREO, con el mismo nroAnálisis en cada lote
        //            final List<Lote> loteListById = loteService.findLoteListById(movimientoDTO.getLoteId());
        //            for (Lote lote : loteListById) {
        //
        //                final List<Movimiento> movimientos = lote.getMovimientos();
        //                boolean existeMuestreo = movimientos.stream()
        //                    .anyMatch(m -> m.getTipoMovimiento() == TipoMovimientoEnum.BAJA
        //                        && m.getMotivo() == MotivoEnum.MUESTREO
        //                        && movimientoDTO.getNroAnalisis().equals(m.getNroAnalisis()));
        //                if (!existeMuestreo) {
        //                    bindingResult.rejectValue(
        //                        "nroAnalisis",
        //                        "",
        //                        "No se encontró un movimiento de tipo BAJA y motivo MUESTREO con Nro de Análisis " + movimientoDTO.getNroAnalisis()
        //                    );
        //                    return;
        //                }
        //            }

        // (4) La fecha de realizado el análisis no puede ser anterior a la fecha de ingreso del lote
        if (movimientoDTO.getFechaRealizadoAnalisis() != null &&
            movimientoDTO.getFechaRealizadoAnalisis().isBefore(loteBulto.getFechaIngreso())) {
            bindingResult.rejectValue("fechaRealizadoAnalisis", "",
                "La fecha de realización no puede ser anterior a la fecha de ingreso del lote");
        }

        // (1) y (2) Validaciones sobre fecha de reanálisis vs proveedor
        //    - Si el lote NO tiene análisis aprobado => la fechaReanálisis no puede pasar
        //      la fechaReanálisisProveedor NI la fechaVencimientoProveedor
        //    - Si el lote YA tiene análisis aprobado => la fechaReanálisis no puede pasar
        //      la fechaVencimientoProveedor
        final List<Analisis> analisisList = loteBulto.getAnalisisList();
        long analisisAprobados = analisisList.stream()
            .filter(a -> a.getDictamen() == DictamenEnum.APROBADO)
            .count();

        if (movimientoDTO.getFechaReanalisis() != null) {
            if (analisisAprobados == 0) {
                // Lote NO fue analizado antes con dictamen APROBADO
                if (loteBulto.getFechaReanalisisProveedor() != null &&
                    movimientoDTO.getFechaReanalisis().isAfter(loteBulto.getFechaReanalisisProveedor())) {
                    bindingResult.rejectValue("fechaReanalisis", "",
                        "La fecha de reanálisis no puede ser posterior a la fecha de reanálisis del proveedor");
                }
                if (loteBulto.getFechaVencimientoProveedor() != null &&
                    movimientoDTO.getFechaReanalisis().isAfter(loteBulto.getFechaVencimientoProveedor())) {
                    bindingResult.rejectValue("fechaReanalisis", "",
                        "La fecha de reanálisis no puede ser posterior a la fecha de vencimiento del proveedor");
                }
            } else {
                // El lote YA tiene al menos un análisis APROBADO
                if (loteBulto.getFechaVencimientoProveedor() != null &&
                    movimientoDTO.getFechaReanalisis().isAfter(loteBulto.getFechaVencimientoProveedor())) {
                    bindingResult.rejectValue("fechaReanalisis", "",
                        "La fecha de reanálisis no puede ser posterior a la fecha de vencimiento del proveedor");
                }
            }
        }

        // (3) La fecha de vencimiento no puede ser posterior a la fecha de vencimiento del proveedor
        if (movimientoDTO.getFechaVencimiento() != null &&
            loteBulto.getFechaVencimientoProveedor() != null &&
            movimientoDTO.getFechaVencimiento().isAfter(loteBulto.getFechaVencimientoProveedor())) {
            bindingResult.rejectValue("fechaVencimiento", "",
                "La fecha de vencimiento no puede ser posterior a la fecha de vencimiento del proveedor");
        }

        // (5) El valor del título no puede ser mayor al valor del título del último análisis aprobado
        //     (si existe un último análisis con dictamen APROBADO)
        Analisis ultimoAprobado = analisisList.stream()
            .filter(a -> a.getDictamen() == DictamenEnum.APROBADO && a.getTitulo() != null)
            .max(Comparator.comparing(Analisis::getFechaYHoraCreacion))
            .orElse(null);

        if (ultimoAprobado != null && movimientoDTO.getTitulo().compareTo(ultimoAprobado.getTitulo()) > 0) {
            bindingResult.rejectValue("titulo", "",
                "El valor del título no puede ser mayor al del último análisis aprobado (" + ultimoAprobado.getTitulo() + ")");
        }
    }

    private void validarcalidadActual(final BindingResult bindingResult, final Lote lote) {
        if (DictamenEnum.VENCIDO.equals(lote.getDictamen())) {
            bindingResult.reject("estado", "El lote no está en un estado válido para muestreo: VENCIDO");
        }
    }

    private void validarCantidadesMovimiento(final MovimientoDTO dto, final Lote lote, final BindingResult bindingResult) {
        final List<UnidadMedidaEnum> unidadesPorTipo = UnidadMedidaEnum.getUnidadesPorTipo(lote.getUnidadMedida());

        if (!unidadesPorTipo.contains(dto.getUnidadMedida())) {
            bindingResult.rejectValue("unidadMedida", "", "Unidad no compatible con el producto.");
            return;
        }

        if (dto.getCantidad() == null || dto.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            bindingResult.rejectValue("cantidad", "", "La cantidad debe ser mayor a 0.");
            return;
        }

        BigDecimal cantidadConvertida = dto.getCantidad().multiply(BigDecimal.valueOf(dto.getUnidadMedida().getFactorConversion() / lote.getUnidadMedida().getFactorConversion()));

        if (cantidadConvertida.compareTo(lote.getCantidadActual()) > 0) {
            bindingResult.rejectValue("cantidad", "", "La cantidad excede el stock disponible del lote.");
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
            bindingResult.rejectValue("nroAnalisis", "", "Debe ingresar un nro de Analisis o Re Analisis");
        }
    }

}

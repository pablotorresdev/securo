package com.mb.conitrack.controller;

import java.time.LocalDateTime;
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

import static com.mb.conitrack.controller.ControllerUtils.populateLoteListByCodigoInterno;
import static com.mb.conitrack.controller.ControllerUtils.validarCantidadesMovimiento;
import static com.mb.conitrack.controller.ControllerUtils.validarContraFechasProveedor;
import static com.mb.conitrack.controller.ControllerUtils.validarDatosMandatoriosResultadoAnalisisInput;
import static com.mb.conitrack.controller.ControllerUtils.validarDatosResultadoAnalisisAprobadoInput;
import static com.mb.conitrack.controller.ControllerUtils.validarExisteMuestreoParaAnalisis;
import static com.mb.conitrack.controller.ControllerUtils.validarFechaMovimientoPosteriorLote;
import static com.mb.conitrack.controller.ControllerUtils.validarNroAnalisisNotNull;
import static com.mb.conitrack.controller.ControllerUtils.validarValorTitulo;
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
        initModelDictamencuarentena(movimientoDTO, model);
        return "calidad/cuarentena";
    }

    @PostMapping("/cuarentena")
    public String procesarDictamenCuarentena(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        final List<Lote> lotesList = new ArrayList<>();
        boolean success = validarNroAnalisisNotNull(movimientoDTO, bindingResult)
            && populateLoteListByCodigoInterno(lotesList, movimientoDTO.getCodigoInterno(), bindingResult, loteService)
            && validarFechaMovimientoPosteriorLote(movimientoDTO, lotesList.get(0), bindingResult);

        if (!success) {
            initModelDictamencuarentena(movimientoDTO, model);
            model.addAttribute("movimientoDTO", movimientoDTO);
            return "calidad/cuarentena";
        }

        dictamenCuarentena(movimientoDTO, lotesList, redirectAttributes);
        return "redirect:/calidad/cuarentena-ok";
    }

    @GetMapping("/cuarentena-ok")
    public String exitoDictamenCuarentena(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "calidad/cuarentena-ok";
    }

    //***************************** CUZ Reanalisis de Producto Aprobado************************************
    // CUZ: Reanalisis de Producto Aprobado
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_CALIDAD')")
    @GetMapping("/reanalisis-producto")
    public String showReanalisisProductoForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        initModelReanalisisProducto(movimientoDTO, model);
        return "calidad/reanalisis-producto";
    }

    @PostMapping("/reanalisis-producto")
    public String procesarReanalisisProducto(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        final List<Lote> lotesList = new ArrayList<>();
        boolean success = validarNroAnalisisNotNull(movimientoDTO, bindingResult)
            && populateLoteListByCodigoInterno(lotesList, movimientoDTO.getCodigoInterno(), bindingResult, loteService)
            && validarFechaMovimientoPosteriorLote(movimientoDTO, lotesList.get(0), bindingResult);

        if (!success) {
            initModelReanalisisProducto(movimientoDTO, model);
            model.addAttribute("movimientoDTO", movimientoDTO);
            return "calidad/reanalisis-producto";
        }

        reanalisisProducto(movimientoDTO, lotesList, redirectAttributes);
        return "redirect:/calidad/reanalisis-producto-ok";
    }

    @GetMapping("/reanalisis-producto-ok")
    public String exitoReanalisisProducto(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "calidad/reanalisis-producto-ok";
    }

    //***************************** CU3 Muestreo************************************
    // CU3: Baja por Muestreo
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_CALIDAD')")
    @GetMapping("/muestreo-bulto")
    public String showMuestreoBultoForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        initModelMuestreoBulto(movimientoDTO, model);
        return "calidad/muestreo-bulto";
    }

    @PostMapping("/muestreo-bulto")
    public String procesarMuestreoBulto(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        if (!validarNroAnalisisNotNull(movimientoDTO, bindingResult)) {
            initModelMuestreoBulto(movimientoDTO, model);
            model.addAttribute("movimientoDTO", movimientoDTO);
            return "calidad/muestreo-bulto";
        }

        Lote lote = loteService.findLoteBultoById(movimientoDTO.getLoteId());
        if (!(validarFechaMovimientoPosteriorLote(movimientoDTO, lote, bindingResult)
            && validarCantidadesMovimiento(movimientoDTO, lote, bindingResult))) {
            initModelMuestreoBulto(movimientoDTO, model);
            model.addAttribute("movimientoDTO", movimientoDTO);
            return "calidad/muestreo-bulto";
        }

        muestreoBulto(movimientoDTO, lote, redirectAttributes);
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
        initModelResultadoAnalisis(movimientoDTO, model);
        return "calidad/resultado-analisis";
    }

    @PostMapping("/resultado-analisis")
    public String procesarResultadoAnalisis(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        // Validar throw new IllegalStateException("Hay más de un análisis activo con fecha de vencimiento");
        if (!validarResultadoAnalisisInput(movimientoDTO, bindingResult)) {
            initModelResultadoAnalisis(movimientoDTO, model);
            return "calidad/resultado-analisis";
        }

        resultadoAnalisis(movimientoDTO, redirectAttributes);
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

    private void dictamenCuarentena(final MovimientoDTO dto, final List<Lote> lotesList, final RedirectAttributes redirectAttributes) {
        dto.setFechaYHoraCreacion(LocalDateTime.now());
        final List<Lote> lotes = loteService.persistirDictamenCuarentena(dto, lotesList);
        redirectAttributes.addFlashAttribute("loteDTO", DTOUtils.mergeEntities(lotes));
        redirectAttributes.addFlashAttribute("success", "Cambio de calidad a Cuarentena exitoso");
    }

    private void initModelDictamencuarentena(final MovimientoDTO movimientoDTO, final Model model) {
        final List<LoteDTO> lotesDtos = getLotesDtosByCodigoInterno(loteService.findAllForCuarentena());
        model.addAttribute("lotesForCuarentena", lotesDtos);
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    private void initModelMuestreoBulto(final MovimientoDTO movimientoDTO, final Model model) {
        final List<LoteDTO> lotesDtos = getLotesDtosByCodigoInterno(loteService.findAllForMuestreo());
        model.addAttribute("lotesMuestreables", lotesDtos);
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    private void initModelReanalisisProducto(final MovimientoDTO movimientoDTO, final Model model) {
        //TODO: implementar el filtro correcto en base a calidad y Analisis (Fecha, calidad)
        final List<LoteDTO> lotesDtos = getLotesDtosByCodigoInterno(loteService.findAllForReanalisisProducto());
        model.addAttribute("lotesForReanalisis", lotesDtos);
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    private void initModelResultadoAnalisis(final MovimientoDTO movimientoDTO, final Model model) {
        final List<LoteDTO> lotesDtos = getLotesDtosByCodigoInterno(loteService.findAllForResultadoAnalisis());
        List<Analisis> analisis = analisisService.findAllEnCursoForLotesCuarentena();
        movimientoDTO.setFechaMovimiento(LocalDateTime.now().toLocalDate());
        model.addAttribute("movimientoDTO", movimientoDTO);
        model.addAttribute("lotesForResultado", lotesDtos);
        model.addAttribute("analisisEnCurso", analisis);
        model.addAttribute("resultados", List.of(DictamenEnum.APROBADO, DictamenEnum.RECHAZADO));
    }

    private void muestreoBulto(final MovimientoDTO movimientoDTO, final Lote lote, final RedirectAttributes redirectAttributes) {
        movimientoDTO.setFechaYHoraCreacion(LocalDateTime.now());
        LoteDTO loteDTO = DTOUtils.mergeEntities(List.of(loteService.persistirMuestreo(movimientoDTO, lote)));

        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null ? "Muestreo registrado correctamente." : "Hubo un error persistiendo el muestreo.");
    }

    private void reanalisisProducto(final MovimientoDTO dto, final List<Lote> lotesList, final RedirectAttributes redirectAttributes) {
        dto.setFechaYHoraCreacion(LocalDateTime.now());
        final List<Lote> lotes = loteService.persistirReanalisisProducto(dto, lotesList);
        redirectAttributes.addFlashAttribute("loteDTO", DTOUtils.mergeEntities(lotes));
        redirectAttributes.addFlashAttribute("success", "Anilisis asignado con éxito");
    }

    private void resultadoAnalisis(final MovimientoDTO movimientoDTO, final RedirectAttributes redirectAttributes) {
        movimientoDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO loteDTO = DTOUtils.mergeEntities(loteService.persistirResultadoAnalisis(movimientoDTO));

        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null ? "Cambio de dictamen a " + movimientoDTO.getDictamenFinal() + " exitoso" : "Hubo un error con el cambio de dictamen.");
    }

    private boolean validarResultadoAnalisisInput(final MovimientoDTO movimientoDTO, final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        final List<Lote> lotesList = new ArrayList<>();
        return validarDatosMandatoriosResultadoAnalisisInput(movimientoDTO, bindingResult)
            && validarDatosResultadoAnalisisAprobadoInput(movimientoDTO, bindingResult)
            && populateLoteListByCodigoInterno(lotesList, movimientoDTO.getCodigoInterno(), bindingResult, loteService)
            && validarExisteMuestreoParaAnalisis(movimientoDTO, lotesList, bindingResult)
            && validarFechaMovimientoPosteriorLote(movimientoDTO, lotesList.get(0), bindingResult)
            && validarContraFechasProveedor(movimientoDTO, lotesList.get(0), bindingResult)
            && validarValorTitulo(movimientoDTO, lotesList, bindingResult);
    }

}

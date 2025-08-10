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
import com.mb.conitrack.utils.ControllerUtils;

import jakarta.validation.Valid;

import static com.mb.conitrack.dto.DTOUtils.getLotesDtosByCodigoInterno;

@Controller
@RequestMapping("/calidad")
public class CalidadController {

    @Autowired
    private LoteService loteService;

    @Autowired
    private AnalisisService analisisService;

    final ControllerUtils controllerUtils = ControllerUtils.getInstance();

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    //***************************** CUZ Reanalisis de Producto Aprobado************************************
    // CUZ: Reanalisis de Producto Aprobado
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_CALIDAD')")
    @GetMapping("/reanalisis-producto")
    public String showReanalisisProductoForm(@ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        initModelReanalisisProducto(movimientoDTO, model);
        return "calidad/reanalisis-producto";
    }

    @PostMapping("/reanalisis-producto")
    public String procesarReanalisisProducto(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        final List<Lote> lotesList = new ArrayList<>();
        boolean success = controllerUtils.validarNroAnalisisNotNull(movimientoDTO, bindingResult) &&
            controllerUtils.populateLoteListByCodigoInterno(
                lotesList,
                movimientoDTO.getCodigoInternoLote(),
                bindingResult,
                loteService) &&
            controllerUtils.validarFechaMovimientoPosteriorIngresoLote(movimientoDTO, lotesList.get(0), bindingResult) &&
            controllerUtils.validarFechaAnalisisPosteriorIngresoLote(movimientoDTO, lotesList.get(0), bindingResult);

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

    //***************************** CU5/6 Resultado Analisis************************************
    // CU5: Resultado QA Aprobado
    // CU6: Resultado QA Rechazado
    // @PreAuthorize("hasAuthority('ROLE_CONTROL_CALIDAD')")
    @GetMapping("/resultado-analisis")
    public String showResultadoAnalisisForm(@ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        //TODO: implementar el filtro correcto en base a Dictamen y Analisis (Fecha, Dictamen)
        //TODO: pasar a DTO
        initModelResultadoAnalisis(movimientoDTO, model);
        return "calidad/resultado-analisis";
    }

    @PostMapping("/resultado-analisis")
    public String procesarResultadoAnalisis(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {
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

    private void reanalisisProducto(
        final MovimientoDTO dto,
        final List<Lote> lotesList,
        final RedirectAttributes redirectAttributes) {
        dto.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO loteDTO = DTOUtils.mergeEntities(loteService.persistirReanalisisProducto(dto, lotesList));
        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null
                ? "Anilisis asignado con éxito: " + loteDTO.getUltimoNroAnalisisDto()
                : "Hubo un error al asignar el analisis.");
    }

    private void resultadoAnalisis(final MovimientoDTO movimientoDTO, final RedirectAttributes redirectAttributes) {
        movimientoDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final LoteDTO loteDTO = DTOUtils.mergeEntities(loteService.persistirResultadoAnalisis(movimientoDTO));

        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null
                ? "Cambio de dictamen a " + movimientoDTO.getDictamenFinal() + " exitoso"
                : "Hubo un error con el cambio de dictamen.");
    }

    private boolean validarResultadoAnalisisInput(
        final MovimientoDTO movimientoDTO,
        final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return false;
        }
        final List<Lote> lotesList = new ArrayList<>();
        return controllerUtils.validarDatosMandatoriosResultadoAnalisisInput(movimientoDTO, bindingResult) &&
            controllerUtils.validarDatosResultadoAnalisisAprobadoInput(movimientoDTO, bindingResult) &&
            controllerUtils.populateLoteListByCodigoInterno(
                lotesList,
                movimientoDTO.getCodigoInternoLote(),
                bindingResult,
                loteService) &&
            controllerUtils.validarExisteMuestreoParaAnalisis(movimientoDTO, lotesList, bindingResult) &&
            controllerUtils.validarFechaMovimientoPosteriorIngresoLote(movimientoDTO, lotesList.get(0), bindingResult) &&
            controllerUtils.validarFechaAnalisisPosteriorIngresoLote(movimientoDTO, lotesList.get(0), bindingResult) &&
            controllerUtils.validarContraFechasProveedor(movimientoDTO, lotesList.get(0), bindingResult) &&
            controllerUtils.validarValorTitulo(movimientoDTO, lotesList, bindingResult);
    }

}

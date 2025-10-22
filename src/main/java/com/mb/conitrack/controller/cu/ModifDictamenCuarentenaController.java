package com.mb.conitrack.controller.cu;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.service.cu.ModifDictamenCuarentenaService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/calidad/dictamen")
public class ModifDictamenCuarentenaController extends AbstractCuController {

    @Autowired
    private ModifDictamenCuarentenaService dictamenCuarentenaService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    //***************************** CU2 Dictamen Lote a Cuarentena************************************
    // CU2: Dictamen Lote a Cuarentena
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_CALIDAD')")
    @GetMapping("/cuarentena")
    public String showDictamenCuarentenaForm(@ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        initModelDictamencuarentena(movimientoDTO, model);
        return "calidad/dictamen/cuarentena";
    }

    @PostMapping("/cuarentena")
    public String dictamenCuarentena(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!dictamenCuarentenaService.validarDictamenCuarentenaInput(movimientoDTO, bindingResult)) {
            initModelDictamencuarentena(movimientoDTO, model);
            return "calidad/dictamen/cuarentena";
        }

        procesarDictamenCuarentena(movimientoDTO, redirectAttributes);
        return "redirect:/calidad/dictamen/cuarentena-ok";
    }

    @GetMapping("/cuarentena-ok")
    public String exitoDictamenCuarentena(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "calidad/dictamen/cuarentena-ok";
    }

    void initModelDictamencuarentena(final MovimientoDTO movimientoDTO, final Model model) {
        model.addAttribute("loteCuarentenaDTOs", loteService.findAllForCuarentenaDTOs());
        model.addAttribute("movimientoDTO", template(movimientoDTO));
    }
    public static MovimientoDTO template(MovimientoDTO dto) {
        dto.setFechaYHoraCreacion(null);
        dto.setFechaMovimiento(LocalDate.of(2025, 10, 22));
        dto.setDictamenInicial(null);
        dto.setDictamenFinal(null);
        dto.setObservaciones("");
        dto.setCantidad(null);
        dto.setUnidadMedida(null);
        dto.setNroAnalisis("1234");
        dto.setNroBulto(null);
        dto.setFechaRealizadoAnalisis(null);
        dto.setFechaReanalisis(null);
        dto.setFechaVencimiento(null);
        dto.setTitulo(null);
        dto.setTipoMovimiento(null);
        dto.setMotivo(null);
        dto.setLoteId(null);
        dto.setTrazaInicial(null);
        dto.setCodigoLote("L-P-004-25.10.22_17.23.47");
        dto.setNroReanalisis("");
        dto.setCodigoMovimiento(null);
        dto.setCodigoMovimientoOrigen(null);
        dto.setOrdenProduccion(null);

        dto.setBultoDTOS(new ArrayList<>());
        dto.setDetalleMovimientoDTOs(new ArrayList<>());
        dto.setTrazaDTOs(new ArrayList<>());

        return dto;
    }


    void procesarDictamenCuarentena(
        final MovimientoDTO dto,
        final RedirectAttributes redirectAttributes) {

        dto.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO loteDTO = dictamenCuarentenaService.persistirDictamenCuarentena(dto);

        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null
                ? "Cambio de calidad a Cuarentena exitoso"
                : "Hubo un error al realizar el cambio de calidad a Cuarentena.");
    }

}

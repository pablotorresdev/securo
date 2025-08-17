package com.mb.conitrack.controller.cu;

import java.time.LocalDateTime;
import java.util.Comparator;
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
import com.mb.conitrack.entity.Bulto;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.QueryServiceLote;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/calidad/baja")
public class BajaMuestreoBultoController extends AbstractCuController {

    @Autowired
    private LoteService loteService;

    @Autowired
    private QueryServiceLote queryServiceLote;

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    //***************************** CU3 Muestreo************************************
    // CU3: Baja por Muestreo
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_CONTROL_CALIDAD')")
    @GetMapping("/muestreo-bulto")
    public String showMuestreoBultoForm(
        @ModelAttribute MovimientoDTO movimientoDTO, Model model) {
        initModelMuestreoBulto(movimientoDTO, model);
        return "calidad/baja/muestreo-bulto";
    }

    @PostMapping("/muestreo-bulto")
    //TODO: soportar multimuestreo (x cant x cada bulto)
    public String procesarMuestreoBulto(
        @Valid @ModelAttribute MovimientoDTO movimientoDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        boolean success = controllerUtils().validarNroAnalisisNotNull(movimientoDTO, bindingResult);
        Lote lote = success
            ? queryServiceLote.findLoteByCodigoInterno(movimientoDTO.getCodigoInternoLote()).orElse(null)
            : null;

        success = success && lote != null;
        success = success &&
            controllerUtils().validarFechaMovimientoPosteriorIngresoLote(movimientoDTO, lote, bindingResult);
        success = success &&
            controllerUtils().validarFechaAnalisisPosteriorIngresoLote(movimientoDTO, lote, bindingResult);

        final int nroBulto = Integer.parseInt(movimientoDTO.getNroBulto());
        Bulto bulto = null;
        if (success && lote.getBultos().size() >= nroBulto) {
            final List<Bulto> bultos = lote.getBultos().stream()
                .sorted(Comparator.comparing(Bulto::getNroBulto))
                .toList();
            bulto = bultos.get(nroBulto - 1);
        }
        success = success && bulto != null;
        success = success && controllerUtils()
            .validarCantidadesMovimiento(movimientoDTO, bulto, bindingResult);

        if (!success) {
            initModelMuestreoBulto(movimientoDTO, model);
            model.addAttribute("movimientoDTO", movimientoDTO);
            return "calidad/baja/muestreo-bulto";
        }

        muestreoBulto(movimientoDTO, bulto, redirectAttributes);
        return "redirect:/calidad/baja/muestreo-bulto-ok";
    }

    @GetMapping("/muestreo-bulto-ok")
    public String exitoMuestreo(
        @ModelAttribute LoteDTO loteDTO) {
        return "calidad/baja/muestreo-bulto-ok";
    }

    void initModelMuestreoBulto(final MovimientoDTO movimientoDTO, final Model model) {
        final List<LoteDTO> lotesDtos = DTOUtils.fromLoteEntities(queryServiceLote.findAllForMuestreo());
        model.addAttribute("lotesMuestreables", lotesDtos);
        model.addAttribute("movimientoDTO", movimientoDTO);
    }

    void muestreoBulto(
        final MovimientoDTO movimientoDTO,
        final Bulto bulto,
        final RedirectAttributes redirectAttributes) {
        movimientoDTO.setFechaYHoraCreacion(LocalDateTime.now());
        LoteDTO loteDTO = DTOUtils.fromLoteEntity(loteService.bajaMuestreo(movimientoDTO, bulto));

        redirectAttributes.addFlashAttribute("loteDTO", loteDTO);
        redirectAttributes.addFlashAttribute("trazasMuestreo", movimientoDTO.getTrazaDTOs());
        redirectAttributes.addFlashAttribute(
            loteDTO != null ? "success" : "error",
            loteDTO != null ? "Muestreo registrado correctamente." : "Hubo un error persistiendo el muestreo.");
    }

}

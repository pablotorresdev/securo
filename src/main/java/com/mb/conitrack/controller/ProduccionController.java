package com.mb.conitrack.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.validation.BajaProduccion;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.LoteService;

import static com.mb.conitrack.dto.DTOUtils.getLotesDtosByCodigoInterno;
import static com.mb.conitrack.enums.UnidadMedidaUtils.convertirCantidadEntreUnidades;
import static com.mb.conitrack.enums.UnidadMedidaUtils.obtenerMenorUnidadMedida;

@Controller
@RequestMapping("/produccion")
public class ProduccionController {

    @Autowired
    private LoteService loteService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    // CU7: Baja por Consumo Producción
    // @PreAuthorize("hasAuthority('ROLE_SUPERVISOR_PLANTA')")
    @GetMapping("/consumo-produccion")
    public String showConsumoProduccionForm(@ModelAttribute LoteDTO loteDTO, Model model) {
        initConsumoProducciondata(loteDTO, model);
        return "produccion/consumo-produccion";
    }

    /**
     * CU‑7 : Baja por Consumo‑Producción – Valida TODO antes de persistir el movimiento
     */
    @PostMapping("/consumo-produccion")
    public String procesarConsumoProduccion(
        @Validated(BajaProduccion.class) @ModelAttribute LoteDTO loteDTO, BindingResult bindingResult, Model model, RedirectAttributes ra) {

        if (bindingResult.hasErrors()) {
            initConsumoProducciondata(loteDTO, model);
            return "produccion/consumo-produccion";
        }

        //TODO: caso donde el lote 2/3 se haya usado, pero el 1/3 no ni el 3/3
        validarCantidades(loteDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            initConsumoProducciondata(loteDTO, model);
            return "produccion/consumo-produccion";
        }

        loteDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final List<Lote> entities = loteService.registrarConsumoProduccion(loteDTO);

        ra.addFlashAttribute("success", "Consumo registrado correctamente para la orden " + loteDTO.getOrdenProduccion());
        ra.addFlashAttribute("loteDTO", DTOUtils.fromEntities(entities));
        return "redirect:/produccion/consumo-produccion-ok";
    }

    @GetMapping("/consumo-produccion-ok")
    public String exitoMuestreo(
        @ModelAttribute LoteDTO loteDTO) {
        return "produccion/consumo-produccion-ok";
    }

    // CU11: Ingreso de Stock por Producción Interna
    // @PreAuthorize("hasAuthority('ROLE_DT')")
    @GetMapping("/cu11")
    public String ingresoProduccionInterna() {
        return "/";
    }

    // CU12: Liberación de Unidad de Venta
    // @PreAuthorize("hasAuthority('ROLE_GERENTE_GARANTIA')")
    @GetMapping("/cu12")
    public String liberacionUnidadVenta() {
        return "/";
    }

    // CU13: Baja por Venta de Producto Propio
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/cu13")
    public String ventaProducto() {
        return "/";
    }

    private void initConsumoProducciondata(final LoteDTO loteDTO, final Model model) {
        List<LoteDTO> lotesProduccion = getLotesDtosByCodigoInterno(loteService.findAllForConsumoProduccion());
        model.addAttribute("lotesProduccion", lotesProduccion);
        model.addAttribute("loteDTO", loteDTO); //  ← mantiene lo que el usuario ingresó
    }

    private void validarCantidades(final LoteDTO loteDTO, final BindingResult bindingResult) {
        List<Lote> lotes = loteService.findLoteListByCodigoInterno(loteDTO.getCodigoInterno())
            .stream()
            .filter(l -> l.getCantidadActual().compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator.comparing(Lote::getFechaIngreso).thenComparing(Lote::getCodigoInterno).thenComparing(Lote::getNroBulto))
            .toList();

        if (lotes.isEmpty()) {
            bindingResult.rejectValue("codigoInterno", "", "Lote inexistente");
        }

        // Debe venir al menos un movimiento
        final List<Integer> nroBultoList = loteDTO.getNroBultoList();
        final List<BigDecimal> cantidadesBultos = loteDTO.getCantidadesBultos();
        final List<UnidadMedidaEnum> unidadMedidaBultos = loteDTO.getUnidadMedidaBultos();

        if (cantidadesBultos == null || cantidadesBultos.isEmpty()) {
            bindingResult.rejectValue("cantidadesBultos", "", "Debe ingresar las cantidades a consumir");
        }

        if (unidadMedidaBultos == null || unidadMedidaBultos.isEmpty()) {
            bindingResult.rejectValue("cantidadesBultos", "", "Debe ingresar las unidades de medida");
        }

        for (int i = 0; i < nroBultoList.size(); i++) {
            final BigDecimal cantidadBulto = cantidadesBultos.get(i);
            if (cantidadBulto.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            final UnidadMedidaEnum uniMedidaBulto = unidadMedidaBultos.get(i);

            if (cantidadBulto == null || cantidadBulto.compareTo(BigDecimal.ZERO) <= 0) {
                bindingResult.rejectValue("cantidadesBultos", "", "La cantidad no puede ser negativa");
            }
            if (uniMedidaBulto == null) {
                bindingResult.rejectValue("cantidadesBultos", "", "Debe indicar la unidad");
            }

            final Lote lote = getLoteByByNroBulto(nroBultoList.get(i), lotes);

            if (lote.getUnidadMedida() == uniMedidaBulto) {
                if (cantidadBulto.compareTo(lote.getCantidadActual()) > 0) {
                    bindingResult.rejectValue("cantidadesBultos", "", "La cantidad no puede superar el stock actual");
                }
            } else {

                UnidadMedidaEnum menorUnidadMedida = obtenerMenorUnidadMedida(lote.getUnidadMedida(), uniMedidaBulto);

                BigDecimal cantidadBultoNormalizada = convertirCantidadEntreUnidades(uniMedidaBulto, cantidadBulto, menorUnidadMedida);
                BigDecimal cantidadLoteNormalizada = convertirCantidadEntreUnidades(lote.getUnidadMedida(), lote.getCantidadActual(), menorUnidadMedida);

                if (cantidadBultoNormalizada.compareTo(cantidadLoteNormalizada) > 0) {
                    bindingResult.rejectValue("cantidadesBultos", "", "La cantidad no puede superar el stock actual");
                }
            }
        }
    }

    private Lote getLoteByByNroBulto(final Integer nroBulto, final List<Lote> lotes) {
        return lotes.stream().filter(l -> l.getNroBulto().equals(nroBulto)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado para el número de bulto: " + nroBulto));
    }

}

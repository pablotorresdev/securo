package com.mb.conitrack.controller.cu;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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

import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.validation.AltaProduccion;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.cu.AltaIngresoProduccionService;

@Controller
@RequestMapping("/produccion/alta")
public class AltaIngresoProduccionController extends AbstractCuController {

    //TODO: Sistema FIFO (fecha reanalisis/vencimiento) para lotes que compartan el mismo producto

    @Autowired
    private AltaIngresoProduccionService ingresoProduccionService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    // CU20 Ingreso por produccion interna *****************************************************
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/ingreso-produccion")
    public String showIngresoProduccion(@ModelAttribute("loteDTO") LoteDTO loteDTO, Model model) {
        initModelIngresoProduccion(loteDTO, model);
        return "produccion/alta/ingreso-produccion";
    }

    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @PostMapping("/ingreso-produccion")
    public String ingresoProduccion(
        @Validated(AltaProduccion.class) @ModelAttribute("loteDTO") LoteDTO loteDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (!ingresoProduccionService.validarIngresoProduccionInput(loteDTO, bindingResult)) {
            initModelIngresoProduccion(loteDTO, model);
            return "produccion/alta/ingreso-produccion";
        }

        procesarIngresoProduccion(loteDTO, redirectAttributes);
        return "redirect:/produccion/alta/ingreso-produccion-ok";
    }

    @GetMapping("/ingreso-produccion-ok")
    public String exitoIngresoProduccion(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "produccion/alta/ingreso-produccion-ok"; // Template Thymeleaf
    }

    private void initModelIngresoProduccion(final LoteDTO loteDTO, final Model model) {
        model.addAttribute("productos", productoService.getProductosInternos());

        if (loteDTO.getCantidadesBultos() == null) {
            loteDTO.setCantidadesBultos(new ArrayList<>());
        }
        if (loteDTO.getUnidadMedidaBultos() == null) {
            loteDTO.setUnidadMedidaBultos(new ArrayList<>());
        }
        model.addAttribute("loteDTO", template(loteDTO));
    }

    public static LoteDTO template(LoteDTO dto) {

        dto.setFechaYHoraCreacion(null);
        dto.setCodigoLote(null);

        dto.setFechaIngreso(LocalDate.of(2025, 10, 22));
        dto.setProductoId(5L);
        dto.setCantidadInicial(new BigDecimal("10"));
        dto.setUnidadMedida(UnidadMedidaEnum.UNIDAD);
        dto.setBultosTotales(3);

        dto.setProveedorId(null);
        dto.setLoteProveedor("LP123");
        dto.setNroRemito(null);
        dto.setFabricanteId(null);
        dto.setPaisOrigen(null);
        dto.setFechaReanalisisProveedor(null);
        dto.setFechaVencimientoProveedor(null);
        dto.setDetalleConservacion("");

        dto.setFechaEgreso(null);
        dto.setOrdenProduccion("OP 123");
        dto.setObservaciones("");
        dto.setLoteOrigenId(null);

        dto.setNroBultoList(new ArrayList<>()); // []
        dto.setCantidadesBultos(new ArrayList<>(List.of(
            new BigDecimal("5"), new BigDecimal("3"), new BigDecimal("2")
        )));
        dto.setUnidadMedidaBultos(new ArrayList<>(List.of(
            UnidadMedidaEnum.UNIDAD, UnidadMedidaEnum.UNIDAD, UnidadMedidaEnum.UNIDAD
        )));

        dto.setCantidadActual(null);
        dto.setTrazado(null);
        dto.setTrazaInicial(null);

        dto.setBultosDTOs(new ArrayList<>());
        dto.setMovimientoDTOs(new ArrayList<>());
        dto.setAnalisisDTOs(new ArrayList<>());
        dto.setTrazaDTOs(new ArrayList<>());

        dto.setNombreProducto(null);
        dto.setCodigoProducto(null);
        dto.setTipoProducto(null);
        dto.setProductoDestino(null);
        dto.setNombreProveedor(null);
        dto.setNombreFabricante(null);
        dto.setDictamen(null);
        dto.setEstado(null);

        return dto;
    }


    private void procesarIngresoProduccion(final LoteDTO loteDTO, final RedirectAttributes redirectAttributes) {

        loteDTO.setFechaYHoraCreacion(OffsetDateTime.now());
        final LoteDTO resultDTO = ingresoProduccionService.altaStockPorProduccion(loteDTO);

        redirectAttributes.addFlashAttribute("loteDTO", resultDTO);
        redirectAttributes.addFlashAttribute(
            resultDTO != null ? "success" : "error",
            resultDTO != null
                ? "Ingreso de stock por produccion exitoso."
                : "Hubo un error en el ingreso de stock por produccón.");
    }

}

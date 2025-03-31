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
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.ProductoService;
import com.mb.conitrack.service.ProveedorService;

import jakarta.validation.Valid;

import static com.mb.conitrack.enums.UnidadMedidaEnum.getUnidadesConvertibles;

@Controller
@RequestMapping("/lotes")
@SessionAttributes("loteDTO")
public class LoteController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private LoteService loteService;

    @ModelAttribute("loteDTO")
    public LoteDTO getLoteDTO() {
        final LoteDTO dto = new LoteDTO();
        dto.setFechaIngreso(LocalDate.now());
        return dto;
    }

    @GetMapping("/list-lotes")
    public String listLotes(Model model) {
        model.addAttribute("lotes", loteService.findAllSortByDateAndNroBulto());
        return "lotes/list-lotes";
    }

    //Salida del CU
    @GetMapping("/cancelar")
    public String cancelarIngreso(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/";
    }

    //***************************** CU1 Ingreso por compra
    @GetMapping("/ingreso-compra")
    public String showIngresoCompraForm(
        @ModelAttribute("loteDTO") LoteDTO dto,
        Model model) {
        addInitDataToModel(model);
        return "lotes/ingreso-compra"; //.html
    }

    private void addInitDataToModel(final Model model) {
        model.addAttribute("productos", productoService.getProductosExternos());
        model.addAttribute("proveedores", proveedorService.getProveedoresExternos());
    }

    @PostMapping("/ingreso-compra")
    public String processIngresoCompra(
        @Valid @ModelAttribute("loteDTO") LoteDTO dto,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes,
        SessionStatus sessionStatus) {

        validateCantidadIngreso(dto, bindingResult);
        if (bindingResult.hasErrors()) {
            addInitDataToModel(model);
            return "lotes/ingreso-compra";
        }

        if (dto.getBultosTotales() > 1) {
            return "redirect:/lotes/distribuir-bultos?bultos=" + dto.getBultosTotales();
        }

        dto.setNroBulto(1);
        loteService.ingresarStockPorCompra(dto);
        closeSession(redirectAttributes, sessionStatus, "Ingreso de stock registrado correctamente.");
        return "redirect:/";
    }

    private static void validateCantidadIngreso(final LoteDTO dto, final BindingResult bindingResult) {
        BigDecimal cantidad = dto.getCantidadInicial();
        if (cantidad == null) {
            bindingResult.rejectValue("cantidadInicial", "error.cantidadInicial", "La cantidad no puede ser nula.");
        } else {
            if (UnidadMedidaEnum.UNIDAD.equals(dto.getUnidadMedida())) {
                if (cantidad.stripTrailingZeros().scale() > 0) {
                    bindingResult.rejectValue("cantidadInicial", "error.cantidadInicial",
                        "La cantidad debe ser un número entero positivo cuando la unidad es UNIDAD.");
                }
                if (cantidad.compareTo(new BigDecimal(dto.getBultosTotales())) < 0) {
                    bindingResult.rejectValue("bultosTotales", "error.bultosTotales",
                        "La cantidad de Unidades (" + cantidad + ") no puede ser menor a la cantidad de  Bultos totales: " + dto.getBultosTotales());
                }
            }
        }
    }

    private static void closeSession(final RedirectAttributes redirectAttributes, final SessionStatus sessionStatus, final String attributeValue) {
        redirectAttributes.addFlashAttribute("success", attributeValue);
        sessionStatus.setComplete();
    }

    //***************************** CU1 Ingreso por compra MultiBulto
    @GetMapping("/distribuir-bultos")
    public String showDistribuirBultos(
        @RequestParam("bultos") Integer bultos,
        @ModelAttribute("loteDTO") LoteDTO dto,
        Model model) {

        initBultosInLoteDto(bultos, dto, model);
        model.addAttribute("loteDTO", dto);
        if (dto.getProductoId() == null) {
            return "redirect:/lotes/ingreso-compra";
        }

        productoService.findById(dto.getProductoId()).ifPresent(p -> model.addAttribute("producto", p));
        return "lotes/distribuir-bultos";
    }

    private static void initBultosInLoteDto(final Integer bultos, final LoteDTO dto, final Model model) {
        if (dto.getUnidadMedida() != null) {
            model.addAttribute("unidadesCompatibles", getUnidadesConvertibles(dto.getUnidadMedida()));
        }
        dto.setCantidadesBultos(new ArrayList<>());
        dto.setUnidadMedidaBultos(new ArrayList<>());
        while (dto.getCantidadesBultos().size() < bultos) {
            dto.getCantidadesBultos().add(BigDecimal.ZERO);
            dto.getUnidadMedidaBultos().add(dto.getUnidadMedida());
        }
    }

    @PostMapping("/distribuir-bultos")
    public String processDistribuirBultos(
        @Valid @ModelAttribute("loteDTO") LoteDTO dto,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes,
        org.springframework.web.bind.support.SessionStatus sessionStatus) {

        validarTipoDeDato(dto, bindingResult);
        validarSumaBultosConvertida(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("unidadesCompatibles", getUnidadesConvertibles(dto.getUnidadMedida()));
            model.addAttribute("loteDTO", dto);
            productoService.findById(dto.getProductoId()).ifPresent(p -> model.addAttribute("producto", p));
            return "lotes/distribuir-bultos";
        }
        loteService.ingresarStockPorCompra(dto);
        closeSession(redirectAttributes, sessionStatus, "Ingreso de stock distribuido correctamente.");
        return "redirect:/";
    }

    private void validarTipoDeDato(final LoteDTO dto, final BindingResult bindingResult) {
        List<BigDecimal> cantidades = dto.getCantidadesBultos();
        List<UnidadMedidaEnum> unidades = dto.getUnidadMedidaBultos();
        for (int i = 0; i < cantidades.size(); i++) {
            BigDecimal cantidad = cantidades.get(i);
            if (cantidad == null) {
                bindingResult.rejectValue("cantidadInicial", "error.cantidadInicial", "La cantidad del Bulto " + (i + 1) + " no puede ser nula.");
            } else {
                if (UnidadMedidaEnum.UNIDAD.equals(unidades.get(i))) {
                    if (cantidad.stripTrailingZeros().scale() > 0) {
                        bindingResult.rejectValue("cantidadInicial", "error.cantidadInicial",
                            "La cantidad del Bulto " + (i + 1) + "  debe ser un número entero positivo cuando la unidad es UNIDAD.");
                    }
                }
            }
        }
    }

    private void validarSumaBultosConvertida(LoteDTO dto, BindingResult bindingResult) {
        List<BigDecimal> cantidades = dto.getCantidadesBultos();
        List<UnidadMedidaEnum> unidades = dto.getUnidadMedidaBultos();
        UnidadMedidaEnum unidadBase = dto.getUnidadMedida();

        if (cantidades == null || unidades == null || cantidades.size() != unidades.size()) {
            bindingResult.rejectValue("cantidadesBultos", "error.cantidadesBultos", "Datos incompletos o inconsistentes.");
            return;
        }
        BigDecimal sumaConvertida = BigDecimal.ZERO;
        for (int i = 0; i < cantidades.size(); i++) {
            BigDecimal cantidad = cantidades.get(i);
            UnidadMedidaEnum unidadBulto = unidades.get(i);
            if (cantidad == null || unidadBulto == null) {
                continue;
            }

            //assert cantidad > 0
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                bindingResult.rejectValue("cantidadesBultos", "error.cantidadesBultos", "La cantidad del Bulto " + (i + 1) + " debe ser mayor a 0.");
                return;
            }

            double factor = unidadBulto.getFactorConversion() / unidadBase.getFactorConversion();
            BigDecimal cantidadConvertida = cantidad.multiply(BigDecimal.valueOf(factor));
            sumaConvertida = sumaConvertida.add(cantidadConvertida);
        }

        //TODO: ver tema suma de cantidades
        // Redondear la suma a 3 decimales para comparar y mostrar
        BigDecimal sumaRedondeada = sumaConvertida.setScale(6, RoundingMode.HALF_UP);
        BigDecimal totalEsperado = dto.getCantidadInicial().setScale(6, RoundingMode.HALF_UP);

        if (sumaRedondeada.compareTo(totalEsperado) != 0) {
            bindingResult.rejectValue(
                "cantidadesBultos",
                "error.cantidadesBultos",
                "La suma de las cantidades individuales (" +
                    sumaRedondeada.stripTrailingZeros().toPlainString() +
                    " " +
                    unidadBase.getSimbolo() +
                    ") no coincide con la cantidad total (" +
                    totalEsperado.stripTrailingZeros().toPlainString() +
                    " " +
                    unidadBase.getSimbolo() +
                    ")."
            );
        }
    }

}



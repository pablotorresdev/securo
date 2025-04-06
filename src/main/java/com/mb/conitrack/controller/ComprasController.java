package com.mb.conitrack.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.ProductoService;
import com.mb.conitrack.service.ProveedorService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/compras")
public class ComprasController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private LoteService loteService;

    @GetMapping("/cancelar")
    public String cancelar() {
        return "redirect:/";
    }

    // CU1 Ingreso por compra *****************************************************
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/ingreso-compra")
    public String showIngresoCompra(@ModelAttribute("loteDTO") LoteDTO dto, Model model) {
        setupModelIngresoCompra(model, dto);
        return "compras/ingreso-compra";
    }

    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @PostMapping("/ingreso-compra")
    public String ingresoCompra(
        @Valid @ModelAttribute("loteDTO") LoteDTO dto,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        validateCantidadIngreso(dto, bindingResult);
        validateFechasProveedor(dto, bindingResult);
        validarBultos(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            setupModelIngresoCompra(model, dto);
            return "compras/ingreso-compra";
        }

        ingresoCompra(dto, redirectAttributes);
        return "redirect:/compras/ingreso-compra-ok";
    }

    @GetMapping("/ingreso-compra-ok")
    public String exitoIngresoCompra(
        @ModelAttribute("newLoteDTO") LoteDTO loteDTO, Model model) {
        if (loteDTO.getNombreProducto() == null) {
            return "redirect:/compras/cancelar";
        }

        model.addAttribute("loteDTO", loteDTO);
        model.addAttribute("movimientos", loteDTO.getMovimientoDTOs());

        return "compras/ingreso-compra-ok"; // Template Thymeleaf
    }

    // CU4: Baja por Devolución Compra *****************************************************
    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @GetMapping("/devolucion-compra")
    public String showDevolucionCompraForm(
        @ModelAttribute("movimientoDTO") MovimientoDTO movimientoDTO, Model model) {
        model.addAttribute("lotesDevolvibles", loteService.findAllForDevolucionCompra());
        return "compras/devolucion-compra";
    }

    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @PostMapping("/devolucion-compra")
    public String procesarDevolucionCompra(
        @Valid @ModelAttribute MovimientoDTO dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        Lote lote = loteService.findLoteBultoById(dto.getLoteId());
        if (bindingResult.hasErrors()) {
            model.addAttribute("lote", lote);
            return "compras/devolucion-compra";
        }

        dto.setFechaYHoraCreacion(LocalDateTime.now());
        final List<Lote> lotes = loteService.persistirDevolucionCompra(dto, lote);

        if (lotes.isEmpty()) {
            model.addAttribute("lote", lote);
            bindingResult.reject("error", "Error al persistir la devoluciono.");
            return "compras/devolucion-compra";
        }

        redirectAttributes.addFlashAttribute("loteDTO", DTOUtils.fromEntities(lotes));
        redirectAttributes.addFlashAttribute("success", "Devolucion realizada correctamente.");
        return "redirect:/lotes/devolucion-compra-ok";
    }

    @GetMapping("/devolucion-compra-ok")
    public String exitoMuestreo(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "compras/devolucion-compra-ok";
    }

    private void ingresoCompra(final LoteDTO dto, final RedirectAttributes redirectAttributes) {
        dto.setFechaYHoraCreacion(LocalDateTime.now());
        final List<Lote> lotes = loteService.ingresarStockPorCompra(dto);
        redirectAttributes.addFlashAttribute("newLoteDTO", DTOUtils.fromEntities(lotes));
        redirectAttributes.addFlashAttribute("success", "Ingreso de stock por compra exitoso.");
    }

    private void validateFechasProveedor(final LoteDTO dto, final BindingResult bindingResult) {
        if (dto.getFechaReanalisisProveedor() != null && dto.getFechaVencimientoProveedor() != null) {
            if (dto.getFechaReanalisisProveedor().isAfter(dto.getFechaVencimientoProveedor())) {
                bindingResult.rejectValue("fechaReanalisisProveedor", "error.fechaReanalisisProveedor",
                    "La fecha de reanálisis no puede ser posterior a la fecha de vencimiento.");
            }
        }
    }

    private void validateCantidadIngreso(final LoteDTO dto, final BindingResult bindingResult) {
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

    private void setupModelIngresoCompra(final Model model, final LoteDTO dto) {
        model.addAttribute("productos", productoService.getProductosExternos());
        model.addAttribute("proveedores", proveedorService.getProveedoresExternos());

        if (dto.getCantidadesBultos() == null) {
            dto.setCantidadesBultos(new ArrayList<>());
        }
        if (dto.getUnidadMedidaBultos() == null) {
            dto.setUnidadMedidaBultos(new ArrayList<>());
        }
        model.addAttribute("loteDTO", dto);

        String[] countryCodes = Locale.getISOCountries();
        List<String> countries = new ArrayList<>();
        for (String code : countryCodes) {
            Locale locale = new Locale("", code);
            countries.add(locale.getDisplayCountry());
        }
        countries.sort(String::compareTo);
        model.addAttribute("paises", countries);
    }

    private void validarBultos(final LoteDTO dto, final BindingResult bindingResult) {
        // Si se ingresan más de 1 bulto, se valida la distribución
        if (dto.getBultosTotales() > 1) {
            validarTipoDeDato(dto, bindingResult);
            validarSumaBultosConvertida(dto, bindingResult);
        } else {
            dto.setNroBulto(1);
        }
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

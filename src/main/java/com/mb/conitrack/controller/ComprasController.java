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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mb.conitrack.dto.DTOUtils;
import com.mb.conitrack.dto.LoteDTO;
import com.mb.conitrack.dto.MovimientoDTO;
import com.mb.conitrack.dto.validation.AltaCompra;
import com.mb.conitrack.entity.Lote;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.ProductoService;
import com.mb.conitrack.service.ProveedorService;

import jakarta.validation.Valid;

import static com.mb.conitrack.dto.DTOUtils.getLotesDtosByCodigoInterno;

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
    public String showIngresoCompra(@ModelAttribute("loteDTO") LoteDTO loteDTO, Model model) {
        setupModelIngresoCompra(model, loteDTO);
        return "compras/ingreso-compra";
    }

    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @PostMapping("/ingreso-compra")
    public String ingresoCompra(
        @Validated(AltaCompra.class) @ModelAttribute("loteDTO") LoteDTO loteDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        validateCantidadIngreso(loteDTO, bindingResult);
        validateFechasProveedor(loteDTO, bindingResult);
        validarBultos(loteDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            setupModelIngresoCompra(model, loteDTO);
            return "compras/ingreso-compra";
        }

        ingresoCompra(loteDTO, redirectAttributes);
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
        model.addAttribute("lotesDevolvibles", getLotesDtosByCodigoInterno(loteService.findAllForDevolucionCompra()));
        return "compras/devolucion-compra";
    }

    // @PreAuthorize("hasAuthority('ROLE_ANALISTA_PLANTA')")
    @PostMapping("/devolucion-compra")
    public String procesarDevolucionCompra(
        @Valid @ModelAttribute MovimientoDTO dto, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("lotesDevolvibles", getLotesDtosByCodigoInterno(loteService.findAllForDevolucionCompra()));
            return "compras/devolucion-compra";
        }

        dto.setFechaYHoraCreacion(LocalDateTime.now());
        final List<Lote> lotes = loteService.persistirDevolucionCompra(dto, dto.getCodigoInterno());

        if (lotes.isEmpty()) {
            model.addAttribute("lotesDevolvibles", getLotesDtosByCodigoInterno(loteService.findAllForDevolucionCompra()));
            bindingResult.reject("error", "Error al persistir la devoluciono.");
            return "compras/devolucion-compra";
        }

        redirectAttributes.addFlashAttribute("loteDTO", DTOUtils.fromEntities(lotes));
        redirectAttributes.addFlashAttribute("success", "Devolucion realizada correctamente.");
        return "redirect:/compras/devolucion-compra-ok";
    }

    @GetMapping("/devolucion-compra-ok")
    public String exitoMuestreo(
        @ModelAttribute("loteDTO") LoteDTO loteDTO) {
        return "compras/devolucion-compra-ok";
    }

    private void ingresoCompra(final LoteDTO loteDTO, final RedirectAttributes redirectAttributes) {
        loteDTO.setFechaYHoraCreacion(LocalDateTime.now());
        final List<Lote> lotes = loteService.ingresarStockPorCompra(loteDTO);
        redirectAttributes.addFlashAttribute("newLoteDTO", DTOUtils.fromEntities(lotes));
        redirectAttributes.addFlashAttribute("success", "Ingreso de stock por compra exitoso.");
    }

    private void validateFechasProveedor(final LoteDTO loteDTO, final BindingResult bindingResult) {
        if (loteDTO.getFechaReanalisisProveedor() != null && loteDTO.getFechaVencimientoProveedor() != null) {
            if (loteDTO.getFechaReanalisisProveedor().isAfter(loteDTO.getFechaVencimientoProveedor())) {
                bindingResult.rejectValue("fechaReanalisisProveedor", "error.fechaReanalisisProveedor",
                    "La fecha de reanálisis no puede ser posterior a la fecha de vencimiento.");
            }
        }
    }

    private void validateCantidadIngreso(final LoteDTO loteDTO, final BindingResult bindingResult) {
        BigDecimal cantidad = loteDTO.getCantidadInicial();
        if (cantidad == null) {
            bindingResult.rejectValue("cantidadInicial", "error.cantidadInicial", "La cantidad no puede ser nula.");
        } else {
            if (UnidadMedidaEnum.UNIDAD.equals(loteDTO.getUnidadMedida())) {
                if (cantidad.stripTrailingZeros().scale() > 0) {
                    bindingResult.rejectValue("cantidadInicial", "error.cantidadInicial",
                        "La cantidad debe ser un número entero positivo cuando la unidad es UNIDAD.");
                }
                if (cantidad.compareTo(new BigDecimal(loteDTO.getBultosTotales())) < 0) {
                    bindingResult.rejectValue("bultosTotales", "error.bultosTotales",
                        "La cantidad de Unidades (" + cantidad + ") no puede ser menor a la cantidad de  Bultos totales: " + loteDTO.getBultosTotales());
                }
            }
        }
    }

    private void setupModelIngresoCompra(final Model model, final LoteDTO loteDTO) {
        model.addAttribute("productos", productoService.getProductosExternos());
        model.addAttribute("proveedores", proveedorService.getProveedoresExternos());

        if (loteDTO.getCantidadesBultos() == null) {
            loteDTO.setCantidadesBultos(new ArrayList<>());
        }
        if (loteDTO.getUnidadMedidaBultos() == null) {
            loteDTO.setUnidadMedidaBultos(new ArrayList<>());
        }
        model.addAttribute("loteDTO", loteDTO);

        String[] countryCodes = Locale.getISOCountries();
        List<String> countries = new ArrayList<>();
        for (String code : countryCodes) {
            Locale locale = new Locale("", code);
            countries.add(locale.getDisplayCountry());
        }
        countries.sort(String::compareTo);
        model.addAttribute("paises", countries);
    }

    private void validarBultos(final LoteDTO loteDTO, final BindingResult bindingResult) {
        // Si se ingresan más de 1 bulto, se valida la distribución
        if (loteDTO.getBultosTotales() > 1) {
            validarTipoDeDato(loteDTO, bindingResult);
            validarSumaBultosConvertida(loteDTO, bindingResult);
        } else {
            loteDTO.setNroBulto(1);
        }
    }

    private void validarTipoDeDato(final LoteDTO loteDTO, final BindingResult bindingResult) {
        List<BigDecimal> cantidades = loteDTO.getCantidadesBultos();
        List<UnidadMedidaEnum> unidades = loteDTO.getUnidadMedidaBultos();
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

    private void validarSumaBultosConvertida(LoteDTO loteDTO, BindingResult bindingResult) {
        List<BigDecimal> cantidades = loteDTO.getCantidadesBultos();
        List<UnidadMedidaEnum> unidades = loteDTO.getUnidadMedidaBultos();
        UnidadMedidaEnum unidadBase = loteDTO.getUnidadMedida();

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
        BigDecimal totalEsperado = loteDTO.getCantidadInicial().setScale(6, RoundingMode.HALF_UP);

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

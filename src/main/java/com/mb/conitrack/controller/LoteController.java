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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mb.conitrack.dto.LoteRequestDTO;
import com.mb.conitrack.enums.UnidadMedidaEnum;
import com.mb.conitrack.service.LoteService;
import com.mb.conitrack.service.ProductoService;
import com.mb.conitrack.service.ProveedorService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/lotes")
@SessionAttributes("loteRequestDTO")
public class LoteController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private LoteService loteService;

    @ModelAttribute("loteRequestDTO")
    public LoteRequestDTO getLoteRequestDTO() {
        final LoteRequestDTO loteRequestDTO = new LoteRequestDTO();
        loteRequestDTO.setFechaIngreso(LocalDate.now());
        return loteRequestDTO;
    }

    @GetMapping("/ingreso-compra")
    public String showIngresoCompraForm(
        @ModelAttribute("loteRequestDTO") LoteRequestDTO dto,
        Model model) {
        model.addAttribute("productos", productoService.getProductosExternos());
        model.addAttribute("proveedores", proveedorService.getProveedoresExternos());
        return "lotes/ingreso-compra"; //.html
    }

    @ResponseBody
    @GetMapping("/unidades-compatibles")
    public List<UnidadMedidaEnum> getUnidadesCompatibles(@RequestParam("unidad") UnidadMedidaEnum unidad) {
        return UnidadMedidaEnum.getUnidadesCompatibles(unidad);
    }

    @PostMapping("/ingreso-compra")
    public String processIngresoCompra(
        @Valid @ModelAttribute("loteRequestDTO") LoteRequestDTO dto,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes,
        org.springframework.web.bind.support.SessionStatus sessionStatus) {

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

        if (bindingResult.hasErrors()) {
            model.addAttribute("productos", productoService.getProductosExternos());
            model.addAttribute("proveedores", proveedorService.getProveedoresExternos());
            return "lotes/ingreso-compra";
        }

        if (dto.getBultosTotales() > 1) {
            return "redirect:/lotes/distribuir-bultos?bultos=" + dto.getBultosTotales();
        }

        dto.setNroBulto(1);
        loteService.ingresarStockPorCompra(dto);
        redirectAttributes.addFlashAttribute("success", "Ingreso de stock registrado correctamente.");
        sessionStatus.setComplete();
        return "redirect:/";
    }

    @GetMapping("/distribuir-bultos")
    public String showDistribuirBultos(
        @RequestParam("bultos") Integer bultos,
        @ModelAttribute("loteRequestDTO") LoteRequestDTO dto,
        Model model) {
        if (dto.getCantidadesBultos() == null) {
            dto.setCantidadesBultos(new ArrayList<>());
            dto.setUnidadMedidaBultos(new ArrayList<>());
        }
        while (dto.getCantidadesBultos().size() < bultos) {
            dto.getCantidadesBultos().add(BigDecimal.ZERO);
            dto.getUnidadMedidaBultos().add(dto.getUnidadMedida());
        }
        if (dto.getUnidadMedida() != null) {
            model.addAttribute("unidadesCompatibles", UnidadMedidaEnum.getUnidadesCompatibles(dto.getUnidadMedida()));
        }

        model.addAttribute("loteRequestDTO", dto);
        if (dto.getProductoId() == null) {
            return "redirect:/ingreso-compra";
        }

        productoService.findById(dto.getProductoId()).ifPresent(p -> model.addAttribute("producto", p));
        return "lotes/distribuir-bultos";
    }

    @PostMapping("/distribuir-bultos")
    public String processDistribuirBultos(
        @Valid @ModelAttribute("loteRequestDTO") LoteRequestDTO dto,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes,
        org.springframework.web.bind.support.SessionStatus sessionStatus) {

        validarTipoDeDato(dto, bindingResult);
        validarSumaBultosConvertida(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute(
                "unidadesCompatibles",
                UnidadMedidaEnum.getUnidadesCompatibles(dto.getUnidadMedida()));
            model.addAttribute("loteRequestDTO", dto);
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("loteRequestDTO", dto);
            return "lotes/distribuir-bultos";
        }
        ingresoMultiBultos(dto);
        sessionStatus.setComplete();
        redirectAttributes.addFlashAttribute("success", "Ingreso de stock distribuido correctamente.");
        return "redirect:/";
    }

    private void validarTipoDeDato(final LoteRequestDTO dto, final BindingResult bindingResult) {
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

    private void validarSumaBultosConvertida(LoteRequestDTO dto, BindingResult bindingResult) {
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
                continue; // ignoramos valores nulos por seguridad
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
        BigDecimal sumaRedondeada = sumaConvertida.setScale(3, RoundingMode.HALF_UP);
        BigDecimal totalEsperado = dto.getCantidadInicial().setScale(3, RoundingMode.HALF_UP);

        if (sumaRedondeada.compareTo(totalEsperado) != 0) {
            bindingResult.rejectValue(
                "cantidadesBultos",
                "error.cantidadesBultos",
                "La suma de las cantidades individuales (" + sumaRedondeada.stripTrailingZeros().toPlainString() +
                    ") no coincide con la cantidad total (" + totalEsperado.stripTrailingZeros().toPlainString() + ")."
            );
        }
    }

    private void ingresoMultiBultos(final LoteRequestDTO dto) {
        for (int i = 0; i < dto.getBultosTotales(); i++) {
            LoteRequestDTO dtoCopia = new LoteRequestDTO();
            dtoCopia.setProductoId(dto.getProductoId());
            dtoCopia.setProveedorId(dto.getProveedorId());
            dtoCopia.setFechaIngreso(dto.getFechaIngreso());
            dtoCopia.setBultosTotales(dto.getBultosTotales()); // Cada lote representa un bulto
            dtoCopia.setCantidadInicial(dto.getCantidadesBultos().get(i));
            dtoCopia.setUnidadMedida(dto.getUnidadMedida());
            dtoCopia.setNroBulto(i + 1); // Cada lote es un bulto individual
            dtoCopia.setNroRemito(dto.getNroRemito());
            dtoCopia.setLoteProveedor(dto.getLoteProveedor());
            dtoCopia.setDetalleConservacion(dto.getDetalleConservacion());
            dtoCopia.setFechaVencimiento(dto.getFechaVencimiento());
            dtoCopia.setFechaReanalisis(dto.getFechaReanalisis());
            dtoCopia.setTitulo(dto.getTitulo());
            dtoCopia.setObservaciones(dto.getObservaciones());
            // Procesar cada lote individualmente
            loteService.ingresarStockPorCompra(dtoCopia);
        }
    }

    @GetMapping("/list-lotes")
    public String listLotes(Model model) {
        model.addAttribute("lotes", loteService.findAll());
        return "lotes/list-lotes";
    }

    @GetMapping("/cancelar")
    public String cancelarIngreso(org.springframework.web.bind.support.SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/";
    }

}



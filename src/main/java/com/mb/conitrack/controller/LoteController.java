package com.mb.conitrack.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mb.conitrack.dto.LoteRequestDTO;
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

    @PostMapping("/ingreso-compra")
    public String processIngresoCompra(
        @Valid @ModelAttribute("loteRequestDTO") LoteRequestDTO dto,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes,
        org.springframework.web.bind.support.SessionStatus sessionStatus) {

//        if (dto.getFechaIngreso() != null && dto.getFechaIngreso().isAfter(LocalDate.now())) {
//            bindingResult.rejectValue("fechaIngreso", "error.fechaIngreso", "La fecha de ingreso no puede ser futura XXX.");
//        }

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
        model.addAttribute("loteRequestDTO", dto);
        return "lotes/distribuir-bultos";
    }

    @PostMapping("/distribuir-bultos")
    public String processDistribuirBultos(
        @Valid @ModelAttribute("loteRequestDTO") LoteRequestDTO dto,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes,
        org.springframework.web.bind.support.SessionStatus sessionStatus) {

        BigDecimal suma = dto.getCantidadesBultos().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (suma.compareTo(dto.getCantidadInicial()) != 0) {
            bindingResult.rejectValue("cantidadesBultos", "error.cantidadesBultos",
                "La suma de las cantidades individuales debe ser igual a la cantidad total (" + dto.getCantidadInicial() + ").");
        }
        if (bindingResult.hasErrors()) {
            return "lotes/distribuir-bultos";
        }
        ingresoMultiBultos(dto);
        sessionStatus.setComplete();
        redirectAttributes.addFlashAttribute("success", "Ingreso de stock distribuido correctamente.");
        return "redirect:/";
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



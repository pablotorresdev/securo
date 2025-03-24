package com.mb.securo.controller;

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

import com.mb.securo.dto.LoteRequestDTO;
import com.mb.securo.entity.Lote;
import com.mb.securo.service.ContactoService;
import com.mb.securo.service.LoteService;
import com.mb.securo.service.ProductoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/lotes")
public class LoteController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ContactoService contactoService;

    @Autowired
    private LoteService loteService;

    @GetMapping("/ingreso-compra")
    public String showIngresoCompraForm(Model model) {
        model.addAttribute("loteRequestDTO", new LoteRequestDTO());
        model.addAttribute("productos", productoService.listAllProductosExternos());
        model.addAttribute("contactos", contactoService.listAllContactosExternos());
        return "lotes/ingreso-compra"; //.html
    }

    @PostMapping("/ingreso-compra")
    public String ingresarStockPorCompra(@Valid @ModelAttribute("loteRequestDTO") LoteRequestDTO loteRequestDTO,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            // Re-populate the dropdown lists if validation fails
            model.addAttribute("productos", productoService.listAllProductosExternos());
            model.addAttribute("contactos", contactoService.listAllContactosExternos());
            return "lotes/ingreso-compra";
        }
        loteService.ingresarStockPorCompra(loteRequestDTO);
        redirectAttributes.addFlashAttribute("success", "Ingreso de stock registrado correctamente.");
        return "index";
    }


    @GetMapping("/list-lotes")
    public String listLotes(Model model) {
        List<Lote> lotes = loteService.findAll(); // Método en el servicio que devuelve la lista de lotes con sus movimientos
        model.addAttribute("lotes", lotes);
        return "lotes/list-lotes"; // Nombre de la plantilla Thymeleaf, por ejemplo: src/main/resources/templates/lotes/list-lotes.html

    }

}


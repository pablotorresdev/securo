package com.mb.conitrack.controller.maestro;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mb.conitrack.entity.maestro.Proveedor;
import com.mb.conitrack.service.ProveedorService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/proveedores")
public class ABMProveedoresController {

    @Autowired
    private ProveedorService proveedorService;

    @GetMapping("/")
    public String proveedoresPage() {
        return "proveedores/index-proveedores"; //.html
    }

    @GetMapping("/list-proveedores")
    public String listProveedores(Model model) {
        model.addAttribute("proveedores", proveedorService.findAll());
        return "proveedores/list-proveedores";
    }

    @GetMapping("/add-proveedor")
    public String showAddProveedorForm(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "proveedores/add-proveedor";
    }

    @PostMapping("/add-proveedor")
    public String addProveedor(@Valid @ModelAttribute Proveedor proveedor, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("proveedores", proveedorService.getProveedoresExternos());// Load roles for dropdown
            model.addAttribute("error", "Validation failed!");
            return "proveedores/add-proveedor";
        }
        proveedor.setActivo(true);  // Aseguramos que se guarde como activo
        proveedorService.save(proveedor);
        return "redirect:/proveedores/list-proveedores";
    }

    // Mostrar el formulario para editar un proveedor
    @GetMapping("/edit-proveedor/{id}")
    public String showEditProveedorForm(@PathVariable Long id, Model model) {
        Optional<Proveedor> proveedorOptional = proveedorService.findById(id);
        if (proveedorOptional.isEmpty()) {
            model.addAttribute("error", "Proveedor not found!");
            return "redirect:/proveedores/list-proveedores";
        }

        model.addAttribute("proveedor", proveedorOptional.get());

        return "proveedores/edit-proveedor"; // Refers to edit-proveedor.html
    }

    // Procesar la actualización de un proveedor
    @PostMapping("/edit-proveedor/{id}")
    public String editProveedor(
        @PathVariable Long id,
        @Valid @ModelAttribute("proveedor") Proveedor proveedor,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            // Si hay errores de validación, se vuelve al formulario
            return "proveedores/edit-proveedor";
        }

        // Aseguramos que el id del objeto coincide con el de la URL
        proveedor.setId(id);
        proveedor.setActivo(true);  // Aseguramos que se guarde como activo
        proveedorService.save(proveedor);
        redirectAttributes.addFlashAttribute("success", "Proveedor updated successfully!");
        return "redirect:/proveedores/list-proveedores";
    }

    // Borrado lógico: se marca el registro como inactivo
    @PostMapping("/delete-proveedor")
    public String deleteProveedor(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<Proveedor> proveedorOptional = proveedorService.findById(id);
        if (proveedorOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Proveedor not found!");
            return "redirect:/proveedores/list-proveedores";
        }

        Proveedor proveedor = proveedorOptional.get();
        proveedor.setActivo(false);
        proveedorService.save(proveedor);
        return "redirect:/proveedores/list-proveedores";
    }

}

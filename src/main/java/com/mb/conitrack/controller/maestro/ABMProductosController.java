package com.mb.conitrack.controller.maestro;

import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.service.maestro.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.util.StringUtils;

import java.util.Optional;

import static java.lang.Boolean.TRUE;

@Controller
@RequestMapping("/productos")
public class ABMProductosController {

    @Autowired
    private ProductoService productoService;

    @GetMapping("/")
    public String productosPage() {
        return "productos/index-productos"; //.html
    }

    @GetMapping("/list-productos")
    public String listProductos(Model model) {
        model.addAttribute("productos", productoService.findByActivoTrueOrderByCodigoProductoAsc());
        return "productos/list-productos";
    }

    @GetMapping("/add-producto")
    public String showAddProductoForm(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("productosDestino", productoService.getProductosInternos());
        return "productos/add-producto";
    }

    @PostMapping("/add-producto")
    public String addProducto(@Valid @ModelAttribute Producto producto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("productosDestino", productoService.getProductosInternos());
            model.addAttribute("error", "Validation failed!");
            return "productos/add-producto";
        }
        producto.setActivo(true);  // Aseguramos que se guarde como activo

        final TipoProductoEnum tipoProducto = producto.getTipoProducto();
        if (tipoProducto.isRequiereProductoDestino()) {
            if (StringUtils.isEmptyOrWhitespace(producto.getProductoDestino())) {
                bindingResult.rejectValue(
                        "productoDestino",
                        "error.productoDestino",
                        "Indique el producto destino para este tipo de producto.");
                model.addAttribute("productosDestino", productoService.getProductosInternos());
                return "productos/add-producto";
            }
        } else {
            producto.setProductoDestino(null);
        }

        productoService.save(producto);
        return "redirect:/productos/list-productos";
    }

    // Mostrar el formulario para editar un producto
    @GetMapping("/edit-producto/{id}")
    public String showEditProductoForm(@PathVariable Long id, Model model) {
        Optional<Producto> productoOptional = productoService.findById(id);
        if (productoOptional.isEmpty()) {
            model.addAttribute("error", "Producto no encontrado");
            return "redirect:/productos/list-productos";
        }

        final Producto producto = productoOptional.get();
        if (!TRUE.equals(producto.getActivo())) {
            model.addAttribute("error", "Producto inactivo");
            return "redirect:/productos/list-productos";
        }

        model.addAttribute("producto", producto);
        model.addAttribute("productosDestino", productoService.getProductosInternos());

        return "productos/edit-producto"; //.html
    }

    @PostMapping("/edit-producto/{id}")
    public String editProducto(
            @PathVariable Long id,
            @Valid @ModelAttribute("producto") Producto producto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("productosDestino", productoService.getProductosInternos());
            return "productos/edit-producto";
        }

        final TipoProductoEnum tipoProducto = producto.getTipoProducto();
        if (tipoProducto.isRequiereProductoDestino()) {
            if (StringUtils.isEmptyOrWhitespace(producto.getProductoDestino())) {
                bindingResult.rejectValue(
                        "productoDestino",
                        "error.productoDestino",
                        "Indique el producto destino para este tipo de producto.");
                model.addAttribute("productosDestino", productoService.getProductosInternos());
                return "productos/add-producto";
            }
        } else {
            producto.setProductoDestino(null);
        }

        producto.setId(id);
        producto.setActivo(true);  // Aseguramos que se guarde como activo
        productoService.save(producto);
        redirectAttributes.addFlashAttribute("success", "Producto editado correctamente!");
        return "redirect:/productos/list-productos";
    }

    @PostMapping("/delete-producto")
    public String deleteProducto(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<Producto> productoOptional = productoService.findById(id);
        if (productoOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            return "redirect:/productos/list-productos";
        }

        Producto producto = productoOptional.get();
        if (!TRUE.equals(producto.getActivo())) {
            redirectAttributes.addFlashAttribute("error", "Producto ya esta inactivo");
            return "redirect:/productos/list-productos";
        }

        producto.setActivo(false);
        productoService.save(producto);
        return "redirect:/productos/list-productos";
    }

}

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
import org.thymeleaf.util.StringUtils;

import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.service.ProductoService;

import jakarta.validation.Valid;

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
        model.addAttribute("productos", productoService.findAll());
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
            if (StringUtils.isEmpty(producto.getProductoDestino())) {
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
        if (!producto.getActivo()) {
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

        //TODO: validar logica de producto de destino
        //        final TipoProductoEnum tipoProducto = producto.getTipoProducto();
        //        if(tipoProducto.requiereProductoDestino()) {
        //            if (producto.getProductoDestino() == null) {
        //                bindingResult.rejectValue("productoDestino", "error.productoDestino", "Indique el producto destino para este tipo de producto.");
        //                model.addAttribute("productosDestino", productoService.getProductosDestinoActive());
        //                return "productos/edit-producto";
        //            }
        //        }
        //        if(!tipoProducto.requiereProductoDestino()) {
        //            producto.setProductoDestino(null);
        //        }

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
        if (!producto.getActivo()) {
            redirectAttributes.addFlashAttribute("error", "Producto ya esta inactivo");
            return "redirect:/productos/list-productos";
        }

        producto.setActivo(false);
        productoService.save(producto);
        return "redirect:/productos/list-productos";
    }

}

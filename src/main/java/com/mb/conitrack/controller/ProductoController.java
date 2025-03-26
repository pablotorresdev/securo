package com.mb.conitrack.controller;

import java.util.Arrays;
import java.util.List;
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

import com.mb.conitrack.entity.maestro.Producto;
import com.mb.conitrack.enums.TipoProductoEnum;
import com.mb.conitrack.repository.maestro.ProductoRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping("/")
    public String productosPage() {
        return "productos/index-productos"; //.html
    }

    // Listar todos los productos activos
    @GetMapping("/list-productos")
    public String listProductos(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        return "productos/list-productos";
    }

    // Mostrar el formulario para dar de alta un nuevo producto
    @GetMapping("/add-producto")
    public String showAddProductoForm(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("productosDestino", getProductosDestino());
        return "productos/add-producto";  // Ubicación: src/main/resources/templates/producto/add-producto.html
    }

    private List<Producto> getProductosDestino() {
        return productoRepository.findByTipoProductoIn(
            Arrays.asList(TipoProductoEnum.SEMIELABORADO, TipoProductoEnum.UNIDAD_VENTA)
        );
    }

    // Procesar el alta del producto
    @PostMapping("/add-producto")
    public String addProducto(@Valid @ModelAttribute Producto producto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("productos", productoRepository.findAll());// Load roles for dropdown
            model.addAttribute("productosDestino", getProductosDestino());
            model.addAttribute("error", "Validation failed!");
            return "productos/add-producto";
        }
        producto.setActivo(true);  // Aseguramos que se guarde como activo

        final TipoProductoEnum tipoProducto = producto.getTipoProducto();
        if(tipoProducto.requiereProductoDestino()) {
            if (producto.getProductoDestino() == null) {
                model.addAttribute("productos", productoRepository.findAll());// Load roles for dropdown
                model.addAttribute("productosDestino", getProductosDestino());
                model.addAttribute("error", "Indique el producto destino para este tipo de producto.");
                return "productos/add-producto";
            }
        }
        if(!tipoProducto.requiereProductoDestino()) {
            producto.setProductoDestino(null);
        }

        productoRepository.save(producto);
        return "redirect:/productos/list-productos";
    }

    // Mostrar el formulario para editar un producto
    @GetMapping("/edit-producto/{id}")
    public String showEditProductoForm(@PathVariable Long id, Model model) {
        Optional<Producto> productoOptional = productoRepository.findById(id);
        if (productoOptional.isEmpty()) {
            model.addAttribute("error", "Producto not found!");
            return "redirect:/productos/list-productos";
        }

        model.addAttribute("producto", productoOptional.get());
        model.addAttribute("productosDestino", getProductosDestino());

        return "productos/edit-producto"; // Refers to edit-producto.html
    }

    // Procesar la actualización de un producto
    @PostMapping("/edit-producto/{id}")
    public String editProducto(
        @PathVariable Long id,
        @Valid @ModelAttribute("producto") Producto producto,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            // Aqui necesito meter los datos de los productos destino
            model.addAttribute("productosDestino", getProductosDestino());
            return "productos/edit-producto";
        }

        final TipoProductoEnum tipoProducto = producto.getTipoProducto();
        if(tipoProducto.requiereProductoDestino()) {
            if (producto.getProductoDestino() == null) {
                model.addAttribute("productosDestino", getProductosDestino());
                model.addAttribute("error", "Indique el producto destino para este tipo de producto.");
                return "productos/edit-producto";
            }
        }
        if(!tipoProducto.requiereProductoDestino()) {
            producto.setProductoDestino(null);
        }

        // Aseguramos que el id del objeto coincide con el de la URL
        producto.setId(id);
        producto.setActivo(true);  // Aseguramos que se guarde como activo
        productoRepository.save(producto);
        redirectAttributes.addFlashAttribute("success", "Producto updated successfully!");
        return "redirect:/productos/list-productos";
    }

    // Borrado lógico: se marca el registro como inactivo
    @PostMapping("/delete-producto")
    public String deleteProducto(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<Producto> productoOptional = productoRepository.findById(id);
        if (productoOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Producto not found!");
            return "redirect:/productos/list-productos";
        }

        Producto producto = productoOptional.get();
        producto.setActivo(false);
        productoRepository.save(producto);
        return "redirect:/productos/list-productos";
    }

}

package com.mb.securo.controller;

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

import com.mb.securo.entity.maestro.Contacto;
import com.mb.securo.repository.maestro.ContactoRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/contactos")
public class ContactoController {

    @Autowired
    private ContactoRepository contactoRepository;

    @GetMapping("/")
    public String contactosPage() {
        return "contactos/index-contactos"; //.html
    }

    // Listar todos los contactos activos
    @GetMapping("/list-contactos")
    public String listContactos(Model model) {
        List<Contacto> contactos = contactoRepository.findAll();
        model.addAttribute("contactos", contactos);
        return "contactos/list-contactos";
    }

    // Mostrar el formulario para dar de alta un nuevo contacto
    @GetMapping("/add-contacto")
    public String showAddContactoForm(Model model) {
        model.addAttribute("contacto", new Contacto());
        return "contactos/add-contacto";  // Ubicación: src/main/resources/templates/contacto/add-contacto.html
    }

    // Procesar el alta del contacto
    @PostMapping("/add-contacto")
    public String addContacto(@Valid @ModelAttribute Contacto contacto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("contactos", contactoRepository.findAll());// Load roles for dropdown
            model.addAttribute("error", "Validation failed!");
            return "contactos/add-contacto";
        }
        contacto.setActivo(true);  // Aseguramos que se guarde como activo
        contactoRepository.save(contacto);
        return "redirect:/contactos/list-contactos";
    }

    // Mostrar el formulario para editar un contacto
    @GetMapping("/edit-contacto/{id}")
    public String showEditContactoForm(@PathVariable Long id, Model model) {
        Optional<Contacto> contactoOptional = contactoRepository.findById(id);
        if (contactoOptional.isEmpty()) {
            model.addAttribute("error", "Contacto not found!");
            return "redirect:/contactos/list-contactos";
        }

        model.addAttribute("contacto", contactoOptional.get());

        return "contactos/edit-contacto"; // Refers to edit-contacto.html
    }

    // Procesar la actualización de un contacto
    @PostMapping("/edit-contacto/{id}")
    public String editContacto(
        @PathVariable Long id,
        @Valid @ModelAttribute("contacto") Contacto contacto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            // Si hay errores de validación, se vuelve al formulario
            return "contactos/edit-contacto";
        }

        // Aseguramos que el id del objeto coincide con el de la URL
        contacto.setId(id);
        contacto.setActivo(true);  // Aseguramos que se guarde como activo
        contactoRepository.save(contacto);
        redirectAttributes.addFlashAttribute("success", "Contacto updated successfully!");
        return "redirect:/contactos/list-contactos";
    }

    // Borrado lógico: se marca el registro como inactivo
    @PostMapping("/delete-contacto")
    public String deleteContacto(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<Contacto> contactoOptional = contactoRepository.findById(id);
        if (contactoOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Contacto not found!");
            return "redirect:/contactos/list-contactos";
        }

        Contacto contacto = contactoOptional.get();
        contacto.setActivo(false);
        contactoRepository.save(contacto);
        return "redirect:/contactos/list-contactos";
    }

}

package com.mb.conitrack.controller.maestro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.repository.maestro.UserRepository;
import com.mb.conitrack.service.SecurityContextService;

import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para el cambio de contraseña del usuario.
 */
@Controller
@RequestMapping("/users")
@Slf4j
public class PasswordChangeController {

    @Autowired
    private SecurityContextService securityContextService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Muestra el formulario de cambio de contraseña.
     */
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        User currentUser = securityContextService.getCurrentUser();
        model.addAttribute("username", currentUser.getUsername());
        return "users/change-password";
    }

    /**
     * Procesa el cambio de contraseña.
     */
    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        try {
            // Obtener usuario actual
            User currentUser = securityContextService.getCurrentUser();

            // Validar contraseña actual
            if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta");
                return "redirect:/users/change-password";
            }

            // Validar que las contraseñas nuevas coincidan
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas nuevas no coinciden");
                return "redirect:/users/change-password";
            }

            // Validar longitud mínima
            if (newPassword.length() < 3) {
                redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 3 caracteres");
                return "redirect:/users/change-password";
            }

            // Validar que la nueva contraseña sea diferente a la actual
            if (currentPassword.equals(newPassword)) {
                redirectAttributes.addFlashAttribute("error", "La nueva contraseña debe ser diferente a la actual");
                return "redirect:/users/change-password";
            }

            // Actualizar contraseña
            currentUser.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(currentUser);

            log.info("Password changed successfully for user: {}", currentUser.getUsername());

            redirectAttributes.addFlashAttribute("success", "Contraseña cambiada exitosamente");
            return "redirect:/";

        } catch (Exception e) {
            log.error("Error changing password", e);
            redirectAttributes.addFlashAttribute("error", "Error al cambiar la contraseña");
            return "redirect:/users/change-password";
        }
    }
}

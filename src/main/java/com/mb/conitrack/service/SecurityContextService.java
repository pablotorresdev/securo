package com.mb.conitrack.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.repository.maestro.UserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para obtener información del usuario actual desde Spring Security.
 * Centraliza el acceso al SecurityContext para facilitar testing y mantenimiento.
 */
@Service
@Slf4j
public class SecurityContextService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtiene el usuario actualmente autenticado.
     * @return User entity del usuario actual
     * @throws SecurityException si no hay usuario autenticado o no existe en BD
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("No hay usuario autenticado en el contexto de seguridad");
            throw new SecurityException("No hay usuario autenticado");
        }

        String username = authentication.getName();
        log.debug("Obteniendo usuario actual: {}", username);

        return userRepository.findByUsername(username)
            .orElseThrow(() -> {
                log.error("Usuario autenticado no encontrado en BD: {}", username);
                return new SecurityException(
                    "Usuario autenticado no encontrado en base de datos: " + username);
            });
    }

    /**
     * Obtiene el username del usuario actual.
     * @return Username del usuario autenticado
     * @throws SecurityException si no hay usuario autenticado
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("No hay usuario autenticado");
        }

        return authentication.getName();
    }

    /**
     * Verifica si hay un usuario autenticado.
     * @return true si hay usuario autenticado
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();

        return authentication != null && authentication.isAuthenticated()
               && !"anonymousUser".equals(authentication.getName());
    }

    /**
     * Verifica si el usuario actual tiene un rol específico.
     * @param roleName Nombre del rol (ej: "ADMIN", "AUDITOR")
     * @return true si el usuario tiene ese rol
     */
    public boolean hasRole(String roleName) {
        if (!isAuthenticated()) {
            return false;
        }

        try {
            User user = getCurrentUser();
            return roleName.equals(user.getRole().getName());
        } catch (Exception e) {
            log.error("Error verificando rol: {}", e.getMessage());
            return false;
        }
    }
}

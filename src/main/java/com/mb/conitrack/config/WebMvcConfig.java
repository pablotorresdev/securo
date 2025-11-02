package com.mb.conitrack.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mb.conitrack.interceptor.AuditorAccessInterceptor;

/**
 * Configuración de Spring MVC para registrar interceptors.
 *
 * Registra el AuditorAccessInterceptor para logging automático
 * de accesos de usuarios AUDITOR.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuditorAccessInterceptor auditorAccessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditorAccessInterceptor)
                .addPathPatterns("/api/**") // Aplicar a todas las rutas de API
                .excludePathPatterns(
                    "/api/public/**",  // Excluir rutas públicas si existen
                    "/css/**",
                    "/js/**",
                    "/img/**"
                );
    }
}

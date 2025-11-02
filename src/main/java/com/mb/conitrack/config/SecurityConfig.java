package com.mb.conitrack.config;

import com.mb.conitrack.enums.PermisosCasoUsoEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Configuración de Spring Security con soporte para jerarquía de usuarios y permisos por caso de uso.
 *
 * Características:
 * - Control de acceso basado en roles por cada caso de uso
 * - AUDITOR: solo acceso a consultas y reportes (read-only)
 * - ADMIN: acceso total incluyendo ABM de usuarios
 * - Cada rol tiene acceso solo a sus casos de uso asignados
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // necesario para que JS pueda acceder
            )
            .authorizeHttpRequests(auth -> {
                // Recursos públicos
                auth.requestMatchers("/css/**", "/js/**", "/img/**").permitAll()
                    .requestMatchers("/api").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/error").permitAll();

                // ===== CASOS DE USO - Configuración desde enum =====
                // Aplicar permisos para cada caso de uso definido
                for (PermisosCasoUsoEnum permiso : PermisosCasoUsoEnum.values()) {
                    auth.requestMatchers(permiso.getUrlPatternWithWildcard())
                        .hasAnyRole(permiso.getRolesAsStringArray());
                }

                // ===== ABM USUARIOS - Solo ADMIN =====
                auth.requestMatchers("/admin/users/**").hasRole("ADMIN")
                    .requestMatchers("/users/**").hasRole("ADMIN");

                // ===== API ENDPOINTS =====
                // Reportes: acceso para todos los roles operativos (incluyendo AUDITOR)
                auth.requestMatchers("/api/reportes/**").hasAnyRole(
                        "AUDITOR",
                        "ADMIN",
                        "DT",
                        "GERENTE_GARANTIA_CALIDAD",
                        "GERENTE_CONTROL_CALIDAD",
                        "SUPERVISOR_PLANTA",
                        "ANALISTA_CONTROL_CALIDAD",
                        "ANALISTA_PLANTA"
                    );

                // Operaciones de ABM API: PROHIBIDO para AUDITOR
                auth.requestMatchers("/api/lotes/**", "/api/movimientos/**", "/api/bultos/**")
                        .hasAnyRole(
                            "ADMIN",
                            "DT",
                            "GERENTE_GARANTIA_CALIDAD",
                            "GERENTE_CONTROL_CALIDAD",
                            "SUPERVISOR_PLANTA",
                            "ANALISTA_CONTROL_CALIDAD",
                            "ANALISTA_PLANTA"
                        );

                // Operaciones de reverso: verificadas a nivel de servicio con ReversoAuthorizationService
                auth.requestMatchers("/api/movimientos/reverso").hasAnyRole(
                        "ADMIN",
                        "DT",
                        "GERENTE_GARANTIA_CALIDAD",
                        "GERENTE_CONTROL_CALIDAD",
                        "SUPERVISOR_PLANTA",
                        "ANALISTA_CONTROL_CALIDAD",
                        "ANALISTA_PLANTA"
                    );

                // Backwards compatibility con roles antiguos (USER1, USER2)
                auth.requestMatchers("/user1/**").hasAnyRole("ADMIN", "USER1")
                    .requestMatchers("/user2/**").hasAnyRole("ADMIN", "USER2");

                // Todo lo demás requiere autenticación
                auth.anyRequest().authenticated();
            })
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

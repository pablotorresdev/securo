package com.conistock.securo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            //            .csrf(csrf -> csrf.disable()) //CSRF protege contra comandos maliciosos enviados desde navegadores autenticados. Deshabilitado para simplificar en desarrollo, debe habilitarse en producción
            //            .csrf(csrf -> csrf
            //                .ignoringRequestMatchers("/api/**") // Ignorar rutas que empiezan con /api/
            //            )
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated() // Todas las rutas requieren autenticación
            )
            .formLogin(form -> form
                .loginPage("/login") // Página de login personalizada
                .defaultSuccessUrl("/", true) // Redirige siempre a / después del login
                .permitAll() // Permitir acceso al login sin autenticación
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // Endpoint para cerrar sesión
                .logoutSuccessUrl("/login?logout") // Redirige al login después de cerrar sesión
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
            .username("admin")
            .password("admin")
            .roles("USER")
            .build();

        return new InMemoryUserDetailsManager(user);
    }

}


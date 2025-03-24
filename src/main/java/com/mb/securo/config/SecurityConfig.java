package com.mb.securo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers("/api").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/error").permitAll() // Allow access to /error
                .requestMatchers("/users/**").hasRole("ADMIN") // Only ADMIN can access /users/*
                .requestMatchers("/contactos/**").hasRole("ADMIN") // Only ADMIN can access /contactos/*
                //.requestMatchers("/admin/users").hasRole("ADMIN") // Only ADMIN can access /admin/users
                .requestMatchers("/user1/**").hasAnyRole("ADMIN", "USER1") // Only ADMIN and USER1 can access /user1/*
                .requestMatchers("/user2/**").hasAnyRole("ADMIN", "USER2") // Only ADMIN and USER2 can access /user2/*
                //.anyRequest().authenticated() // All other requests require authentication
                .anyRequest().permitAll() // Permit all for development
            )
            .formLogin(form -> form
                .loginPage("/login") // Custom login page
                .defaultSuccessUrl("/", true) // Redirect to "/" after login
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
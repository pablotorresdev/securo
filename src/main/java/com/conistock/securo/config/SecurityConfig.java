package com.conistock.securo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll() // Allow access to /error
                .requestMatchers("/admin/**").hasRole("ADMIN") // Only ADMIN can access /admin/*
                .requestMatchers("/user1/**").hasAnyRole("ADMIN","USER1") // Only ADMIN and USER1 can access /user1/*
                .requestMatchers("/user2/**").hasAnyRole("ADMIN","USER2") // Only ADMIN and USER2 can access /user2/*
                .anyRequest().authenticated() // All other requests require authentication
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
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder.encode("admin"))
            .roles("ADMIN")
            .build();

        UserDetails user1 = User.builder()
            .username("user1")
            .password(passwordEncoder.encode("user1"))
            .roles("USER1")
            .build();

        UserDetails user2 = User.builder()
            .username("user2")
            .password(passwordEncoder.encode("user2"))
            .roles("USER2")
            .build();

        return new InMemoryUserDetailsManager(admin, user1, user2);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
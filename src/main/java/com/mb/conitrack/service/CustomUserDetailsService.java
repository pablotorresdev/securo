package com.mb.conitrack.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.repository.maestro.RoleRepository;
import com.mb.conitrack.repository.maestro.UserRepository;

import jakarta.annotation.PostConstruct;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    public CustomUserDetailsService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .roles(user.getRole().getName()) // Extraer el nombre del rol
            .build();
    }

    @PostConstruct
    public void saveDefaultUsers() {
        Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> roleRepository.save(new Role("ADMIN")));
        Role userRole1 = roleRepository.findByName("USER1").orElseGet(() -> roleRepository.save(new Role("USER1")));
        Role userRole2 = roleRepository.findByName("USER2").orElseGet(() -> roleRepository.save(new Role("USER2")));

        if (userRepository.findByUsername("admin").isEmpty()) {
            userRepository.save(new User("admin", passwordEncoder().encode("admin"), adminRole));
        }
        if (userRepository.findByUsername("pablo").isEmpty()) {
            userRepository.save(new User("pablo", passwordEncoder().encode("pablo"), adminRole));
        }
        if (userRepository.findByUsername("user1").isEmpty()) {
            userRepository.save(new User("user1", passwordEncoder().encode("user1"), userRole1));
        }
        if (userRepository.findByUsername("user2").isEmpty()) {
            userRepository.save(new User("user2", passwordEncoder().encode("user2"), userRole2));
        }
    }

    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Use a private static encoder here
    }

}

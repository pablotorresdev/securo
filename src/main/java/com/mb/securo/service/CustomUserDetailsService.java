package com.mb.securo.service;

import com.mb.securo.entity.User;
import com.mb.securo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .roles(user.getRole())
            .build();
    }

    @PostConstruct
    public void saveDefaultUsers() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            userRepository.save(new User("admin", passwordEncoder().encode("admin"), "ADMIN"));
        }
        if (userRepository.findByUsername("user1").isEmpty()) {
            userRepository.save(new User("user1", passwordEncoder().encode("user1"), "USER1"));
        }
        if (userRepository.findByUsername("user2").isEmpty()) {
            userRepository.save(new User("user2", passwordEncoder().encode("user2"), "USER2"));
        }
    }

    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Use a private static encoder here
    }
}

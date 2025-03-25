package com.mb.conitrack.service;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.repository.maestro.RoleRepository;
import com.mb.conitrack.repository.maestro.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    private UserRepository userRepository;

    private RoleRepository roleRepository;

    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        roleRepository = Mockito.mock(RoleRepository.class);
        customUserDetailsService = new CustomUserDetailsService(userRepository, roleRepository);
    }

    @Test
    void testLoadUserByUsername_UserExists() {
        User user = new User("admin", "encodedPassword", new Role("ADMIN"));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin");

        assertEquals("admin", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Exception exception = assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername("nonexistent"));

        assertEquals("User not found: nonexistent", exception.getMessage());
    }

    @Test
    void testSaveDefaultUsers_DefaultUsersAdded() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("user2")).thenReturn(Optional.empty());

        customUserDetailsService.saveDefaultUsers();

        verify(userRepository, times(1)).save(argThat(user -> user.getUsername().equals("admin")));
        verify(userRepository, times(1)).save(argThat(user -> user.getUsername().equals("user1")));
        verify(userRepository, times(1)).save(argThat(user -> user.getUsername().equals("user2")));
    }

}

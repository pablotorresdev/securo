package com.mb.conitrack.service.maestro;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.repository.maestro.RoleRepository;
import com.mb.conitrack.repository.maestro.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for package-protected methods in CustomUserDetailsService.
 * This test class is in the same package to access package-protected methods.
 */
@DisplayName("CustomUserDetailsService Package Tests")
class CustomUserDetailsServicePackageTest {

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
    @DisplayName("createOrUpdateRole debe crear nuevo rol cuando no existe")
    void testCreateOrUpdateRole_CreatesNewRole() {
        // Arrange
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
        Role savedRole = Role.fromEnum(RoleEnum.ADMIN);
        savedRole.setId(1L);
        when(roleRepository.save(Mockito.any(Role.class))).thenReturn(savedRole);

        // Act
        Role result = customUserDetailsService.createOrUpdateRole(RoleEnum.ADMIN);

        // Assert
        assertEquals("ADMIN", result.getName());
        assertEquals(6, result.getNivel());
        verify(roleRepository, times(1)).save(Mockito.any(Role.class));
    }

    @Test
    @DisplayName("createOrUpdateRole debe actualizar rol existente con nivel diferente")
    void testCreateOrUpdateRole_UpdatesExistingRoleWithDifferentLevel() {
        // Arrange
        Role existingRole = Role.fromEnum(RoleEnum.ADMIN);
        existingRole.setId(1L);
        existingRole.setNivel(5); // Wrong level (should be 6)

        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(existingRole));
        when(roleRepository.save(Mockito.any(Role.class))).thenReturn(existingRole);

        // Act
        Role result = customUserDetailsService.createOrUpdateRole(RoleEnum.ADMIN);

        // Assert
        assertEquals("ADMIN", result.getName());
        assertEquals(6, result.getNivel()); // Should be updated to correct level
        verify(roleRepository, times(1)).save(existingRole);
    }

    @Test
    @DisplayName("createOrUpdateRole debe mantener rol existente con mismo nivel")
    void testCreateOrUpdateRole_KeepsExistingRoleWithSameLevel() {
        // Arrange
        Role existingRole = Role.fromEnum(RoleEnum.ADMIN);
        existingRole.setId(1L);
        existingRole.setNivel(6); // Correct level

        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(existingRole));

        // Act
        Role result = customUserDetailsService.createOrUpdateRole(RoleEnum.ADMIN);

        // Assert
        assertEquals("ADMIN", result.getName());
        assertEquals(6, result.getNivel());
        verify(roleRepository, times(0)).save(Mockito.any(Role.class)); // Should NOT save
    }

    @Test
    @DisplayName("createUserIfNotExists debe crear nuevo usuario cuando no existe")
    void testCreateUserIfNotExists_CreatesNewUser() {
        // Arrange
        Role role = Role.fromEnum(RoleEnum.ADMIN);
        role.setId(1L);

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        customUserDetailsService.createUserIfNotExists("newuser", "password", role);

        // Assert
        verify(userRepository, times(1)).save(Mockito.argThat(user ->
            user.getUsername().equals("newuser") &&
            user.getRole().equals(role)
        ));
    }

    @Test
    @DisplayName("createUserIfNotExists no debe crear usuario existente")
    void testCreateUserIfNotExists_DoesNotCreateExistingUser() {
        // Arrange
        Role role = Role.fromEnum(RoleEnum.ADMIN);
        role.setId(1L);
        User existingUser = new User("existinguser", "encodedPassword", role);

        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        // Act
        customUserDetailsService.createUserIfNotExists("existinguser", "password", role);

        // Assert
        verify(userRepository, times(0)).save(Mockito.any(User.class)); // Should NOT save
    }

    @Test
    @DisplayName("passwordEncoder debe retornar BCryptPasswordEncoder válido")
    void testPasswordEncoder_ReturnsValidEncoder() {
        // Act
        PasswordEncoder encoder = customUserDetailsService.passwordEncoder();

        // Assert
        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);

        // Verify it actually encodes passwords
        String encoded = encoder.encode("test");
        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$2"));  // BCrypt hash starts with $2a, $2b, or $2y
    }
}

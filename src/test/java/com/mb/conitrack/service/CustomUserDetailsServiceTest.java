package com.mb.conitrack.service;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.repository.maestro.RoleRepository;
import com.mb.conitrack.repository.maestro.UserRepository;
import com.mb.conitrack.service.maestro.CustomUserDetailsService;

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
        // Mock roleRepository to return roles when findByName is called
        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setId(1L);
        Role dtRole = Role.fromEnum(RoleEnum.DT);
        dtRole.setId(2L);
        Role gerenteGarantiaRole = Role.fromEnum(RoleEnum.GERENTE_GARANTIA_CALIDAD);
        gerenteGarantiaRole.setId(3L);
        Role gerenteControlRole = Role.fromEnum(RoleEnum.GERENTE_CONTROL_CALIDAD);
        gerenteControlRole.setId(4L);
        Role supervisorRole = Role.fromEnum(RoleEnum.SUPERVISOR_PLANTA);
        supervisorRole.setId(5L);
        Role analistaControlRole = Role.fromEnum(RoleEnum.ANALISTA_CONTROL_CALIDAD);
        analistaControlRole.setId(6L);
        Role analistaPlantaRole = Role.fromEnum(RoleEnum.ANALISTA_PLANTA);
        analistaPlantaRole.setId(7L);
        Role auditorRole = Role.fromEnum(RoleEnum.AUDITOR);
        auditorRole.setId(8L);

        // Mock roleRepository.findByName to return empty (so save will be called)
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.findByName("DT")).thenReturn(Optional.empty());
        when(roleRepository.findByName("GERENTE_GARANTIA_CALIDAD")).thenReturn(Optional.empty());
        when(roleRepository.findByName("GERENTE_CONTROL_CALIDAD")).thenReturn(Optional.empty());
        when(roleRepository.findByName("SUPERVISOR_PLANTA")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ANALISTA_CONTROL_CALIDAD")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ANALISTA_PLANTA")).thenReturn(Optional.empty());
        when(roleRepository.findByName("AUDITOR")).thenReturn(Optional.empty());

        // Mock roleRepository.save to return the roles with IDs
        when(roleRepository.save(Mockito.any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            if (role.getId() == null) {
                role.setId(1L); // Assign a dummy ID
            }
            return role;
        });

        // Mock userRepository.findByUsername to return empty for all users
        when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.empty());

        customUserDetailsService.saveDefaultUsers();

        // Verify that admin user was created
        verify(userRepository, times(1)).save(argThat(user -> user.getUsername().equals("admin")));
        // Verify that test users were created
        verify(userRepository, times(1)).save(argThat(user -> user.getUsername().equals("user_dt")));
        verify(userRepository, times(1)).save(argThat(user -> user.getUsername().equals("user_auditor")));
    }

}

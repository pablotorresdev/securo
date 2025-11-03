package com.mb.conitrack.controller.maestro;

import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.enums.RoleEnum;
import com.mb.conitrack.repository.maestro.UserRepository;
import com.mb.conitrack.service.SecurityContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordChangeController Tests")
class PasswordChangeControllerTest {

    @Mock
    private SecurityContextService securityContextService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordChangeController controller;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        // Setup test user
        Role adminRole = Role.fromEnum(RoleEnum.ADMIN);
        adminRole.setId(1L);
        testUser = new User("testuser", "encodedOldPassword", adminRole);
        testUser.setId(1L);

        when(securityContextService.getCurrentUser()).thenReturn(testUser);
    }

    @Test
    @DisplayName("GET /users/change-password - Debe mostrar formulario con username")
    void testShowChangePasswordForm() throws Exception {
        mockMvc.perform(get("/users/change-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/change-password"))
                .andExpect(model().attribute("username", "testuser"));

        verify(securityContextService).getCurrentUser();
    }

    @Test
    @DisplayName("POST /users/change-password - Cambio exitoso con contraseña válida")
    void testChangePassword_Success() throws Exception {
        // Arrange
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/users/change-password")
                        .param("currentPassword", "oldPassword")
                        .param("newPassword", "newPassword123")
                        .param("confirmPassword", "newPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("success", "Contraseña cambiada exitosamente"));

        // Verify password was updated
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    @DisplayName("POST /users/change-password - Rechazar cuando contraseña actual es incorrecta")
    void testChangePassword_CurrentPasswordIncorrect() throws Exception {
        // Arrange
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/users/change-password")
                        .param("currentPassword", "wrongPassword")
                        .param("newPassword", "newPassword123")
                        .param("confirmPassword", "newPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/change-password"))
                .andExpect(flash().attribute("error", "La contraseña actual es incorrecta"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("POST /users/change-password - Rechazar cuando contraseñas nuevas no coinciden")
    void testChangePassword_NewPasswordsDoNotMatch() throws Exception {
        // Arrange
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/users/change-password")
                        .param("currentPassword", "oldPassword")
                        .param("newPassword", "newPassword123")
                        .param("confirmPassword", "differentPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/change-password"))
                .andExpect(flash().attribute("error", "Las contraseñas nuevas no coinciden"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("POST /users/change-password - Rechazar cuando contraseña es muy corta")
    void testChangePassword_PasswordTooShort() throws Exception {
        // Arrange
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/users/change-password")
                        .param("currentPassword", "oldPassword")
                        .param("newPassword", "ab")  // Solo 2 caracteres
                        .param("confirmPassword", "ab"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/change-password"))
                .andExpect(flash().attribute("error", "La contraseña debe tener al menos 3 caracteres"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("POST /users/change-password - Rechazar cuando nueva contraseña es igual a la actual")
    void testChangePassword_NewPasswordSameAsOld() throws Exception {
        // Arrange
        when(passwordEncoder.matches("samePassword", "encodedOldPassword")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/users/change-password")
                        .param("currentPassword", "samePassword")
                        .param("newPassword", "samePassword")
                        .param("confirmPassword", "samePassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/change-password"))
                .andExpect(flash().attribute("error", "La nueva contraseña debe ser diferente a la actual"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("POST /users/change-password - Manejar excepción durante guardado")
    void testChangePassword_ExceptionDuringSave() throws Exception {
        // Arrange
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/users/change-password")
                        .param("currentPassword", "oldPassword")
                        .param("newPassword", "newPassword123")
                        .param("confirmPassword", "newPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/change-password"))
                .andExpect(flash().attribute("error", "Error al cambiar la contraseña"));
    }

    @Test
    @DisplayName("POST /users/change-password - Cambio exitoso con contraseña de exactamente 3 caracteres")
    void testChangePassword_PasswordExactlyThreeCharacters() throws Exception {
        // Arrange
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("abc")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/users/change-password")
                        .param("currentPassword", "oldPassword")
                        .param("newPassword", "abc")  // Exactamente 3 caracteres (mínimo)
                        .param("confirmPassword", "abc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("success", "Contraseña cambiada exitosamente"));

        verify(userRepository).save(any(User.class));
    }
}

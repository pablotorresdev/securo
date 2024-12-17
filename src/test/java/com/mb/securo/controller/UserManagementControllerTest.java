package com.mb.securo.controller;

import com.mb.securo.entity.User;
import com.mb.securo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserManagementControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserManagementController controller;

    private Model model;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        model = new ConcurrentModel();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    @Test
    void listUsers() {
        // Arrange
        List<User> users = List.of(new User("user1", "password", "ROLE_USER"));
        when(userRepository.findAll()).thenReturn(users);

        // Act
        String viewName = controller.listUsers(model);

        // Assert
        assertThat(viewName).isEqualTo("admin/users");
        assertThat(model.getAttribute("users")).isEqualTo(users);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void showAddUserForm() {
        // Act
        String viewName = controller.showAddUserForm(model);

        // Assert
        assertThat(viewName).isEqualTo("admin/add-user");
    }

    @Test
    void addUser_UserAlreadyExists() {
        // Arrange
        User user = new User("user1", "password", "ROLE_USER");
        BindingResult bindingResult = mock(BindingResult.class);

        // Simulate no validation errors
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));

        // Act
        String viewName = controller.addUser(user, bindingResult, model);

        // Assert
        assertThat(viewName).isEqualTo("admin/add-user");
        assertThat(model.getAttribute("error")).isEqualTo("User already exists!");
        verify(userRepository, times(1)).findByUsername("user1");
    }

    @Test
    void addUser_Success() {
        // Arrange
        User user = new User("user1", "password", "ROLE_USER");
        BindingResult bindingResult = mock(BindingResult.class);

        // Simulate no validation errors
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        // Act
        String viewName = controller.addUser(user, bindingResult, model);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/admin/users");
        verify(userRepository, times(1)).save(any(User.class));
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
    }


    @Test
    void showEditUserForm_UserExists() {
        // Arrange
        User user = new User("user1", "password", "ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        String viewName = controller.showEditUserForm(1L, model);

        // Assert
        assertThat(viewName).isEqualTo("admin/edit-user");
        assertThat(model.getAttribute("user")).isEqualTo(user);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void showEditUserForm_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        String viewName = controller.showEditUserForm(1L, model);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/admin/users");
        assertThat(model.getAttribute("error")).isEqualTo("User not found!");
    }

    @Test
    void editUser_UserExists() {
        // Arrange
        User user = new User("user1", "oldPassword", "ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");

        // Act
        String viewName = controller.editUser(1L, "newPassword", "ROLE_ADMIN", redirectAttributes);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/admin/users");
        assertThat(redirectAttributes.getFlashAttributes().get("success")).isEqualTo("User updated successfully!");
        verify(userRepository, times(1)).save(any(User.class));
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        assertThat(user.getRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void editUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        String viewName = controller.editUser(1L, "password", "ROLE_ADMIN", redirectAttributes);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/admin/users");
        assertThat(redirectAttributes.getFlashAttributes().get("error")).isEqualTo("User not found!");
    }

    @Test
    void deleteUser_UserExists() {
        // Arrange
        User user = new User("user1", "password", "ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        String viewName = controller.deleteUser(1L, redirectAttributes);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/admin/users");
        assertThat(redirectAttributes.getFlashAttributes().get("success")).isEqualTo("User deleted successfully!");
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        String viewName = controller.deleteUser(1L, redirectAttributes);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/admin/users");
        assertThat(redirectAttributes.getFlashAttributes().get("error")).isEqualTo("User not found!");
        verify(userRepository, times(0)).deleteById(anyLong());
    }
}

package com.mb.conitrack.controller;

import java.util.List;
import java.util.Optional;

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

import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.repository.maestro.RoleRepository;
import com.mb.conitrack.repository.maestro.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsersControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsersController controller;

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
        List<User> users = List.of(new User("user1", "password", new Role("ROLE_USER")));
        when(userRepository.findAll()).thenReturn(users);

        // Act
        String viewName = controller.listUsers(model);

        // Assert
        assertThat(viewName).isEqualTo("users/list-users");
        assertThat(model.getAttribute("users")).isEqualTo(users);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void showAddUserForm() {
        // Act
        String viewName = controller.showAddUserForm(model);

        // Assert
        assertThat(viewName).isEqualTo("users/add-user");
    }

    @Test
    void addUser_resultWithErrors() {
        BindingResult bindingResult = mock(BindingResult.class);

        // Simulate no validation errors
        when(bindingResult.hasErrors()).thenReturn(true);

        List<Role> roles = List.of(new Role("ROLE_ADMIN"), new Role("ROLE_USER"));
        when(roleRepository.findAll()).thenReturn(roles);

        // Act
        String viewName = controller.addUser(null, bindingResult, model);

        // Assert
        assertThat(viewName).isEqualTo("users/add-user");
        assertThat(model.getAttribute("roles")).isEqualTo(roles);
        assertThat(model.getAttribute("error")).isEqualTo("Validation failed!");
    }

    @Test
    void addUser_UserAlreadyExists() {
        // Arrange
        User user = new User("user1", "password", new Role("ROLE_USER"));
        BindingResult bindingResult = mock(BindingResult.class);

        // Simulate no validation errors
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));

        List<Role> roles = List.of(new Role("ROLE_ADMIN"), new Role("ROLE_USER"));
        when(roleRepository.findAll()).thenReturn(roles);

        // Act
        String viewName = controller.addUser(user, bindingResult, model);

        // Assert
        assertThat(viewName).isEqualTo("users/add-user");
        assertThat(model.getAttribute("error")).isEqualTo("User already exists!");
        assertThat(model.getAttribute("roles")).isEqualTo(roles);
    }

    @Test
    void addUser_nullRole() {
        // Arrange
        User user = new User("user1", "password", null);
        BindingResult bindingResult = mock(BindingResult.class);

        // Simulate no validation errors
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());

        List<Role> roles = List.of(new Role("ROLE_ADMIN"), new Role("ROLE_USER"));
        when(roleRepository.findAll()).thenReturn(roles);

        // Act
        String viewName = controller.addUser(user, bindingResult, model);

        // Assert
        assertThat(viewName).isEqualTo("users/add-user");
        assertThat(model.getAttribute("error")).isEqualTo("Role is required!");
        assertThat(model.getAttribute("roles")).isEqualTo(roles);
    }

    @Test
    void addUser_nullRoleId() {
        // Arrange
        User user = new User("user1", "password", new Role("ROLE_USER"));
        BindingResult bindingResult = mock(BindingResult.class);

        // Simulate no validation errors
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());

        List<Role> roles = List.of(new Role("ROLE_ADMIN"), new Role("ROLE_USER"));
        when(roleRepository.findAll()).thenReturn(roles);

        // Act
        String viewName = controller.addUser(user, bindingResult, model);

        // Assert
        assertThat(viewName).isEqualTo("users/add-user");
        assertThat(model.getAttribute("error")).isEqualTo("Role is required!");
        assertThat(model.getAttribute("roles")).isEqualTo(roles);
    }

    @Test
    void addUser_roleNotFound() {
        Role role = mock(Role.class);
        when(role.getId()).thenReturn(1L);

        // Arrange
        User user = new User("user1", "password", role);
        BindingResult bindingResult = mock(BindingResult.class);

        // Simulate no validation errors
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        List<Role> roles = List.of(new Role("ROLE_ADMIN"), new Role("ROLE_USER"));
        when(roleRepository.findAll()).thenReturn(roles);

        // Act
        String viewName = controller.addUser(user, bindingResult, model);

        // Assert
        assertThat(viewName).isEqualTo("users/add-user");
        assertThat(model.getAttribute("error")).isEqualTo("Role not found!");
        assertThat(model.getAttribute("roles")).isEqualTo(roles);
    }

    @Test
    void addUser_Success() {
        Role role = mock(Role.class);
        when(role.getId()).thenReturn(1L);

        // Arrange
        User user = new User("user1", "password", role);
        BindingResult bindingResult = mock(BindingResult.class);

        // Simulate no validation errors
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        // Act
        String viewName = controller.addUser(user, bindingResult, model);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/users/list-users");
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password");
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    void showEditUserForm_UserExists() {
        // Arrange
        User user = new User("user1", "password", new Role("ROLE_USER"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        String viewName = controller.showEditUserForm(1L, model);

        // Assert
        assertThat(viewName).isEqualTo("users/edit-user");
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
        assertThat(viewName).isEqualTo("redirect:/users/list-users");
        assertThat(model.getAttribute("error")).isEqualTo("User not found!");
    }

    @Test
    void editUser_UserExists() {
        // Arrange
        User user = new User("user1", "oldPassword", new Role("ROLE_USER"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(eq("ROLE_ADMIN"))).thenReturn(Optional.of(new Role("ROLE_ADMIN")));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");

        // Act
        String viewName = controller.editUser(1L, "newPassword", "ROLE_ADMIN", redirectAttributes);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/users/list-users");
        assertThat(redirectAttributes.getFlashAttributes().get("success")).isEqualTo("User updated successfully!");
        verify(userRepository, times(1)).save(any(User.class));
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        assertThat(user.getRole().getName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void editUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        String viewName = controller.editUser(1L, "password", "ROLE_ADMIN", redirectAttributes);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/users/list-users");
        assertThat(redirectAttributes.getFlashAttributes().get("error")).isEqualTo("User not found!");
    }

    @Test
    void deleteUser_UserExists() {
        // Arrange
        User user = new User("user1", "password", new Role("ROLE_USER"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        String viewName = controller.deleteUser(1L, redirectAttributes);

        // Assert
        assertThat(viewName).isEqualTo("redirect:/users/list-users");
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
        assertThat(viewName).isEqualTo("redirect:/users/list-users");
        assertThat(redirectAttributes.getFlashAttributes().get("error")).isEqualTo("User not found!");
        verify(userRepository, times(0)).deleteById(anyLong());
    }

}

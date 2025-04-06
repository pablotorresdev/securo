package com.mb.conitrack.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mb.conitrack.entity.maestro.Role;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.repository.maestro.RoleRepository;
import com.mb.conitrack.repository.maestro.UserRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/users")
public class ABMUsersController {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    // Store usernames manually for simplicity
    private final List<String> usernames;

    private final RoleRepository roleRepository;

    public ABMUsersController(UserRepository userRepository, PasswordEncoder passwordEncoder, final RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.usernames = new ArrayList<>();
        // Preload existing users (admin, user1, user2)
        usernames.add("pablo");
        usernames.add("admin");
        usernames.add("user1");
        usernames.add("user2");
        this.roleRepository = roleRepository;
    }

    @GetMapping("/")
    public String usersPage() {
        return "users/index-users"; //.html
    }

    @GetMapping("/list-users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "users/list-users"; // Refers to list-index-users.html in the templates directory
    }

    @GetMapping("/add-user")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleRepository.findAll());
        return "users/add-user"; // Refers to add-user.html
    }

    @PostMapping("/add-user")
    public String addUser(@Valid @ModelAttribute User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", roleRepository.findAll()); // Load roles for dropdown
            model.addAttribute("error", "Validation failed!");
            return "users/add-user";
        }

        // Check if the user already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("roles", roleRepository.findAll()); // Load roles for dropdown
            model.addAttribute("error", "User already exists!");
            return "users/add-user";
        }

        // Validate role field in the User object
        if (user.getRole() == null || user.getRole().getId() == null) {
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("error", "Role is required!");
            return "users/add-user";
        }

        // Fetch the Role entity from the database
        Optional<Role> maybeRole = roleRepository.findById(user.getRole().getId());

        if (maybeRole.isEmpty()) {
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("error", "Role not found!");
            return "users/add-user";
        }

        // Set the role and encode the password
        user.setRole(maybeRole.get());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save the user
        userRepository.save(user);

        return "redirect:/users/list-users";
    }

    @GetMapping("/edit-user/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        // Fetch user details by ID
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            model.addAttribute("error", "User not found!");
            return "redirect:/users/list-users";
        }

        model.addAttribute("user", userOptional.get());
        model.addAttribute("roles", roleRepository.findAll());

        return "users/edit-user"; // Refers to edit-user.html
    }

    @PostMapping("/edit-user/{id}")
    public String editUser(
        @PathVariable Long id,
        @RequestParam String password,
        @RequestParam String roleName,
        RedirectAttributes redirectAttributes) {
        // Check if the user exists
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found!");
            return "redirect:/users/list-users";
        }

        // Update the user's details
        User user = userOptional.get();
        final Optional<Role> roleByName = roleRepository.findByName(roleName);
        if (roleByName.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Role not found!");
            return "redirect:/users/list-users";
        }

        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        user.setRole(roleByName.get());

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        return "redirect:/users/list-users"; // Redirect back to the user list
    }

    @PostMapping("/delete-user")
    public String deleteUser(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found!");
            return "redirect:/users/list-users";
        }

        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        return "redirect:/users/list-users";
    }

}


package com.mb.securo.controller;

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

import com.mb.securo.entity.Role;
import com.mb.securo.entity.User;
import com.mb.securo.repository.RoleRepository;
import com.mb.securo.repository.UserRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class UserManagementController {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    // Store usernames manually for simplicity
    private final List<String> usernames;

    private final RoleRepository roleRepository;

    public UserManagementController(UserRepository userRepository, PasswordEncoder passwordEncoder, final RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.usernames = new ArrayList<>();
        // Preload existing users (admin, user1, user2)
        usernames.add("admin");
        usernames.add("user1");
        usernames.add("user2");
        this.roleRepository = roleRepository;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users"; // Refers to users.html in the templates directory
    }

    @GetMapping("/add-user")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/add-user"; // Refers to add-user.html
    }

    @PostMapping("/add-user")
    public String addUser(@Valid @ModelAttribute User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", roleRepository.findAll()); // Load roles for dropdown
            model.addAttribute("error", "Validation failed!");
            return "admin/add-user";
        }

        // Check if the user already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("roles", roleRepository.findAll()); // Load roles for dropdown
            model.addAttribute("error", "User already exists!");
            return "admin/add-user";
        }

        // Validate role field in the User object
        if (user.getRole() == null || user.getRole().getId() == null) {
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("error", "Role is required!");
            return "admin/add-user";
        }

        // Fetch the Role entity from the database
        Role role = roleRepository.findById(user.getRole().getId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid role ID"));

        // Set the role and encode the password
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save the user
        userRepository.save(user);

        return "redirect:/admin/users";
    }


    @GetMapping("/edit-user/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        // Fetch user details by ID
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            model.addAttribute("error", "User not found!");
            return "redirect:/admin/users";
        }

        model.addAttribute("user", userOptional.get());
        model.addAttribute("roles", roleRepository.findAll());

        return "admin/edit-user"; // Refers to edit-user.html
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
            return "redirect:/admin/users";
        }

        // Update the user's details
        User user = userOptional.get();
        final Optional<Role> roleByName = roleRepository.findByName(roleName);
        if (roleByName.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Role not found!");
            return "redirect:/admin/users";
        }

        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        user.setRole(roleByName.get());

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        return "redirect:/admin/users"; // Redirect back to the user list
    }

    @PostMapping("/delete-user")
    public String deleteUser(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found!");
            return "redirect:/admin/users";
        }

        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        return "redirect:/admin/users";
    }

}


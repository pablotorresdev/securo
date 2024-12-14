package com.mb.securo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mb.securo.entity.User;
import com.mb.securo.repository.UserRepository;

@Controller
@RequestMapping("/admin")
public class UserManagementController {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    // Store usernames manually for simplicity
    private final List<String> usernames;

    public UserManagementController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.usernames = new ArrayList<>();
        // Preload existing users (admin, user1, user2)
        usernames.add("admin");
        usernames.add("user1");
        usernames.add("user2");
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users"; // Refers to users.html in the templates directory
    }

    @GetMapping("/add-user")
    public String showAddUserForm() {
        return "admin/add-user"; // Refers to add-user.html
    }

    @PostMapping("/add-user")
    public String addUser(
        @RequestParam String username,
        @RequestParam String password,
        @RequestParam String role,
        Model model) {
        // Check if the user already exists
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "User already exists!");
            return "admin/add-user";
        }

        User user = new User(username, passwordEncoder.encode(password), role);

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

        return "admin/edit-user"; // Refers to edit-user.html
    }

    @PostMapping("/edit-user/{id}")
    public String editUser(
        @PathVariable Long id,
        @RequestParam String password,
        @RequestParam String role,
        RedirectAttributes redirectAttributes) {
        // Check if the user exists
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found!");
            return "redirect:/admin/users";
        }

        // Update the user's details
        User user = userOptional.get();
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        return "redirect:/admin/users"; // Redirect back to the user list
    }

    @PostMapping("/delete-user")
    public String deleteUser(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        // Check if the user exists
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found!");
            return "redirect:/admin/users";
        }

        // Delete the user
        userRepository.deleteById(id);

        redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        return "redirect:/admin/users"; // Redirect back to the user list
    }

}


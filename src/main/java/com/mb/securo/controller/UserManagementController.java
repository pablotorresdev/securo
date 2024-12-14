package com.mb.securo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class UserManagementController {

    private final InMemoryUserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;

    // Store usernames manually for simplicity
    private final List<String> usernames;

    public UserManagementController(InMemoryUserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
        this.usernames = new ArrayList<>();
        // Preload existing users (admin, user1, user2)
        usernames.add("admin");
        usernames.add("user1");
        usernames.add("user2");
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        // Fetch all users and their details
        List<UserDetails> users = new ArrayList<>();
        for (String username : usernames) {
            if(!userDetailsManager.userExists(username)) {
                continue;
            }
            UserDetails user = userDetailsManager.loadUserByUsername(username);
            users.add(user);
        }
        model.addAttribute("users", users);
        return "admin/users"; // Refers to users.html in the templates directory
    }

    @GetMapping("/add-user")
    public String showAddUserForm() {
        return "admin/add-user"; // Refers to add-user.html
    }

    @PostMapping("/add-user")
    public String addUser(@RequestParam String username,
        @RequestParam String password,
        @RequestParam String role,
        Model model) {
        // Check if the user already exists
        if (userDetailsManager.userExists(username)) {
            model.addAttribute("error", "User already exists!");
            return "admin/add-user"; // Reload the form with an error message
        }

        // Create and add the new user
        userDetailsManager.createUser(User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .roles(role)
            .build());

        // Add the username to the list for tracking
        usernames.add(username);

        // Redirect to the user list
        return "redirect:/admin/users";
    }

    @GetMapping("/edit-user/{username}")
    public String showEditUserForm(@PathVariable String username, Model model) {
        // Fetch user details
        if (!userDetailsManager.userExists(username)) {
            model.addAttribute("error", "User not found!");
            return "redirect:/admin/users";
        }
        UserDetails user = userDetailsManager.loadUserByUsername(username);
        model.addAttribute("user", user);
        return "admin/edit-user"; // Refers to edit-user.html
    }

    @PostMapping("/edit-user/{username}")
    public String editUser(@PathVariable String username,
        @RequestParam String password,
        @RequestParam String role,
        Model model) {
        // Check if the user exists
        if (!userDetailsManager.userExists(username)) {
            model.addAttribute("error", "User not found!");
            return "redirect:/admin/users";
        }

        // Update the user's details
        userDetailsManager.updateUser(User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .roles(role)
            .build());

        return "redirect:/admin/users"; // Redirect back to the user list
    }

    @PostMapping("/delete-user")
    public String deleteUser(@RequestParam String username, RedirectAttributes redirectAttributes) {
        // Check if the user exists
        if (!userDetailsManager.userExists(username)) {
            redirectAttributes.addFlashAttribute("error", "User not found!");
            return "redirect:/admin/users";
        }

        // Delete the user
        userDetailsManager.deleteUser(username);
        usernames.remove(username);

        redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        return "redirect:/admin/users";
    }


}


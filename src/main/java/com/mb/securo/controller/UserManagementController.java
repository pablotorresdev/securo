package com.mb.securo.controller;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
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

    @GetMapping("/admin/users")
    public String listUsers(Model model) {
        // Fetch all users and their details
        List<UserDetails> users = new ArrayList<>();
        for (String username : usernames) {
            UserDetails user = userDetailsManager.loadUserByUsername(username);
            users.add(user);
        }
        model.addAttribute("users", users);
        return "users"; // Refers to users.html in the templates directory
    }

    @GetMapping("/admin/add-user")
    public String showAddUserForm() {
        return "add-user"; // Refers to add-user.html
    }

    @PostMapping("/admin/add-user")
    public String addUser(@RequestParam String username,
        @RequestParam String password,
        @RequestParam String role,
        Model model) {
        // Check if the user already exists
        if (userDetailsManager.userExists(username)) {
            model.addAttribute("error", "User already exists!");
            return "add-user"; // Reload the form with an error message
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
}


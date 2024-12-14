package com.mb.securo.controller;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class UserManagementController {

    private final InMemoryUserDetailsManager userDetailsManager;

    // Store usernames manually for simplicity
    private final List<String> usernames;

    public UserManagementController(InMemoryUserDetailsManager userDetailsManager) {
        this.userDetailsManager = userDetailsManager;
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
}

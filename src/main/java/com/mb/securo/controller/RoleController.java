package com.mb.securo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoleController {

    @GetMapping("/admin")
    public String adminPage() {
        return "admin"; // admin.html
    }

    @GetMapping("/user1")
    public String user1Page() {
        return "user1"; // user1.html
    }

    @GetMapping("/user2")
    public String user2Page() {
        return "user2"; // user2.html
    }

}


package com.mb.securo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/users")
    public String usersPage() {
        return "users"; // users.html
    }

    @GetMapping("/contactos")
    public String contactosPage() {
        return "contactos"; // contactos.html
    }

    @GetMapping("/productos")
    public String productosPage() {
        return "productos"; // productos.html
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


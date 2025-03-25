package com.mb.conitrack.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import jakarta.servlet.ServletException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class LoginControllerTest {

    private final MockMvc mockMvc;

    public LoginControllerTest() {
        // Set up MockMvc with a View Resolver
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/"); // Adjust this if needed
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(new LoginController())
            .setViewResolvers(viewResolver) // Add the view resolver
            .build();
    }

    @Test
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk()) // Expect HTTP 200
            .andExpect(view().name("login")); // View name is "login"
    }

    @Test
    void testIndexPage() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk()) // Expect HTTP 200
            .andExpect(view().name("index")); // View name is "index"
    }

    @Test
    void testTriggerError() throws Exception {
        assertThrows(ServletException.class, () -> {
            mockMvc.perform(get("/error-test"));
        });
    }

}

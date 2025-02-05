package com.mb.securo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class RoleControllerTest {

    private final MockMvc mockMvc;

    public RoleControllerTest() {
        // Set up MockMvc with a View Resolver
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/"); // Adjust this if needed
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(new RoleController())
            .setViewResolvers(viewResolver) // Add the view resolver
            .build();
    }

    @Test
    void testAdminPage() throws Exception {
        mockMvc.perform(get("/admin"))
            .andExpect(status().isOk()) // Verify the status is 200 OK
            .andExpect(view().name("admin")); // Verify the view name is "admin"
    }

    @Test
    void testUser1Page() throws Exception {
        mockMvc.perform(get("/user1"))
            .andExpect(status().isOk()) // Verify the status is 200 OK
            .andExpect(view().name("user1")); // Verify the view name is "user1"
    }

    @Test
    void testUser2Page() throws Exception {
        mockMvc.perform(get("/user2"))
            .andExpect(status().isOk()) // Verify the status is 200 OK
            .andExpect(view().name("user2")); // Verify the view name is "user2"
    }

}


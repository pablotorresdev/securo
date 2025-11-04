package com.mb.conitrack.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.mb.conitrack.controller.maestro.LoginController;
import com.mb.conitrack.dto.DashboardMetricsDTO;
import com.mb.conitrack.dto.UserInfoDTO;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.service.DashboardService;
import com.mb.conitrack.service.PermisosCasoUsoService;
import com.mb.conitrack.service.SecurityContextService;

import jakarta.servlet.ServletException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private SecurityContextService securityContextService;

    @Mock
    private PermisosCasoUsoService permisosCasoUsoService;

    @InjectMocks
    private LoginController loginController;

    private MockMvc mockMvc;

    @Test
    void testIndexPage() throws Exception {
        // Setup mocks
        User mockUser = new User();
        mockUser.setUsername("testuser");

        UserInfoDTO mockUserInfo = new UserInfoDTO();
        mockUserInfo.setUsername("testuser");

        DashboardMetricsDTO mockMetrics = new DashboardMetricsDTO();
        mockMetrics.setLotesActivos(10L);

        when(securityContextService.getCurrentUser()).thenReturn(mockUser);
        when(dashboardService.getUserInfo(mockUser)).thenReturn(mockUserInfo);
        when(dashboardService.getDashboardMetrics()).thenReturn(mockMetrics);

        // Setup MockMvc
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(loginController)
            .setViewResolvers(viewResolver)
            .build();

        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("index"))
            .andExpect(model().attributeExists("userInfo"))
            .andExpect(model().attributeExists("metrics"))
            .andExpect(model().attributeExists("systemVersion"));
    }

    @Test
    void testLoginPage() throws Exception {
        // Setup MockMvc for this test
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(loginController)
            .setViewResolvers(viewResolver)
            .build();

        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("login"));
    }

    @Test
    void testTriggerError() throws Exception {
        // Setup MockMvc for this test
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(loginController)
            .setViewResolvers(viewResolver)
            .build();

        assertThrows(
            ServletException.class, () -> {
                mockMvc.perform(get("/error-test"));
            });
    }

}

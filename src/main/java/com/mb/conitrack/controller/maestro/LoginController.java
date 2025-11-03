package com.mb.conitrack.controller.maestro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mb.conitrack.dto.DashboardMetricsDTO;
import com.mb.conitrack.dto.UserInfoDTO;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.service.DashboardService;
import com.mb.conitrack.service.SecurityContextService;

@Controller
public class LoginController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private SecurityContextService securityContextService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/")
    public String index(Model model) {
        // Obtener usuario actual
        User currentUser = securityContextService.getCurrentUser();

        // Obtener información del usuario
        UserInfoDTO userInfo = dashboardService.getUserInfo(currentUser);
        model.addAttribute("userInfo", userInfo);

        // Obtener métricas del dashboard
        DashboardMetricsDTO metrics = dashboardService.getDashboardMetrics();
        model.addAttribute("metrics", metrics);

        // Información del sistema (versión, etc.)
        model.addAttribute("systemVersion", "v1.0.0");

        return "index";
    }

    @GetMapping("/error-test")
    public String triggerError() {
        throw new RuntimeException("This is a test error.");
    }

}


package com.mb.conitrack.controller.maestro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mb.conitrack.dto.DashboardMetricsDTO;
import com.mb.conitrack.dto.UserInfoDTO;
import com.mb.conitrack.entity.maestro.User;
import com.mb.conitrack.service.DashboardService;
import com.mb.conitrack.service.PermisosCasoUsoService;
import com.mb.conitrack.service.SecurityContextService;

@Controller
public class LoginController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private SecurityContextService securityContextService;

    @Autowired
    private PermisosCasoUsoService permisosCasoUsoService;

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

        // Agregar servicio de permisos para uso en el template
        model.addAttribute("permisosCasoUsoService", permisosCasoUsoService);

        // Información del sistema (versión, etc.)
        model.addAttribute("systemVersion", "v1.0.0");

        return "index";
    }

    @GetMapping("/error-test")
    public String triggerError() {
        throw new RuntimeException("This is a test error.");
    }

}


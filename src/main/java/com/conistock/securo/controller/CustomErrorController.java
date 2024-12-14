package com.conistock.securo.controller;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public String handleError(WebRequest request, Model model) {
        // Get error attributes
        Map<String, Object> errorAttributes = this.errorAttributes.getErrorAttributes(
            request,
            ErrorAttributeOptions.of(Include.MESSAGE, Include.EXCEPTION)
        );

        // Add error details to the model
        model.addAttribute("error", errorAttributes);

        return "error"; // Refers to error.html in the templates directory
    }

    @GetMapping("/error-test")
    public String triggerError() {
        throw new RuntimeException("Testing error handling");
    }
}


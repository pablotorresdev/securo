package com.mb.securo.controller;
import com.mb.securo.controller.CustomErrorController;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CustomErrorControllerTest {

    @Test
    void testHandleError() {
        // Mock dependencies
        ErrorAttributes errorAttributes = mock(ErrorAttributes.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        Model model = new ExtendedModelMap();

        // Controller under test
        CustomErrorController controller = new CustomErrorController(errorAttributes);

        // Create a ServletWebRequest for testing
        ServletWebRequest webRequest = new ServletWebRequest(httpServletRequest);

        // Mock error details
        Map<String, Object> errorDetails = Map.of(
            "message", "Error Message",
            "exception", "Exception Details"
        );

        // Mock behavior of ErrorAttributes
        when(errorAttributes.getErrorAttributes(any(ServletWebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(errorDetails);

        // Execute the method under test
        String result = controller.handleError(httpServletRequest, model);

        // Assert the returned view name
        assertEquals("error", result);
        assertEquals("Error Message", ((Map<?, ?>)Objects.requireNonNull(model.getAttribute("error"))).get("message"));
        assertEquals("Exception Details", ((Map<?, ?>)Objects.requireNonNull(model.getAttribute("error"))).get("exception"));
    }
}

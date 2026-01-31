package com.github.cauebf.authfy.config;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    // AuthenticationEntryPoint is a interface used to handle unauthorized access attempts

    // method triggered when an unauthenticated user tries to access a protected resource    
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        // writes a custom json response indicating the user is not authenticated
        response.getWriter().write("{\"authenticated\": false, \"message\": \"User is not authenticated\"}");
    }
    
}

package com.github.cauebf.authfy.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.cauebf.authfy.service.AppUserDetailsService;
import com.github.cauebf.authfy.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    
    private final AppUserDetailsService appUserDetailsService;
    private final JwtUtil jwtUtil;

    // list of public endpoints that do not require authentication
    private static final List<String> PUBLIC_URLS = List.of("/login", "/register", "/send-reset-otp", "/reset-password", "/logout");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // retrieves the requested endpoint path
        String path = request.getServletPath();

        // skips jwt validation for public endpoints
        if (PUBLIC_URLS.contains(path)) {
            filterChain.doFilter(request, response); // proceed to next filter
            return;
        }

        String jwt = null;
        String email = null;

        // 1. tries to extract the token from the authorization header
        final String authorizationHeader = request.getHeader("Authorization");

        // checks if the header exists and starts with "bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // removes "bearer " prefix to get the token
            jwt = authorizationHeader.substring(7);
        }

        // 2. if token was not found in the header, tries to extract it from cookies
        if (jwt == null) {
            Cookie[] cookies = request.getCookies();

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    // looks for a cookie named "jwt"
                    if (cookie.getName().equals("jwt")) {
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // 3. validates the token and sets authentication in security context
        if (jwt != null) {
            // extracts the email (subject) from the token
            email = jwtUtil.extractEmail(jwt);

            // checks if email is valid and authentication is not already set
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // loads user details using the extracted email                
                UserDetails userDetails = appUserDetailsService.loadUserByUsername(email);

                // validates the token against user details
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // creates authentication token and sets it in the security context
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }

        // continues the filter chain
        filterChain.doFilter(request, response);
    }
}

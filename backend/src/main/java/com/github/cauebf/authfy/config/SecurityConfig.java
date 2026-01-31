package com.github.cauebf.authfy.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.github.cauebf.authfy.filter.JwtRequestFilter;
import com.github.cauebf.authfy.service.AppUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppUserDetailsService appUserDetailsService;
    private final JwtRequestFilter jwtRequestFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults()) // enable CORS with default settings
            .csrf(AbstractHttpConfigurer::disable) // disable CSRF since we are using stateless JWT authentication
            .authorizeHttpRequests(auth -> auth
                // public endpoints that do not require authentication                
                .requestMatchers("/login", "/register", "/send-reset-otp", "/reset-password", "/logout").permitAll() // public endpoints
                .anyRequest().authenticated() // all other endpoints require authentication
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // stateless session management
            .logout(AbstractHttpConfigurer::disable) // disable default Spring Security logout endpoint
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class) // add JWT filter before the username-password authentication filter
            .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(customAuthenticationEntryPoint)); // custom entry point for unauthorized access
        return http.build();
    }

    // bean for password encoding using BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // bean for handling CORS requests
    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }

    // defines the CORS configuration source
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000")); // allow requests from the frontend origin
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")); // allow these HTTP methods
        config.setAllowedHeaders(List.of("Authorization", "Content-Type")); // allow these headers
        config.setAllowCredentials(true); // allow credentials (e.g. cookies)

        // register the configuration for all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // bean for authentication manager, used to authenticate users
    @Bean
    public AuthenticationManager authenticationManager() {
        // dao authentication provider to check credentials from userDetailsService        
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(appUserDetailsService);
        // set password encoder
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        // return provider manager with the authentication provider
        return new ProviderManager(authenticationProvider);
    }
}

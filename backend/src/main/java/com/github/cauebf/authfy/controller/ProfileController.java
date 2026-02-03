package com.github.cauebf.authfy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import com.github.cauebf.authfy.io.ProfileRequest;
import com.github.cauebf.authfy.io.ProfileResponse;
import com.github.cauebf.authfy.service.EmailService;
import com.github.cauebf.authfy.service.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final EmailService emailService;
    
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@Valid @RequestBody ProfileRequest request) {
        // creates a new user profile using the request data and sends a welcome email
        ProfileResponse response = profileService.createProfile(request);
        emailService.sendWelcomeEmail(response.getEmail(), response.getName());
        return response;
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile(Authentication authentication) {
        // extracts the authenticated user's identifier (email in this case) from the security context     
        String email = authentication.getName();
        // returns the profile associated with the authenticated email        
        return profileService.getProfile(email);
    }
}

package com.github.cauebf.authfy.controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.github.cauebf.authfy.io.ResetPasswordRequest;
import com.github.cauebf.authfy.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.github.cauebf.authfy.io.AuthRequest;
import com.github.cauebf.authfy.io.AuthResponse;
import com.github.cauebf.authfy.service.AppUserDetailsService;
import com.github.cauebf.authfy.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final ProfileService profileService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // authenticate user credentials            
            authenticate(request.getEmail(), request.getPassword());

            // load user details after successful authentication            
            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            // generate jwt token for the authenticated user            
            final String jwtToken = jwtUtil.generateToken(userDetails);

            // create an http-only cookie to store the jwt token            
            ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofDays(1)) // 1 day
                    .sameSite("Strict")
                    .build();

            // return jwt token in both cookie and response body                    
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AuthResponse(request.getEmail(), jwtToken));
        } catch(BadCredentialsException e) {
            // handle invalid credentials            
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch(DisabledException e) {
            // handle disabled accounts            
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Account is disabled");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch(Exception e) {
            // handle any other exceptions during authentication            
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "An error occurred during authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    // helper method to authenticate email and password using authentication manager    
    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    @GetMapping("/is-authenticated")
    public ResponseEntity<Boolean> isAuthenticated(Authentication authentication) {
        // checks if the user is authenticated based on the security context        
        boolean isAuthenticated = (authentication != null && authentication.isAuthenticated());
        return ResponseEntity.ok(isAuthenticated);
    }

    @PostMapping("/send-reset-otp")
    public void sendResetOtp(@RequestParam String email) {
        try {
            profileService.sendResetOtp(email);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            profileService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/send-verify-otp")
    public void sendVerifyOtp(Authentication authentication) {
        try {
            // get the user's email from the authentication context
            String email = authentication.getName();
            // send the verify OTP to the user's email
            profileService.sendVerifyOtp(email);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public void verifyEmail(@RequestBody Map<String, Object> request, Authentication authentication) {
        // verify that the request contains an OTP
        if (request.get("otp") == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP is required");

        try {
            // get the user's email from the authentication context
            String email = authentication.getName();
            // verify the OTP using the user's email and the OTP provided in the request
            profileService.verifyOtp(email, request.get("otp").toString());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}

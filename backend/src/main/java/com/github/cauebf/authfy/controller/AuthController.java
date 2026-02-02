package com.github.cauebf.authfy.controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.cauebf.authfy.io.AuthRequest;
import com.github.cauebf.authfy.io.AuthResponse;
import com.github.cauebf.authfy.service.AppUserDetailsService;
import com.github.cauebf.authfy.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    
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
}

package com.github.cauebf.authfy.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    // generates a jwt token based on the authenticated user details
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // creates and returns the jwt token using the user's email as subject
        return createToken(claims, userDetails.getUsername());
    }

    // builds and signs the jwt token with claims, subject, issue date and expiration
    private String createToken(Map<String, Object> claims, String email) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // extracts all claims from the token using the secret key
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    // extracts a specific claim from the token using a resolver function
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);

        // applies the resolver function to extract the desired claim
        return claimsResolver.apply(claims);
    }

    // extracts the email (subject) from the token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // extracts the expiration date from the token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // checks if the token is expired based on the current date
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // validates the token by checking the email and expiration
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);

        // validates email match and ensures token is not expired
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}

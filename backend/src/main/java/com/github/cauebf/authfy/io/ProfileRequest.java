package com.github.cauebf.authfy.io;

import org.springframework.stereotype.Component;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class ProfileRequest {
    
    @NotBlank(message = "Name cannot be blank")
    private String name;
    @NotNull(message = "Email cannot be null")
    @Email(message = "Enter a valid email")
    private String email;
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
}

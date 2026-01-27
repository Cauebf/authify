package com.github.cauebf.authfy.io;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class ProfileResponse {
    
    private String userId;
    private String name;
    private String email;
    private Boolean isAccountVerified;
}

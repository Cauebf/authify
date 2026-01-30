package com.github.cauebf.authfy.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.github.cauebf.authfy.io.ProfileRequest;
import com.github.cauebf.authfy.io.ProfileResponse;
import com.github.cauebf.authfy.model.UserEntity;
import com.github.cauebf.authfy.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // injects final fields via constructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        UserEntity newProfile = convertToUserEntity(request);

        if(userRepository.existsByEmail(newProfile.getEmail())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");

        newProfile = userRepository.save(newProfile);
        return convertToProfileResponse(newProfile);
    }

    private ProfileResponse convertToProfileResponse(UserEntity newProfile) {
        return ProfileResponse.builder()
            .userId(newProfile.getUserId())
            .name(newProfile.getName())
            .email(newProfile.getEmail())
            .isAccountVerified(newProfile.getIsAccountVerified())
            .build();
    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
            .userId(UUID.randomUUID().toString())
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .isAccountVerified(false)
            .resetOtpExpireAt(0L)
            .verifyOtp(null)
            .verifyOtpExpireAt(0L)
            .resetOtp(null)
            .build();
    }
    
}

package com.github.cauebf.authfy.service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final EmailService emailService;

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        UserEntity newProfile = convertToUserEntity(request);

        if(userRepository.existsByEmail(newProfile.getEmail())) 
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");

        newProfile = userRepository.save(newProfile);
        return convertToProfileResponse(newProfile);
    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return convertToProfileResponse(existingUser);
    }

    @Override
    public void sendResetOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // generate a random 6-digit OTP
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        // set the OTP expiration time (15 minutes from now)
        long expirationTime = System.currentTimeMillis() + (15 * 60 * 1000);

        // update the user's reset OTP and expiration time
        existingUser.setResetOtp(otp);
        existingUser.setResetOtpExpireAt(expirationTime);

        // save the updated user entity to the database
        userRepository.save(existingUser);

        // send the reset OTP to the user's email
        try {
            emailService.sendResetOtpEmail(existingUser.getEmail(), otp);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send reset OTP");
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // check if OTP is valid
        if (existingUser.getResetOtp() == null || !existingUser.getResetOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        // check if the OTP has expired
        if (existingUser.getResetOtpExpireAt() < System.currentTimeMillis()) {
            throw new RuntimeException("OTP has expired");
        }

        // update the user's password and save to the database
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setResetOtp(null);
        existingUser.setResetOtpExpireAt(0L);

        userRepository.save(existingUser);
    }

    private ProfileResponse convertToProfileResponse(UserEntity user) {
        return ProfileResponse.builder()
            .userId(user.getUserId())
            .name(user.getName())
            .email(user.getEmail())
            .isAccountVerified(user.getIsAccountVerified())
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

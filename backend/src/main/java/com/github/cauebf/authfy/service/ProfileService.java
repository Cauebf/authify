package com.github.cauebf.authfy.service;

import com.github.cauebf.authfy.io.ProfileRequest;
import com.github.cauebf.authfy.io.ProfileResponse;

public interface ProfileService {

    ProfileResponse createProfile(ProfileRequest request);
    ProfileResponse getProfile(String email);
    void sendResetOtp(String mail);
    void resetPassword(String email, String otp, String newPassword);
} 

package com.bakare_dev.logistics.service;

import com.bakare_dev.logistics.dto.request.ChangePasswordRequest;
import com.bakare_dev.logistics.dto.request.LoginRequest;
import com.bakare_dev.logistics.dto.request.RegisterRequest;
import com.bakare_dev.logistics.dto.request.UpdateProfileRequest;
import com.bakare_dev.logistics.dto.response.AuthResponse;
import com.bakare_dev.logistics.dto.response.UserResponse;
import com.bakare_dev.logistics.entity.Role;

import java.util.List;

public interface UserService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getProfile(Long userId);
    UserResponse getUserByEmail(String email);
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    List<UserResponse> getUsersByRole(Role role);
    List<UserResponse> getAllUsers();
}

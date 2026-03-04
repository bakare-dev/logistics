package com.bakare_dev.logistics.controller;

import com.bakare_dev.logistics.annotation.RateLimit;
import com.bakare_dev.logistics.dto.request.LoginRequest;
import com.bakare_dev.logistics.dto.request.RegisterRequest;
import com.bakare_dev.logistics.dto.response.AuthResponse;
import com.bakare_dev.logistics.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @RateLimit(requests = 5, minutes = 15)
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(userService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @RateLimit(requests = 10, minutes = 5)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }
}

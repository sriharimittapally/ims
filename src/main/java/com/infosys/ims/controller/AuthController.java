package com.infosys.ims.controller;

import com.infosys.ims.dtos.request.AuthRequest;
import com.infosys.ims.dtos.response.ApiResponse;
import com.infosys.ims.dtos.response.AuthResponse;
import com.infosys.ims.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @GetMapping("/session")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> session() {
        return ResponseEntity.ok(ApiResponse.success("Session active", "ACTIVE"));
    }
}
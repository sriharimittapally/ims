package com.infosys.ims.controller;

import com.infosys.ims.dtos.request.CreateUserRequest;
import com.infosys.ims.dtos.response.*;
import com.infosys.ims.enums.Role;
import com.infosys.ims.service.DashboardService;
import com.infosys.ims.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final DashboardService dashboardService;

    // ── Dashboard ──────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard fetched", dashboardService.getAdminDashboard()));
    }

    // ── Create Manager ─────────────────────────────────────────────────────
    @PostMapping("/users/manager")
    public ResponseEntity<ApiResponse<String>> createManager(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Manager created successfully", userService.createManager(request)));
    }

    // ── Create Staff directly (admin shortcut) ─────────────────────────────
    @PostMapping("/users/staff")
    public ResponseEntity<ApiResponse<String>> createStaff(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff created successfully", userService.createStaff(request)));
    }

    // ── Assign manager to a (different) warehouse ──────────────────────────
    @PutMapping("/warehouses/{warehouseId}/assign-manager/{managerId}")
    public ResponseEntity<ApiResponse<String>> assignManager(@PathVariable Long warehouseId,
                                                             @PathVariable Long managerId) {
        userService.assignManagerToWarehouse(warehouseId, managerId);
        return ResponseEntity.ok(ApiResponse.success("Manager assigned to warehouse"));
    }

    // ── List all users ─────────────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("Users fetched", userService.getAllUsers()));
    }

    // ── List by role ───────────────────────────────────────────────────────
    @GetMapping("/users/role/{role}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable Role role) {
        return ResponseEntity.ok(ApiResponse.success("Users fetched", userService.getUsersByRole(role)));
    }

    // ── Activate / Deactivate user ─────────────────────────────────────────
    @PutMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<String>> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User activated"));
    }

    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated"));
    }
}
package com.infosys.ims.controller;

import com.infosys.ims.dtos.request.CreateUserRequest;
import com.infosys.ims.dtos.response.ApiResponse;
import com.infosys.ims.dtos.response.ManagerDashboardResponse;
import com.infosys.ims.dtos.response.UserResponse;
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
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerController {

    private final UserService userService;
    private final DashboardService dashboardService;

    // ── Dashboard ──────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<ManagerDashboardResponse>> getDashboard(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Dashboard fetched",
                dashboardService.getManagerDashboard(auth.getName())));
    }

    // ── Create staff for this warehouse ───────────────────────────────────
    @PostMapping("/staff")
    public ResponseEntity<ApiResponse<String>> createStaff(Authentication auth,
                                                           @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff created",
                        userService.createStaffByManager(request, auth.getName())));
    }

    // ── List staff in my warehouse ─────────────────────────────────────────
    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getMyStaff(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Staff fetched",
                userService.getStaffForMyWarehouse(auth.getName())));
    }

    // ── Activate / Deactivate staff ────────────────────────────────────────
    @PutMapping("/staff/{id}/activate")
    public ResponseEntity<ApiResponse<String>> activateStaff(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("Staff activated"));
    }

    @PutMapping("/staff/{id}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateStaff(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("Staff deactivated"));
    }
}
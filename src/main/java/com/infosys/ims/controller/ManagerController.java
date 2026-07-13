package com.infosys.ims.controller;

import com.infosys.ims.dtos.request.CreateUserRequest;
import com.infosys.ims.dtos.response.ApiResponse;
import com.infosys.ims.dtos.response.ManagerDashboardResponse;
import com.infosys.ims.dtos.response.PageResponse;
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

    // â”€â”€ Dashboard â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<ManagerDashboardResponse>> getDashboard(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Dashboard fetched",
                dashboardService.getManagerDashboard(auth.getName())));
    }

    // â”€â”€ Create staff for this warehouse â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    @PostMapping("/staff")
    public ResponseEntity<ApiResponse<String>> createStaff(Authentication auth,
                                                           @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff created",
                        userService.createStaffByManager(request, auth.getName())));
    }

    // â”€â”€ List staff in my warehouse â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getMyStaff(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Staff fetched",
                userService.getStaffForMyWarehouse(auth.getName())));
    }

    @GetMapping("/staff/page")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getMyStaffPage(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "ALL") String status) {
        return ResponseEntity.ok(ApiResponse.success("Staff page fetched",
                userService.getStaffForMyWarehousePaged(auth.getName(), page, size, search, status)));
    }

    // â”€â”€ Activate / Deactivate staff â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
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

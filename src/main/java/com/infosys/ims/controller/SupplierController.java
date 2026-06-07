package com.infosys.ims.controller;

import com.infosys.ims.dtos.request.ApprovalRequest;
import com.infosys.ims.dtos.request.CreateUserRequest;
import com.infosys.ims.dtos.request.SupplierProfileRequest;
import com.infosys.ims.dtos.response.ApiResponse;
import com.infosys.ims.dtos.response.SupplierDashboardResponse;
import com.infosys.ims.dtos.response.SupplierProfileResponse;
import com.infosys.ims.service.DashboardService;
import com.infosys.ims.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;
    private final DashboardService dashboardService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Supplier registered. Please complete your profile after login.",
                        supplierService.registerSupplierUser(request)));
    }

    @PostMapping("/profile")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<ApiResponse<String>> completeProfile(Authentication auth,
                                                               @Valid @RequestBody SupplierProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile completed",
                supplierService.completeProfile(auth.getName(), request)));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<ApiResponse<SupplierProfileResponse>> getMyProfile(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Profile fetched",
                supplierService.getProfile(auth.getName())));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<ApiResponse<SupplierDashboardResponse>> getDashboard(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Dashboard fetched",
                dashboardService.getSupplierDashboard(auth.getName())));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<List<SupplierProfileResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Suppliers fetched", supplierService.getAllSuppliers()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SupplierProfileResponse>>> getPending() {
        return ResponseEntity.ok(ApiResponse.success("Pending suppliers fetched", supplierService.getPendingSuppliers()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<SupplierProfileResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Supplier fetched", supplierService.getSupplierById(id)));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> approve(@PathVariable Long id, Authentication auth) {
        supplierService.approveSupplier(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Supplier approved"));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> reject(@PathVariable Long id,
                                                      Authentication auth,
                                                      @Valid @RequestBody ApprovalRequest request) {
        supplierService.rejectSupplier(id, auth.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Supplier rejected"));
    }

    // NEW — Revoke Approval (APPROVED → PENDING)
    @PutMapping("/{id}/revoke-approval")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> revokeApproval(@PathVariable Long id, Authentication auth) {
        supplierService.revokeApproval(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Supplier approval revoked. Status reset to PENDING."));
    }

    // NEW — Revoke Rejection (REJECTED → PENDING)
    @PutMapping("/{id}/revoke-rejection")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> revokeRejection(@PathVariable Long id, Authentication auth) {
        supplierService.revokeRejection(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Supplier rejection revoked. Status reset to PENDING."));
    }
}
package com.infosys.ims.controller;

import com.infosys.ims.dtos.request.StockIssueRejectRequest;
import com.infosys.ims.dtos.response.ApiResponse;
import com.infosys.ims.dtos.response.StaffDashboardResponse;
import com.infosys.ims.dtos.response.StockIssueResponse;
import com.infosys.ims.service.DashboardService;
import com.infosys.ims.service.StockIssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-issues")
@RequiredArgsConstructor
public class StockIssueController {

    private final StockIssueService stockIssueService;
    private final DashboardService dashboardService;

    // ── STAFF — Dashboard ──────────────────────────────────────────────────
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<StaffDashboardResponse>> getStaffDashboard(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Dashboard fetched",
                dashboardService.getStaffDashboard(auth.getName())));
    }

    // ── STAFF — Create issue header ────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<StockIssueResponse>> create(Authentication auth,
                                                                  @RequestParam(required = false) String note) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock issue created",
                        stockIssueService.createIssue(auth.getName(), note)));
    }

    // ── STAFF — Add item ───────────────────────────────────────────────────
    @PostMapping("/{issueId}/items")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<StockIssueResponse>> addItem(@PathVariable Long issueId,
                                                                   @RequestParam Long productId,
                                                                   @RequestParam int quantity,
                                                                   Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Item added",
                stockIssueService.addItem(issueId, productId, quantity, auth.getName())));
    }

    // ── STAFF — Remove item ────────────────────────────────────────────────
    @DeleteMapping("/{issueId}/items/{itemId}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<StockIssueResponse>> removeItem(@PathVariable Long issueId,
                                                                      @PathVariable Long itemId,
                                                                      Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Item removed",
                stockIssueService.removeItem(issueId, itemId, auth.getName())));
    }

    // ── STAFF — Cancel ─────────────────────────────────────────────────────
    @PutMapping("/{issueId}/cancel")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<StockIssueResponse>> cancel(@PathVariable Long issueId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Stock issue cancelled",
                stockIssueService.cancelIssue(issueId, auth.getName())));
    }

    // ── STAFF — Submit for manager review (DRAFT → PENDING) ───────────────────
    @PutMapping("/{issueId}/submit")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<StockIssueResponse>> submit(
            @PathVariable Long issueId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Stock issue submitted for review",
                stockIssueService.submitForReview(issueId, auth.getName())));
    }

    // ── STAFF — Execute (issue stock out) ──────────────────────────────────
    @PutMapping("/{issueId}/issue")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<StockIssueResponse>> issueStock(@PathVariable Long issueId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Stock issued",
                stockIssueService.issueStock(issueId, auth.getName())));
    }

    // ── MANAGER — Approve ──────────────────────────────────────────────────
    @PutMapping("/{issueId}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<StockIssueResponse>> approve(@PathVariable Long issueId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Stock issue approved",
                stockIssueService.approveIssue(issueId, auth.getName())));
    }

    // ── MANAGER — Reject ───────────────────────────────────────────────────
    @PutMapping("/{issueId}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<StockIssueResponse>> reject(@PathVariable Long issueId,
                                                                  Authentication auth,
                                                                  @Valid @RequestBody StockIssueRejectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock issue rejected",
                stockIssueService.rejectIssue(issueId, auth.getName(), request)));
    }

    // ── MANAGER — Pending issues in my warehouse ───────────────────────────
    @GetMapping("/warehouse/pending")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<StockIssueResponse>>> getPendingForWarehouse(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Pending issues fetched",
                stockIssueService.getPendingIssuesForWarehouse(auth.getName())));
    }

    // ── MANAGER — All issues in my warehouse ──────────────────────────────
    @GetMapping("/warehouse/all")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<StockIssueResponse>>> getAllForWarehouse(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("All warehouse issues fetched",
                stockIssueService.getAllIssuesForWarehouse(auth.getName())));
    }

    // ── STAFF — My issues ──────────────────────────────────────────────────
    @GetMapping("/my-issues")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<List<StockIssueResponse>>> getMyIssues(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Stock issues fetched",
                stockIssueService.getIssuesCreatedBy(auth.getName())));
    }

    // ── ANY INTERNAL — Get by ID ───────────────────────────────────────────
    @GetMapping("/{issueId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<ApiResponse<StockIssueResponse>> getById(@PathVariable Long issueId) {
        return ResponseEntity.ok(ApiResponse.success("Stock issue fetched",
                stockIssueService.getIssueById(issueId)));
    }
}
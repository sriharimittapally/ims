package com.infosys.ims.controller;

import com.infosys.ims.dtos.request.PurchaseOrderRejectionRequest;
import com.infosys.ims.dtos.request.PurchaseOrderRequest;
import com.infosys.ims.dtos.response.ApiResponse;
import com.infosys.ims.dtos.response.PurchaseOrderResponse;
import com.infosys.ims.enums.PurchaseOrderStatus;
import com.infosys.ims.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    // ── MANAGER — Create PO ────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> create(Authentication auth,
                                                                     @Valid @RequestBody PurchaseOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase order created",
                        purchaseOrderService.createPO(auth.getName(), request)));
    }

    // ── MANAGER — Send PO to supplier ──────────────────────────────────────
    @PutMapping("/{id}/send")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> send(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Purchase order sent",
                purchaseOrderService.sendPO(id, auth.getName())));
    }

    // ── STAFF — Receive PO ─────────────────────────────────────────────────
    @PutMapping("/{id}/receive")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> receive(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Purchase order received",
                purchaseOrderService.receivePO(id, auth.getName())));
    }

    // ── MANAGER/ADMIN — Cancel ─────────────────────────────────────────────
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> cancel(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Purchase order cancelled",
                purchaseOrderService.cancelPO(id, auth.getName())));
    }

    // ── ADMIN — All POs ────────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Purchase orders fetched", purchaseOrderService.getAllPOs()));
    }

    // ── ADMIN — By status ──────────────────────────────────────────────────
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getByStatus(@PathVariable PurchaseOrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Purchase orders fetched",
                purchaseOrderService.getPOsByStatus(status)));
    }

    // ── MANAGER — My warehouse POs ─────────────────────────────────────────
    @GetMapping("/my-warehouse")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getMyWarehousePOs(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Purchase orders fetched",
                purchaseOrderService.getMyWarehousePOs(auth.getName())));
    }

    // ── ANY AUTH — Get by ID ───────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPPLIER')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Purchase order fetched",
                purchaseOrderService.getPOById(id)));
    }

    // ── SUPPLIER — My POs ──────────────────────────────────────────────────
    @GetMapping("/supplier/my-pos")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getMyPOs(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Your purchase orders fetched",
                purchaseOrderService.getMyPOs(auth.getName())));
    }

    @GetMapping("/supplier/status/{status}")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<ApiResponse<List<PurchaseOrderResponse>>> getMyPOsByStatus(
            Authentication auth, @PathVariable PurchaseOrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Purchase orders fetched",
                purchaseOrderService.getMyPOsByStatus(auth.getName(), status)));
    }

    // ── SUPPLIER — Accept ──────────────────────────────────────────────────
    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> accept(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Purchase order accepted",
                purchaseOrderService.acceptPO(id, auth.getName())));
    }

    // ── SUPPLIER — Reject ──────────────────────────────────────────────────
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> reject(@PathVariable Long id,
                                                                     Authentication auth,
                                                                     @Valid @RequestBody PurchaseOrderRejectionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Purchase order rejected",
                purchaseOrderService.rejectPO(id, auth.getName(), request)));
    }

    // ── SUPPLIER — Ship ────────────────────────────────────────────────────
    @PutMapping("/{id}/ship")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<ApiResponse<PurchaseOrderResponse>> ship(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Purchase order shipped",
                purchaseOrderService.shipPO(id, auth.getName())));
    }
}
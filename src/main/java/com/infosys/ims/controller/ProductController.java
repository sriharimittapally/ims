package com.infosys.ims.controller;

import com.infosys.ims.dtos.request.ProductRequest;
import com.infosys.ims.dtos.request.SupplierLinkRequest;
import com.infosys.ims.dtos.response.ApiResponse;
import com.infosys.ims.dtos.response.ProductResponse;
import com.infosys.ims.dtos.response.ProductSupplierResponse;
import com.infosys.ims.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ── ADMIN/MANAGER — CRUD ───────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created", productService.createProduct(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Product updated", productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deactivated"));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> activate(@PathVariable Long id) {
        productService.activateProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product activated"));
    }

    // ── READ — all roles ───────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Products fetched", productService.getAllProducts()));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPPLIER')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getActive() {
        return ResponseEntity.ok(ApiResponse.success("Active products fetched", productService.getActiveProducts()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPPLIER')")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Product fetched", productService.getProductById(id)));
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPPLIER')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(ApiResponse.success("Products fetched", productService.getProductsByCategory(categoryId)));
    }

    // ── MANAGER — Preferred supplier ──────────────────────────────────────
    @PutMapping("/{productId}/preferred-supplier/{productSupplierId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<String>> setPreferred(@PathVariable Long productId,
                                                            @PathVariable Long productSupplierId) {
        productService.setPreferredSupplier(productId, productSupplierId);
        return ResponseEntity.ok(ApiResponse.success("Preferred supplier set"));
    }

    // ── MANAGER/ADMIN — Remove supplier link ──────────────────────────────
    @DeleteMapping("/supplier-links/{productSupplierId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<String>> removeLink(@PathVariable Long productSupplierId) {
        productService.removeSupplierLink(productSupplierId);
        return ResponseEntity.ok(ApiResponse.success("Supplier link removed"));
    }

    // ── SUPPLIER — Browse products in my categories ────────────────────────
    @GetMapping("/my-categories")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsInMyCategories(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Products fetched",
                productService.getProductsInMyCategories(auth.getName())));
    }

    // ── SUPPLIER — My linked products ─────────────────────────────────────
    @GetMapping("/my-linked")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getMyLinkedProducts(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Linked products fetched",
                productService.getMyLinkedProducts(auth.getName())));
    }

    // ── SUPPLIER — Link to a product ──────────────────────────────────────
    @PostMapping("/{productId}/link")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<ApiResponse<ProductSupplierResponse>> linkProduct(
            @PathVariable Long productId,
            @Valid @RequestBody SupplierLinkRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product linked successfully",
                        productService.linkProduct(productId, auth.getName(), request)));
    }

    // ── SUPPLIER — Update my price ─────────────────────────────────────────
    @PutMapping("/supplier-links/{productSupplierId}/price")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<ApiResponse<ProductSupplierResponse>> updateMyPrice(
            @PathVariable Long productSupplierId,
            @RequestParam BigDecimal purchasePrice,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Price updated",
                productService.updateMyPrice(productSupplierId, auth.getName(), purchasePrice)));
    }
}
package com.infosys.ims.service;

import com.infosys.ims.dtos.request.ApprovalRequest;
import com.infosys.ims.dtos.request.CreateUserRequest;
import com.infosys.ims.dtos.request.SupplierProfileRequest;
import com.infosys.ims.dtos.response.SupplierProfileResponse;
import com.infosys.ims.entity.Supplier;

import java.util.List;

public interface SupplierService {

    String registerSupplierUser(CreateUserRequest request);
    String completeProfile(String email, SupplierProfileRequest request);
    SupplierProfileResponse getProfile(String email);
    List<SupplierProfileResponse> getAllSuppliers();
    List<SupplierProfileResponse> getPendingSuppliers();
    SupplierProfileResponse getSupplierById(Long id);
    void approveSupplier(Long supplierId, String adminEmail);
    void rejectSupplier(Long supplierId, String adminEmail, ApprovalRequest request);
    void revokeApproval(Long supplierId, String adminEmail);     // NEW
    void revokeRejection(Long supplierId, String adminEmail);    // NEW
    Supplier getApprovedSupplierEntity(String email);
    Supplier getSupplierEntity(String email);
}
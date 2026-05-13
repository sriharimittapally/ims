package com.infosys.ims.mapper;

import com.infosys.ims.dtos.response.SupplierProfileResponse;
import com.infosys.ims.entity.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SupplierMapper {

    private final CategoryMapper categoryMapper;

    public SupplierProfileResponse mapToResponse(Supplier supplier) {
        SupplierProfileResponse response = new SupplierProfileResponse();
        response.setId(supplier.getId());
        response.setName(supplier.getUser().getName());
        response.setEmail(supplier.getUser().getEmail());
        response.setUserCode(supplier.getUser().getUserCode());
        response.setCompanyName(supplier.getCompanyName());
        response.setAddress(supplier.getAddress());
        response.setGstNumber(supplier.getGstNumber());
        response.setPhone(supplier.getPhone());
        // FIX: enum → String
        response.setApprovalStatus(supplier.getApprovalStatus().name());
        response.setRejectionReason(supplier.getRejectionReason());
        response.setCreatedAt(supplier.getCreatedAt());
        response.setReviewedAt(supplier.getReviewedAt());

        return response;
    }
}
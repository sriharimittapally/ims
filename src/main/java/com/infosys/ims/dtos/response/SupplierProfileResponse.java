package com.infosys.ims.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String userCode;
    private String companyName;
    private String address;
    private String gstNumber;
    private String phone;
    private String approvalStatus;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private List<CategoryResponse> categories;
}
package com.infosys.ims.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {
    private Long id;
    private String poNumber;
    private Long supplierId;
    private String supplierName;
    private String supplierUserCode;
    private String companyName;
    private Long warehouseId;
    private String warehouseName;
    private String warehouseCity;
    private String status;
    private BigDecimal totalAmount;
    private String note;
    private String rejectionReason;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime receivedAt;
    private List<PurchaseOrderItemResponse> items;
}
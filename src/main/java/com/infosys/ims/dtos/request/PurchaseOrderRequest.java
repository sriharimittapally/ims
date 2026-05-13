package com.infosys.ims.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderRequest {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @Valid
    @NotEmpty(message = "At least one item is required")
    private List items;

    private String note;

    private LocalDate expectedDelivery;
}
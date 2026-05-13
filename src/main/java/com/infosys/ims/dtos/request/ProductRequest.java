package com.infosys.ims.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotBlank(message = "Product name is required")
    private String productName;

    private String description;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.01", message = "Selling price must be greater than 0")
    private BigDecimal sellingPrice;

    @NotNull(message = "Reorder level is required")
    @Min(value = 1, message = "Reorder level must be at least 1")
    private Integer reorderLevel;
}
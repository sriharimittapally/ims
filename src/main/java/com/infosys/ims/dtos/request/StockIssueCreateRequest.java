package com.infosys.ims.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
 * CHANGES MADE:
 *
 * 1. Added items list.
 *    -> Earlier stock issue creation was incomplete.
 *
 * 2. Added nested validation.
 *
 * 3. Added constructors.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockIssueCreateRequest {

    @NotNull(message = "Warehouse id is required")
    private Long warehouseId;

    @Valid
    @NotEmpty(message = "At least one item is required")
    private List<StockIssueItemRequest> items;

    private String note;
}
package com.infosys.ims.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierProfileRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "GST number is required")
//    @Pattern(
//            regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
//            message = "Invalid GST number format"
//    )
    private String gstNumber;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;

    @NotEmpty(message = "Select at least one category")
    private List<Long> categoryIds;
}
package com.infosys.ims.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponse {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String status;
    private Long managerId;
    private String managerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
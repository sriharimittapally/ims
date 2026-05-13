package com.infosys.ims.mapper;

import com.infosys.ims.dtos.response.UserResponse;
import com.infosys.ims.entity.Users;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse mapToResponse(Users user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole().name());
        response.setStatus(user.getStatus().name());
        response.setUserCode(user.getUserCode());
        response.setCreatedAt(user.getCreatedAt());
        if (user.getWarehouse() != null) {
            response.setWarehouseId(user.getWarehouse().getId());
            response.setWarehouseName(user.getWarehouse().getName());
        }
        return response;
    }
}
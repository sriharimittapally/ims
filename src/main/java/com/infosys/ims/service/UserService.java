package com.infosys.ims.service;

import com.infosys.ims.dtos.request.CreateUserRequest;
import com.infosys.ims.dtos.response.PageResponse;
import com.infosys.ims.dtos.response.UserResponse;
import com.infosys.ims.entity.Users;
import com.infosys.ims.enums.Role;

import java.util.List;

public interface UserService {

     /** Admin creates a manager and assigns to a warehouse */
     String createManager(CreateUserRequest request);

     /** Admin creates staff and assigns to a warehouse directly */
     String createStaff(CreateUserRequest request);

     /** Manager creates staff for their own warehouse */
     String createStaffByManager(CreateUserRequest request, String managerEmail);

     void activateUser(Long userId);

     void deactivateUser(Long userId);

     /** Admin reassigns a manager to a different warehouse */
     void assignManagerToWarehouse(Long warehouseId, Long managerId);

     List<UserResponse> getAllUsers();

     List<UserResponse> getUsersByRole(Role role);

     List<UserResponse> getStaffForMyWarehouse(String managerEmail);
     PageResponse<UserResponse> getUsersByRolePaged(
             Role role,
             int page,
             int size,
             String search,
             String status);

     PageResponse<UserResponse> getStaffForMyWarehousePaged(
             String managerEmail,
             int page,
             int size,
             String search,
             String status);

     Users getUserEntity(String email);

     Users getUserEntityById(Long id);
}

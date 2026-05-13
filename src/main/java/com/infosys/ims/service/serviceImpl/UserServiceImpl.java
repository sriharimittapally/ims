package com.infosys.ims.service.serviceImpl;

import com.infosys.ims.dtos.request.CreateUserRequest;
import com.infosys.ims.dtos.response.UserResponse;
import com.infosys.ims.entity.Users;
import com.infosys.ims.entity.Warehouse;
import com.infosys.ims.enums.Role;
import com.infosys.ims.enums.UserStatus;
import com.infosys.ims.exception.BadRequestException;
import com.infosys.ims.exception.DuplicateResourceException;
import com.infosys.ims.exception.ForbiddenOperationException;
import com.infosys.ims.exception.ResourceNotFoundException;
import com.infosys.ims.mapper.UserMapper;
import com.infosys.ims.repository.UserRepository;
import com.infosys.ims.repository.WarehouseRepository;
import com.infosys.ims.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    private String generateUserCode(Role role) {
        long count = userRepository.countByRole(role) + 1;
        String prefix = switch (role) {
            case ADMIN    -> "ADM";
            case MANAGER  -> "MGR";
            case STAFF    -> "STF";
            case SUPPLIER -> "SUP";
        };
        return prefix + "-" + String.format("%04d", count);
    }

    @Override
    @Transactional
    public String createManager(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }


        Users manager = buildUser(request, Role.MANAGER, null);
        Users saved = userRepository.save(manager);

        return saved.getUserCode();
    }

    @Override
    @Transactional
    public String createStaff(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }
        if (request.getWarehouseId() == null) {
            throw new BadRequestException("Warehouse ID is required when creating staff");
        }

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + request.getWarehouseId()));

        Users staff = buildUser(request, Role.STAFF, warehouse);
        Users saved = userRepository.save(staff);
        return saved.getUserCode();
    }

    @Override
    @Transactional
    public String createStaffByManager(CreateUserRequest request, String managerEmail) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }

        Users manager = getUserEntity(managerEmail);
        if (manager.getWarehouse() == null) {
            throw new BadRequestException("Manager is not assigned to any warehouse");
        }

        Users staff = buildUser(request, Role.STAFF, manager.getWarehouse());
        Users saved = userRepository.save(staff);
        return saved.getUserCode();
    }

    @Override
    @Transactional
    public void activateUser(Long userId) {
        Users user = getUserEntityById(userId);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivateUser(Long userId) {
        Users user = getUserEntityById(userId);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void assignManagerToWarehouse(Long warehouseId, Long managerId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + warehouseId));

        Users manager = getUserEntityById(managerId);
        if (manager.getRole() != Role.MANAGER) {
            throw new BadRequestException("User is not a manager");
        }

        // Unassign old manager if present
        if (warehouse.getManager() != null && !warehouse.getManager().getId().equals(managerId)) {
            Users oldManager = warehouse.getManager();
            oldManager.setWarehouse(null);
            userRepository.save(oldManager);
        }

        // Unassign manager from their previous warehouse if any
        if (manager.getWarehouse() != null && !manager.getWarehouse().getId().equals(warehouseId)) {
            Warehouse oldWarehouse = manager.getWarehouse();
            oldWarehouse.setManager(null);
            warehouseRepository.save(oldWarehouse);
        }

        manager.setWarehouse(warehouse);
        userRepository.save(manager);

        warehouse.setManager(manager);
        warehouseRepository.save(warehouse);
    }

    @Override
    public List getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(userMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List getStaffForMyWarehouse(String managerEmail) {
        Users manager = getUserEntity(managerEmail);
        if (manager.getWarehouse() == null) {
            throw new BadRequestException("Manager is not assigned to any warehouse");
        }
        return userRepository.findByWarehouseAndRole(manager.getWarehouse(), Role.STAFF)
                .stream()
                .map(userMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Users getUserEntity(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    @Override
    public Users getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    // ---- Helper ----
    private Users buildUser(CreateUserRequest request, Role role, Warehouse warehouse) {
        Users user = new Users();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setWarehouse(warehouse);
        user.setUserCode(generateUserCode(role));
        return user;
    }
}
package com.infosys.ims.service.serviceImpl;

import com.infosys.ims.dtos.request.ApprovalRequest;
import com.infosys.ims.dtos.request.CreateUserRequest;
import com.infosys.ims.dtos.request.SupplierProfileRequest;
import com.infosys.ims.dtos.response.SupplierProfileResponse;
import com.infosys.ims.entity.Category;
import com.infosys.ims.entity.Supplier;
import com.infosys.ims.entity.Users;
import com.infosys.ims.enums.ApprovalStatus;
import com.infosys.ims.enums.Role;
import com.infosys.ims.exception.BadRequestException;
import com.infosys.ims.exception.DuplicateResourceException;
import com.infosys.ims.exception.ForbiddenOperationException;
import com.infosys.ims.exception.ResourceNotFoundException;
import com.infosys.ims.mapper.SupplierMapper;
import com.infosys.ims.repository.CategoryRepository;
import com.infosys.ims.repository.SupplierRepository;
import com.infosys.ims.repository.UserRepository;
import com.infosys.ims.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional
    public String registerSupplierUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        long count = userRepository.countByRole(Role.SUPPLIER) + 1;
        String userCode = "SUP-" + String.format("%04d", count);
        Users user = new Users();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(Role.SUPPLIER);
        user.setUserCode(userCode);
        userRepository.save(user);
        return userCode;
    }

    @Override
    @Transactional
    public String completeProfile(String email, SupplierProfileRequest request) {
        Users user = getUserEntity(email);
        if (supplierRepository.existsByUser(user)) {
            throw new DuplicateResourceException("Profile already completed for: " + email);
        }
        List<Category> categories = request.getCategoryIds().stream()
                .map(id -> categoryRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id)))
                .collect(Collectors.toList());
        Supplier supplier = new Supplier();
        supplier.setUser(user);
        supplier.setCompanyName(request.getCompanyName());
        supplier.setAddress(request.getAddress());
        supplier.setGstNumber(request.getGstNumber());
        supplier.setPhone(request.getPhone());
        supplier.setCategories(categories);
        supplierRepository.save(supplier);
        return "Profile completed. Awaiting admin approval.";
    }

    @Override
    public SupplierProfileResponse getProfile(String email) {
        Users user = getUserEntity(email);
        Supplier supplier = supplierRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier profile not found. Please complete your profile first."));
        return supplierMapper.mapToResponse(supplier);
    }

    @Override
    public List<SupplierProfileResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(supplierMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupplierProfileResponse> getPendingSuppliers() {
        return supplierRepository.findByApprovalStatus(ApprovalStatus.PENDING).stream()
                .map(supplierMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SupplierProfileResponse getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + id));
        return supplierMapper.mapToResponse(supplier);
    }

    @Override
    @Transactional
    public void approveSupplier(Long supplierId, String adminEmail) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + supplierId));
        if (supplier.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new BadRequestException("Supplier is not in PENDING status");
        }
        Users admin = getUserEntity(adminEmail);
        supplier.setApprovalStatus(ApprovalStatus.APPROVED);
        supplier.setReviewedBy(admin);
        supplier.setReviewedAt(LocalDateTime.now());
        supplierRepository.save(supplier);
    }

    @Override
    @Transactional
    public void rejectSupplier(Long supplierId, String adminEmail, ApprovalRequest request) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + supplierId));
        if (supplier.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new BadRequestException("Supplier is not in PENDING status");
        }
        Users admin = getUserEntity(adminEmail);
        supplier.setApprovalStatus(ApprovalStatus.REJECTED);
        supplier.setRejectionReason(request.getReason());
        supplier.setReviewedBy(admin);
        supplier.setReviewedAt(LocalDateTime.now());
        supplierRepository.save(supplier);
    }

    @Override
    @Transactional
    public void revokeApproval(Long supplierId, String adminEmail) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + supplierId));
        if (supplier.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new BadRequestException("Supplier is not in APPROVED status");
        }
        supplier.setApprovalStatus(ApprovalStatus.PENDING);
        supplier.setReviewedBy(null);
        supplier.setReviewedAt(null);
        supplierRepository.save(supplier);
    }

    @Override
    @Transactional
    public void revokeRejection(Long supplierId, String adminEmail) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + supplierId));
        if (supplier.getApprovalStatus() != ApprovalStatus.REJECTED) {
            throw new BadRequestException("Supplier is not in REJECTED status");
        }
        supplier.setApprovalStatus(ApprovalStatus.PENDING);
        supplier.setRejectionReason(null);
        supplier.setReviewedBy(null);
        supplier.setReviewedAt(null);
        supplierRepository.save(supplier);
    }

    @Override
    public Supplier getApprovedSupplierEntity(String email) {
        Users user = getUserEntity(email);
        Supplier supplier = supplierRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier profile not found for: " + email));
        if (supplier.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new ForbiddenOperationException("Supplier account is not approved yet");
        }
        return supplier;
    }

    @Override
    public Supplier getSupplierEntity(String email) {
        Users user = getUserEntity(email);
        return supplierRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier profile not found for: " + email));
    }

    private Users getUserEntity(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
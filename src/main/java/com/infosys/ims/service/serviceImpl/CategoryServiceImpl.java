package com.infosys.ims.service.serviceImpl;

import com.infosys.ims.dtos.request.CategoryRequest;
import com.infosys.ims.dtos.response.CategoryResponse;
import com.infosys.ims.entity.Category;
import com.infosys.ims.enums.CategoryStatus;
import com.infosys.ims.exception.BadRequestException;
import com.infosys.ims.exception.DuplicateResourceException;
import com.infosys.ims.exception.ResourceNotFoundException;
import com.infosys.ims.mapper.CategoryMapper;
import com.infosys.ims.repository.CategoryRepository;
import com.infosys.ims.repository.ProductRepository;
import com.infosys.ims.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category already exists: " + request.getName());
        }
        Category category = categoryMapper.mapToEntity(request);
        return categoryMapper.mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = getEntity(id);
        // Check name uniqueness (excluding self)
        categoryRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("Category name already in use: " + request.getName());
            }
        });
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return categoryMapper.mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getEntity(id);
        category.setStatus(CategoryStatus.INACTIVE);
        categoryRepository.save(category);
    }
    @Override
    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll().stream()
                .map(category -> {

                    CategoryResponse response =
                            categoryMapper.mapToResponse(category);

                    long productCount =
                            productRepository.countByCategory_Id(category.getId());

                    response.setProductCount(productCount);

                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByStatus(CategoryStatus.ACTIVE).stream()
                .map(categoryMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        return categoryMapper.mapToResponse(getEntity(id));
    }

    @Override
    @Transactional
    public void deactivateCategory(Long id) {
        Category category = getEntity(id);
        category.setStatus(CategoryStatus.INACTIVE);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void activateCategory(Long id) {
        Category category = getEntity(id);
        category.setStatus(CategoryStatus.ACTIVE);
        categoryRepository.save(category);
    }

    private Category getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }
}
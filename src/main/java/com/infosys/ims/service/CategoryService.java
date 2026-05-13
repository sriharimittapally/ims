package com.infosys.ims.service;

import com.infosys.ims.dtos.request.CategoryRequest;
import com.infosys.ims.dtos.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);

    List<CategoryResponse> getAllCategories();

    List<CategoryResponse> getActiveCategories();

    CategoryResponse getCategoryById(Long id);

    void deactivateCategory(Long id);

    void activateCategory(Long id);
}
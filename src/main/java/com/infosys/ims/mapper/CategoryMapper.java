package com.infosys.ims.mapper;

import com.infosys.ims.dtos.request.CategoryRequest;
import com.infosys.ims.dtos.response.CategoryResponse;
import com.infosys.ims.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse mapToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        // FIX: enum → String
        response.setStatus(category.getStatus().name());
        response.setCreatedAt(category.getCreatedAt());
        return response;
    }

    public Category mapToEntity(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return category;
    }
}
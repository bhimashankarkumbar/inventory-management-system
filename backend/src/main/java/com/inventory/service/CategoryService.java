package com.inventory.service;

import com.inventory.entity.Category;
import com.inventory.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Category with this name already exists");
        }
        return categoryRepository.save(category);
    }

    public List<Category> getAllActiveCategories() {
        return categoryRepository.findAll()
                .stream()
                .filter(Category::getIsActive)
                .toList();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    }

    public Category updateCategory(Long id, Category updatedData) {
        Category existing = getCategoryById(id);
        existing.setName(updatedData.getName());
        existing.setDescription(updatedData.getDescription());
        return categoryRepository.save(existing);
    }

    public void deactivateCategory(Long id) {
        Category existing = getCategoryById(id);
        existing.setIsActive(false);
        categoryRepository.save(existing);
    }
}
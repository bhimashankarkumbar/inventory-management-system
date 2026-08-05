package com.inventory.service;

import com.inventory.entity.Category;
import com.inventory.entity.Product;
import com.inventory.repository.CategoryRepository;
import com.inventory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Product createProduct(Product product, Long categoryId) {
        if (productRepository.existsBySku(product.getSku())) {
            throw new IllegalArgumentException("Product with this SKU already exists");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        product.setCategory(category);
        return productRepository.save(product);
    }

    public List<Product> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public Product updateProduct(Long id, Product updatedData, Long categoryId) {
        Product existing = getProductById(id);

        existing.setName(updatedData.getName());
        existing.setUnit(updatedData.getUnit());
        existing.setMinStockThreshold(updatedData.getMinStockThreshold());
        existing.setBarcode(updatedData.getBarcode());
        existing.setImagePath(updatedData.getImagePath());

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            existing.setCategory(category);
        }

        return productRepository.save(existing);
    }

    public void deactivateProduct(Long id) {
        Product existing = getProductById(id);
        existing.setIsActive(false);
        productRepository.save(existing);
    }
}
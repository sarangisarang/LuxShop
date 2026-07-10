package com.luxshop.shop.service;
import com.luxshop.shop.domain.Category;
import com.luxshop.shop.domain.Product;
import com.luxshop.shop.repository.CategoryRepository;
import com.luxshop.shop.repository.ProductRepository;
import com.luxshop.shop.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;

    // #4: create with a generated id and a required name.
    public Category createCategory(Category category){
        validateCategory(category);
        category.setId(UUID.randomUUID().toString());
        return categoryRepository.save(category);
    }

    public Category CreateCategoryOrder(Category category, String id){
        validateCategory(category);
        Category categoryToUpdate = categoryRepository.findById(id).orElseThrow();
        categoryToUpdate.setName(category.getName());
        categoryToUpdate.setDescription(category.getDescription());
        categoryToUpdate.setImage(category.getImage());
        return categoryRepository.save(categoryToUpdate);
    }

    private void validateCategory(Category category){
        if (category.getName() == null || category.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name is required");
        }
    }

    public void deleteCategory(String id){
        Category category = categoryRepository.findById(id).orElseThrow();
        // orElse avoids a NoSuchElementException from .get() if the Optional is ever absent.
        List<Product> products = productRepository.findAllByCategory(category).orElse(List.of());
        if(products.isEmpty()){
            categoryRepository.delete(category);
        }else{
            throw new ConflictException("Not allowed to delete this Category");
        }
    }
}
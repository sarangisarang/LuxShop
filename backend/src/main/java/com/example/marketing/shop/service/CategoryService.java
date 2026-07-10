package com.example.marketing.shop.service;
import com.example.marketing.shop.domain.Category;
import com.example.marketing.shop.domain.Product;
import com.example.marketing.shop.repository.CategoryRepository;
import com.example.marketing.shop.repository.ProductRepository;
import com.example.marketing.shop.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;

    public Category CreateCategoryOrder(Category category, String id){
        Category categoryToUpdate = categoryRepository.findById(id).orElseThrow();
        categoryToUpdate.setName(category.getName());
        categoryToUpdate.setDescription(category.getDescription());
        categoryToUpdate.setImage(category.getImage());
        return categoryRepository.save(categoryToUpdate);
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
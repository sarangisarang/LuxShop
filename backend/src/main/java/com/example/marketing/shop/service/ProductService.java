package com.example.marketing.shop.service;
import com.example.marketing.shop.domain.Category;
import com.example.marketing.shop.domain.OrderDetails;
import com.example.marketing.shop.domain.Product;
import com.example.marketing.shop.repository.CategoryRepository;
import com.example.marketing.shop.repository.OrderDetailsRepository;
import com.example.marketing.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;
    @Autowired
    private ProductRepository productRepository;

    public List<Product> creategetOrderedProductsByCategory(String categoryName){
        Category category = categoryRepository.findByName(categoryName);
        List<OrderDetails> details = orderDetailsRepository.findAllByProductCategory(category);
        return details.stream().map(d -> d.getProduct()).toList();
    }

    public void deleteProduct(String id){
        Product product = productRepository.findById(id).orElseThrow();
        Optional<List<OrderDetails>> orderDetailsList = orderDetailsRepository.findAllByProduct(product);
        if(orderDetailsList.get().isEmpty()){
            productRepository.delete(product);
        }else{
            throw new RuntimeException("can not delete this Product");
        }
    }

    public Product createSaveProduct(@RequestBody Product product, String categoryId){
        product.setId(UUID.randomUUID().toString());
        Category category = categoryRepository.findById(categoryId).orElseThrow();
        product.setCategory(category);
        return productRepository.save(product);
    }

    public Product createUpdateProucts(@RequestBody Product product,String id){
        Product productsToUpdate = productRepository.findById(id).orElseThrow();
        productsToUpdate.setProductName(product.getProductName());
        productsToUpdate.setProductDesc(product.getProductDesc());
        productsToUpdate.setImage1(product.getImage1());
        productsToUpdate.setImage2(product.getImage2());
        productsToUpdate.setImage3(product.getImage3());
        productsToUpdate.setPrece(product.getPrece());
        productsToUpdate.setStock(product.getStock());
        return productRepository.save(productsToUpdate);
    }
}

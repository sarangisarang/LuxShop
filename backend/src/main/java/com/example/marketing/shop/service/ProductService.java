package com.example.marketing.shop.service;
import com.example.marketing.shop.domain.Category;
import com.example.marketing.shop.domain.OrderDetails;
import com.example.marketing.shop.domain.Product;
import com.example.marketing.shop.repository.CategoryRepository;
import com.example.marketing.shop.repository.OrderDetailsRepository;
import com.example.marketing.shop.repository.ProductRepository;
import com.example.marketing.shop.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
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
        // findAllByProduct returns Optional<List>; orElse yields an empty list instead of
        // risking a NoSuchElementException from .get() on an absent Optional.
        List<OrderDetails> orderDetailsList = orderDetailsRepository.findAllByProduct(product).orElse(List.of());
        if(orderDetailsList.isEmpty()){
            productRepository.delete(product);
        }else{
            throw new ConflictException("can not delete this Product");
        }
    }

    public Product createSaveProduct(@RequestBody Product product, String categoryId){
        validateProduct(product);
        product.setId(UUID.randomUUID().toString());
        Category category = categoryRepository.findById(categoryId).orElseThrow();
        product.setCategory(category);
        return productRepository.save(product);
    }

    public Product createUpdateProucts(@RequestBody Product product,String id){
        validateProduct(product);
        Product productsToUpdate = productRepository.findById(id).orElseThrow();
        productsToUpdate.setProductName(product.getProductName());
        productsToUpdate.setProductDesc(product.getProductDesc());
        productsToUpdate.setImage1(product.getImage1());
        productsToUpdate.setImage2(product.getImage2());
        productsToUpdate.setImage3(product.getImage3());
        productsToUpdate.setPrice(product.getPrice());
        productsToUpdate.setStock(product.getStock());
        return productRepository.save(productsToUpdate);
    }

    // #5: a product needs a name and non-negative price/stock.
    private void validateProduct(Product product) {
        if (product.getProductName() == null || product.getProductName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name is required");
        }
        if (product.getPrice() == null || product.getPrice().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must be zero or positive");
        }
        if (product.getStock() == null || product.getStock() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock must be zero or positive");
        }
    }
}

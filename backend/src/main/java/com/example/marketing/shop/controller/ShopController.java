package com.example.marketing.shop.controller;
import com.example.marketing.shop.domain.Category;
import com.example.marketing.shop.domain.Customer;
import com.example.marketing.shop.domain.Product;
import com.example.marketing.shop.dto.CustomerResponse;
import com.example.marketing.shop.repository.*;
import com.example.marketing.shop.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/shop")
public class ShopController{
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrdersRepository ordersRepository;
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private OrderDetailsService orderDetailsService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductService productService;

    //GetMapping, PostMapping, PutMapping, DeleteMapping.

    @GetMapping("/categories")
    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }

    @GetMapping("/category/{id}")
    public Category getCategory(@PathVariable String id) {
        return categoryRepository.findById(id).orElseThrow();
    }

    @PostMapping("/category")
    @ResponseStatus(HttpStatus.CREATED)
    public Category saveCategory(@RequestBody Category category){
        return categoryService.createCategory(category);
    }

    @PutMapping("/category/{id}") // This is tested, works.
    public Category updateCategory(@RequestBody Category category, @PathVariable String id){
        return categoryService.CreateCategoryOrder(category,id);
    }

    @DeleteMapping("/category/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
    }

    //GetMapping, GetMappig(add Id), PostMapping, PutMapping, DeleteMapping.

    @GetMapping("/customers")
    public List<CustomerResponse> getAllCustumeries(){
        // Map to DTOs so the password is never serialized to clients.
        return customerRepository.findAll().stream().map(CustomerResponse::from).toList();
    }

    @GetMapping("/customer/{id}")
    public CustomerResponse getCustomer(@PathVariable String id) {
        return CustomerResponse.from(customerRepository.findById(id).orElseThrow());
    }

    @PostMapping("/customer")
    public CustomerResponse saveCustomer(@RequestBody Customer customer){
        customer.setId(UUID.randomUUID().toString());
        return CustomerResponse.from(customerRepository.save(customer));
    }

    @PutMapping("/customer/{id}")
    public CustomerResponse updateCustomer(@RequestBody Customer customer, @PathVariable String id){
        return CustomerResponse.from(customerService.CreateCustomerOrder(customer,id));
    }

    @DeleteMapping("/customer/{id}")
    public void deleteCustomer(@PathVariable String id) {
        Customer customer = customerRepository.findById(id).orElseThrow();
        customerRepository.delete(customer);
    }

    // GetMapping, PostMapping, PutMapping, DeleteMapping.

    @GetMapping("/products")
    public List<Product> getAllProduct(){
        return productRepository.findAll();
    }

    @GetMapping("/products/{categoryName}")
    public List<Product> getProductsByCategory(@PathVariable String categoryName){
        return productRepository.findAllByCategoryName(categoryName);
    }

    @GetMapping("/products/{categoryName}/ordered")
    public List<Product> getOrderedProductsByCategory(@PathVariable String categoryName) {
        return productService.creategetOrderedProductsByCategory(categoryName);
    }

    @GetMapping("/products/TotalPrice")
    public BigInteger getTotalOrderedAmount() {
        return orderService.getTotalOrderedAmount();
    }

    @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable String id) {
        return productRepository.findById(id).orElseThrow();
    }

    @PostMapping("/product/{categoryId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Product saveProduct(@RequestBody Product product, @PathVariable String categoryId){
        return productService.createSaveProduct(product,categoryId);
    }

    @PutMapping("/product/{id}")
    public Product updateProucts(@RequestBody Product product, @PathVariable String id){
        return productService.createUpdateProucts(product,id);
    }

    @DeleteMapping("/product/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProducts(@PathVariable String id) {
        productService.deleteProduct(id);
    }
}
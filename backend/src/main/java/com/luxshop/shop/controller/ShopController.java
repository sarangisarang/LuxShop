package com.luxshop.shop.controller;
import com.luxshop.shop.domain.Category;
import com.luxshop.shop.domain.Customer;
import com.luxshop.shop.domain.Product;
import com.luxshop.shop.dto.CategoryResponse;
import com.luxshop.shop.dto.CustomerResponse;
import com.luxshop.shop.dto.ProductResponse;
import com.luxshop.shop.repository.*;
import com.luxshop.shop.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
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
    public Page<CategoryResponse> getAllCategories(@PageableDefault(size = 12, sort = "id") Pageable pageable){
        return categoryRepository.findAll(pageable).map(CategoryResponse::from);
    }

    @GetMapping("/category/{id}")
    public CategoryResponse getCategory(@PathVariable String id) {
        return CategoryResponse.from(categoryRepository.findById(id).orElseThrow());
    }

    @PostMapping("/category")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse saveCategory(@Valid @RequestBody Category category){
        return CategoryResponse.from(categoryService.createCategory(category));
    }

    @PutMapping("/category/{id}") // This is tested, works.
    public CategoryResponse updateCategory(@Valid @RequestBody Category category, @PathVariable String id){
        return CategoryResponse.from(categoryService.CreateCategoryOrder(category,id));
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
    public CustomerResponse saveCustomer(@Valid @RequestBody Customer customer){
        customer.setId(UUID.randomUUID().toString());
        return CustomerResponse.from(customerRepository.save(customer));
    }

    @PutMapping("/customer/{id}")
    public CustomerResponse updateCustomer(@Valid @RequestBody Customer customer, @PathVariable String id){
        return CustomerResponse.from(customerService.CreateCustomerOrder(customer,id));
    }

    @DeleteMapping("/customer/{id}")
    public void deleteCustomer(@PathVariable String id) {
        Customer customer = customerRepository.findById(id).orElseThrow();
        customerRepository.delete(customer);
    }

    // GetMapping, PostMapping, PutMapping, DeleteMapping.

    @GetMapping("/products")
    public Page<ProductResponse> getAllProduct(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 12, sort = "id") Pageable pageable){
        Page<Product> page = (q == null || q.isBlank())
                ? productRepository.findAll(pageable)
                : productRepository.findByProductNameContainingIgnoreCaseOrProductDescContainingIgnoreCase(
                        q.trim(), q.trim(), pageable);
        return page.map(ProductResponse::from);
    }

    @GetMapping("/products/{categoryName}")
    public List<ProductResponse> getProductsByCategory(@PathVariable String categoryName){
        return productRepository.findAllByCategoryName(categoryName).stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/products/{categoryName}/ordered")
    public List<ProductResponse> getOrderedProductsByCategory(@PathVariable String categoryName) {
        return productService.creategetOrderedProductsByCategory(categoryName).stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/products/TotalPrice")
    public BigDecimal getTotalOrderedAmount() {
        return orderService.getTotalOrderedAmount();
    }

    @GetMapping("/product/{id}")
    public ProductResponse getProduct(@PathVariable String id) {
        return ProductResponse.from(productRepository.findById(id).orElseThrow());
    }

    @PostMapping("/product/{categoryId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse saveProduct(@Valid @RequestBody Product product, @PathVariable String categoryId){
        return ProductResponse.from(productService.createSaveProduct(product,categoryId));
    }

    @PutMapping("/product/{id}")
    public ProductResponse updateProucts(@Valid @RequestBody Product product, @PathVariable String id){
        return ProductResponse.from(productService.createUpdateProucts(product,id));
    }

    @DeleteMapping("/product/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProducts(@PathVariable String id) {
        productService.deleteProduct(id);
    }
}
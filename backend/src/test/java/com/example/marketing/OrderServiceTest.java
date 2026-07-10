package com.example.marketing;
import com.example.marketing.shop.domain.*;
import com.example.marketing.shop.repository.*;
import com.example.marketing.shop.service.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

// Roll back each test so the shared in-memory DB stays clean between methods.
// This matters now that entities are soft-deleted (rows persist), so reused ids
// would otherwise collide across tests.
@SpringBootTest
@Transactional
public class OrderServiceTest {
    @Autowired
    private OrdersRepository ordersRepository;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;
    @Autowired
    private OrderDetailsService orderDetailsService;

    @Test
    public void given_order_with_all_when_has_status_progress_cannot_delete(){
        Orders orders = new Orders();
        orders.setOrderNo(20);
        orders.setOrderTotal(BigDecimal.valueOf(50));
        orders.setShippingDate(LocalDate.now());
        orders.setOrderDate(LocalDate.now());
        orders.setIsDelivered("tashipare");
        orders.setOrderStatus(OrderStatus.Processing);
        orders.setId("1234");
        ordersRepository.save(orders);
        Orders neworders =  ordersRepository.findById("1234").orElseThrow();
        Assertions.assertNotNull(neworders);
        Exception exception = assertThrows(Exception.class,()-> orderService.deleteOrderWithDetails(neworders.getId()));
        assertEquals(exception.getMessage(),"Not allowed to delete  order" );
    }
    @Test
    public void given_order_with_all_when_has_status_pending_can_delete() throws Exception {
        Orders orders = new Orders();
        orders.setOrderNo(20);
        orders.setOrderTotal(BigDecimal.valueOf(50));
        orders.setShippingDate(LocalDate.now());
        orders.setOrderDate(LocalDate.now());
        orders.setIsDelivered("tashipare");
        orders.setOrderStatus(OrderStatus.Pending);
        orders.setId("1234");
        ordersRepository.save(orders);
        Orders neworders =  ordersRepository.findById("1234").orElseThrow();
        Assertions.assertNotNull(neworders);
        orderService.deleteOrderWithDetails(neworders.getId());
        Exception exception = assertThrows(Exception.class,()-> ordersRepository.findById("1234").orElseThrow(()->new RuntimeException("order not finde")));
        assertEquals(exception.getMessage(),"order not finde");
    }
    @Test
    public void given_product_with_all_when_has_in_orderdetails_cannot_delete(){
        Product product = new Product();
        product.setProductDesc("Apfel");
        product.setProductName("Iphone");
        product.setId("2345");
        product.setPrice(BigDecimal.valueOf(1234));
        product.setImage1("204.29.207.217".getBytes());
        product.setImage2("204.29.207.218".getBytes());
        product.setImage3("204.29.207.219".getBytes());
        product.setStock(2000);
        productRepository.save(product);
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setId("1234");
        orderDetails.setProduct(product);
        orderDetails.setSubtotal(BigDecimal.valueOf(200));
        orderDetails.setPrice(BigDecimal.valueOf(200));
        orderDetails.setQty(200);
        orderDetailsRepository.save(orderDetails);
        Product newproducts = productRepository.findById("2345").orElseThrow();
        Assertions.assertNotNull(newproducts);
        Exception exception = assertThrows(Exception.class,()-> productService.deleteProduct(newproducts.getId()));
        assertEquals(exception.getMessage(),"can not delete this Product");
    }
    @Test
    public void given_product_with_all_when_hasnot_in_orderdetails_can_delete(){
        Product product = new Product();
        product.setProductDesc("Apfel");
        product.setProductName("Iphone");
        product.setId("2345");
        product.setPrice(BigDecimal.valueOf(1234));
        product.setImage1("204.29.207.217".getBytes());
        product.setImage2("204.29.207.218".getBytes());
        product.setImage3("204.29.207.219".getBytes());
        product.setStock(2000);
        Assertions.assertNotNull(product);
        Exception exception = assertThrows(Exception.class,()-> productService.deleteProduct(product.getId()));
        assertNotEquals(exception.getMessage(),"order not finde");
    }
    @Test
    public void givenn_catalogy_with_all_when_has_in_product_cannot_delete(){
        Category category = new Category();
        category.setId("1234");
        category.setName("Book");
        category.setName("Image");
        category.setDescription("clasica");
        categoryRepository.save(category);
        Product product = new Product();
        product.setProductDesc("Apfel");
        product.setProductName("Iphone");
        product.setId("2345");
        product.setPrice(BigDecimal.valueOf(1234));
        product.setImage1("204.29.207.217".getBytes());
        product.setImage2("204.29.207.218".getBytes());
        product.setImage3("204.29.207.219".getBytes());
        product.setStock(2000);
        product.setCategory(category);
        productRepository.save(product);
        Exception exception = assertThrows(Exception.class,()->categoryService.deleteCategory(category.getId()));
        assertEquals(exception.getMessage(),"Not allowed to delete this Category");
    }
    @Test
    public void givenn_catalogy_with_all_when_has_not_in_product_can_delete() {
        Category category = new Category();
        category.setId("1234");
        category.setName("Book");
        category.setName("Image");
        category.setDescription("clasica");
        categoryRepository.save(category);
        categoryService.deleteCategory(category.getId());
        Exception exception = assertThrows(Exception.class, () -> categoryRepository.findById("1234").orElseThrow(() -> new RuntimeException("order not finde")));
        assertEquals(exception.getMessage(), "order not finde");
    }
    @Test
    public void given_category_with_all(){
        Category category = new Category();
        category.setName("Book");
        category.setImage("Image");
        category.setDescription("Clasica");
        category.setId("5678");
        categoryRepository.save(category);
        Category NewCategory = categoryRepository.findById("5678").orElseThrow();
        Assertions.assertNotNull(NewCategory);
    }
    @Test
    public void given_customer_with_all(){
        Customer customer = new Customer();
        customer.setPhone(1545453234);
        customer.setCity("Dusseldorf");
        customer.setAddress("Berlinealle50");
        customer.setPostcode(40232);
        customer.setPassword("Anonimus");
        customer.setEmail("beka.kikalishvili.it@gmail.com");
        customer.setFirstName("beka");
        customer.setLastName("kikalishvili");
        customer.setId("6789");
        customerRepository.save(customer);
        Customer newcustomer = customerRepository.findById("6789").orElseThrow();
        Assertions.assertNotNull(newcustomer);
    }
    @Test
    public void given_orderdetails_with_all(){
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setSubtotal(BigDecimal.valueOf(123));
        orderDetails.setQty(3);
        orderDetails.setId("789");
        orderDetails.setPrice(BigDecimal.valueOf(3000));
        orderDetailsRepository.save(orderDetails);
        OrderDetails neworderdetails = orderDetailsRepository.findById("789").orElseThrow();
        Assertions.assertNotNull(neworderdetails);
    }
}

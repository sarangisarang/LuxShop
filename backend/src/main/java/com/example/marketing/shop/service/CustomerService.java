package com.example.marketing.shop.service;
import com.example.marketing.shop.domain.Customer;
import com.example.marketing.shop.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    public Customer CreateCustomerOrder(Customer customer, String id){
        Customer customerToUpdate = customerRepository.findById(id).orElseThrow();
        customerToUpdate.setLastName(customer.getLastName());
        customerToUpdate.setFirstName(customer.getFirstName());
        customerToUpdate.setEmail(customer.getEmail());
        customerToUpdate.setAddress(customer.getAddress());
        customerToUpdate.setPostcode(customer.getPostcode());
        customerToUpdate.setCity(customer.getCity());
        customerToUpdate.setPhone(customer.getPhone());
        return customerRepository.save(customerToUpdate);
    }
}

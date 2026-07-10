package com.luxshop.shop.service;
import com.luxshop.shop.domain.Customer;
import com.luxshop.shop.repository.CustomerRepository;
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

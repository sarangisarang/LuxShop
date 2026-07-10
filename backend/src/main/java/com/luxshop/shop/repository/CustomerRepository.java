package com.luxshop.shop.repository;
import com.luxshop.shop.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,String>{
    Optional<Customer> findByEmail(String email);

    // Email is not unique in the seed data, so take the first match for guest checkout.
    Optional<Customer> findFirstByEmail(String email);
}

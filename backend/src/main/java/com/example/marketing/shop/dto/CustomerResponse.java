package com.example.marketing.shop.dto;

import com.example.marketing.shop.domain.Customer;

/**
 * Public view of a customer. Deliberately omits the password so it is never
 * serialized to clients, unlike returning the Customer entity directly.
 */
public record CustomerResponse(
        String id,
        String email,
        String firstName,
        String lastName,
        String address,
        Integer postcode,
        String city,
        Integer phone
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getEmail(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getAddress(),
                customer.getPostcode(),
                customer.getCity(),
                customer.getPhone()
        );
    }
}

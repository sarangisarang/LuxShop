package com.luxshop.shop.domain;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Customer {
    @Id
    private String id;
    @Email(message = "Invalid email format")
    private String email;
    private String firstName;
    private String lastName;
    private String Address;
    private Integer Postcode;
    private String City;
    private Integer Phone;
}

package com.example.marketing.shop.domain;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Customer {
    @Id
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String Address;
    private Integer Postcode;
    private String City;
    private Integer Phone;
}

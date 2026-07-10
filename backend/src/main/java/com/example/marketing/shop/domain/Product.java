package com.example.marketing.shop.domain;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;


@Entity
@Getter
@Setter
public class Product {
    @Id
    private String id;
    private String productName;
    private String productDesc;
    private byte[] image1;
    private byte[] image2;
    private byte[] image3;
    private BigInteger Prece;
    private BigInteger Stock;
    @ManyToOne
    @JoinColumn(name="Category_id")
    private Category category;
}

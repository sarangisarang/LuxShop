package com.example.marketing.shop.domain;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class OrderDetails{
    @Id
    private String id;
    private Integer Qty;
    private BigDecimal Price;
    private BigDecimal Subtotal;

    @ManyToOne
    @JoinColumn(name="Order_id")
    private Orders orders;

    @ManyToOne
    @JoinColumn(name="Product_id")
    private Product product;
}

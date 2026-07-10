package com.example.marketing.shop.domain;
import com.example.marketing.shop.service.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Orders {
    @Id
    private String id;
    private Integer orderNo;
    private LocalDate orderDate;
    private Integer orderTotal;
    private LocalDate shippingDate;
    private String isDelivered;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne
    @JoinColumn(name="Customer_id")
    private Customer customer;
}

package com.luxshop.shop.domain;
import com.luxshop.shop.service.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
// Soft delete: cancelled/removed orders stay in the table but drop out of queries.
@SQLDelete(sql = "UPDATE orders SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Orders {
    @Id
    private String id;
    private Integer orderNo;
    private LocalDate orderDate;
    private BigDecimal orderTotal;
    private LocalDate shippingDate;
    private Boolean isDelivered;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne
    @JoinColumn(name="Customer_id")
    private Customer customer;

    @Column(name = "is_deleted", nullable = false)
    @ColumnDefault("false")
    @JsonIgnore
    private boolean deleted = false;
}

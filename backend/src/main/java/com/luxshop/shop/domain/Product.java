package com.luxshop.shop.domain;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;


@Entity
@Getter
@Setter
// Soft delete: repository.delete() flips the flag instead of removing the row,
// and every query is transparently filtered to hide soft-deleted rows.
@SQLDelete(sql = "UPDATE product SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Product {
    @Id
    private String id;
    @NotBlank(message = "{validation.product.name.required}")
    private String productName;
    private String productDesc;
    private byte[] image1;
    private byte[] image2;
    private byte[] image3;
    @NotNull(message = "{validation.product.price.required}")
    @PositiveOrZero(message = "{validation.product.price.positive}")
    private BigDecimal Price;
    @NotNull(message = "{validation.product.stock.required}")
    @PositiveOrZero(message = "{validation.product.stock.positive}")
    private Integer Stock;
    @ManyToOne
    @JoinColumn(name="Category_id")
    private Category category;

    @Column(name = "is_deleted", nullable = false)
    @ColumnDefault("false")
    @JsonIgnore
    private boolean deleted = false;
}

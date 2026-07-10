package com.luxshop.shop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * A product's name/description in one language. Portable across H2 and Postgres
 * (a plain relational table, unlike a Postgres-only JSONB column).
 */
@Entity
@Table(name = "product_translation",
        uniqueConstraints = @UniqueConstraint(name = "uk_product_translation_lang",
                columnNames = {"product_id", "language_code"}))
@Getter
@Setter
public class ProductTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @Column(name = "language_code", nullable = false, length = 8)
    private String languageCode;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;
}

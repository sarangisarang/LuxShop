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
 * A category's name/description in one language. Relational (H2 + Postgres
 * portable) rather than a Postgres-only JSONB column.
 */
@Entity
@Table(name = "category_translation",
        uniqueConstraints = @UniqueConstraint(name = "uk_category_translation_lang",
                columnNames = {"category_id", "language_code"}))
@Getter
@Setter
public class CategoryTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private Category category;

    @Column(name = "language_code", nullable = false, length = 8)
    private String languageCode;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;
}

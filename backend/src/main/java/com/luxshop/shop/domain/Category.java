package com.luxshop.shop.domain;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


@Entity
@Getter
@Setter
// Soft delete: hide removed categories from queries and keep the row for history.
@SQLDelete(sql = "UPDATE category SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Category {
    @Id
    private String id;
    @NotBlank(message = "{validation.category.name.required}")
    private String name;
    private String image;
    private String description;

    @Column(name = "is_deleted", nullable = false)
    @ColumnDefault("false")
    @JsonIgnore
    private boolean deleted = false;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CategoryTranslation> translations = new ArrayList<>();

    /** Name in the current request locale (Accept-Language), falling back to the base name. */
    public String getLocalizedName() {
        return localized(CategoryTranslation::getName, name);
    }

    /** Description in the current request locale, falling back to the base description. */
    public String getLocalizedDescription() {
        return localized(CategoryTranslation::getDescription, description);
    }

    private String localized(Function<CategoryTranslation, String> field, String fallback) {
        String lang = LocaleContextHolder.getLocale().getLanguage();
        return translations.stream()
                .filter(t -> lang.equals(t.getLanguageCode()))
                .findFirst()
                .map(field)
                .filter(v -> v != null && !v.isBlank())
                .orElse(fallback);
    }
}
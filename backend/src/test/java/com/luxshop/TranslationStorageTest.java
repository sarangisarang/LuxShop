package com.luxshop;

import com.luxshop.shop.domain.Category;
import com.luxshop.shop.domain.CategoryTranslation;
import com.luxshop.shop.domain.Product;
import com.luxshop.shop.domain.ProductTranslation;
import com.luxshop.shop.repository.CategoryRepository;
import com.luxshop.shop.repository.ProductRepository;
import com.luxshop.shop.repository.ProductTranslationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Storage-layer tests for i18n translation tables: a translation is persisted
 * via the parent (cascade) and getLocalized* returns the requested language,
 * falling back to the base fields when a translation is missing.
 */
@SpringBootTest
@Transactional
class TranslationStorageTest {

    private static final Locale KA = Locale.forLanguageTag("ka");

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductTranslationRepository productTranslationRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void product_returnsLocalizedNameAndFallsBack() {
        Product product = new Product();
        product.setId("t-product-1");
        product.setProductName("Laptop");
        product.setProductDesc("A portable computer");
        product.setPrice(BigDecimal.valueOf(1000));
        product.setStock(5);

        ProductTranslation ka = new ProductTranslation();
        ka.setLanguageCode("ka");
        ka.setName("ლეპტოპი");
        ka.setDescription("პორტატული კომპიუტერი");
        ka.setProduct(product);
        product.getTranslations().add(ka);

        productRepository.save(product); // cascades the translation

        assertTrue(productTranslationRepository.findByProductIdAndLanguageCode("t-product-1", "ka").isPresent());

        LocaleContextHolder.setLocale(KA);
        assertEquals("ლეპტოპი", product.getLocalizedName());
        assertEquals("პორტატული კომპიუტერი", product.getLocalizedDescription());

        LocaleContextHolder.setLocale(Locale.ENGLISH);
        assertEquals("Laptop", product.getLocalizedName()); // no en translation -> base
        assertEquals("A portable computer", product.getLocalizedDescription());
    }

    @Test
    void category_returnsLocalizedNameAndFallsBack() {
        Category category = new Category();
        category.setId("t-category-1");
        category.setName("Books");
        category.setDescription("Printed books");

        CategoryTranslation ka = new CategoryTranslation();
        ka.setLanguageCode("ka");
        ka.setName("წიგნები");
        ka.setCategory(category);
        category.getTranslations().add(ka);

        categoryRepository.save(category);

        LocaleContextHolder.setLocale(KA);
        assertEquals("წიგნები", category.getLocalizedName());
        // No ka description -> falls back to the base description.
        assertEquals("Printed books", category.getLocalizedDescription());

        LocaleContextHolder.setLocale(Locale.ENGLISH);
        assertEquals("Books", category.getLocalizedName());
    }
}

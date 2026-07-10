package com.luxshop.shop.repository;
import com.luxshop.shop.domain.Category;
import com.luxshop.shop.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Product,String>{
     List <Product> findAllByCategoryName(String categoryName);

     Optional<List<Product>> findAllByCategory(Category category); // testing here

     // Free-text storefront search over the base (English) name and description.
     // Product names are English brand names across all locales, so name matches
     // work regardless of the shopper's chosen language.
     Page<Product> findByProductNameContainingIgnoreCaseOrProductDescContainingIgnoreCase(
             String name, String description, Pageable pageable);
}

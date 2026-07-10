package com.example.marketing.shop.repository;
import com.example.marketing.shop.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoryRepository extends JpaRepository<Category,String> {
    Category findByName(String categoryName);
}

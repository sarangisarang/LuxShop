package com.example.marketing.shop.domain;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
public class Category {
    @Id
    private String id;
    private String name;
    private String image;
    private String description;
}
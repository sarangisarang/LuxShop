package com.example.marketing.shop.domain;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;


@Entity
@Getter
@Setter
// Soft delete: hide removed categories from queries and keep the row for history.
@SQLDelete(sql = "UPDATE category SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Category {
    @Id
    private String id;
    @NotBlank(message = "Category name is required")
    private String name;
    private String image;
    private String description;

    @Column(name = "is_deleted", nullable = false)
    @ColumnDefault("false")
    @JsonIgnore
    private boolean deleted = false;
}
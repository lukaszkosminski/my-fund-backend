package com.myfund.models;

import com.myfund.services.encryption.StringEncryptor;
import jakarta.persistence.*;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Table(name = "category")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Convert(converter = StringEncryptor.class)
    private String name;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SubCategory> subCategories;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public static Category create(Category category, User user) {
        Category newCategory = Category.builder()
                .name(category.getName())
                .user(user)
                .build();

        if (category.getSubCategories() != null) {
            newCategory.setSubCategories(category.getSubCategories().stream()
                    .map(subCategory -> {
                        SubCategory newSubCategory = new SubCategory();
                        newSubCategory.setName(subCategory.getName());
                        newSubCategory.setCategory(newCategory);
                        return newSubCategory;
                    })
                    .collect(Collectors.toList()));
        } else {
            newCategory.setSubCategories(new ArrayList<>());
        }

        return newCategory;
    }


    public static Category update(Category category, Category newCategory) {
        List<SubCategory> existingSubCategories = category.getSubCategories();
        newCategory.getSubCategories().forEach(subCategory -> {
            Optional<SubCategory> existingSubCategory = existingSubCategories.stream()
                    .filter(sc -> sc.getName().equals(subCategory.getName()))
                    .findFirst();
            if (!existingSubCategory.isPresent()) {
                subCategory.setCategory(category);
                existingSubCategories.add(subCategory);
            }
        });
        return Category.builder()
                .id(category.getId())
                .name(newCategory.getName())
                .subCategories(existingSubCategories)
                .user(category.getUser())
                .build();
    }

}

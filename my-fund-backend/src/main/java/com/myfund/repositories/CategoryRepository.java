package com.myfund.repository;

import com.myfund.model.Category;
import com.myfund.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);

    Optional<Category> findByNameAndUser(String categoryName, User user);


    Optional<Category> findByIdAndUser(Long categoryId, User user);

    List<Category> findAllCategoriesByUser(User user);
}

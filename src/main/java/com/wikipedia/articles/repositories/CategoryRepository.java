package com.wikipedia.articles.repositories;

import com.wikipedia.articles.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByTitleIgnoreCase(String title);
    Optional<Category> findByTitle(String title);
    List<Category> findByIsProtectedFalse();
}
package com.wikipedia.articles.services;

import com.wikipedia.articles.models.Category;
import com.wikipedia.articles.repositories.CategoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service responsible for category-related business logic.
 * Handles:
 * - Retrieving categories
 * - Creating new categories
 * - Updating existing categories
 * - Deleting categories
 * Applies validation and business rules before persistence operations.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Constructor-based dependency injection.
     *
     * @param categoryRepository repository used for category persistence operations
     */
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Retrieves all categories from the database.
     *
     * @return list of all categories
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Retrieves categories that are editable (not protected).
     * Protected categories are excluded from the result.
     *
     * @return list of editable categories
     */
    public List<Category> getAllEditableCategories() {
        return categoryRepository.findByIsProtectedFalse();
    }
}

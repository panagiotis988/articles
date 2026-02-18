package com.wikipedia.articles.controllers;

import com.wikipedia.articles.models.Category;
import com.wikipedia.articles.services.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller responsible for handling category-related operations.
 * Provides endpoints for:
 * - Retrieving all categories
 * - Retrieving public/editable categories
 * - Creating new categories
 * - Updating existing categories
 * - Deleting categories
 * Applies validation and business rules for category management.
 */
@RestController
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Constructor-based dependency injection for CategoryService.
     *
     * @param categoryService service layer handling category business logic
     */
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Retrieves all available categories.
     *
     * @return list of all categories in the system
     */
    @GetMapping("/api/categories")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    /**
     * Retrieves only editable/public categories.
     * This endpoint can be used when users should only see
     * categories that are allowed for selection or modification.
     *
     * @return list of editable/public categories
     */
    @GetMapping("/api/categories/public")
    public List<Category> getAllEditableCategories() {
        return categoryService.getAllEditableCategories();
    }
}

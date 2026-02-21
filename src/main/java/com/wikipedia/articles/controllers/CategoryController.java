package com.wikipedia.articles.controllers;

import com.wikipedia.articles.dto.CategoryCreateRequest;
import com.wikipedia.articles.models.Category;
import com.wikipedia.articles.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.wikipedia.articles.dto.CategoryUpdateRequest;

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

    // Nick

    @PutMapping("/api/categories/{id}")
    public Category updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        return categoryService.updateCategory(id, request.getTitle());
    }

    /**
     * Creates a new category if it does not already exist.
     * Accepts a {@link CategoryCreateRequest} containing the category name.
     * If a category with the same name already exists, it will return the existing one.
     *
     * @param request the category creation request containing the name
     * @return the newly created or existing {@link Category} object
     */
    @PostMapping("/api/categories")
    public Category createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        return categoryService.createCategory(request.getName());
    }
    // End Nick
    /**
     * Deletes a category by ID and reassigns its articles to "Uncategorized".
     *
     * @param categoryId the category ID to delete
     * @return HTTP 204 No Content when deletion succeeds
     */
    @DeleteMapping("/api/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}

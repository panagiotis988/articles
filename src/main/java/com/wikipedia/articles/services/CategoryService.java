package com.wikipedia.articles.services;

import com.wikipedia.articles.models.Article;
import com.wikipedia.articles.models.Category;
import com.wikipedia.articles.repositories.ArticleRepository;
import com.wikipedia.articles.repositories.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

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
    private final ArticleRepository articleRepository;

    /**
     * Constructor-based dependency injection.
     *
     * @param categoryRepository repository used for category persistence operations
     */
    public CategoryService(CategoryRepository categoryRepository, ArticleRepository articleRepository) {
        this.categoryRepository = categoryRepository;
        this.articleRepository = articleRepository;
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

    /**
     * Creates a new category with the given title.
     * - Validates that the title is not empty.
     * - Checks if a category with the same title (case-insensitive) already exists.
     *   If it does, throws a 400 BAD_REQUEST with message "Category already exists".
     * - If the title is valid and unique, creates and saves a new Category.
     *
     * @param title the name of the category to create
     * @return the newly created {@link Category} object
     * @throws ResponseStatusException if the title is empty or already exists
     */
    public Category createCategory(String title) {
        if (!StringUtils.hasText(title)) {
            throw new ResponseStatusException(BAD_REQUEST, "Category name is required");
        }

        String normalizedTitle = title.trim();

        boolean exists = categoryRepository.existsByTitleIgnoreCase(normalizedTitle);
        if (exists) {
            throw new ResponseStatusException(BAD_REQUEST, "Category already exists");
        }

        Category category = new Category();
        category.setTitle(normalizedTitle);
        return categoryRepository.save(category);
    }

    /**
     * Deletes a category and reassigns its articles to the "Uncategorized" category.
     * Steps:
     * - Find the "Uncategorized" category
     * - Find the category to delete
     * - Move all articles from the category to delete into "Uncategorized"
     * - Delete the category
     *
     * @param categoryId category ID to delete
     */
    public void deleteCategory(Long categoryId) {
        Category uncategorized = categoryRepository.findByTitle("Uncategorized")
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Uncategorized category not found"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));

        if (category.isProtected()) {
            throw new ResponseStatusException(BAD_REQUEST, "Protected categories cannot be deleted");
        }

        List<Article> articles = articleRepository.findByCategoryId(categoryId);
        if (!articles.isEmpty()) {
            for (Article article : articles) {
                article.setCategory(uncategorized);
            }
            articleRepository.saveAll(articles);
        }

        categoryRepository.delete(category);
    }
}

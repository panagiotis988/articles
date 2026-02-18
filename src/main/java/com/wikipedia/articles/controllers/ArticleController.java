package com.wikipedia.articles.controllers;

import com.wikipedia.articles.dto.ArticleCreateRequest;
import com.wikipedia.articles.dto.ArticleUpdateRequest;
import com.wikipedia.articles.models.Article;
import com.wikipedia.articles.services.ArticleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller responsible for handling article-related operations.
 * Provides endpoints for:
 * - Searching Wikipedia articles
 * - Managing user's saved articles
 * - Creating, updating, and deleting articles
 * Base path: /api
 */
@RestController
@RequestMapping("/api")
public class ArticleController {

    private final ArticleService articleService;

    /**
     * Constructor-based dependency injection for ArticleService.
     *
     * @param articleService service layer handling article business logic
     */
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * Searches Wikipedia articles using a search query.
     *
     * @param search the search term entered by the user
     * @param page   the page number for pagination (default = 1)
     * @param size   the number of results per page (default = 10)
     * @return a map containing Wikipedia search results and metadata
     */
    @GetMapping("/search")
    public Map<String, Object> searchArticles(
            @RequestParam String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return articleService.searchWikipedia(search, page, size);
    }

    /**
     * Retrieves the user's saved articles, optionally filtered.
     * Optional filters:
     * - search: filter articles by title/content
     * - categoryId: filter articles by category
     *
     * @param search      optional search text filter
     * @param category optional category filter
     * @return a map of articles grouped by some key (e.g., category or type)
     */
    @GetMapping("/articles/my-articles")
    public Map<String, List<Article>> myArticles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long category
    ) {

        return articleService.myArticles(search, category);
    }

    /**
     * Creates a new article entry for the user.
     *
     * @param request DTO containing article creation data
     * @return the created Article entity
     */
    @PostMapping("/articles")
    public Article createArticle(@Valid @RequestBody ArticleCreateRequest request) {
        return articleService.createArticle(
                request.getCategoryId(),
                request.getGrade(),
                request.getPageId(),
                request.getSnippet(),
                request.getTitle(),
                request.getComment()
        );
    }

    /**
     * Updates an existing article.
     *
     * @param id      the ID of the article to update
     * @param request DTO containing updated article fields
     * @return the updated Article entity
     */
    @PatchMapping("/articles/my-articles/{id}")
    public Article updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleUpdateRequest request
    ) {
        return articleService.updateArticle(
                id,
                request.getGrade(),
                request.getComment(),
                request.getCategoryId()
        );
    }

    /**
     * Deletes an article by its ID.
     *
     * @param id the ID of the article to delete
     * @return HTTP 204 No Content if deletion is successful
     */
    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }
}

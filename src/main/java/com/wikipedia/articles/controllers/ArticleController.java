package com.wikipedia.articles.controllers;

import com.wikipedia.articles.dto.ArticleCreateRequest;
import com.wikipedia.articles.models.Article;
import com.wikipedia.articles.services.ArticleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/search")
    public Map<String, Object> searchArticles(
            @RequestParam String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return articleService.searchWikipedia(search, page, size);
    }

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
}

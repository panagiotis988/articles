package com.wikipedia.articles.controllers;

import com.wikipedia.articles.models.Category;
import com.wikipedia.articles.services.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/api/categories")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/api/categories/public")
    public List<Category> getAllEditableCategories() {
        return categoryService.getAllEditableCategories();
    }
}
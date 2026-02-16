package com.wikipedia.articles.services;

import com.wikipedia.articles.models.Category;
import com.wikipedia.articles.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getAllEditableCategories() {
        return categoryRepository.findByIsProtectedFalse();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElse(null);
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }
}

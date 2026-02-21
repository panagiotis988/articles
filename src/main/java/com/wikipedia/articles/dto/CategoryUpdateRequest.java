package com.wikipedia.articles.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryUpdateRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 40, message = "Category name must be at most 40 characters")
    private String title;

    public CategoryUpdateRequest() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
package com.wikipedia.articles.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public class CategoryCreateRequest {

    @NotBlank(message = "Category name is required")
    @JsonAlias({"title"})
    private String name;

    public CategoryCreateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
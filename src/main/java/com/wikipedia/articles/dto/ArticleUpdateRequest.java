package com.wikipedia.articles.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ArticleUpdateRequest {

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Grade is required")
    @Min(value = 1, message = "Grade must be at least 1")
    @Max(value = 5, message = "Grade must be at most 5")
    private Integer grade;

    private String comment;

    public ArticleUpdateRequest() {
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

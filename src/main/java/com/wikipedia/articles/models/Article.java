package com.wikipedia.articles.models;

import jakarta.persistence.*;

@Entity
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer pageId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String comments;

    private Integer grade;

    public Article() {}

    public Article(Integer pageId, Category category, String comments, Integer grade) {
        this.pageId = pageId;
        this.category = category;
        this.comments = comments;
        this.grade = grade;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) {
        if (grade != null && (grade < 1 || grade > 5)) {
            throw new IllegalArgumentException("Grade must be between 1 and 5");
        }
        this.grade = grade;
    }
}

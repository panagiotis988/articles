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
    private String title;
    @Column(length = 20000)
    private String snippet;

    private Integer grade;

    public Article() {}

    public Article(Integer pageId, Category category, String comments, Integer grade, String title, String snippet) {
        this.pageId = pageId;
        this.category = category;
        this.comments = comments;
        this.grade = grade;
        this.title = title;
        this.snippet = snippet;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getPageId() { return pageId; }
    public void setPageId(Integer pageId) { this.pageId = pageId; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }


    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }

    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) {
        if (grade != null && (grade < 1 || grade > 5)) {
            throw new IllegalArgumentException("Grade must be between 1 and 5");
        }
        this.grade = grade;
    }
}

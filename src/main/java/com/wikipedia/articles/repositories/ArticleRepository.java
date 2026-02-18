package com.wikipedia.articles.repositories;

import com.wikipedia.articles.models.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findByPageId(Integer pageId);
    List<Article> id(Long id);
    List<Article> findByPageIdIn(List<Integer> pageId);
    List<Article> findByTitleContainingIgnoreCaseOrSnippetContainingIgnoreCase(String tittle, String snippet);
    List<Article> findByCategoryId(Long categoryId);
    List<Article> findByCategoryIdAndTitleContainingIgnoreCaseOrCategoryIdAndSnippetContainingIgnoreCase(Long categoryIdTittle,String tittle,Long categoryIdSnippet, String snippet);
}
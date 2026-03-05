package com.wikipedia.articles.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wikipedia.articles.models.Category;
import com.wikipedia.articles.repositories.ArticleRepository;
import com.wikipedia.articles.repositories.CategoryRepository;
import com.wikipedia.articles.repositories.StatisticRepository;
import org.jsoup.Jsoup;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.wikipedia.articles.models.Article;
import com.wikipedia.articles.models.Statistic;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for article-related business logic.
 * Handles:
 * - Searching Wikipedia
 * - Managing saved articles
 * - Adding article details
 * - Tracking search statistics
 * - Creating, updating, and deleting articles
 */
@Service
public class ArticleService {

    private static final String WIKI_API = "https://el.wikipedia.org/w/api.php";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ArticleRepository articleRepository;
    private final StatisticRepository statisticRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Constructor-based dependency injection.
     */
    public ArticleService(ArticleRepository articleRepository, StatisticRepository statisticRepository, CategoryRepository categoryRepository) {
        this.articleRepository = articleRepository;
        this.statisticRepository = statisticRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Searches Wikipedia using the API.
     * Adds local article details and tracks search statistics.
     *
     * @param search the search query
     * @param page   page number
     * @param size   number of results per page
     * @return map with paginated search results
     */
    public Map<String, Object> searchWikipedia(String search, int page, int size) {

        try {
            int offset = (page - 1) * size;
            String url = buildWikiUrl(search, size, offset);
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "MyWikipediaApp/1.0");

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class
            );

            Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), new TypeReference<>() {
            });
            Map<String, Object> queryMap = asMap(responseMap.get("query"));
            List<Map<String, Object>> results = asListOfMap(queryMap.get("search"));

            results.forEach(this::cleanSnippet);

            int totalHits = Optional.ofNullable(asMap(queryMap.get("searchinfo")).get("totalhits"))
                    .map(v -> (Integer) v)
                    .orElse(0);

            searchCounter(search);
            addArticleDetails(results);

            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("page", page);
            finalResponse.put("size", size);
            finalResponse.put("totalHits", totalHits);
            finalResponse.put("totalPages", (int) Math.ceil((double) totalHits / size));
            finalResponse.put("results", results);

            addArticleDetails(results);

            return finalResponse;

        } catch (Exception e) {
            return Map.of(
                    "error", "Failed to fetch data from Wikipedia",
                    "message", e.getMessage()
            );
        }
    }


    /**
     * Builds a safe Wikipedia API URL with sanitized search string.
     */
    private String buildWikiUrl(String search, int size, int offset) {
        String safeSearch = search.trim().replace(" ", "+");
        safeSearch = safeSearch.replace("#", "")
                .replace("&", "")
                .replace("?", "")
                .replace("%", "")
                .replace("<", "")
                .replace(">", "")
                .replace("\"", "")
                .replace("'", "");

        return String.format(
                "%s?action=query&list=search&srsearch=%s&srlimit=%d&sroffset=%d&srsort=relevance&format=json&formatversion=2&utf8=1",
                WIKI_API, safeSearch, size, offset);
    }


    /**
     * Cleans the "snippet" field of a Wikipedia search result.
     * Wikipedia returns snippets containing HTML tags (e.g., <span> highlights).
     * This method parses the snippet using Jsoup and extracts plain text,
     * replacing the original HTML snippet with a clean text version.
     *
     * @param article a map representing a single Wikipedia search result
     */
    private void cleanSnippet(Map<String, Object> article) {
        if (article.get("snippet") != null) {
            String snippet = Jsoup.parse((String) article.get("snippet")).text();
            article.put("snippet", snippet);

        }
    }

    /**
     * Adds local article details (grade, comment, category) to Wikipedia search results.
     */
    private void addArticleDetails(List<Map<String, Object>> results) {
        List<Article> savedArticles = savedPageIds(results);

        Map<Integer, Article> savedByPageId = new HashMap<>();
        for (Article a : savedArticles) {
            if (a.getPageId() != null) {
                savedByPageId.put(a.getPageId(), a);

            }
        }

        for (Map<String, Object> r : results) {
            Object pageIdObj = r.get("pageid");

            if (!(pageIdObj instanceof Number)) continue;

            int pageId = ((Number) pageIdObj).intValue();

            Article saved = savedByPageId.get(pageId);

            if (saved != null) {
                r.put("comment", saved.getComments());
                r.put("grade", saved.getGrade());
                r.put("category", saved.getCategory().getTitle());
            }
        }
    }

    /**
     * Converts an object to a Map<String, Object> using ObjectMapper.
     */
    private Map<String, Object> asMap(Object obj) {
        return objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * Converts an object to a List<Map<String, Object>> using ObjectMapper.
     */
    private List<Map<String, Object>> asListOfMap(Object obj) {
        return objectMapper.convertValue(obj, new TypeReference<List<Map<String, Object>>>() {
        });
    }

    /**
     * Retrieves saved articles matching Wikipedia page IDs.
     */
    private List<Article> savedPageIds(List<Map<String, Object>> results) {
        List<Integer> wikiPageIds = new ArrayList<>();

        for (Map<String, Object> article : results) {
            Object wikiPageIdsObj = article.get("pageid");
            int wikiPageId = ((Number) wikiPageIdsObj).intValue();
            wikiPageIds.add(wikiPageId);
        }

        return articleRepository.findByPageIdIn(wikiPageIds);
    }

    /**
     * Increments search counter for a given word.
     * Creates a new Statistic record if the word does not exist.
     */
    private void searchCounter(String search) {
        Statistic stat;
        String word = search.trim().toLowerCase();
        Optional<Statistic> findWord = statisticRepository.findByWord(word);

        if (findWord.isPresent()) {
            stat = findWord.get();
            Integer current = stat.getCounter();
            stat.setCounter((current == null ? 0 : current) + 1);
        } else {
            stat = new Statistic(word, 1);
        }
        statisticRepository.save(stat);
    }

    /**
     * Creates a new article.
     * Validates uniqueness, category existence, and grade value.
     */
    public Article createArticle(
            Long categoryId,
            Integer grade,
            Integer pageId,
            String snippet,
            String title,
            String comment) {
        if (articleRepository.findByPageId(pageId).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "Article already exists");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));

        Article article = new Article();
        article.setPageId(pageId);
        article.setCategory(category);
        article.setComments(comment);
        article.setTitle(title);
        article.setSnippet(snippet);

        try {
            article.setGrade(grade);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(BAD_REQUEST, ex.getMessage());
        }

        return articleRepository.save(article);
    }

    /**
     * Retrieves articles for a user filtered by search text and category.
     */
    public Map<String, List<Article>> myArticles(String search, Long categoryId) {
        boolean hasSearch =
                search != null && !search.trim().isEmpty();
        boolean hasCategoryId =
                categoryId != null;
        List<Article> articles;

        if ((hasSearch && hasCategoryId)) {
            articles = articleRepository.findByCategoryIdAndTitleContainingIgnoreCaseOrCategoryIdAndSnippetContainingIgnoreCase(categoryId, search, categoryId, search);
        } else if (!hasSearch && hasCategoryId) {
            articles = articleRepository.findByCategoryId(categoryId);
        } else if (hasSearch && !hasCategoryId) {
            articles = articleRepository.findByTitleContainingIgnoreCaseOrSnippetContainingIgnoreCase(search, search);
        } else {
            articles = articleRepository.findAll();
        }
        Map<String, List<Article>> groupedArticles =
                articles.stream().collect(Collectors.groupingBy(a -> a.getCategory().getTitle()));

        return groupedArticles;
    }



    /**
     * Updates an existing article.
     * Can update grade, comment, and optionally category.
     */
    public Article updateArticle(Long id, Integer grade, String comment, Long categoryId) {

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        article.setGrade(grade);
        article.setComments(comment);

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            article.setCategory(category);
        }

        return articleRepository.save(article);
    }


    /**
     * Deletes an article by ID.
     * Throws NOT_FOUND if article does not exist.
     */
    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Article not found"));
        articleRepository.delete(article);
    }
}

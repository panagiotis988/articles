package com.wikipedia.articles.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ArticleService {

    private static final String WIKI_API = "https://el.wikipedia.org/w/api.php";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

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

            //TODO remove and replace with real data once database data implementation works
            addTestFields(results);

            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("page", page);
            finalResponse.put("size", size);
            finalResponse.put("totalHits", totalHits);
            finalResponse.put("totalPages", (int) Math.ceil((double) totalHits / size));
            finalResponse.put("results", results);

            return finalResponse;

        } catch (Exception e) {
            return Map.of(
                    "error", "Failed to fetch data from Wikipedia",
                    "message", e.getMessage()
            );
        }
    }

    private String buildWikiUrl(String search, int size, int offset) {
        return String.format("%s?action=query&list=search&srsearch=%s&srlimit=%d&sroffset=%d&srsort=last_edit_desc&format=json",
                WIKI_API, URLEncoder.encode(search, StandardCharsets.UTF_8), size, offset);
    }

    private void cleanSnippet(Map<String, Object> article) {
        if (article.get("snippet") != null) {
            String snippet = Jsoup.parse((String) article.get("snippet")).text();
            article.put("snippet", snippet);
        }
    }

    private void addTestFields(List<Map<String, Object>> results) {
        if (!results.isEmpty()) {
            Map<String, Object> first = results.get(0);
            first.put("comment", "just a test comment");
            first.put("grade", 5);
            first.put("category", "test");
        }
    }

    private Map<String, Object> asMap(Object obj) {
        return objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {
        });
    }

    private List<Map<String, Object>> asListOfMap(Object obj) {
        return objectMapper.convertValue(obj, new TypeReference<List<Map<String, Object>>>() {
        });
    }
}

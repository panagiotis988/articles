package com.wikipedia.articles.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArticleController {

    @GetMapping("/hello")
    public String hello() {

        return "Hello World";
    }
}

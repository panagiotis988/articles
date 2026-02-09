package com.wikipedia.articles.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Statistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String word;

    private Integer counter;

    public Statistic() {}

    public Statistic(String word, Integer counter) {
        this.word = word;
        this.counter = counter;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public Integer getCounter() { return counter; }
    public void setCounter(Integer counter) { this.counter = counter; }
}

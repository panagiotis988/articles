package com.wikipedia.articles.seeders;

import com.wikipedia.articles.models.Category;
import com.wikipedia.articles.repositories.CategoryRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategorySeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String @NonNull ... args) throws Exception {
        List<String> categories = List.of(
                "Science",
                "Technology",
                "History",
                "Arts",
                "Sports",
                "Countries"
        );

        for (String title : categories) {
            categoryRepository.findByTitle(title)
                    .orElseGet(() -> categoryRepository.save(new Category(title)));
        }

        System.out.println("✅ Categories seeded successfully!");
    }
}

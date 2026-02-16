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
                "Uncategorized",
                "Technology",
                "History",
                "Arts",
                "Sports",
                "Countries",
                "Science"
        );

        for (String title : categories) {
            categoryRepository.findByTitle(title)
                    .orElseGet(() -> {
                        boolean isProtected = "Uncategorized".equalsIgnoreCase(title);

                        Category category = new Category(title, isProtected);
                        return categoryRepository.save(category);
                    });
        }

        System.out.println("✅ Categories seeded successfully!");
    }
}

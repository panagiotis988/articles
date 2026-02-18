package com.wikipedia.articles.seeders;

import com.wikipedia.articles.models.Category;
import com.wikipedia.articles.repositories.CategoryRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Seeder component responsible for initializing default categories in the database.
 * This runs automatically at application startup and ensures that a predefined set of
 * categories exist. The "Uncategorized" category is marked as protected.
 */
@Component
public class CategorySeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    /**
     * Constructor-based dependency injection.
     *
     * @param categoryRepository repository for managing category persistence
     */
    public CategorySeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Runs the seeding process at application startup.
     * Logic:
     * - Predefined categories: "Uncategorized", "Technology", "History", "Arts", "Sports", "Countries", "Science"
     * - Checks if each category already exists by title
     * - If not present, creates and saves the category
     * - Marks "Uncategorized" category as protected
     *
     * @param args command-line arguments passed to the application
     * @throws Exception if any error occurs during seeding
     */
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

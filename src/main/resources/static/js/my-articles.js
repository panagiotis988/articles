document.addEventListener("DOMContentLoaded", function () {

    const searchBtn = document.getElementById("searchBtn");
    const openFiltersBtn = document.getElementById("openFiltersBtn");
    const closeFiltersBtn = document.getElementById("closeFiltersBtn");
    const applyFiltersBtn = document.getElementById("applyFiltersBtn");
    const filtersModal = document.getElementById("filtersModal");
    const categoryDropdown = document.getElementById("categoryDropdown");
    const searchInput = document.getElementById("searchInput");
    const container = document.getElementById("categories-container");

    const editModal = document.getElementById("editModal");
    const editComment = document.getElementById("editComment");
    const editCategory = document.getElementById("editCategory");
    const editGrade = document.getElementById("editGrade");
    const cancelEdit = document.getElementById("cancelEdit");
    const saveEdit = document.getElementById("saveEdit");

    const deleteModal = document.getElementById("deleteModal");
    const cancelDelete = document.getElementById("cancelDelete");
    const confirmDelete = document.getElementById("confirmDelete");

    let articleToDelete = null;

    let currentEditingArticle = null;

    function loadCategories() {
        axios.get("/api/categories")
            .then(response => {
                const categories = response.data;
                categoryDropdown.innerHTML = '<option value="">All Categories</option>';
                editCategory.innerHTML = '';
                categories.forEach(cat => {
                    const option1 = document.createElement("option");
                    option1.value = cat.id;
                    option1.textContent = cat.title;
                    categoryDropdown.appendChild(option1);

                    const option2 = document.createElement("option");
                    option2.value = cat.id;
                    option2.textContent = cat.title;
                    editCategory.appendChild(option2);
                });
            })
            .catch(() => {
                categoryDropdown.innerHTML = '<option value="">Failed to load</option>';
                editCategory.innerHTML = '<option value="">Failed to load</option>';
            });
    }

    function createGradeStars(grade) {
        const container = document.createElement("span");
        container.classList.add("grade-stars");
        for (let i = 1; i <= 5; i++) {
            const star = document.createElement("span");
            star.textContent = "★";
            if (i <= grade) star.classList.add("filled");
            container.appendChild(star);
        }
        return container;
    }

    function getExpandedCategories() {
        return Array.from(document.querySelectorAll(".category-header.expanded"))
            .map(header => header.textContent.split(" (")[0]);
    }


    function loadArticles() {

        const expandedCategories = getExpandedCategories();

        const selectedCategoryId = categoryDropdown.value;
        const searchText = searchInput.value.trim();

        const params = {};
        if (selectedCategoryId) params.category = selectedCategoryId;
        if (searchText) params.search = searchText;

        axios.get("/api/articles/my-articles", {params})
            .then(response => {

                const categories = response.data;
                container.innerHTML = "";

                for (const [categoryName, articles] of Object.entries(categories)) {

                    if (!articles || articles.length === 0) continue;

                    const categoryDiv = document.createElement("div");
                    categoryDiv.classList.add("category");

                    const headerDiv = document.createElement("div");
                    headerDiv.classList.add("category-header");
                    headerDiv.textContent =
                        `${categoryName} (${articles.length} ${articles.length === 1 ? "article" : "articles"})`;

                    const articlesContainer = document.createElement("div");
                    articlesContainer.classList.add("articles-container");

                    if (expandedCategories.includes(categoryName)) {
                        articlesContainer.style.display = "block";
                        headerDiv.classList.add("expanded");
                    } else {
                        articlesContainer.style.display = "none";
                    }

                    categoryDiv.appendChild(headerDiv);
                    categoryDiv.appendChild(articlesContainer);

                    articles.forEach(article => {

                        const articleDiv = document.createElement("div");
                        articleDiv.classList.add("category-row");

                        const top = document.createElement("div");
                        top.classList.add("article-top");

                        const title = document.createElement("div");
                        title.classList.add("article-title");

                        const titleLink = document.createElement("a");
                        titleLink.href = `https://el.wikipedia.org/?curid=${article.pageId}`;
                        titleLink.target = "_blank";
                        titleLink.innerHTML = `🔗 ${article.title}`;
                        title.appendChild(titleLink);

                        const buttons = document.createElement("div");
                        buttons.classList.add("article-buttons");

                        const editBtn = document.createElement("button");
                        editBtn.classList.add("edit");
                        editBtn.textContent = "Edit";
                        editBtn.addEventListener("click", () => openEditModal(article));

                        const deleteBtn = document.createElement("button");
                        deleteBtn.classList.add("delete");
                        deleteBtn.textContent = "Delete";
                        deleteBtn.addEventListener("click", () => deleteArticle(article.id));

                        buttons.appendChild(editBtn);
                        buttons.appendChild(deleteBtn);

                        top.appendChild(title);
                        top.appendChild(buttons);
                        articleDiv.appendChild(top);

                        const snippet = document.createElement("div");
                        snippet.classList.add("article-snippet");
                        snippet.textContent = article.snippet;
                        articleDiv.appendChild(snippet);

                        const extra = document.createElement("div");
                        extra.classList.add("article-extra");

                        const commentsP = document.createElement("p");
                        commentsP.innerHTML =
                            `<strong>Comments:</strong> <span class="article-comments">${article.comments}</span>`;

                        const gradeP = document.createElement("p");
                        gradeP.innerHTML = `<strong>Grade:</strong> `;
                        gradeP.appendChild(createGradeStars(article.grade));

                        const categoryP = document.createElement("p");
                        categoryP.innerHTML =
                            `<strong>Category:</strong> ${article.category.title}`;

                        extra.appendChild(commentsP);
                        extra.appendChild(gradeP);
                        extra.appendChild(categoryP);

                        articleDiv.appendChild(extra);
                        articlesContainer.appendChild(articleDiv);
                    });

                    headerDiv.addEventListener("click", () => {
                        const isVisible = articlesContainer.style.display === "block";
                        articlesContainer.style.display = isVisible ? "none" : "block";
                        headerDiv.classList.toggle("expanded", !isVisible);
                    });

                    container.appendChild(categoryDiv);
                }

            })
            .catch(error =>
                console.error("Error loading articles:", error)
            );
    }

    function openEditModal(article) {
        currentEditingArticle = article;

        editComment.value = article.comments;
        editCategory.value = article.category.id;

        editGrade.innerHTML = "";
        for (let i = 1; i <= 5; i++) {
            const star = document.createElement("span");
            star.textContent = "★";
            if (i <= article.grade) star.classList.add("filled");
            star.addEventListener("click", () => {
                article.grade = i;
                Array.from(editGrade.children).forEach((s, idx) => {
                    s.classList.toggle("filled", idx < article.grade);
                });
            });
            editGrade.appendChild(star);
        }

        editModal.classList.remove("hidden");
    }

    cancelEdit.addEventListener("click", () => {
        editModal.classList.add("hidden");
        currentEditingArticle = null;
    });

    saveEdit.addEventListener("click", () => {
        if (!currentEditingArticle) return;

        const articleId = currentEditingArticle.id;

        const updatePayload = {
            categoryId: parseInt(editCategory.value),
            grade: currentEditingArticle.grade,
            comment: editComment.value
        };

        axios.patch(`/api/articles/my-articles/${articleId}`, updatePayload)
            .then(response => {
                currentEditingArticle = response.data;
                editModal.classList.add("hidden");
                loadArticles();
            })
            .catch(error => {
                console.error("Failed to update article:", error);
                alert("Failed to save changes. Please try again.");
            });
    });

    function deleteArticle(articleId) {
        articleToDelete = articleId;
        deleteModal.classList.remove("hidden");
    }

    cancelDelete.addEventListener("click", () => {
        deleteModal.classList.add("hidden");
        articleToDelete = null;
    });

    confirmDelete.addEventListener("click", () => {
        if (!articleToDelete) return;

        axios.delete(`/api/articles/${articleToDelete}`)
            .then(() => {
                deleteModal.classList.add("hidden");
                articleToDelete = null;
                loadArticles();
            })
            .catch(error => {
                console.error("Failed to delete article:", error);
                alert("Failed to delete article. Please try again.");
            });
    });

    deleteModal.addEventListener("click", e => {
        if (e.target === deleteModal) {
            deleteModal.classList.add("hidden");
            articleToDelete = null;
        }
    });

    searchBtn.addEventListener("click", () => {

        categoryDropdown.value = "";
        searchInput.value = "";

        loadArticles();
    });

    applyFiltersBtn.addEventListener("click", () => {
        loadArticles();
        filtersModal.classList.add("hidden");
    });
    openFiltersBtn.addEventListener("click", () => filtersModal.classList.remove("hidden"));
    closeFiltersBtn.addEventListener("click", () => filtersModal.classList.add("hidden"));
    filtersModal.addEventListener("click", e => {
        if (e.target === filtersModal) filtersModal.classList.add("hidden");
    });

    const clearFiltersBtn = document.getElementById("clearFiltersBtn");
    clearFiltersBtn.addEventListener("click", () => {
        categoryDropdown.value = "";
        searchInput.value = "";
    });

    loadCategories();
});
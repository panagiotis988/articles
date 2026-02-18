document.addEventListener("DOMContentLoaded", () => {
    const container = document.getElementById("categories-container");

    axios.get("/api/statistics")
        .then(response => {
            const data = response.data;
            const categories = data.categories;
            const statistics = data.statistics;

            if (statistics.length === 0) {
                container.innerHTML = '<p style="text-align:center; font-style:italic;">No statistics were found.</p>';
                return;
            }

            for (const [categoryName, articles] of Object.entries(categories)) {
                const categoryDiv = document.createElement("div");
                categoryDiv.className = "category";

                const headerDiv = document.createElement("div");
                headerDiv.className = "category-header";
                const articleCount = articles.length;
                headerDiv.textContent = `${categoryName} (${articleCount} ${articleCount === 1 ? "article" : "articles"})`;
                categoryDiv.appendChild(headerDiv);

                const articlesContainer = document.createElement("div");
                articlesContainer.className = "articles-container";
                articlesContainer.style.display = "none";
                categoryDiv.appendChild(articlesContainer);

                articles.forEach(article => {
                    const articleDiv = document.createElement("div");
                    articleDiv.className = "article";

                    articleDiv.innerHTML = `
                       <p>
                            <strong>Title:</strong> 
                            <a href="https://el.wikipedia.org/?curid=${article.pageId}" target="_blank">
                                ${article.title}
                            </a>
                        </p>                      
                        <p><strong>Comments:</strong> ${article.comments || "—"}</p>
                        <p>
                          <strong>Grade:</strong> 
                          <span class="article-grade">
                            ${article.grade != null ? "★".repeat(article.grade) + "☆".repeat(5 - article.grade) : "—"}
                          </span>
                        </p>`;
                    articlesContainer.appendChild(articleDiv);
                });

                headerDiv.addEventListener("click", () => {
                    const isVisible = articlesContainer.style.display === "block";
                    articlesContainer.style.display = isVisible ? "none" : "block";
                    headerDiv.classList.toggle("expanded", !isVisible);
                });

                container.appendChild(categoryDiv);
            }

            if (statistics && statistics.length > 0) {
                const keywordsCard = document.createElement("div");
                keywordsCard.className = "keywords-card";

                const tableTitle = document.createElement("h2");
                tableTitle.textContent = "Most Searched Keywords";
                keywordsCard.appendChild(tableTitle);

                const table = document.createElement("table");
                table.className = "keywords-table";
                table.innerHTML = `
                    <thead>
                        <tr>
                            <th>Keyword</th>
                            <th>Times Used</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${statistics.map(stat => `
                            <tr>
                                <td>${stat.word}</td>
                                <td>${stat.counter}</td>
                            </tr>`).join('')}
                    </tbody>`;

                keywordsCard.appendChild(table);
                container.appendChild(keywordsCard);
            }
        })
        .catch(error => {
            container.innerHTML = `<p style="color:red;">Failed to load statistics: ${error}</p>`;
            console.error(error);
        });
});

const exportPdfBtn = document.getElementById('exportPdfBtn');

exportPdfBtn.addEventListener('click', async () => {
    try {
        const response = await axios.get('/api/statistics/export-pdf', {
            responseType: 'blob'
        });

        const blob = new Blob([response.data], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);

        const link = document.createElement('a');
        link.href = url;

        const contentDisposition = response.headers['content-disposition'];
        let filename = 'download.pdf';

        if (contentDisposition) {
            const match = contentDisposition.match(/filename="?(.+?)"?$/);
            if (match && match.length === 2) {
                filename = match[1];
            }
        }

        link.setAttribute('download', filename);

        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);
    } catch (error) {
        console.error('Failed to export PDF:', error);
        alert('Failed to export PDF. Please try again.');
    }
});

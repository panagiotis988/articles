const searchInput = document.getElementById('searchInput');
const searchButton = document.getElementById('searchButton');
const resultsDiv = document.getElementById('results');
const paginationDiv = document.getElementById('pagination');
const articleTemplate = document.getElementById('articleTemplate');

let currentPage = 1;
let currentQuery = '';
let pageSize = 10;
let totalPages = 0;

async function fetchResults(query, page = 1) {
    currentQuery = query;
    currentPage = page;
    resultsDiv.innerHTML = '<div class="loader"></div>';
    paginationDiv.innerHTML = '';

    try {
        const response = await axios.get('/api/search', {
            params: {search: query, page: page, size: pageSize}
        });
        const data = response.data;

        resultsDiv.innerHTML = '';
        if (data.error) {
            resultsDiv.innerHTML = `<p>${data.error}: ${data.message}</p>`;
            return;
        }

        totalPages = data.totalPages;

        if (data.results.length === 0) {
            resultsDiv.innerHTML = '<p>No results found.</p>';
            return;
        }

        data.results.forEach(article => {
            const clone = articleTemplate.content.cloneNode(true);

            const titleLink = clone.querySelector('.article-title a');
            const snippetEl = clone.querySelector('.article-snippet');
            const commentsEl = clone.querySelector('.article-comments');
            const gradeEl = clone.querySelector('.article-grade');
            const categoryEl = clone.querySelector('.article-category');
            const actionBtn = clone.querySelector('.article-action-button');

            actionBtn.id = `btn-${article.pageid}`;

            titleLink.textContent = article.title;
            titleLink.href = `https://el.wikipedia.org/?curid=${article.pageid}`;
            snippetEl.textContent = article.snippet;

            if (article.category) {
                const comment = article.comment !== undefined && article.comment !== null ? article.comment : "";
                const grade = article.grade !== undefined && article.grade !== null ? article.grade : "undefined";
                const category = article.category;

                commentsEl.textContent = comment;
                gradeEl.textContent = grade;
                categoryEl.textContent = category;

                actionBtn.disabled = true;
                actionBtn.replaceWith(actionBtn.cloneNode(true));
            } else {
                commentsEl.parentElement.remove();
                gradeEl.parentElement.remove();
                categoryEl.parentElement.remove();

                actionBtn.disabled = false;
                actionBtn.addEventListener('click', () => {
                    console.log(`Pressed for article: ${article.title}, pageId: ${article.pageid}`);
                });
            }

            resultsDiv.appendChild(clone);
        });

        renderPagination();

    } catch (error) {
        resultsDiv.innerHTML = `<p>Error fetching data: ${error.message}</p>`;
    }
}

function renderPagination() {
    paginationDiv.innerHTML = '';

    const createButton = (pageNum) => {
        const btn = document.createElement('button');
        btn.textContent = pageNum;
        if (pageNum === currentPage) btn.classList.add('active');
        btn.addEventListener('click', () => fetchResults(currentQuery, pageNum));
        paginationDiv.appendChild(btn);
    };

    const startPage = Math.max(1, currentPage - 5);
    const endPage = Math.min(totalPages, startPage + 9);

    for (let i = startPage; i <= endPage; i++) {
        createButton(i);
    }
}

searchButton.addEventListener('click', () => {
    const query = searchInput.value.trim();
    if (query) fetchResults(query, 1);
});

searchInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        const query = searchInput.value.trim();
        if (query) fetchResults(query, 1);
    }
});

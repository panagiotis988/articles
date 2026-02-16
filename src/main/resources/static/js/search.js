const searchInput = document.getElementById('searchInput');
const searchButton = document.getElementById('searchButton');
const resultsDiv = document.getElementById('results');
const paginationDiv = document.getElementById('pagination');
const articleTemplate = document.getElementById('articleTemplate');

let currentPage = 1;
let currentQuery = '';
let pageSize = 10;
let totalPages = 0;

let selectedArticleElement = null;
let selectedArticleData = null;

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

            const articleDiv = clone.querySelector('.article');
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
                commentsEl.textContent = article.comment ?? '';
                gradeEl.innerHTML = renderStars(article.grade ?? 0);
                categoryEl.textContent = article.category;

                actionBtn.disabled = true;
            } else {
                commentsEl.parentElement.remove();
                gradeEl.parentElement.remove();
                categoryEl.parentElement.remove();

                actionBtn.disabled = false;
                actionBtn.addEventListener('click', () => openModal(article, articleDiv));
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

    for (let i = startPage; i <= endPage; i++) createButton(i);
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

const modal = document.getElementById('saveModal');
const cancelBtn = document.getElementById('cancelSave');
const confirmBtn = document.getElementById('confirmSave');
const categorySelect = document.getElementById('categorySelect');
const commentsInput = document.getElementById('commentsInput');
const starButtons = document.querySelectorAll('#gradeStars .star');

let selectedGrade = 1;

async function loadCategories() {
    try {
        const response = await axios.get('/api/categories');
        const categories = response.data;
        categorySelect.innerHTML = '<option value="">Select category</option>';
        categories.forEach(cat => {
            const option = document.createElement('option');
            option.value = cat.id;
            option.textContent = cat.title;
            option.dataset.title = cat.title;
            categorySelect.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

loadCategories();

function openModal(articleData, articleElement) {
    selectedArticleData = articleData;
    selectedArticleElement = articleElement;
    selectedGrade = 1;
    updateStarDisplay();
    modal.classList.remove('hidden');
}

function closeModal() {
    modal.classList.add('hidden');
    categorySelect.value = '';
    commentsInput.value = '';
    selectedGrade = 1;
    updateStarDisplay();
    selectedArticleData = null;
    selectedArticleElement = null;
}

cancelBtn.addEventListener('click', closeModal);
window.addEventListener('click', (e) => {
    if (e.target === modal) closeModal();
});
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeModal();
});

starButtons.forEach(star => {
    star.addEventListener('click', () => {
        selectedGrade = parseInt(star.dataset.value);
        updateStarDisplay();
    });
    star.addEventListener('mouseover', () => {
        const hoverValue = parseInt(star.dataset.value);
        starButtons.forEach(s => s.classList.toggle('selected', parseInt(s.dataset.value) <= hoverValue));
    });
    star.addEventListener('mouseout', () => updateStarDisplay());
});

function updateStarDisplay() {
    starButtons.forEach(star => star.classList.toggle('selected', parseInt(star.dataset.value) <= selectedGrade));
}

confirmBtn.addEventListener('click', async () => {
    if (!selectedArticleData) return;

    const selectedOption = categorySelect.selectedOptions[0];
    const categoryId = selectedOption?.value;
    const categoryTitle = selectedOption?.dataset.title;
    const comment = commentsInput.value.trim();
    const grade = selectedGrade;

    if (!categoryId) {
        alert('Please select a category.');
        return;
    }

    const payload = {
        pageId: selectedArticleData.pageid,
        categoryId: Number(categoryId),
        grade: grade,
        comment: comment,
        title: selectedArticleData.title,
        snippet: selectedArticleData.snippet
    };

    let saved;
    try {
        const response = await axios.post('/api/articles', payload);
        saved = response.data;
    } catch (error) {
        console.log(error)
        const message = error?.response?.data?.message ?? error.message;
        alert(`Save failed: ${message}`);
        return;
    }

    const savedComment = saved?.comment ?? comment;
    const savedGrade = saved?.grade ?? grade;
    const savedCategoryTitle = saved?.category?.title ?? categoryTitle;

    const actionBtn = selectedArticleElement.querySelector('.article-action-button');
    actionBtn.disabled = true;
    actionBtn.textContent = 'Save';

    const extraDiv = selectedArticleElement.querySelector('.article-extra');
    if (!selectedArticleElement.querySelector('.article-comments')) {
        const commentP = document.createElement('p');
        commentP.innerHTML = `<strong>Comments:</strong> <span class="article-comments">${savedComment}</span>`;
        extraDiv.appendChild(commentP);

        const gradeP = document.createElement('p');
        gradeP.innerHTML = `<strong>Grade:</strong> <span class="article-grade">${renderStars(savedGrade)}</span>`;
        extraDiv.appendChild(gradeP);

        const categoryP = document.createElement('p');
        categoryP.innerHTML = `<strong>Category:</strong> <span class="article-category">${savedCategoryTitle}</span>`;
        extraDiv.appendChild(categoryP);
    }

    closeModal();
});

function renderStars(grade) {
    let stars = '';
    for (let i = 1; i <= 5; i++) {
        stars += i <= grade ? '<span class="filled">★</span>' : '<span class="empty">☆</span>';
    }
    return stars;
}

let categories = [];

const container = document.getElementById('categories-container');
let deleteTargetId = null;

const modal = document.getElementById('deleteModal');
const confirmDeleteBtn = document.getElementById('confirmDelete');
const cancelDeleteBtn = document.getElementById('cancelDelete');
const createModal = document.getElementById('createModal');
const createCategoryBtn = document.getElementById('createCategoryBtn');
const confirmCreateBtn = document.getElementById('confirmCreate');
const cancelCreateBtn = document.getElementById('cancelCreate');
const newCategoryTitleInput = document.getElementById('newCategoryTitle');


function loadCategories() {
    axios.get('/api/categories/public')
        .then(response => {
            categories = response.data;
            renderCategories();
        })
        .catch(error => {
            console.error('Error loading categories:', error);
        });
}
function renderCategories() {
    container.innerHTML = '';

    categories.forEach(category => {

        const row = document.createElement('div');
        row.className = 'category-row';

        const titleDiv = document.createElement('div');
        titleDiv.className = 'category-title';
        titleDiv.textContent = category.title;

        const buttonsDiv = document.createElement('div');
        buttonsDiv.className = 'category-buttons';

        const editBtn = document.createElement('button');
        editBtn.textContent = 'Edit';
        editBtn.className = 'edit';
        editBtn.onclick = () => toggleEdit(category, titleDiv, editBtn);

        const deleteBtn = document.createElement('button');
        deleteBtn.textContent = 'Delete';
        deleteBtn.className = 'delete';
        deleteBtn.onclick = () => deleteCategory(category.id);

        buttonsDiv.appendChild(editBtn);
        buttonsDiv.appendChild(deleteBtn);

        row.appendChild(titleDiv);
        row.appendChild(buttonsDiv);

        container.appendChild(row);
    });
}
function toggleEdit(category, titleDiv, editBtn) {
    const isEditing = editBtn.classList.contains('save');

    if (!isEditing) {

        const input = document.createElement('input');
        input.type = 'text';
        input.value = category.title;
        input.className = 'edit-input';
        input.maxLength = 40;

        titleDiv.innerHTML = '';
        titleDiv.appendChild(input);
        input.focus();

        editBtn.textContent = 'Save';
        editBtn.classList.remove('edit');
        editBtn.classList.add('save');

    } else {

        const input = titleDiv.querySelector('input');
        const newTitle = input.value.trim();

        if (!newTitle) return;

        // Backend PATCH
        /*
        axios.put(`/api/categories/${category.id}`, {
            title: newTitle
        }).then(() => {
            loadCategories();
        }).catch(error => {
            console.error('Update failed:', error);
        });
        */

        // Local PATCH
        category.title = newTitle;

        titleDiv.textContent = category.title;

        editBtn.textContent = 'Edit';
        editBtn.classList.remove('save');
        editBtn.classList.add('edit');
    }
}


function deleteCategory(id) {
    deleteTargetId = id;
    modal.classList.remove('hidden');
}

confirmDeleteBtn.onclick = () => {

    // Backend DELETE
    /*
    axios.delete(`/api/categories/${deleteTargetId}`)
        .then(() => {
            modal.classList.add('hidden');
            loadCategories();
        })
        .catch(error => {
            console.error('Delete failed:', error);
        });
    */

    // Local DELETE
    const index = categories.findIndex(c => c.id === deleteTargetId);
    if (index !== -1) {
        categories.splice(index, 1);
    }

    modal.classList.add('hidden');
    renderCategories();
};


cancelDeleteBtn.onclick = () => {
    modal.classList.add('hidden');
};

createCategoryBtn.onclick = () => {
    newCategoryTitleInput.value = '';
    createModal.classList.remove('hidden');
    newCategoryTitleInput.focus();
};

cancelCreateBtn.onclick = () => {
    createModal.classList.add('hidden');
};

confirmCreateBtn.onclick = () => {
    const title = newCategoryTitleInput.value.trim();
    if (!title) return alert('Please enter a title');

    // Backend POST
    /*
    axios.post('/api/categories', { title })
        .then(() => {
            createModal.classList.add('hidden');
            loadCategories();
        })
        .catch(err => console.error('Create failed:', err));
    */

    // Local POST
    const newId = categories.length ? Math.max(...categories.map(c => c.id)) + 1 : 1;
    categories.push({ id: newId, title: title });
    createModal.classList.add('hidden');
    renderCategories();
};

loadCategories();

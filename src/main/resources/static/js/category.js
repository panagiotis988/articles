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

        axios.patch(`/api/categories/${category.id}`, {
            title: newTitle
        })
            .then(() => {
                category.title = newTitle;
                titleDiv.textContent = category.title;

                editBtn.textContent = 'Edit';
                editBtn.classList.remove('save');
                editBtn.classList.add('edit');
            })
            .catch(error => {
                let message = 'Update failed.';

                if (error.response) {
                    message = error.response.data?.message || message;
                }

                showErrorModal(message);
            });
    }
}


function deleteCategory(id) {
    deleteTargetId = id;
    modal.classList.remove('hidden');
}

confirmDeleteBtn.onclick = () => {

    axios.delete(`/api/categories/${deleteTargetId}`)
        .then(() => {
            modal.classList.add('hidden');
            loadCategories();
        })
        .catch(error => {
            modal.classList.add('hidden');
            let message = 'Delete failed.';
            if (error.response) {
                message = error.response.data?.message || message;
            } else {
                console.error('Delete failed:', error);
            }
            showErrorModal(message);
        });
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

const errorModal = document.getElementById('errorModal');
const errorMessage = document.getElementById('errorModalMessage');
const closeErrorBtn = document.getElementById('closeErrorModal');

document.getElementById('createCategoryBtn').onclick = () => {
    createModal.classList.remove('hidden');
};

cancelCreateBtn.onclick = () => {
    createModal.classList.add('hidden');
    newCategoryTitleInput.value = '';
};

confirmCreateBtn.onclick = () => {
    const title = newCategoryTitleInput.value.trim();
    if (!title) {
        showErrorModal('Please enter a title');
        return;
    }

    axios.post('/api/categories', {title})
        .then(() => {
            createModal.classList.add('hidden');
            newCategoryTitleInput.value = '';
            loadCategories();
        })
        .catch(error => {
            let message = 'Category creation failed.';
            if (error.response) {
                message = error.response.data?.message || message;
            } else {
                console.error('Create failed:', error);
            }
            showErrorModal(message);
        });
};

function showErrorModal(message) {
    errorMessage.textContent = message;
    errorModal.classList.remove('hidden');
}

closeErrorBtn.onclick = () => {
    errorModal.classList.add('hidden');
};


loadCategories();

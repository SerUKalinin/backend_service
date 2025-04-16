// Токен и данные пользователя
let jwtToken = localStorage.getItem('jwt');
let currentUser = null;
let isAdmin = false;
let currentParentId = null; // Добавляем глобальную переменную для хранения ID текущего родительского объекта
let lastSelectedObjectType = null; // Добавляем переменную для хранения последнего выбранного типа

// Проверка авторизации при загрузке
document.addEventListener('DOMContentLoaded', function() {
    // Если токена нет - редирект на страницу входа
    if (!jwtToken) {
        window.location.href = 'login.html';
        return;
    }

    // Загружаем данные пользователя
    loadUserData();

    // Настройка навигации
    setupNavigation();

    // Загружаем начальные данные для дашборда
    loadDashboardData();

    // Запускаем автоматическое обновление
    startDashboardAutoRefresh();

    // Обработчики для аккордеона
    document.addEventListener('click', function(e) {
        const header = e.target.closest('[data-toggle="details"]');
        if (!header) return;

        const body = header.nextElementSibling;
        header.classList.toggle('collapsed');
        body.classList.toggle('collapsed');
    });

    // Обработчики для кнопок ответственного
    document.addEventListener('click', function(e) {
        const button = e.target.closest('[data-action]');
        if (!button) return;

        const action = button.dataset.action;
        const objectId = button.dataset.objectId;

        if (action === 'assign') {
            showAssignResponsibleModal(objectId);
        } else if (action === 'remove') {
            removeResponsibleUser(objectId);
        }
    });
});

// Загрузка данных пользователя
function loadUserData() {
    fetch("http://localhost:8080/users/info", {
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка авторизации');
            }
            return response.json();
        })
        .then(data => {
            currentUser = data;
            updateUIWithUserData(data);
        })
        .catch(error => {
            console.error('Ошибка:', error);
            logout();
        });
}

// Обновление UI с данными пользователя
function updateUIWithUserData(userData) {
    document.getElementById('user-name').textContent = userData.username || 'Пользователь';
    document.getElementById('user-avatar').textContent = getInitials(userData.username || 'П');

    // Проверяем, является ли пользователь администратором
    isAdmin = userData.roles === 'ROLE_ADMIN';
    document.getElementById('user-role').textContent = isAdmin ? 'Администратор' : 'Пользователь';

    // Показываем/скрываем административные разделы
    document.querySelectorAll('.admin-only').forEach(el => {
        el.style.display = isAdmin ? 'flex' : 'none';
    });
}

// Получение инициалов
function getInitials(username) {
    if (!username) return 'U';
    const parts = username.split(' ');
    if (parts.length > 1) {
        return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }
    return username.substring(0, 2).toUpperCase();
}

// Настройка навигации
function setupNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', function() {
            // Удаляем активный класс у всех элементов
            navItems.forEach(i => i.classList.remove('active'));
            // Добавляем активный класс текущему элементу
            this.classList.add('active');

            // Получаем секцию для отображения
            const section = this.getAttribute('data-section');
            showSection(section);
        });
    });
}

// Показать секцию
function showSection(sectionId) {
    // Скрываем все секции
    document.querySelectorAll('.content-section').forEach(section => {
        section.style.display = 'none';
    });

    // Показываем выбранную секцию
    const selectedSection = document.getElementById(sectionId + '-section');
    if (selectedSection) {
        selectedSection.style.display = 'block';

        // Загружаем данные для выбранной секции
        switch(sectionId) {
            case 'objects':
                loadObjects();
                break;
            case 'tasks':
                loadTasks();
                break;
            case 'profile':
                loadProfile();
                break;
            case 'users':
                loadUsers();
                break;
        }
    }

    // Обновляем активный пункт меню в сайдбаре
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });

    // Находим соответствующий пункт меню в сайдбаре и делаем его активным
    const navItem = document.querySelector(`.nav-item[data-section="${sectionId}"]`);
    if (navItem) {
        navItem.classList.add('active');
    }
}

// Загрузка данных для дашборда
function loadDashboardData() {
    // Загружаем объекты недвижимости
    fetch("http://localhost:8080/real-estate-objects", {
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) throw new Error('Ошибка загрузки объектов');
            return response.json();
        })
        .then(objects => {
            // Показываем общее количество всех объектов, включая дочерние
            let totalObjects = objects.length;
            console.log('Total objects found:', totalObjects);
            document.getElementById('objects-count').textContent = totalObjects;
        })
        .catch(error => {
            console.error('Ошибка загрузки объектов:', error);
            document.getElementById('objects-count').textContent = '0';
        });

    // Загружаем задачи
    fetch("http://localhost:8080/tasks", {
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) throw new Error('Ошибка загрузки задач');
            return response.json();
        })
        .then(tasks => {
            const activeTasks = tasks.filter(task =>
                task.status === 'NEW' || task.status === 'IN_PROGRESS' || task.status === 'URGENT');
            const expiredTasks = tasks.filter(task => task.status === 'EXPIRED');

            document.getElementById('active-tasks-count').textContent = activeTasks.length || 0;
            document.getElementById('expired-tasks-count').textContent = expiredTasks.length || 0;

            // Отображаем последние задачи
            renderRecentTasks(tasks.slice(0, 5));
        })
        .catch(error => {
            console.error('Ошибка загрузки задач:', error);
            document.getElementById('active-tasks-count').textContent = '0';
            document.getElementById('expired-tasks-count').textContent = '0';
            renderRecentTasks([]);
        });
}

// Функция для автоматического обновления данных дашборда
function startDashboardAutoRefresh() {
    // Обновляем данные каждую минуту
    setInterval(loadDashboardData, 60000);
}

// Отображение последних задач
function renderRecentTasks(tasks) {
    const container = document.getElementById('recent-tasks-list');
    container.innerHTML = '';

    if (!tasks || tasks.length === 0) {
        container.innerHTML = '<div class="table-row"><div class="table-cell" colspan="5">Задачи не найдены</div></div>';
        return;
    }

    tasks.forEach(task => {
        const row = document.createElement('div');
        row.className = 'table-row';

        // Форматируем дату дедлайна
        let deadline = 'Не указан';
        if (task.deadline) {
            const deadlineDate = new Date(task.deadline);
            deadline = deadlineDate.toLocaleDateString() + ' ' + deadlineDate.toLocaleTimeString();
        }

        // Определяем класс статуса
        let statusClass = '';
        let statusText = '';
        if (task.status === 'NEW') {
            statusClass = 'status-new';
            statusText = 'Новая';
        } else if (task.status === 'IN_PROGRESS') {
            statusClass = 'status-in-progress';
            statusText = 'В работе';
        } else if (task.status === 'COMPLETED') {
            statusClass = 'status-completed';
            statusText = 'Завершена';
        } else if (task.status === 'EXPIRED') {
            statusClass = 'status-expired';
            statusText = 'Просрочена';
        } else if (task.status === 'URGENT') {
            statusClass = 'status-expired';
            statusText = 'Срочная';
        }

        row.innerHTML = `
                <div class="table-cell">${task.title || 'Без названия'}</div>
                <div class="table-cell">${task.realEstateObject ? task.realEstateObject.name : 'Не указан'}</div>
                <div class="table-cell"><span class="status-badge ${statusClass}">${statusText}</span></div>
                <div class="table-cell">${deadline}</div>
                <div class="table-cell">
                    <div class="action-btn edit-btn" onclick="editTask('${task.id}')">
                        <i class="fas fa-edit"></i>
                    </div>
                </div>
            `;
        container.appendChild(row);
    });
}

// Выход из системы
function logout() {
    // Отправляем запрос на сервер для выхода
    fetch("http://localhost:8080/auth/logout", {
        method: "POST",
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .finally(() => {
            // Очищаем локальное хранилище и перенаправляем на страницу входа
            localStorage.removeItem('jwt');
            window.location.href = 'login.html';
        });
}

// Загрузка объектов (заглушка)
function loadObjects() {
    const pageTitle = document.querySelector('#objects-section .page-title');
    pageTitle.textContent = 'Объекты недвижимости';

    fetch("http://localhost:8080/real-estate-objects", {
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(objects => {
            const container = document.getElementById('objects-content');
            container.innerHTML = `
                <div class="data-table objects-table">
                    <div class="table-header">
                        <h3 class="table-title">Список объектов</h3>
                        <div class="table-filters">
                            <select class="filter-select" id="object-type-filter" onchange="filterObjects()">
                                <option value="">Все типы</option>
                                <option value="BUILDING" selected>Здание</option>
                                <option value="ENTRANCE">Подъезд</option>
                                <option value="BASEMENT_FLOOR">Цокольный этаж</option>
                                <option value="FLOOR">Этаж</option>
                                <option value="STAIRWELL">Лестничный пролет</option>
                                <option value="ELEVATOR">Лифт</option>
                                <option value="FLOOR_BALCONY">Балкон этажа</option>
                                <option value="CORRIDOR">Коридор</option>
                                <option value="ELEVATOR_HALL">Холл лифта</option>
                                <option value="APARTMENT">Квартира</option>
                                <option value="APARTMENT_BALCONY">Балкон квартиры</option>
                                <option value="ROOM">Комната</option>
                                <option value="TASK">Задача</option>
                            </select>
                        </div>
                    </div>
                    <div class="table-body">
                        <div class="table-row header">
                            <div class="table-cell">Название</div>
                            <div class="table-cell">Тип</div>
                            <div class="table-cell">Родительский объект</div>
                            <div class="table-cell">Создатель</div>
                            <div class="table-cell">Дата создания</div>
                            <div class="table-cell">Действия</div>
                        </div>
                        <div id="objects-list">
                            ${objects.length > 0 ? objects.map(object => `
                                <div class="table-row" data-type="${object.objectType}">
                                    <div class="table-cell">
                                        <a href="#" onclick="showObjectDetails(${object.id})" class="object-name-link">
                                            ${object.name}
                                        </a>
                                    </div>
                                    <div class="table-cell">${getObjectTypeName(object.objectType)}</div>
                                    <div class="table-cell">
                                        ${object.parentId ?
                `<button class="view-hierarchy-btn" onclick="event.stopPropagation(); showHierarchyModal(${object.id})">
                                                <i class="fas fa-sitemap"></i> Просмотреть иерархию
                                            </button>` :
                '-'
            }
                                    </div>
                                    <div class="table-cell">
                                        ${object.createdByFirstName && object.createdByLastName ?
                `${object.createdByFirstName} ${object.createdByLastName}` :
                '-'
            }
                                    </div>
                                    <div class="table-cell">${new Date(object.createdAt).toLocaleString()}</div>
                                    <div class="table-cell">
                                        <div class="action-btn edit-btn" onclick="editObject(${object.id})" title="Редактировать">
                                            <i class="fas fa-edit"></i>
                                        </div>
                                        <div class="action-btn delete-btn" onclick="deleteObject(${object.id})" title="Удалить">
                                            <i class="fas fa-trash"></i>
                                        </div>
                                    </div>
                                </div>
                            `).join('') : '<div class="table-row"><div class="table-cell" colspan="6">Нет объектов</div></div>'}
                        </div>
                    </div>
                </div>
            `;
            // Применяем фильтр по умолчанию
            filterObjects();
        })
        .catch(error => {
            console.error('Error loading objects:', error);
            container.innerHTML = '<div class="error">Ошибка загрузки объектов</div>';
        });
}

function getObjectTypeName(type) {
    const types = {
        'BUILDING': 'Здание',
        'ENTRANCE': 'Подъезд',
        'BASEMENT_FLOOR': 'Цокольный этаж',
        'FLOOR': 'Этаж',
        'STAIRWELL': 'Лестничный пролет',
        'ELEVATOR': 'Лифт',
        'FLOOR_BALCONY': 'Балкон этажа',
        'CORRIDOR': 'Коридор',
        'ELEVATOR_HALL': 'Холл лифта',
        'APARTMENT': 'Квартира',
        'APARTMENT_BALCONY': 'Балкон квартиры',
        'ROOM': 'Комната',
        'TASK': 'Задача'
    };
    return types[type] || type;
}

// Функция фильтрации объектов
function filterObjects() {
    const selectedType = document.getElementById('object-type-filter').value;
    const rows = document.querySelectorAll('#objects-list .table-row');

    rows.forEach(row => {
        if (!selectedType || row.getAttribute('data-type') === selectedType) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

// Обновляем функцию showObjectDetails
function showObjectDetails(objectId) {
    console.log('Opening object details for ID:', objectId);

    fetch(`http://localhost:8080/real-estate-objects/${objectId}`, {
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка загрузки деталей объекта');
            }
            return response.json();
        })
        .then(object => {
            // Определяем, является ли объект дочерним
            const isChildObject = object.parentId !== null;
            const containerClass = isChildObject ? 'child-object-details' : '';

            // Если есть ответственный пользователь, загружаем его полную информацию
            let responsibleUserPromise = Promise.resolve(null);
            if (object.responsibleUserId) {
                responsibleUserPromise = fetch(`http://localhost:8080/users/info/${object.responsibleUserId}`, {
                    headers: {
                        'Authorization': `Bearer ${jwtToken}`,
                        'Content-Type': 'application/json'
                    }
                })
                    .then(response => response.json())
                    .catch(error => {
                        console.error('Error loading responsible user:', error);
                        return null;
                    });
            }

            // Формируем хлебные крошки
            let breadcrumbsPromise = Promise.resolve('');
            if (object.parentId) {
                breadcrumbsPromise = fetch(`http://localhost:8080/real-estate-objects/${object.parentId}`, {
                    headers: {
                        'Authorization': `Bearer ${jwtToken}`,
                        'Content-Type': 'application/json'
                    }
                })
                    .then(response => response.json())
                    .then(parentObject => {
                        return `
                        <div class="breadcrumbs">
                            <a href="#" onclick="showObjectDetails(${parentObject.id})" class="object-name-link">
                                ${parentObject.name}
                            </a>
                            <span class="breadcrumb-separator"> / </span>
                            <span class="current-object">${object.name}</span>
                        </div>
                    `;
                    });
            }

            // Ждем загрузки всех данных
            return Promise.all([responsibleUserPromise, breadcrumbsPromise])
                .then(([responsibleUser, breadcrumbsHtml]) => {
                    // Обновляем содержимое
                    const container = document.getElementById('objects-content');
                    container.innerHTML = `
                        ${breadcrumbsHtml}
                        <div class="children-section">
                            <div class="children-header">
                            </div>
                            <div class="children-grid" id="children-list">
                                <!-- Дочерние объекты будут загружены здесь -->
                            </div>
                        </div>
                        <div class="${containerClass}">
                            <div class="data-table object-details-table">
                                <div class="table-header" data-toggle="details">
                                    <h3 class="table-title">Детали объекта</h3>
                                    <i class="fas fa-chevron-down toggle-icon"></i>
                                </div>
                                <div class="table-body">
                                    <div class="table-row">
                                        <div class="table-cell">
                                            <div class="detail-item">
                                                <span class="detail-label">Название:</span>
                                                <span>${object.name}</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="table-row">
                                        <div class="table-cell">
                                            <div class="detail-item">
                                                <span class="detail-label">Тип:</span>
                                                <span>${getObjectTypeName(object.objectType)}</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="table-row">
                                        <div class="table-cell">
                                            <div class="detail-item">
                                                <span class="detail-label">Дата создания:</span>
                                                <span>${new Date(object.createdAt).toLocaleString()}</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="table-row">
                                        <div class="table-cell">
                                            <div class="detail-item">
                                                <span class="detail-label">Создатель:</span>
                                                <span>${object.createdByFirstName && object.createdByLastName ?
                        `${object.createdByFirstName} ${object.createdByLastName}` :
                        '-'
                    }</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="table-row">
                                        <div class="table-cell">
                                            <div class="detail-item">
                                                <span class="detail-label">Ответственный:</span>
                                                <div class="responsible-user-container">
                                                    <span id="responsible-user-display">
                                                        ${responsibleUser ?
                        `${responsibleUser.firstName} ${responsibleUser.lastName} <span class="status-badge ${getRoleClass(responsibleUser.roles)}">${getRoleName(responsibleUser.roles)}</span>` :
                        'Не назначен'
                    }
                                                    </span>
                                                    <div class="responsible-user-actions">
                                                        <button class="btn btn-sm btn-primary" data-action="assign" data-object-id="${object.id}">
                                                            <i class="fas fa-user-plus"></i> Назначить
                                                        </button>
                                                        ${object.responsibleUserId ? `
                                                            <button class="btn btn-sm btn-danger" data-action="remove" data-object-id="${object.id}">
                                                                <i class="fas fa-user-minus"></i> Удалить
                                                            </button>
                                                        ` : ''}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="back-button">
                            <button class="btn btn-primary" onclick="loadObjects()">
                                <i class="fas fa-arrow-left"></i>
                                Вернуться к списку
                            </button>
                        </div>
                    `;

                    // Сохраняем ID текущего объекта
                    currentParentId = objectId;

                    // Загружаем дочерние объекты
                    loadChildren(objectId);
                });
        })
        .catch(error => {
            console.error('Ошибка загрузки деталей объекта:', error);
            showNotification('Ошибка загрузки деталей объекта', 'error');
        });
}

// Обновляем функцию loadChildren
function loadChildren(parentId) {
    const jwtToken = localStorage.getItem('jwt');
    const container = document.getElementById('children-list');
    container.innerHTML = '<div class="loading">Загрузка дочерних объектов...</div>';

    fetch(`http://localhost:8080/real-estate-objects/${parentId}/children`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка загрузки дочерних объектов');
            }
            return response.json();
        })
        .then(children => {
            let childrenHTML = '';

            if (children && children.length > 0) {
                children.forEach(child => {
                    childrenHTML += `
                        <div class="child-card">
                            <div class="child-name">
                                <a href="#" onclick="showObjectDetails(${child.id}); return false;" class="object-name-link">
                                    ${child.name}
                                </a>
                            </div>
                            <div class="child-type">${getObjectTypeName(child.objectType)}</div>
                            <div class="child-actions">
                                <div class="action-btn edit-btn" onclick="editObject(${child.id})" title="Редактировать">
                                    <i class="fas fa-edit"></i>
                                </div>
                                <div class="action-btn delete-btn" onclick="deleteObject(${child.id})" title="Удалить">
                                    <i class="fas fa-trash"></i>
                                </div>
                            </div>
                        </div>
                    `;
                });
            }

            // Добавляем карточку для создания нового дочернего объекта
            const addChildCardHTML = `
                <div class="add-child-card" onclick="showAddChildObjectModal(${parentId})">
                    <div class="add-child-card-content">
                        <i class="fas fa-plus"></i>
                    </div>
                </div>
            `;

            container.innerHTML = childrenHTML + addChildCardHTML;
        })
        .catch(error => {
            console.error('Ошибка загрузки дочерних объектов:', error);
            container.innerHTML = '<div class="error">Ошибка загрузки дочерних объектов</div>';
        });
}

// Добавляем новую функцию для показа модального окна создания дочернего объекта
function showAddChildObjectModal(parentId) {
    const modal = document.getElementById('object-modal');
    document.getElementById('modal-title').textContent = 'Создание дочернего объекта';
    document.querySelector('#object-form button[type="submit"]').textContent = 'Создать';

    // Очищаем форму и сбрасываем обработчик
    const form = document.getElementById('object-form');
    const newForm = form.cloneNode(true);
    form.parentNode.replaceChild(newForm, form);
    const currentForm = document.getElementById('object-form');
    currentForm.reset();
    currentForm.onsubmit = null;
    document.getElementById('object-id').value = '';
    document.getElementById('parent-select').disabled = true;

    // Устанавливаем последний выбранный тип объекта, если он есть
    if (lastSelectedObjectType) {
        document.getElementById('object-type').value = lastSelectedObjectType;
    }

    // Загружаем родителя
    const parentSelect = document.getElementById('parent-select');
    fetch(`http://localhost:8080/real-estate-objects/${parentId}`, {
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(parentObject => {
            parentSelect.innerHTML = `<option value="${parentId}" selected>${parentObject.name} (${getObjectTypeName(parentObject.objectType)})</option>`;
        })
        .catch(error => {
            console.error('Error loading parent object:', error);
            parentSelect.innerHTML = '<option value="">Ошибка загрузки родителя</option>';
        });

    // Назначаем обработчик для СОЗДАНИЯ ДОЧЕРНЕГО
    currentForm.onsubmit = function(e) {
        e.preventDefault();
        const objectName = document.getElementById('object-name').value;
        const objectType = document.getElementById('object-type').value;
        // Сохраняем выбранный тип объекта
        lastSelectedObjectType = objectType;
        const data = { name: objectName, objectType: objectType, parentId: parentId };

        fetch('http://localhost:8080/real-estate-objects', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${jwtToken}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        })
            .then(response => {
                if (!response.ok) { return response.text().then(text => { throw new Error(text || 'Ошибка при создании объекта'); }); }
                return response.json();
            })
            .then(data => {
                closeObjectModal();
                loadChildren(parentId); // Обновляем список дочерних объектов
            })
            .catch(error => {
                console.error('Ошибка при создании объекта:', error);
                alert('Ошибка при создании объекта: ' + error.message);
            });
    };

    modal.style.display = 'block';
}

// Обновляем обработчик формы
document.getElementById('object-form').addEventListener('submit', function(e) {
    e.preventDefault();

    const objectName = document.getElementById('object-name').value;
    const objectType = document.getElementById('object-type').value;
    const parentId = document.getElementById('parent-select').value;

    // Формируем данные объекта
    const data = {
        name: objectName,
        objectType: objectType
    };

    // Добавляем parentId только если он выбран
    if (parentId) {
        data.parentId = parseInt(parentId);
    }

    console.log('Sending data to server:', data);

    fetch('http://localhost:8080/real-estate-objects', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || 'Ошибка при создании объекта');
                });
            }
            return response.json();
        })
        .then(data => {
            console.log('Object created successfully:', data);
            // Закрываем модальное окно
            document.getElementById('object-modal').style.display = 'none';

            // Обновляем список объектов
            if (parentId) {
                // Если создавали дочерний объект, обновляем детали родителя
                showObjectDetails(parentId);
            } else {
                // Если создавали обычный объект, обновляем список
                loadObjects();
            }
        })
        .catch(error => {
            console.error('Ошибка при создании объекта:', error);
            alert('Ошибка при создании объекта: ' + error.message);
        });
});

// Функция для закрытия модального окна
function closeObjectModal() {
    const modal = document.getElementById('object-modal');
    modal.style.display = 'none';
    const form = document.getElementById('object-form');
    // Клонируем форму, чтобы удалить все слушатели событий
    const newForm = form.cloneNode(true);
    form.parentNode.replaceChild(newForm, form);
    // Сбрасываем значения новой формы
    newForm.reset();
    newForm.onsubmit = null; // Явно удаляем обработчик
    document.getElementById('object-id').value = '';
    document.getElementById('parent-select').disabled = false;
    document.getElementById('parent-select').innerHTML = '<option value="">Загрузка...</option>';
    document.querySelector('#object-form button[type="submit"]').textContent = 'Создать'; // Возвращаем текст кнопки по умолчанию
}

// Функция для возврата к списку объектов
function showObjectsList() {
    loadObjects();
}

// Добавляем функцию для отображения заметок объекта
function showObjectNotes(objectId) {
    console.log('Opening notes for object ID:', objectId);

    // Получаем информацию об объекте
    fetch(`http://localhost:8080/real-estate-objects/${objectId}`, {
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(object => {
            // Создаем модальное окно для заметок
            const modal = document.createElement('div');
            modal.className = 'modal';
            modal.id = 'notes-modal';
            modal.innerHTML = `
                <div class="modal-content">
                    <div class="modal-header">
                        <h3>Заметки объекта: ${object.name}</h3>
                        <button class="close-btn" onclick="closeNotesModal()">&times;</button>
                    </div>
                    <div class="notes-content">
                        <div class="notes-list" id="notes-list">
                            <!-- Заметки будут загружены здесь -->
                        </div>
                        <div class="notes-form">
                            <textarea id="new-note" placeholder="Введите новую заметку..." rows="3"></textarea>
                            <button class="btn btn-primary" onclick="addNote(${objectId})">Добавить заметку</button>
                        </div>
                    </div>
                </div>
            `;

            document.body.appendChild(modal);
            modal.style.display = 'block';

            // Загружаем существующие заметки
            loadNotes(objectId);
        })
        .catch(error => {
            console.error('Error loading object details:', error);
            alert('Ошибка загрузки информации об объекте');
        });
}

// Функция для загрузки заметок
function loadNotes(objectId) {
    const notesList = document.getElementById('notes-list');
    if (!notesList) return;

    // Здесь будет запрос к API для получения заметок
    // Пока что просто показываем заглушку
    notesList.innerHTML = '<div class="no-notes">Нет заметок</div>';
}

// Функция для добавления заметки
function addNote(objectId) {
    const noteText = document.getElementById('new-note').value;
    if (!noteText.trim()) {
        alert('Введите текст заметки');
        return;
    }

    // Здесь будет запрос к API для добавления заметки
    console.log('Adding note:', noteText, 'for object:', objectId);

    // Очищаем поле ввода
    document.getElementById('new-note').value = '';
}

// Функция для закрытия модального окна заметок
function closeNotesModal() {
    const modal = document.getElementById('notes-modal');
    if (modal) {
        modal.remove();
    }
}

// Функция для удаления объекта
function deleteObject(objectId) {
    if (!confirm('Вы уверены, что хотите удалить этот объект?')) {
        return;
    }

    fetch(`http://localhost:8080/real-estate-objects/${objectId}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || 'Ошибка при удалении объекта');
                });
            }

            // Проверяем, находимся ли мы в деталях объекта
            const objectsContent = document.getElementById('objects-content');
            const isInDetailsView = objectsContent.querySelector('.children-section') !== null;

            if (isInDetailsView) {
                console.log('Updating children list for parent:', currentParentId);
                loadChildren(currentParentId);
            } else {
                console.log('Updating main objects list');
                loadObjects();
            }

            // Обновляем данные на дашборде
            loadDashboardData();
        })
        .catch(error => {
            console.error('Ошибка при удалении объекта:', error);
            alert('Ошибка при удалении объекта: ' + error.message);
        });
}

// Функции для работы с иерархией объектов
function showHierarchyModal(objectId) {
    const modal = document.getElementById('hierarchy-modal');
    const hierarchyPath = document.getElementById('hierarchy-path');
    hierarchyPath.innerHTML = '<div class="loading">Загрузка иерархии...</div>';
    modal.style.display = 'block';

    // Рекурсивная функция для получения цепочки родителей
    async function getParentChain(id, chain = []) {
        try {
            const response = await fetch(`http://localhost:8080/real-estate-objects/${id}`, {
                headers: {
                    'Authorization': `Bearer ${jwtToken}`,
                    'Content-Type': 'application/json'
                }
            });
            const object = await response.json();
            chain.unshift(object); // Добавляем объект в начало массива

            if (object.parentId) {
                return getParentChain(object.parentId, chain);
            }
            return chain;
        } catch (error) {
            console.error('Error loading object:', error);
            return chain;
        }
    }

    // Загружаем и отображаем цепочку родителей
    getParentChain(objectId).then(chain => {
        hierarchyPath.innerHTML = chain.map((obj, index) => `
                <div class="hierarchy-item">
                    <div class="hierarchy-name">${obj.name}</div>
                    <div class="hierarchy-type">${getObjectTypeName(obj.objectType)}</div>
                </div>
            `).join('');
    }).catch(error => {
        hierarchyPath.innerHTML = '<div class="error">Ошибка загрузки иерархии</div>';
    });
}

function closeHierarchyModal() {
    document.getElementById('hierarchy-modal').style.display = 'none';
}

function editObject(objectId) {
    console.log('Editing object:', objectId);

    // Устанавливаем заголовок модального окна
    document.getElementById('modal-title').textContent = 'Редактирование объекта';

    // Меняем текст кнопки
    document.querySelector('#object-form button[type="submit"]').textContent = 'Сохранить';

    // Клонируем форму, чтобы удалить старые обработчики
    const form = document.getElementById('object-form');
    const newForm = form.cloneNode(true);
    form.parentNode.replaceChild(newForm, form);

    // Получаем данные объекта
    fetch(`http://localhost:8080/real-estate-objects/${objectId}`, {
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка загрузки данных объекта');
            }
            return response.json();
        })
        .then(object => {
            console.log('Loaded object data:', object);

            // Заполняем форму данными объекта
            const form = document.getElementById('object-form');
            form.querySelector('#object-name').value = object.name;
            form.querySelector('#object-type').value = object.objectType;

            // Если есть родительский объект, загружаем его данные
            if (object.parentId) {
                fetch(`http://localhost:8080/real-estate-objects/${object.parentId}`, {
                    headers: {
                        'Authorization': `Bearer ${jwtToken}`,
                        'Content-Type': 'application/json'
                    }
                })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Ошибка загрузки данных родительского объекта');
                        }
                        return response.json();
                    })
                    .then(parentObject => {
                        const parentSelect = form.querySelector('#parent-select');
                        parentSelect.innerHTML = `<option value="${parentObject.id}">${parentObject.name} (${getObjectTypeName(parentObject.objectType)})</option>`;
                        parentSelect.value = parentObject.id;
                        parentSelect.disabled = true; // Блокируем изменение родителя при редактировании
                    })
                    .catch(error => {
                        console.error('Error loading parent object:', error);
                        const parentSelect = form.querySelector('#parent-select');
                        parentSelect.innerHTML = '<option value="">Ошибка загрузки родительского объекта</option>';
                    });
            } else {
                // Если родителя нет, очищаем селект
                const parentSelect = form.querySelector('#parent-select');
                parentSelect.innerHTML = '<option value="">Без родительского объекта</option>';
                parentSelect.value = '';
            }

            // Устанавливаем обработчик отправки формы
            form.onsubmit = function(e) {
                e.preventDefault();

                const formData = {
                    name: form.querySelector('#object-name').value,
                    objectType: form.querySelector('#object-type').value,
                    parentId: object.parentId // Сохраняем текущего родителя
                };

                console.log('Sending update request with data:', formData);

                fetch(`http://localhost:8080/real-estate-objects/${objectId}`, {
                    method: 'PUT',
                    headers: {
                        'Authorization': `Bearer ${jwtToken}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(formData)
                })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Ошибка обновления объекта');
                        }
                        return response.json();
                    })
                    .then(updatedObject => {
                        console.log('Object updated successfully:', updatedObject);
                        closeObjectModal();

                        // Проверяем, находимся ли мы в деталях объекта
                        const objectsContent = document.getElementById('objects-content');
                        const isInDetailsView = objectsContent.querySelector('.children-section') !== null;

                        if (isInDetailsView) {
                            console.log('Updating children list for parent:', currentParentId);
                            loadChildren(currentParentId);
                        } else {
                            console.log('Updating main objects list');
                            loadObjects();
                        }

                        // Обновляем данные на дашборде
                        loadDashboardData();
                    })
                    .catch(error => {
                        console.error('Error updating object:', error);
                        alert('Ошибка при обновлении объекта: ' + error.message);
                    });
            };
        })
        .catch(error => {
            console.error('Error loading object:', error);
            alert('Ошибка при загрузке данных объекта: ' + error.message);
        });

    // Показываем модальное окно
    document.getElementById('object-modal').style.display = 'block';
}

// Функция для показа модального окна создания объекта (главного)
function showAddObjectModal() {
    const modal = document.getElementById('object-modal');
    document.getElementById('modal-title').textContent = 'Создание объекта';
    document.querySelector('#object-form button[type="submit"]').textContent = 'Создать';

    // Очищаем форму и сбрасываем обработчик
    const form = document.getElementById('object-form');
    const newForm = form.cloneNode(true);
    form.parentNode.replaceChild(newForm, form);
    const currentForm = document.getElementById('object-form');
    currentForm.reset();
    currentForm.onsubmit = null;
    document.getElementById('object-id').value = '';
    document.getElementById('parent-select').disabled = false;

    // Устанавливаем последний выбранный тип объекта, если он есть
    if (lastSelectedObjectType) {
        document.getElementById('object-type').value = lastSelectedObjectType;
    }

    // Загружаем список родительских объектов
    const parentSelect = document.getElementById('parent-select');
    parentSelect.innerHTML = '<option value="">Загрузка...</option>';

    fetch('http://localhost:8080/real-estate-objects', {
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(objects => {
            parentSelect.innerHTML = '<option value="">Без родительского объекта</option>';
            objects.forEach(obj => {
                const option = document.createElement('option');
                option.value = obj.id;
                option.textContent = `${obj.name} (${getObjectTypeName(obj.objectType)})`;
                parentSelect.appendChild(option);
            });
        })
        .catch(error => {
            console.error('Error loading parent objects:', error);
            parentSelect.innerHTML = '<option value="">Ошибка загрузки</option>';
        });

    // Назначаем обработчик для СОЗДАНИЯ
    currentForm.onsubmit = function(e) {
        e.preventDefault();
        const objectName = document.getElementById('object-name').value;
        const objectType = document.getElementById('object-type').value;
        // Сохраняем выбранный тип объекта
        lastSelectedObjectType = objectType;
        const parentId = document.getElementById('parent-select').value;
        const data = { name: objectName, objectType: objectType };
        if (parentId) { data.parentId = parseInt(parentId); }

        fetch('http://localhost:8080/real-estate-objects', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${jwtToken}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        })
            .then(response => {
                if (!response.ok) { return response.text().then(text => { throw new Error(text || 'Ошибка при создании объекта'); }); }
                return response.json();
            })
            .then(data => {
                closeObjectModal();
                loadObjects(); // Обновляем главный список
            })
            .catch(error => {
                console.error('Ошибка при создании объекта:', error);
                alert('Ошибка при создании объекта: ' + error.message);
            });
    };

    modal.style.display = 'block';
}

// Функция для загрузки списка пользователей
function loadUsers() {
    fetch("http://localhost:8080/users/info/all", {
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка загрузки пользователей');
            }
            return response.json();
        })
        .then(users => {
            const container = document.getElementById('users-content');
            container.innerHTML = `
                <div class="data-table users-table">
                    <div class="table-header">
                        <h3 class="table-title">Список пользователей</h3>
                    </div>
                    <div class="table-body">
                        <table class="users-table">
                            <thead>
                                <tr>
                                    <th class="id-cell sortable-header" onclick="sortTable('id')">ID</th>
                                    <th class="name-cell sortable-header" onclick="sortTable('username')">Имя пользователя</th>
                                    <th class="email-cell sortable-header" onclick="sortTable('email')">Email</th>
                                    <th class="name-cell sortable-header" onclick="sortTable('firstName')">Имя</th>
                                    <th class="name-cell sortable-header" onclick="sortTable('lastName')">Фамилия</th>
                                    <th class="role-cell sortable-header" onclick="sortTable('roles')">Роль</th>
                                    <th class="status-cell sortable-header" onclick="sortTable('active')">Статус</th>
                                    <th class="action-cell">Действия</th>
                                </tr>
                            </thead>
                            <tbody id="users-table-body">
                                ${users.length > 0 ? users.map(user => `
                                    <tr>
                                        <td class="id-cell">${user.id}</td>
                                        <td class="name-cell">${user.username}</td>
                                        <td class="email-cell">${user.email || '-'}</td>
                                        <td class="name-cell">${user.firstName || '-'}</td>
                                        <td class="name-cell">${user.lastName || '-'}</td>
                                        <td class="role-cell">
                                            ${user.roles ? (() => {
                let roleClass = 'status-new';
                let roleName = 'Пользователь';

                switch(user.roles) {
                    case 'ROLE_ADMIN':
                        roleClass = 'status-in-progress';
                        roleName = 'Админ';
                        break;
                    case 'ROLE_DIRECTOR':
                        roleClass = 'status-completed';
                        roleName = 'Директор';
                        break;
                    case 'ROLE_CHIEF':
                        roleClass = 'status-in-progress';
                        roleName = 'Начальник';
                        break;
                    case 'ROLE_RESPONSIBLE':
                        roleClass = 'status-new';
                        roleName = 'Ответственный';
                        break;
                }

                return `<span class="status-badge ${roleClass}">${roleName}</span>`;
            })() : '-'}
                                        </td>
                                        <td class="status-cell">
                                            <span class="status-badge ${user.active ? 'status-completed' : 'status-expired'}">
                                                ${user.active ? 'Активен' : 'Заблокирован'}
                                            </span>
                                        </td>
                                        <td class="action-cell">
                                            <div class="action-btn edit-btn" onclick="editUser(${user.id})" title="Редактировать">
                                                <i class="fas fa-edit"></i>
                                            </div>
                                            <div class="action-btn delete-btn" onclick="deleteUser(${user.id})" title="Удалить">
                                                <i class="fas fa-trash"></i>
                                            </div>
                                        </td>
                                    </tr>
                                `).join('') : '<tr><td colspan="8" style="text-align: center;">Нет пользователей</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;

            // Сохраняем данные пользователей для сортировки
            window.usersData = users;
            // Инициализируем сортировку
            initSorting();
        })
        .catch(error => {
            console.error('Ошибка загрузки пользователей:', error);
            const container = document.getElementById('users-content');
            container.innerHTML = '<div class="error">Ошибка загрузки пользователей</div>';
        });
}

// Добавляем функции для сортировки
let currentSort = {
    column: null,
    direction: 'asc'
};

function initSorting() {
    // Устанавливаем начальную сортировку по ID
    sortTable('id');
}

function sortTable(column) {
    const tbody = document.getElementById('users-table-body');
    const headers = document.querySelectorAll('.sortable-header');

    // Сбрасываем классы сортировки у всех заголовков
    headers.forEach(header => {
        header.classList.remove('asc', 'desc');
    });

    // Определяем направление сортировки
    if (currentSort.column === column) {
        currentSort.direction = currentSort.direction === 'asc' ? 'desc' : 'asc';
    } else {
        currentSort.column = column;
        currentSort.direction = 'asc';
    }

    // Добавляем класс сортировки к текущему заголовку
    const currentHeader = document.querySelector(`.sortable-header[onclick*="${column}"]`);
    currentHeader.classList.add(currentSort.direction);

    // Сортируем данные
    const sortedUsers = [...window.usersData].sort((a, b) => {
        let aValue = a[column];
        let bValue = b[column];

        // Обработка специальных случаев
        if (column === 'roles') {
            aValue = getRolePriority(a.roles);
            bValue = getRolePriority(b.roles);
        } else if (column === 'active') {
            aValue = a.active ? 1 : 0;
            bValue = b.active ? 1 : 0;
        }

        // Сравнение значений
        if (aValue === null || aValue === undefined) aValue = '';
        if (bValue === null || bValue === undefined) bValue = '';

        if (aValue < bValue) return currentSort.direction === 'asc' ? -1 : 1;
        if (aValue > bValue) return currentSort.direction === 'asc' ? 1 : -1;
        return 0;
    });

    // Обновляем таблицу
    tbody.innerHTML = sortedUsers.map(user => `
            <tr>
                <td class="id-cell">${user.id}</td>
                <td class="name-cell">${user.username}</td>
                <td class="email-cell">${user.email || '-'}</td>
                <td class="name-cell">${user.firstName || '-'}</td>
                <td class="name-cell">${user.lastName || '-'}</td>
                <td class="role-cell">
                    ${user.roles ? (() => {
        let roleClass = 'status-new';
        let roleName = 'Пользователь';

        switch(user.roles) {
            case 'ROLE_ADMIN':
                roleClass = 'status-in-progress';
                roleName = 'Админ';
                break;
            case 'ROLE_DIRECTOR':
                roleClass = 'status-completed';
                roleName = 'Директор';
                break;
            case 'ROLE_CHIEF':
                roleClass = 'status-in-progress';
                roleName = 'Начальник';
                break;
            case 'ROLE_RESPONSIBLE':
                roleClass = 'status-new';
                roleName = 'Ответственный';
                break;
        }

        return `<span class="status-badge ${roleClass}">${roleName}</span>`;
    })() : '-'}
                </td>
                <td class="status-cell">
                    <span class="status-badge ${user.active ? 'status-completed' : 'status-expired'}">
                        ${user.active ? 'Активен' : 'Заблокирован'}
                    </span>
                </td>
                <td class="action-cell">
                    <div class="action-btn edit-btn" onclick="editUser(${user.id})" title="Редактировать">
                        <i class="fas fa-edit"></i>
                    </div>
                    <div class="action-btn delete-btn" onclick="deleteUser(${user.id})" title="Удалить">
                        <i class="fas fa-trash"></i>
                    </div>
                </td>
            </tr>
        `).join('');
}

function getRolePriority(role) {
    switch(role) {
        case 'ROLE_ADMIN': return 1;
        case 'ROLE_DIRECTOR': return 2;
        case 'ROLE_CHIEF': return 3;
        case 'ROLE_RESPONSIBLE': return 4;
        case 'ROLE_USER': return 5;
        default: return 6;
    }
}

// Функция для удаления пользователя (заглушка)
function deleteUser(userId) {
    if (!confirm('Вы уверены, что хотите удалить этого пользователя?')) {
        return;
    }

    fetch(`http://localhost:8080/users/${userId}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка удаления пользователя');
            }
            loadUsers(); // Обновляем список пользователей после удаления
        })
        .catch(error => {
            console.error('Ошибка при удалении пользователя:', error);
            alert('Ошибка при удалении пользователя: ' + error.message);
        });
}

// Функция для открытия модального окна редактирования пользователя
function editUser(userId) {
    const modal = document.getElementById('edit-user-modal');

    // Загружаем данные пользователя
    fetch(`http://localhost:8080/users/info/${userId}`, {
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка загрузки данных пользователя');
            }
            return response.json();
        })
        .then(user => {
            // Заполняем форму данными пользователя
            document.getElementById('edit-username').value = user.username;
            document.getElementById('edit-email').value = user.email || '';
            document.getElementById('edit-firstname').value = user.firstName || '';
            document.getElementById('edit-lastname').value = user.lastName || '';
            document.getElementById('edit-roles').value = user.roles || 'ROLE_USER';
            document.getElementById('edit-active').value = user.active ? 'true' : 'false';

            // Обработчик отправки формы
            document.getElementById('edit-user-form').onsubmit = function(e) {
                e.preventDefault();

                const firstName = document.getElementById('edit-firstname').value;
                const lastName = document.getElementById('edit-lastname').value;
                const email = document.getElementById('edit-email').value;
                const roles = document.getElementById('edit-roles').value;
                const active = document.getElementById('edit-active').value === 'true';

                const updatePromises = [];
                const updateTypes = [];

                if (firstName !== user.firstName) {
                    updatePromises.push(
                        fetch(`http://localhost:8080/users/update/${userId}/first-name`, {
                            method: 'PUT',
                            headers: {
                                'Authorization': `Bearer ${jwtToken}`,
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({ firstName })
                        })
                    );
                    updateTypes.push('имя');
                }

                if (lastName !== user.lastName) {
                    updatePromises.push(
                        fetch(`http://localhost:8080/users/update/${userId}/last-name`, {
                            method: 'PUT',
                            headers: {
                                'Authorization': `Bearer ${jwtToken}`,
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({ lastName })
                        })
                    );
                    updateTypes.push('фамилию');
                }

                if (email !== user.email) {
                    updatePromises.push(
                        fetch(`http://localhost:8080/users/update/${userId}/email`, {
                            method: 'PUT',
                            headers: {
                                'Authorization': `Bearer ${jwtToken}`,
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({ email })
                        })
                    );
                    updateTypes.push('email');
                }

                if (roles !== user.roles) {
                    updatePromises.push(
                        fetch(`http://localhost:8080/users/update/${userId}/role`, {
                            method: 'PUT',
                            headers: {
                                'Authorization': `Bearer ${jwtToken}`,
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({ role: roles })
                        })
                    );
                    updateTypes.push('роль');
                }

                if (active !== user.active) {
                    updatePromises.push(
                        fetch(`http://localhost:8080/users/update/${userId}/active`, {
                            method: 'PUT',
                            headers: {
                                'Authorization': `Bearer ${jwtToken}`,
                                'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({ active })
                        })
                    );
                    updateTypes.push('статус активности');
                }

                if (updatePromises.length === 0) {
                    showNotification('error', 'Нет изменений', 'Не было внесено никаких изменений для сохранения');
                    return;
                }

                Promise.all(updatePromises)
                    .then(responses => {
                        const allSuccessful = responses.every(response => response.ok);
                        if (allSuccessful) {
                            const updatedFields = updateTypes.join(', ');
                            showNotification('success', 'Успешно', `Обновлены поля: ${updatedFields}`);
                            document.getElementById('edit-user-modal').style.display = 'none';
                            loadUsers();
                        } else {
                            throw new Error('Ошибка при обновлении данных');
                        }
                    })
                    .catch(error => {
                        console.error('Ошибка при обновлении данных:', error);
                        const failedFields = updateTypes.join(', ');
                        showNotification('error', 'Ошибка', `Не удалось обновить поля: ${failedFields}`);
                    });
            };

            modal.style.display = 'block';
        })
        .catch(error => {
            console.error('Ошибка загрузки данных пользователя:', error);
            alert('Ошибка загрузки данных пользователя: ' + error.message);
        });
}

// Функция для закрытия модального окна редактирования пользователя
function closeEditUserModal() {
    const modal = document.getElementById('edit-user-modal');
    modal.style.display = 'none';
}

// Функция для отображения уведомлений
function showNotification(type, title, message) {
    const container = document.getElementById('notification-container');
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;

    const icon = type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle';

    notification.innerHTML = `
            <i class="fas ${icon}"></i>
            <div class="notification-content">
                <div class="notification-title">${title}</div>
                <div class="notification-message">${message}</div>
            </div>
            <button class="notification-close" onclick="this.parentElement.remove()">
                <i class="fas fa-times"></i>
            </button>
        `;

    container.appendChild(notification);

    // Автоматическое удаление уведомления через 5 секунд
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => notification.remove(), 300);
    }, 5000);
}

// Функция загрузки профиля
function loadProfile() {
    const container = document.getElementById('profile-content');
    container.innerHTML = `
            <div class="profile-container">
                <div class="profile-sidebar">
                    <div class="profile-card">
                        <div class="profile-avatar" id="profile-avatar">JD</div>
                        <h2 class="profile-name" id="profile-name">John Doe</h2>
                        <div class="profile-role" id="profile-role">Пользователь</div>
                        <div class="profile-stats">
                            <div class="stat-item">
                                <div class="stat-value" id="objects-count">0</div>
                                <div class="stat-label">Объектов</div>
                            </div>
                            <div class="stat-item">
                                <div class="stat-value" id="tasks-count">0</div>
                                <div class="stat-label">Задач</div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="profile-content">
                    <div class="profile-section">
                        <div class="profile-section-title">
                            <span>Личная информация</span>
                        </div>
                        <div class="profile-form" id="profile-form">
                            <div class="form-row">
                                <div class="form-group">
                                    <label class="form-label">Имя пользователя</label>
                                    <input type="text" class="form-input" id="profile-username" disabled>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Email</label>
                                    <input type="email" class="form-input" id="profile-email">
                                </div>
                            </div>
                            <div class="form-row">
                                <div class="form-group">
                                    <label class="form-label">Имя</label>
                                    <input type="text" class="form-input" id="profile-firstname">
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Фамилия</label>
                                    <input type="text" class="form-input" id="profile-lastname">
                                </div>
                            </div>
                            <div class="profile-actions">
                                <button class="btn btn-primary" onclick="saveProfile()">
                                    <i class="fas fa-save"></i>
                                    Сохранить
                                </button>
                                <button class="btn change-password-btn" onclick="showChangePasswordModal()">
                                    <i class="fas fa-key"></i>
                                    Сменить пароль
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;

    // Загружаем данные пользователя
    fetch("http://localhost:8080/users/info", {
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка загрузки данных профиля');
            }
            return response.json();
        })
        .then(user => {
            // Обновляем информацию в профиле
            document.getElementById('profile-avatar').textContent = getInitials(user.username);
            document.getElementById('profile-name').textContent = user.username;
            document.getElementById('profile-role').textContent = getRoleName(user.roles);
            document.getElementById('profile-username').value = user.username;
            document.getElementById('profile-email').value = user.email || '';
            document.getElementById('profile-firstname').value = user.firstName || '';
            document.getElementById('profile-lastname').value = user.lastName || '';

            // Загружаем статистику
            loadProfileStats();
        })
        .catch(error => {
            console.error('Ошибка загрузки профиля:', error);
            showNotification('error', 'Ошибка', 'Не удалось загрузить данные профиля');
        });
}

// Функция загрузки статистики профиля
function loadProfileStats() {
    // Загружаем количество объектов
    fetch("http://localhost:8080/real-estate-objects", {
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(objects => {
            document.getElementById('objects-count').textContent = objects.length;
        })
        .catch(error => {
            console.error('Ошибка загрузки объектов:', error);
        });

    // Загружаем количество задач
    fetch("http://localhost:8080/tasks", {
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(tasks => {
            document.getElementById('tasks-count').textContent = tasks.length;
        })
        .catch(error => {
            console.error('Ошибка загрузки задач:', error);
        });
}

// Функция получения названия роли
function getRoleName(role) {
    switch(role) {
        case 'ROLE_ADMIN':
            return 'Администратор';
        case 'ROLE_DIRECTOR':
            return 'Директор';
        case 'ROLE_CHIEF':
            return 'Начальник';
        case 'ROLE_RESPONSIBLE':
            return 'Ответственный';
        case 'ROLE_USER':
            return 'Пользователь';
        default:
            return 'Пользователь';
    }
}

// Функция редактирования профиля
function editProfile() {
    const inputs = document.querySelectorAll('#profile-form input:not([disabled])');
    inputs.forEach(input => {
        input.disabled = false;
    });
}

// Функция сохранения профиля
function saveProfile() {
    const email = document.getElementById('profile-email').value;
    const firstName = document.getElementById('profile-firstname').value;
    const lastName = document.getElementById('profile-lastname').value;

    const updatePromises = [];
    const updateTypes = [];

    if (email !== currentUser.email) {
        updatePromises.push(
            fetch(`http://localhost:8080/users/update/${currentUser.id}/email`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${jwtToken}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email })
            })
        );
        updateTypes.push('email');
    }

    if (firstName !== currentUser.firstName) {
        updatePromises.push(
            fetch(`http://localhost:8080/users/update/${currentUser.id}/first-name`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${jwtToken}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ firstName })
            })
        );
        updateTypes.push('имя');
    }

    if (lastName !== currentUser.lastName) {
        updatePromises.push(
            fetch(`http://localhost:8080/users/update/${currentUser.id}/last-name`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${jwtToken}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ lastName })
            })
        );
        updateTypes.push('фамилию');
    }

    if (updatePromises.length === 0) {
        showNotification('info', 'Нет изменений', 'Не было внесено никаких изменений');
        return;
    }

    Promise.all(updatePromises)
        .then(responses => {
            const allSuccessful = responses.every(response => response.ok);
            if (allSuccessful) {
                const updatedFields = updateTypes.join(', ');
                showNotification('success', 'Успешно', `Обновлены поля: ${updatedFields}`);
                loadUserData(); // Обновляем данные пользователя
            } else {
                throw new Error('Ошибка при обновлении данных');
            }
        })
        .catch(error => {
            console.error('Ошибка при обновлении профиля:', error);
            showNotification('error', 'Ошибка', 'Не удалось обновить профиль');
        });
}

// Функция показа модального окна смены пароля
function showChangePasswordModal() {
    const modal = document.createElement('div');
    modal.className = 'modal';
    modal.id = 'change-password-modal';
    modal.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h3>Смена пароля</h3>
                    <button class="close-btn" onclick="closeChangePasswordModal()">&times;</button>
                </div>
                <form id="change-password-form" onsubmit="changePassword(event)">
                    <div class="form-group">
                        <label class="form-label" for="current-password">Текущий пароль</label>
                        <input type="password" class="form-input" id="current-password" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="new-password">Новый пароль</label>
                        <input type="password" class="form-input" id="new-password" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="confirm-password">Подтвердите пароль</label>
                        <input type="password" class="form-input" id="confirm-password" required>
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">Сменить пароль</button>
                    </div>
                </form>
            </div>
        `;
    document.body.appendChild(modal);
    modal.style.display = 'block';
}

// Функция закрытия модального окна смены пароля
function closeChangePasswordModal() {
    const modal = document.getElementById('change-password-modal');
    if (modal) {
        modal.remove();
    }
}

// Функция смены пароля
function changePassword(event) {
    event.preventDefault();

    const currentPassword = document.getElementById('current-password').value;
    const newPassword = document.getElementById('new-password').value;
    const confirmPassword = document.getElementById('confirm-password').value;

    if (newPassword !== confirmPassword) {
        showNotification('error', 'Ошибка', 'Новые пароли не совпадают');
        return;
    }

    fetch(`http://localhost:8080/users/update/${currentUser.id}/password`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            currentPassword,
            newPassword
        })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка при смене пароля');
            }
            showNotification('success', 'Успешно', 'Пароль успешно изменен');
            closeChangePasswordModal();
        })
        .catch(error => {
            console.error('Ошибка при смене пароля:', error);
            showNotification('error', 'Ошибка', 'Не удалось изменить пароль');
        });
}

// Функция для показа модального окна назначения ответственного
function showAssignResponsibleModal(objectId) {
    const modal = document.getElementById('assign-responsible-modal');
    modal.style.display = 'block';

    // Сохраняем ID объекта в модальном окне
    modal.dataset.objectId = objectId;

    // Загружаем список пользователей
    fetch("http://localhost:8080/users/info/all", {
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(users => {
            const select = document.getElementById('responsible-user-select');
            select.innerHTML = '<option value="">Выберите пользователя</option>' +
                users.map(user => `
                    <option value="${user.id}">
                        ${user.firstName} ${user.lastName} (${getRoleName(user.roles)})
                    </option>
                `).join('');
        });
}

// Функция для закрытия модального окна назначения ответственного
function closeAssignResponsibleModal() {
    document.getElementById('assign-responsible-modal').style.display = 'none';
}

// Функция для назначения ответственного пользователя
function assignResponsibleUser() {
    const modal = document.getElementById('assign-responsible-modal');
    const objectId = modal.dataset.objectId;
    const userId = document.getElementById('responsible-user-select').value;

    if (!userId) {
        alert('Пожалуйста, выберите пользователя');
        return;
    }

    fetch(`http://localhost:8080/real-estate-objects/${objectId}/assign-responsible/${userId}`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || 'Ошибка при назначении ответственного');
                });
            }
            return response.json();
        })
        .then(updatedObject => {
            closeAssignResponsibleModal();
            showObjectDetails(objectId); // Обновляем детали объекта
            showNotification('Ответственный пользователь успешно назначен', 'success');
        })
        .catch(error => {
            console.error('Ошибка при назначении ответственного:', error);
            showNotification('Ошибка при назначении ответственного: ' + error.message, 'error');
        });
}

// Функция для удаления ответственного пользователя
function removeResponsibleUser(objectId) {
    if (!confirm('Вы уверены, что хотите удалить ответственного пользователя?')) {
        return;
    }

    fetch(`http://localhost:8080/real-estate-objects/${objectId}/remove-responsible`, {
        method: 'PUT',
        headers: {
            'Authorization': `Bearer ${jwtToken}`,
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || 'Ошибка при удалении ответственного');
                });
            }
            return response.json();
        })
        .then(updatedObject => {
            showObjectDetails(objectId); // Обновляем детали объекта
        })
        .catch(error => {
            console.error('Ошибка при удалении ответственного:', error);
            alert('Ошибка при удалении ответственного: ' + error.message);
        });
}

function getRoleClass(role) {
    switch(role) {
        case 'ROLE_ADMIN':
            return 'status-in-progress';
        case 'ROLE_DIRECTOR':
            return 'status-completed';
        case 'ROLE_CHIEF':
            return 'status-in-progress';
        case 'ROLE_RESPONSIBLE':
            return 'status-new';
        case 'ROLE_USER':
            return 'status-new';
        default:
            return 'status-new';
    }
}

function loadTasks() {
    fetch('http://localhost:8080/tasks', {
        headers: {
            'Authorization': `Bearer ${jwtToken}`
        }
    })
        .then(response => response.json())
        .then(tasks => {
            const container = document.getElementById('tasks-content');
            container.innerHTML = `
                <div class="data-table tasks-table">
                    <div class="table-header">
                        <h3 class="table-title">Задачи</h3>
                        <div class="table-actions">
                            <button class="btn btn-primary" onclick="showCreateTaskModal()">
                                <i class="fas fa-plus"></i> Создать задачу
                            </button>
                        </div>
                    </div>
                    // ... existing code ...
                </div>
            `;
        });
}

function loadObjectTasks(objectId) {
    fetch(`http://localhost:8080/tasks/by-object/${objectId}`, {
        headers: {
            'Authorization': `Bearer ${jwtToken}`
        }
    })
        .then(response => response.json())
        .then(tasks => {
            const container = document.getElementById('object-tasks');
            container.innerHTML = `
                <div class="data-table tasks-table">
                    <div class="table-header">
                        <h3 class="table-title">Задачи объекта</h3>
                        <div class="table-actions">
                            <button class="btn btn-primary" onclick="showCreateTaskModal()">
                                <i class="fas fa-plus"></i> Создать задачу
                            </button>
                        </div>
                    </div>
                    // ... existing code ...
                </div>
            `;
        });
}
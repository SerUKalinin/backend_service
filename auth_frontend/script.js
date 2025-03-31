const API_URL = "http://localhost:8080/auth"; // Бэк работает на 8080

// Регистрация
document.getElementById("register-form")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const username = document.getElementById("username").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    const response = await fetch(`${API_URL}/register-user`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, email, password })
    });

    if (response.ok) {
        alert("Регистрация успешна! Проверьте почту.");
        window.location.href = "verify-email.html";
    } else {
        alert("Ошибка регистрации.");
    }
});

// Вход
document.getElementById("login-form")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const login = document.getElementById("login").value;
    const password = document.getElementById("password").value;

    const response = await fetch(`${API_URL}/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: login, password })
    });

    if (response.ok) {
        const data = await response.json();
        document.cookie = `token=${data.jwtToken}; path=/; HttpOnly`;
        window.location.href = "index.html";
    } else {
        alert("Ошибка входа.");
    }
});

// Подтверждение email
document.getElementById("verify-form")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    const email = document.getElementById("verify-email").value;
    const code = document.getElementById("verify-code").value;

    const response = await fetch(`${API_URL}/verify-email`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, code })
    });

    if (response.ok) {
        alert("Email подтверждён!");
        window.location.href = "login.html";
    } else {
        alert("Ошибка подтверждения.");
    }
});
document.addEventListener("DOMContentLoaded", function () {
    loadUserInfo();
    loadObjects();
});

// Получение данных пользователя
function loadUserInfo() {
    fetch("http://localhost:8080/users/info", {
        method: "GET",
        headers: { "Authorization": `Bearer ${getCookie("jwt")}` }
    })
        .then(response => response.json())
        .then(user => {
            document.getElementById("username").textContent = user.username;
            document.getElementById("email").textContent = user.email;

            // Если администратор, загружаем список пользователей
            if (user.role === "ADMIN") {
                document.getElementById("adminSection").style.display = "block";
                loadUsers();
            }
        })
        .catch(error => console.error("Ошибка загрузки данных пользователя:", error));
}

// Добавление нового объекта
function openCreateObjectModal() {
    document.getElementById("createObjectModal").style.display = "flex";
}

function closeCreateObjectModal() {
    document.getElementById("createObjectModal").style.display = "none";
}

function createObject() {
    const name = document.getElementById("objectName").value;
    const type = document.getElementById("objectType").value;

    if (!name.trim()) {
        alert("Введите название объекта!");
        return;
    }

    fetch("http://localhost:8080/real-estate-objects", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${getCookie("jwt")}`
        },
        body: JSON.stringify({ name, type })
    })
        .then(response => response.json())
        .then(() => {
            loadObjects();
            closeCreateObjectModal();
        })
        .catch(error => console.error("Ошибка при создании объекта:", error));
}

// Загрузка объектов недвижимости
function loadObjects() {
    fetch("http://localhost:8080/real-estate-objects", {
        method: "GET",
        headers: { "Authorization": `Bearer ${getCookie("jwt")}` }
    })
        .then(response => response.json())
        .then(objects => {
            const objectsList = document.getElementById("objectsList");
            objectsList.innerHTML = "";
            objects.forEach(object => {
                const objectItem = document.createElement("div");
                objectItem.innerHTML = `
                <p><strong>${object.name}</strong> (${object.type})</p>
                <button onclick="deleteObject(${object.id})">Удалить</button>
            `;
                objectsList.appendChild(objectItem);
            });
        })
        .catch(error => console.error("Ошибка загрузки объектов:", error));
}

// Получение JWT из куки
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(";").shift();
}

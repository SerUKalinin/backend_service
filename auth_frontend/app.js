const API_URL = "http://localhost:8080/auth";

// Регистрация пользователя
document.getElementById("registerForm")?.addEventListener("submit", async (e) => {
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
        window.location.href = `verify.html?email=${email}`;
    } else {
        alert("Ошибка регистрации");
    }
});

// Вход пользователя
document.getElementById("loginForm")?.addEventListener("submit", async (e) => {
    e.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    const response = await fetch(`${API_URL}/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
    });

    if (response.ok) {
        const data = await response.json();
        localStorage.setItem("token", data.token);  // Сохраняем токен
        window.location.href = "profile.html";
    } else {
        alert("Ошибка входа");
    }
});


// Подтверждение почты
async function verifyEmail() {
    const urlParams = new URLSearchParams(window.location.search);
    const email = urlParams.get("email");
    const code = document.getElementById("verificationCode").value;

    const response = await fetch(`${API_URL}/verify-email`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, code })
    });

    if (response.ok) {
        window.location.href = "login.html";
    } else {
        document.getElementById("message").innerText = "Ошибка подтверждения";
    }
}

// Выход
function logout() {
    fetch(`${API_URL}/logout`, {
        method: "POST",
        headers: { "Authorization": `Bearer ${localStorage.getItem("token")}` }
    }).finally(() => {
        localStorage.removeItem("token");  // Удаляем токен
        window.location.href = "login.html";
    });
}


// Загрузка профиля
async function loadProfile() {
    const token = localStorage.getItem("token");
    if (!token) {
        window.location.href = "login.html";
        return;
    }

    const response = await fetch(`${API_URL}/profile`, {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${token}`  // Передаём токен
        }
    });

    if (response.ok) {
        const user = await response.json();
        document.getElementById("userInfo").innerHTML = `Логин: ${user.username} <br> Email: ${user.email}`;
    } else {
        localStorage.removeItem("token");
        window.location.href = "login.html";
    }


    // Функция загрузки объектов недвижимости
    async function loadObjects() {
        const token = localStorage.getItem("token");
        if (!token) {
            window.location.href = "login.html";
            return;
        }

        const response = await fetch(`${API_URL}/real-estate-objects`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        if (response.ok) {
            const objects = await response.json();
            const table = document.getElementById("objectsTable");
            table.innerHTML = "";
            objects.forEach(obj => {
                table.innerHTML += `
                <tr>
                    <td>${obj.id}</td>
                    <td>${obj.name}</td>
                    <td>${obj.objectType}</td>
                    <td>${obj.parent ? obj.parent.id : "—"}</td>
                    <td>
                        <button onclick="deleteObject(${obj.id})">Удалить</button>
                    </td>
                </tr>
            `;
            });
        } else {
            alert("Ошибка загрузки объектов");
        }
    }

// Форма создания объекта
    document.getElementById("objectForm")?.addEventListener("submit", async (e) => {
        e.preventDefault();

        const name = document.getElementById("objectName").value;
        const objectType = document.getElementById("objectType").value;
        const parentId = document.getElementById("parentId").value || null;

        const token = localStorage.getItem("token");
        if (!token) {
            alert("Вы не авторизованы!");
            return;
        }

        const response = await fetch(`${API_URL}/real-estate-objects`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({ name, objectType, parent: parentId ? { id: parentId } : null })
        });

        if (response.ok) {
            loadObjects(); // Перезагрузить список
            document.getElementById("objectForm").reset();
        } else {
            alert("Ошибка при создании объекта");
        }
    });

// Удаление объекта
    async function deleteObject(id) {
        const token = localStorage.getItem("token");
        if (!token) {
            alert("Вы не авторизованы!");
            return;
        }

        const confirmed = confirm("Вы уверены, что хотите удалить объект?");
        if (!confirmed) return;

        const response = await fetch(`${API_URL}/real-estate-objects/${id}`, {
            method: "DELETE",
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (response.ok) {
            loadObjects(); // Перезагрузить список
        } else {
            alert("Ошибка при удалении объекта");
        }
    }
}
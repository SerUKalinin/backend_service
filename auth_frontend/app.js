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
}
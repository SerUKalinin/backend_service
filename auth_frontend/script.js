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

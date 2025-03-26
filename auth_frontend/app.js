// Регистрация пользователя
const registerForm = document.getElementById('register-form');
if (registerForm) {
    registerForm.addEventListener('submit', function (event) {
        event.preventDefault();

        const userSignupDto = {
            username: document.getElementById('username').value, // Изменил на username
            email: document.getElementById('email').value,
            password: document.getElementById('password').value
        };

        fetch('http://localhost:8080/auth/register-user', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userSignupDto)
        })
            .then(response => {
                if (response.ok) {
                    alert('Регистрация успешна. Пожалуйста, подтвердите ваш email.');
                    window.location.href = 'verify.html'; // Перенаправляем на страницу подтверждения
                } else {
                    alert('Ошибка регистрации.');
                }
            })
            .catch(error => {
                console.error('Ошибка:', error);
                alert('Произошла ошибка при отправке запроса.');
            });
    });
}

// Подтверждение email
const verifyForm = document.getElementById('verify-form');
if (verifyForm) {
    verifyForm.addEventListener('submit', function (event) {
        event.preventDefault();

        const emailVerificationDto = {
            email: document.getElementById('verify-email').value,
            code: document.getElementById('verify-code').value
        };

        fetch('http://localhost:8080/auth/verify-email', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(emailVerificationDto)
        })
            .then(response => {
                if (response.ok) {
                    alert('Email подтвержден. Переход на страницу входа.');
                    window.location.href = 'login.html'; // Перенаправляем на страницу входа
                } else {
                    alert('Ошибка подтверждения email.');
                }
            })
            .catch(error => {
                console.error('Ошибка:', error);
                alert('Произошла ошибка при подтверждении email.');
            });
    });
}

// Вход в систему
const loginForm = document.getElementById('login-form');
if (loginForm) {
    loginForm.addEventListener('submit', function (event) {
        event.preventDefault();

        const loginDto = {
            username: document.getElementById('username').value, // Используем поле username для логина или email
            password: document.getElementById('login-password').value
        };

        fetch('http://localhost:8080/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginDto)
        })
            .then(response => {
                if (!response.ok) { // Если ответ не успешный
                    return response.text().then(text => {
                        throw new Error(text || 'Ошибка при входе в систему');
                    });
                }
                return response.json(); // Преобразуем ответ в JSON
            })
            .then(data => {
                if (data && data.token) {
                    alert('Авторизация успешна!');
                    localStorage.setItem('jwtToken', data.token); // Сохраняем JWT в localStorage
                    window.location.href = 'profile.html'; // Переход на страницу профиля
                } else {
                    alert('Неверные данные для входа.');
                }
            })
            .catch(error => {
                console.error('Ошибка:', error); // Печать ошибки в консоль
                alert('Произошла ошибка при авторизации: ' + error.message); // Сообщение пользователю
            });
    });
}


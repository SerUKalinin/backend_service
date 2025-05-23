# Backend Service

## Описание проекта
Backend Service - это сервис, разработанный на Spring Boot, обеспечивающий функциональность для управления пользователями, объектами недвижимости и задачами. Сервис предоставляет REST API для аутентификации, управления пользователями, объектами недвижимости и задачами.

## Архитектура
Сервис построен по модульной архитектуре с четким разделением ответственности между компонентами:

### Основные модули
1. **Аутентификация и авторизация**
   - JWT аутентификация
   - Управление сессиями через Redis
   - Ролевая авторизация

2. **Управление пользователями**
   - Регистрация и профили пользователей
   - Роли и права доступа
   - Управление учетными данными

3. **Управление объектами недвижимости**
   - Создание и управление объектами
   - Иерархия объектов
   - Назначение ответственных

4. **Управление задачами**
   - Создание и управление задачами
   - Управление статусами

5. **Уведомления**
   - Отправка уведомлений

## Основные компоненты

### Модели данных
- `User` - модель пользователя с полями для хранения учетных данных и информации
- `Role` - модель ролей пользователей
- `Task` - модель задач
- `TaskAttachment` - модель вложений к задачам
- `ObjectEntity` - модель объектов
- `ObjectType` - типы объектов
- `PasswordResetToken` - токен для сброса пароля
- `TaskStatus` - статусы задач

### Конфигурация
- `WebConfig` - конфигурация веб-слоя
- `ModelMapperConfig` - конфигурация маппинга моделей
- `SecurityConfig` - конфигурация безопасности
- `RedisConfig` - конфигурация Redis

### Сервисы
- Сервисы для работы с пользователями
- Сервисы для работы с задачами
- Сервисы для работы с объектами
- Сервисы аутентификации и авторизации

### Репозитории
- Репозитории для работы с сущностями в базе данных

### DTO
- Data Transfer Objects для передачи данных между слоями приложения

### Аспекты
- Аспекты для обработки cross-cutting concerns

### Аннотации
- Пользовательские аннотации для метаданных

## Технологии
- Java 17
- Spring Boot
- Spring Security
- Redis
- PostgreSQL
- Gradle
- Docker
- Docker Compose

## Запуск проекта
1. Убедитесь, что у вас установлены:
   - Java 17 или выше
   - Docker и Docker Compose
   - Gradle

2. Клонируйте репозиторий:
```bash
git clone https://github.com/SerUKalinin/backend_service
```

3. Запустите приложение:
```bash
docker-compose up -d
```

## API Endpoints
### Аутентификация
- POST /auth/register-user - регистрация нового пользователя
- POST /auth/register-admin - регистрация нового администратора
- POST /auth/login - вход в систему
- POST /auth/logout - выход из системы
- POST /auth/verify-email - подтверждение email
- POST /auth/resend-verification - повторная отправка кода подтверждения
- POST /auth/refresh - обновление JWT-токена
- POST /auth/forgot-password - запрос на сброс пароля
- POST /auth/reset-password - сброс пароля
- GET /auth/validate - проверка валидности токена

### Пользователи
#### Получение информации
- GET /users/info - получение информации о текущем пользователе
- GET /users/info/all - получение списка всех пользователей (только для администраторов)
- GET /users/info/{id} - получение информации о пользователе по ID (только для администраторов)

#### Обновление данных
- PUT /users/update/{userId}/first-name - обновление имени пользователя
- PUT /users/update/{userId}/last-name - обновление фамилии пользователя
- PUT /users/update/{userId}/email - обновление email пользователя
- PUT /users/update/{userId}/role - обновление роли пользователя (только для администраторов)
- PUT /users/update/{userId}/active - обновление статуса активности пользователя (только для администраторов)

#### Удаление
- DELETE /users/{id} - удаление пользователя (только для администраторов)

### Объекты недвижимости
- GET /real-estate-objects - получение списка всех объектов
- GET /real-estate-objects/{id} - получение объекта по ID
- GET /real-estate-objects/by-type - получение объектов по типу
- GET /real-estate-objects/{id}/children - получение дочерних объектов
- POST /real-estate-objects - создание нового объекта
- PUT /real-estate-objects/{id} - обновление объекта
- DELETE /real-estate-objects/{id} - удаление объекта
- GET /real-estate-objects/my-objects - получение объектов текущего пользователя
- GET /real-estate-objects/by-responsible/{userId} - получение объектов по ответственному пользователю
- PUT /real-estate-objects/{id}/assign-responsible/{userId} - назначение ответственного пользователя
- PUT /real-estate-objects/{id}/remove-responsible - удаление ответственного пользователя

### Задачи
- POST /tasks - создание новой задачи
- GET /tasks - получение списка всех задач
- GET /tasks/{id} - получение задачи по ID
- PUT /tasks/{id} - обновление задачи
- DELETE /tasks/{id} - удаление задачи

## Безопасность
- JWT аутентификация
- Ролевая авторизация
- Защита от CSRF
- Хеширование паролей
- Защита от брутфорса
- Rate limiting
- Валидация входных данных
- Безопасная обработка файлов

## Мониторинг
- Логирование через SLF4J
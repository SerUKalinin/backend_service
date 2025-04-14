-- Добавляем колонку для ответственного пользователя
ALTER TABLE objects ADD COLUMN responsible_user_id BIGINT REFERENCES users(id);

-- Создаем индекс для оптимизации поиска по ответственному пользователю
CREATE INDEX idx_objects_responsible_user_id ON objects(responsible_user_id); 
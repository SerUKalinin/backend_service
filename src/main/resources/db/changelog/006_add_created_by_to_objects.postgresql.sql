-- Добавление поля created_by в таблицу objects
ALTER TABLE objects ADD COLUMN created_by BIGINT REFERENCES users(id) ON DELETE SET NULL; 
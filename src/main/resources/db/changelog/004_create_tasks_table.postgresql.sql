-- Создаём таблицу tasks
CREATE TABLE tasks (
                       id SERIAL PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       status VARCHAR(50) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                       deadline TIMESTAMP,
                       object_id BIGINT NOT NULL,
                       CONSTRAINT fk_tasks_object FOREIGN KEY (object_id) REFERENCES objects (id) ON DELETE CASCADE
);

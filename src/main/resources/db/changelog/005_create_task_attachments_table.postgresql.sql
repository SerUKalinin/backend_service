-- Таблица для файлов, прикреплённых к задачам
CREATE TABLE task_attachments (
                                  id SERIAL PRIMARY KEY,
                                  task_id BIGINT NOT NULL,
                                  file_path TEXT NOT NULL,
                                  uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                  CONSTRAINT fk_task_attachments_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE
);

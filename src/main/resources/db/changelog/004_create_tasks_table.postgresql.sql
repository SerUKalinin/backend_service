CREATE TABLE tasks (
                       id SERIAL PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       status VARCHAR(50) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                       deadline TIMESTAMP,
                       object_id BIGINT NOT NULL,
                       created_by_id BIGINT,
                       responsible_user_id BIGINT,
                       CONSTRAINT fk_tasks_object FOREIGN KEY (object_id) REFERENCES objects (id) ON DELETE CASCADE,
                       CONSTRAINT fk_tasks_created_by FOREIGN KEY (created_by_id) REFERENCES users (id),
                       CONSTRAINT fk_tasks_responsible_user FOREIGN KEY (responsible_user_id) REFERENCES users (id)
);

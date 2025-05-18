ALTER TABLE tasks ADD COLUMN created_by_id BIGINT;
ALTER TABLE tasks ADD CONSTRAINT fk_tasks_created_by FOREIGN KEY (created_by_id) REFERENCES users(id);
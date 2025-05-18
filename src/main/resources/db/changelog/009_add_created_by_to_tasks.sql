ALTER TABLE tasks ADD COLUMN created_by_id BIGINT;
ALTER TABLE tasks ADD CONSTRAINT fk_tasks_created_by FOREIGN KEY (created_by_id) REFERENCES users(id);
ALTER TABLE tasks ADD COLUMN responsible_user_id BIGINT;
ALTER TABLE tasks ADD CONSTRAINT fk_task_responsible_user FOREIGN KEY (responsible_user_id) REFERENCES users(id);
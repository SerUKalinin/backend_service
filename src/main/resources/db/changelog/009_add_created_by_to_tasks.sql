-- db/changelog/009_update_tasks_and_attachments.sql

-- ========================= TASKS =========================

-- Добавляем колонку created_by_id, если не существует
ALTER TABLE tasks
    ADD COLUMN IF NOT EXISTS created_by_id BIGINT;

-- Добавляем внешний ключ для created_by_id, если не существует
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.table_constraints tc
                     JOIN information_schema.key_column_usage kcu
                          ON tc.constraint_name = kcu.constraint_name
            WHERE tc.table_name = 'tasks'
              AND tc.constraint_type = 'FOREIGN KEY'
              AND kcu.column_name = 'created_by_id'
        ) THEN
            ALTER TABLE tasks
                ADD CONSTRAINT fk_tasks_created_by FOREIGN KEY (created_by_id) REFERENCES users(id);
        END IF;
    END $$;

-- Добавляем колонку responsible_user_id, если не существует
ALTER TABLE tasks
    ADD COLUMN IF NOT EXISTS responsible_user_id BIGINT;

-- Добавляем внешний ключ для responsible_user_id, если не существует
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.table_constraints tc
                     JOIN information_schema.key_column_usage kcu
                          ON tc.constraint_name = kcu.constraint_name
            WHERE tc.table_name = 'tasks'
              AND tc.constraint_type = 'FOREIGN KEY'
              AND kcu.column_name = 'responsible_user_id'
        ) THEN
            ALTER TABLE tasks
                ADD CONSTRAINT fk_tasks_responsible_user FOREIGN KEY (responsible_user_id) REFERENCES users(id);
        END IF;
    END $$;

-- ========================= TASK_ATTACHMENTS =========================

-- Добавляем колонку original_file_name, если не существует
ALTER TABLE task_attachments
    ADD COLUMN IF NOT EXISTS original_file_name VARCHAR(255) NOT NULL DEFAULT '';

-- Добавляем колонку size, если не существует
ALTER TABLE task_attachments
    ADD COLUMN IF NOT EXISTS size BIGINT NOT NULL DEFAULT 0;

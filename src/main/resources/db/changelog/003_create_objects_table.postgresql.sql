CREATE TABLE objects (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,    -- Название объекта
    object_type VARCHAR(50) NOT NULL,    -- Тип объекта (проект, этаж, квартира, задача и т.д.)
    parent_id   BIGINT REFERENCES objects(id) ON DELETE CASCADE, -- Родительский объект
    created_at  TIMESTAMP DEFAULT now(), -- Дата создания
    updated_at  TIMESTAMP DEFAULT now()  -- Дата обновления
);

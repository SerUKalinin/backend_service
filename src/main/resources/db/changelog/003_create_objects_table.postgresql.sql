CREATE TABLE objects (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,    -- Название объекта
    object_type VARCHAR(50) NOT NULL,    -- Тип объекта (здание, подъезд, этаж, лестничный пролет, лифт, коридор, квартира, комната, задача и т.д.)
    parent_id   BIGINT REFERENCES objects(id) ON DELETE CASCADE, -- Родительский объект
    created_at  TIMESTAMP DEFAULT now(), -- Дата создания
    updated_at  TIMESTAMP DEFAULT now()  -- Дата обновления
);
CREATE OR REPLACE FUNCTION update_timestamp()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_objects_timestamp
    BEFORE UPDATE ON objects
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

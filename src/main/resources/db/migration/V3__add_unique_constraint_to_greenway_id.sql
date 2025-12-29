-- Удаление дубликатов: оставляем только первую запись для каждого greenway_id
-- (запись с минимальным creation_date)
DELETE FROM authorized_users a
USING authorized_users b
WHERE a.greenway_id = b.greenway_id
  AND a.creation_date > b.creation_date;

-- Добавляем UNIQUE constraint на greenway_id
ALTER TABLE authorized_users
ADD CONSTRAINT uk_greenway_id UNIQUE (greenway_id);

-- Добавляем индекс для оптимизации поиска по greenway_id
CREATE INDEX idx_authorized_users_greenway_id ON authorized_users(greenway_id);

-- Добавляем комментарий
COMMENT ON CONSTRAINT uk_greenway_id ON authorized_users IS 'Ensures one Greenway ID can only be associated with one Telegram account';

-- Удаляем UNIQUE constraint на greenway_id
ALTER TABLE authorized_users
DROP CONSTRAINT IF EXISTS uk_greenway_id;

-- Удаляем индекс (он был создан вместе с constraint)
DROP INDEX IF EXISTS idx_authorized_users_greenway_id;

-- Добавляем комментарий
COMMENT ON COLUMN authorized_users.greenway_id IS 'Greenway partner identifier (uniqueness checked in application code)';

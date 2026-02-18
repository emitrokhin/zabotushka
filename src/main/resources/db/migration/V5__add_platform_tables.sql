-- Rename authorized_users to authorized_telegram_users
ALTER TABLE authorized_users RENAME TO authorized_telegram_users;

-- Drop and recreate FK on user_group_memberships pointing to renamed table
ALTER TABLE user_group_memberships
    DROP CONSTRAINT IF EXISTS fk_user_group_memberships_telegram_id;

ALTER TABLE user_group_memberships
    ADD CONSTRAINT fk_user_group_memberships_telegram_id
    FOREIGN KEY (telegram_id) REFERENCES authorized_telegram_users(telegram_id)
    ON DELETE CASCADE;

-- Rename index
ALTER INDEX IF EXISTS idx_authorized_users_telegram_id RENAME TO idx_authorized_telegram_users_telegram_id;

-- Update table comment
COMMENT ON TABLE authorized_telegram_users IS 'Stores authorized Telegram users with their Greenway credentials';

-- Create authorized_max_users table
CREATE TABLE authorized_max_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    max_id BIGINT NOT NULL UNIQUE,
    greenway_id BIGINT NOT NULL,
    reg_date VARCHAR(255) NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    CONSTRAINT uk_max_id UNIQUE (max_id)
);

-- Create index on max_id for faster lookups
CREATE INDEX idx_authorized_max_users_max_id ON authorized_max_users(max_id);

-- Add comments
COMMENT ON TABLE authorized_max_users IS 'Stores authorized Max users with their Greenway credentials';
COMMENT ON COLUMN authorized_max_users.max_id IS 'Unique Max user identifier';
COMMENT ON COLUMN authorized_max_users.greenway_id IS 'Greenway partner identifier';
COMMENT ON COLUMN authorized_max_users.reg_date IS 'Partner registration date in Greenway system';
COMMENT ON COLUMN authorized_max_users.creation_date IS 'Timestamp when the record was created';

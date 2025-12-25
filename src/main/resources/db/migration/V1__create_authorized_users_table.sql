-- Create authorized_users table
CREATE TABLE authorized_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    telegram_id BIGINT NOT NULL UNIQUE,
    greenway_id BIGINT NOT NULL,
    reg_date VARCHAR(255) NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    CONSTRAINT uk_telegram_id UNIQUE (telegram_id)
);

-- Create index on telegram_id for faster lookups
CREATE INDEX idx_authorized_users_telegram_id ON authorized_users(telegram_id);

-- Add comment to table
COMMENT ON TABLE authorized_users IS 'Stores authorized users with their Telegram and Greenway credentials';
COMMENT ON COLUMN authorized_users.telegram_id IS 'Unique Telegram user identifier';
COMMENT ON COLUMN authorized_users.greenway_id IS 'Greenway partner identifier';
COMMENT ON COLUMN authorized_users.reg_date IS 'Partner registration date in Greenway system';
COMMENT ON COLUMN authorized_users.creation_date IS 'Timestamp when the record was created';

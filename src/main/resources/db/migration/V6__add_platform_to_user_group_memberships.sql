-- Drop foreign key constraint (column no longer references only telegram users)
ALTER TABLE user_group_memberships
    DROP CONSTRAINT fk_user_group_memberships_telegram_id;

-- Rename column telegram_id → platform_user_id
ALTER TABLE user_group_memberships
    RENAME COLUMN telegram_id TO platform_user_id;

-- Add platform column (existing rows are all Telegram)
ALTER TABLE user_group_memberships
    ADD COLUMN platform VARCHAR(16) NOT NULL DEFAULT 'TELEGRAM';

-- Drop old unique constraint
ALTER TABLE user_group_memberships
    DROP CONSTRAINT uk_telegram_chat;

-- Add new unique constraint including platform
ALTER TABLE user_group_memberships
    ADD CONSTRAINT uk_platform_user_chat UNIQUE (platform_user_id, chat_id, platform);

-- Rename index
ALTER INDEX idx_user_group_memberships_telegram_id
    RENAME TO idx_user_group_memberships_platform_user_id;

-- Add index on platform
CREATE INDEX idx_user_group_memberships_platform ON user_group_memberships(platform);

-- Update comments
COMMENT ON TABLE user_group_memberships IS 'Stores information about user membership in groups across platforms';
COMMENT ON COLUMN user_group_memberships.platform_user_id IS 'Platform-specific user identifier (Telegram ID or Max user ID)';
COMMENT ON COLUMN user_group_memberships.platform IS 'Platform type: TELEGRAM or MAX';

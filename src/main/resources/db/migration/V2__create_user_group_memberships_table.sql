-- Create user_group_memberships table
CREATE TABLE user_group_memberships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    telegram_id BIGINT NOT NULL,
    chat_id BIGINT NOT NULL,
    joined_at TIMESTAMP NOT NULL,
    last_checked_at TIMESTAMP,
    CONSTRAINT uk_telegram_chat UNIQUE (telegram_id, chat_id)
);

-- Create indexes for faster lookups
CREATE INDEX idx_user_group_memberships_telegram_id ON user_group_memberships(telegram_id);
CREATE INDEX idx_user_group_memberships_chat_id ON user_group_memberships(chat_id);
CREATE INDEX idx_user_group_memberships_last_checked ON user_group_memberships(last_checked_at);

-- Add foreign key constraint to authorized_users
ALTER TABLE user_group_memberships
    ADD CONSTRAINT fk_user_group_memberships_telegram_id
    FOREIGN KEY (telegram_id) REFERENCES authorized_users(telegram_id)
    ON DELETE CASCADE;

-- Add comments to table and columns
COMMENT ON TABLE user_group_memberships IS 'Stores information about user membership in Telegram groups';
COMMENT ON COLUMN user_group_memberships.telegram_id IS 'Telegram user identifier';
COMMENT ON COLUMN user_group_memberships.chat_id IS 'Telegram chat/group identifier';
COMMENT ON COLUMN user_group_memberships.joined_at IS 'Timestamp when user joined the group';
COMMENT ON COLUMN user_group_memberships.last_checked_at IS 'Timestamp of last qualification check';

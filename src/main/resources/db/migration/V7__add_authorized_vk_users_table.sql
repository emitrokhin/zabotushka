CREATE TABLE authorized_vk_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vk_id BIGINT NOT NULL,
    greenway_id BIGINT NOT NULL,
    reg_date VARCHAR(255) NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    CONSTRAINT uk_vk_id UNIQUE (vk_id)
);

CREATE INDEX idx_authorized_vk_users_vk_id ON authorized_vk_users(vk_id);

COMMENT ON TABLE authorized_vk_users IS 'Stores authorized VK users with their Greenway credentials';
COMMENT ON COLUMN authorized_vk_users.vk_id IS 'Unique VK user identifier';
COMMENT ON COLUMN authorized_vk_users.greenway_id IS 'Greenway partner identifier';
COMMENT ON COLUMN authorized_vk_users.reg_date IS 'Partner registration date in Greenway system';
COMMENT ON COLUMN authorized_vk_users.creation_date IS 'Timestamp when the record was created';

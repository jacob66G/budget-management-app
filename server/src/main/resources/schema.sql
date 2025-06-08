CREATE TABLE IF NOT EXISTS CHAT_MEMORY (    -- combining specific conversation with user
    conversation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id bigint NOT NULL,
    description VARCHAR(256),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL, -- conversation create time
    last_usage TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_chat_memory_user_id ON CHAT_MEMORY(user_id);

create table tms.refresh_tokens (
    id bigint primary key,
    token varchar(255) not null unique,
    user_id bigint not null,
    expires_at timestamptz         NOT NULL,
    created_at timestamptz         NOT NULL,
    updated_at timestamptz,
    foreign key (user_id) references tms.users(id) ON DELETE CASCADE
);
CREATE INDEX idx_token ON tms.refresh_tokens (token);
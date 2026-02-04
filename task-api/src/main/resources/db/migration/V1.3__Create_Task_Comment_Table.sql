CREATE TABLE comments
(
    id         bigint primary key,
    content    text      not null,
    task_id    bigint    not null,
    author_id  bigint    not null,
    created_at timestamptz not null,
    updated_at timestamptz,
    FOREIGN KEY (author_id) references users (id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) references tasks (id) ON DELETE CASCADE
);
CREATE INDEX comments_task_id_idx ON comments (task_id);
CREATE INDEX comments_author_id_idx ON comments (author_id);
CREATE INDEX comments_created_at_idx ON comments (created_at);

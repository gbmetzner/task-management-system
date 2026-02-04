CREATE TYPE tasks_status AS ENUM ('TODO', 'IN_PROGRESS', 'DONE');
CREATE TYPE tasks_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');

CREATE TABLE tasks
(
    id           bigint primary key,
    title        varchar(255)   not null,
    description  text,
    status       tasks_status   not null default 'TODO',
    priority     tasks_priority not null DEFAULT 'LOW',
    created_at   timestamptz    not null,
    updated_at   timestamptz,
    completed_at timestamptz,
    due_date     timestamptz,
    project_id   bigint         not null,
    assignee_id  bigint,
    reporter_id  bigint         not null,
    FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    FOREIGN KEY (assignee_id) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (reporter_id) REFERENCES users (id) ON DELETE SET NULL
);
CREATE INDEX tasks_project_id_idx ON tasks (project_id);
CREATE INDEX tasks_assignee_id_idx ON tasks (assignee_id);
CREATE INDEX tasks_reporter_id_idx ON tasks (reporter_id);
CREATE INDEX tasks_due_date_idx ON tasks (due_date);
CREATE INDEX tasks_status_idx ON tasks (status);
CREATE INDEX tasks_priority_idx ON tasks (priority);
CREATE INDEX tasks_created_at_idx ON tasks (created_at);
CREATE INDEX tasks_completed_at_idx ON tasks (completed_at);
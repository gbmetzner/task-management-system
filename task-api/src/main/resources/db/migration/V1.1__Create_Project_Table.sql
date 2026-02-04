-- START Project Table
CREATE TYPE project_status AS ENUM ('ACTIVE', 'ARCHIVED');
CREATE TABLE projects
(
    id          bigint PRIMARY KEY,
    name        VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(255)        NOT NULL,
    status      project_status      NOT NULL DEFAULT 'ACTIVE',
    owner_id    bigint              NOT NULL,
    created_at  timestamptz         NOT NULL,
    updated_at  timestamptz,
    FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE
);
CREATE INDEX project_owner_idx ON projects (owner_id);
CREATE INDEX project_name_idx ON projects (name);
CREATE INDEX project_status_idx ON projects (status);
-- END Project Table
-- START Project Member Table
CREATE TABLE project_members
(
    member_id  bigint      not null,
    project_id bigint      not null,
    joined_at  timestamptz not null,
    PRIMARY KEY (member_id, project_id),
    FOREIGN KEY (member_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
);
CREATE INDEX project_member_idx ON project_members (member_id);
CREATE INDEX project_project_idx ON project_members (project_id);
-- END Project Member Table
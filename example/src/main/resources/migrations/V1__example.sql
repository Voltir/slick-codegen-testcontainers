set search_path to service, public;

CREATE TABLE IF NOT EXISTS service.examples(
    id uuid PRIMARY KEY,
    name text NOT NULL,
    name2 text NOT NULL,
    is_deleted boolean NOT NULL DEFAULT false
);
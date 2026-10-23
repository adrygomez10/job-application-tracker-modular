CREATE TABLE reminders (
  activity_id uuid NOT NULL,
  application_id uuid NOT NULL,
  kind varchar(20) NOT NULL,
  title varchar(180) NOT NULL,
  due_at timestamptz NOT NULL,
  active boolean NOT NULL,
  PRIMARY KEY (activity_id, kind, due_at)
);
CREATE TABLE reminder_refresh (id integer PRIMARY KEY CHECK (id = 1), checked_at timestamptz);
INSERT INTO reminder_refresh (id) VALUES (1);

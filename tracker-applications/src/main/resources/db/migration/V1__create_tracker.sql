CREATE TABLE job_applications (
    id uuid PRIMARY KEY, version bigint NOT NULL, mutation_count bigint NOT NULL,
    company varchar(160) NOT NULL, role varchar(180) NOT NULL, url varchar(2000) NOT NULL,
    source varchar(160) NOT NULL, location varchar(160) NOT NULL, work_mode varchar(32) NOT NULL,
    technologies varchar(500) NOT NULL, contact varchar(500) NOT NULL, notes varchar(10000) NOT NULL,
    status varchar(32) NOT NULL, applied_on date, archived boolean NOT NULL,
    created_at timestamptz NOT NULL, updated_at timestamptz NOT NULL,
    CONSTRAINT application_status CHECK (status IN ('SAVED','APPLIED','IN_PROGRESS','OFFER_RECEIVED','ACCEPTED','REJECTED','WITHDRAWN')),
    CONSTRAINT application_date CHECK ((status = 'SAVED' AND applied_on IS NULL) OR status = 'WITHDRAWN' OR (status <> 'SAVED' AND applied_on IS NOT NULL)),
    CONSTRAINT application_work_mode CHECK (work_mode IN ('UNSPECIFIED','REMOTE','HYBRID','ONSITE'))
);
CREATE INDEX applications_list ON job_applications (archived, updated_at DESC, id);
CREATE INDEX applications_status ON job_applications (archived, status);
CREATE INDEX applications_applied ON job_applications (applied_on);
CREATE TABLE application_timeline (
    application_id uuid NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    position integer NOT NULL, id uuid NOT NULL, occurred_at timestamptz NOT NULL,
    kind varchar(255) NOT NULL, previous_status varchar(32), next_status varchar(32), body varchar(10000) NOT NULL,
    PRIMARY KEY (application_id, position), UNIQUE(id)
);
CREATE TABLE application_interviews (
    application_id uuid NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    position integer NOT NULL, id uuid NOT NULL, title varchar(180) NOT NULL, kind varchar(32) NOT NULL,
    starts_at timestamptz NOT NULL, location varchar(500) NOT NULL, url varchar(2000) NOT NULL,
    notes varchar(10000) NOT NULL, outcome varchar(32) NOT NULL, result varchar(10000) NOT NULL,
    PRIMARY KEY (application_id, position), UNIQUE(id)
);
CREATE INDEX interviews_agenda ON application_interviews (outcome, starts_at);
CREATE TABLE application_tasks (
    application_id uuid NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    position integer NOT NULL, id uuid NOT NULL, title varchar(180) NOT NULL, due_on date NOT NULL, completed boolean NOT NULL,
    PRIMARY KEY (application_id, position), UNIQUE(id)
);
CREATE INDEX tasks_agenda ON application_tasks (completed, due_on);

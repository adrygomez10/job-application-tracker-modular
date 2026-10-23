CREATE TABLE imported_offers (
  source varchar(160) NOT NULL,
  external_id varchar(180) NOT NULL,
  fingerprint char(64) NOT NULL,
  application_id uuid NOT NULL UNIQUE,
  PRIMARY KEY (source, external_id)
);

-- Fictional data only. Safe to repeat: existing identities are never updated.
WITH inserted AS (
INSERT INTO job_applications (id,version,mutation_count,company,role,url,source,location,work_mode,technologies,contact,notes,status,applied_on,archived,created_at,updated_at)
VALUES ('00000000-0000-4000-8000-000000000001',0,0,'Example Labs','Java Backend Developer','https://example.com/jobs/java','Demostración','Madrid','HYBRID','Java, Spring Boot, PostgreSQL','Equipo de selección ficticio','Preparar ejemplos del portfolio. Datos ficticios.','IN_PROGRESS',CURRENT_DATE-3,false,now(),now())
ON CONFLICT DO NOTHING RETURNING id)
INSERT INTO application_timeline (application_id,position,id,occurred_at,kind,previous_status,next_status,body)
SELECT id,0,'00000000-0000-4000-8000-000000000002',now(),'CREATED',null,'IN_PROGRESS','Datos ficticios de demostración.' FROM inserted;

-- Migración para corregir los tipos de datos SERIAL (Integer 32 bits)
-- a BIGINT (64 bits) en las tablas de la Iteración 3, 
-- para alinear con el mapeo @Id private Long id; de Hibernate.

ALTER TABLE identity.users ALTER COLUMN id TYPE BIGINT;
ALTER TABLE common.audit_log ALTER COLUMN id TYPE BIGINT;
ALTER TABLE sync.sync_queue ALTER COLUMN id TYPE BIGINT;

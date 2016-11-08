-- Ticket #23
-- To facilitate corefactor upserts/migrations across environments, the schema needs to support business keys 
-- that are unique and preserved across environments. We'll leverage the PKs as business keys, and not have them 
-- generated via sequences 
-- As an implementation detail, for now, we'll leave the sequences themselves as is, but simply alter the default value 
-- generation for the PK columns
--
-- @author: NShah
-- @created: 09/21/2016

ALTER TABLE employmeo.corefactor_descriptions ALTER COLUMN cfdesc_id  DROP DEFAULT;

--//@UNDO

ALTER TABLE employmeo.corefactor_descriptions ALTER COLUMN cfdesc_id SET DEFAULT nextval('employmeo.corefactor_descriptions_cfdesc_id_seq');



-- Ticket #23
-- To facilitate survey definition migrations across environments, the schema needs to support business keys 
-- that are unique and preserved across environments. Instead of adding new business key fields across all survey tables,
-- we'll leverage the PKs as business keys, and not have them generated via sequences (which can make them inconsistent 
-- and unreliable across environments)
-- As an implementation detail, for now, we'll leave the sequences themselves as is, but simply alter the default value 
-- generation for the PK columns
--
-- @author: NShah
-- @created: 09/20/2016

ALTER TABLE employmeo.surveys  ALTER COLUMN survey_id  DROP DEFAULT;
ALTER TABLE employmeo.survey_questions ALTER COLUMN sq_id  DROP DEFAULT;
ALTER TABLE employmeo.questions ALTER COLUMN question_id  DROP DEFAULT;
ALTER TABLE employmeo.answers ALTER COLUMN answer_id  DROP DEFAULT;

--//@UNDO

ALTER TABLE employmeo.answers ALTER COLUMN answer_id SET DEFAULT nextval('employmeo.answers_answer_id_seq');
ALTER TABLE employmeo.questions ALTER COLUMN question_id SET DEFAULT nextval('employmeo.questions_question_id_seq');
ALTER TABLE employmeo.survey_questions ALTER COLUMN sq_id SET DEFAULT nextval('employmeo.survey_questions_sq_id_seq');
ALTER TABLE employmeo.surveys ALTER COLUMN survey_id SET DEFAULT nextval('employmeo.surveys_survey_id_seq');



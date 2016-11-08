-- Creates a changelog table driven by the DBDeploy Plugin for database change tracking and versioning.
-- @author: NShah
-- @created: 09/16/2016

CREATE TABLE employmeo.changelog (
  change_number  bigint NOT NULL,
  complete_dt    timestamp WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  applied_by     varchar(100) NOT NULL,
  description    varchar(500) NOT NULL,
  /* Keys */
  CONSTRAINT changelog_pkey
    PRIMARY KEY (change_number)
) WITH (
    OIDS = FALSE
  );
  
--//@UNDO
 
DROP TABLE employmeo.changelog;

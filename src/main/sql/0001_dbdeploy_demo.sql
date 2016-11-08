-- Demo script
-- Creates a dummy table named dbdeploy_demo to test db change tracking and versioning.

-- @author: NShah
-- @created: 09/16/2016

CREATE TABLE employmeo.dbdeploy_demo (id INTEGER, data VARCHAR(100));

--//@UNDO

DROP TABLE employmeo.dbdeploy_demo;

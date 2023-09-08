-- Changed column `propValue` to be able to store type configuration of the KnowledgeBase
ALTER TABLE TEST_UPDATE_TABLE ADD ("tmp_col" NCLOB);
UPDATE TEST_UPDATE_TABLE SET "tmp_col" = "col1";
ALTER TABLE TEST_UPDATE_TABLE DROP COLUMN "col1";
ALTER TABLE TEST_UPDATE_TABLE RENAME COLUMN "tmp_col" TO "col1";
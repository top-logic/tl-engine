-- Changed column `propValue` to be able to store type configuration of the KnowledgeBase
ALTER TABLE TL_PROPERTIES ADD ("tmp_col" NCLOB);
UPDATE TL_PROPERTIES SET "tmp_col" = "propValue";
ALTER TABLE TL_PROPERTIES DROP COLUMN "propValue";
ALTER TABLE TL_PROPERTIES RENAME COLUMN "tmp_col" TO "propValue";
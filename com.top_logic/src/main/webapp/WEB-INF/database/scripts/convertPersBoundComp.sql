
-- Convert PERS_BOUND_COMP Table to use an IDENTIFIER of its own
-- This will work for MySQL only, as of now 
-- $Id :$

ALTER TABLE PERS_BOUND_COMP 
   ADD COLUMN ID VARCHAR(254) NOT NULL AFTER REMOVED;

UPDATE PERS_BOUND_COMP SET ID=IDENTIFIER;

CREATE UNIQUE INDEX ID_IDX ON PERS_BOUND_COMP(ID);

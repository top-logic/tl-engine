-- Use the script to update old data schema (pre TL5.3) to TL5.4
-- This script resolves the conflicting types of
-- CalculatedPrimitiveMetaAttribute and ExternalContactAttribute

UPDATE META_ATTRIBUTE 
   SET ATTRIBUTE_TYPE=32774 
 WHERE ATTRIBUTE_TYPE=32772;

UPDATE META_ATTRIBUTE 
   SET ATTRIBUTE_TYPE=32773 
 WHERE ATTRIBUTE_TYPE=32771;

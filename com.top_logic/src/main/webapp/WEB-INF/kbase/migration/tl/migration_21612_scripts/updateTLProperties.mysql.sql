-- Changed column `propValue` to be able to store type configuration of the KnowledgeBase
ALTER TABLE TL_PROPERTIES CHANGE `propValue` `propValue` LONGTEXT;
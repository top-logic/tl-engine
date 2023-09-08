UPDATE IGNORE do_storage                  SET sval="mailuser1@mailtest.top-logic.com" WHERE attr="email" OR attr="otherMailbox" OR attr="mail";
UPDATE IGNORE tl_user                     SET other_mail="mailuser1@mailtest.top-logic.com";
UPDATE IGNORE tl_user                     SET mail="mailuser1@mailtest.top-logic.com";
UPDATE IGNORE external_contact_all        SET e_mail="mailuser1@mailtest.top-logic.com";
UPDATE IGNORE external_contact_assignment SET e_mail="mailuser1@mailtest.top-logic.com";
COMMIT;

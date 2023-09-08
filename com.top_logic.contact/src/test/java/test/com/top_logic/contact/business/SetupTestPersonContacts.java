/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.contact.business;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.contact.business.AddressHolder;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;


/**
 * !CAUTION! do not put this in the automated testsuite because the created persons are not deleted after testrun!
 * Special test case to create some data! Person contacts and company contacts are created
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class SetupTestPersonContacts extends BasicTestCase {

    public void testCreatePersonContactList () throws Exception{
        ContactFactory fac = ContactFactory.getInstance();
        String[][] infos = createPersonInfos();
        PersonContact newContact = null;
        for (int i=0;i < infos.length;i++) {
            newContact = fac.createNewPersonContact(infos[i][1],infos[i][0]);
            if (infos[i][2] != null) {
                newContact.setValue(PersonContact.ATT_TITLE,infos[i][2]);
            }
        }
        if (newContact != null) {
            newContact.getKnowledgeBase().commit();
        }
    }
    
    public void testCreateCompanyContacts () throws Exception {
        ContactFactory fac = ContactFactory.getInstance();
        CompanyContact newContact = null;
        String[] names = new String[]{"Deutsche Post","Siemens L&A","Calendra"};
        AddressHolder[] addresses = createAddressList();
        for (int i = 0; i < 3; i++) {
            newContact = fac.createNewCompanyContact(names[i],addresses[i]);
        }
        if (newContact != null) {
            newContact.getKnowledgeBase().commit();
        }
    }
    
    private AddressHolder[] createAddressList () throws Exception {
        AddressHolder[] result = new AddressHolder[3];
        
        for (int i=0; i <3; i++) {
            result[i] = new AddressHolder();
			result[i].load(new FileInputStream(new File(ModuleLayoutConstants.SRC_TEST_DIR
				+ "/test/com/top_logic/contact/business/addressData_" + (i + 1) + ".properties")));
        }    
        return result;
    }
    
    private String[][] createPersonInfos(){
        String[][] result = new String[10][3];
        result[0][0] = "Seppl";         result[0][1] = "Herberger";     result[0][2] = "Prof.Dr."; 
        result[1][0] = "Hugo Egon";     result[1][1] = "Balder";        result[1][2] = null; 
        result[2][0] = "Hella";         result[2][1] = "von Sinnen";    result[2][2] = null; 
        result[3][0] = "Bernard";       result[3][1] = "Hoecker";       result[3][2] = "Dr."; 
        result[4][0] = "Helge";         result[4][1] = "Schneider";     result[4][2] = "Prof.";
        result[5][0] = "Maddin";        result[5][1] = "Schneider";     result[5][2] = null;
        result[6][0] = "Benno";         result[6][1] = "vom Bommelberg";result[6][2] = "Baron";
        result[7][0] = "Donald";        result[7][1] = "Duck";          result[7][2] = null;
        result[8][0] = "Daisy";         result[8][1] = "Duck";          result[8][2] = "Prof.";
        result[9][0] = "Honigtau";      result[9][1] = "Bunsenbrenner"; result[9][2] = "Prof.";
        return result;
    }
    /** 
     * Return the suite of tests to perform. 
     * 
     * @return the suite
     */
    public static Test suite () {
		return KBSetup.getSingleKBTest(SetupTestPersonContacts.class);
    }

}

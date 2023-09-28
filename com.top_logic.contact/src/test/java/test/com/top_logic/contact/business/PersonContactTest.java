/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.contact.business;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.contact.filter.PersonContactFilter;
import com.top_logic.contact.layout.ContactResourceProvider;
import com.top_logic.knowledge.service.KnowledgeBase;


/**
 * Test relations between person contacts.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class PersonContactTest extends BasicTestCase {

    public void testCreateContact () throws Exception {
        ContactFactory theFac = ContactFactory.getInstance();

        PersonContact thePerson = theFac.createNewPersonContact("Herberger","Seppl");
        assertNotNull(thePerson);
        thePerson.setValue(PersonContact.TITLE,"Prof.Dr.");
        
        assertEquals ("Prof.Dr. Seppl Herberger",thePerson.getFullname());
        KnowledgeBase theBase = thePerson.getKnowledgeBase();
        theBase.commit();
        
        PersonContactFilter pfc = new PersonContactFilter();
        assertTrue (pfc.accept(thePerson));
        assertFalse(pfc.accept(theFac)   );
        
		ContactResourceProvider clr = TypedConfigUtil.newConfiguredInstance(ContactResourceProvider.class);
        assertTrue(clr.getLabel(thePerson).startsWith("Herberger"));
        assertTrue(clr.getLabel(thePerson).startsWith("Herberger"));
        
        // get rid of the person wrapper and knowledgeObject!
        thePerson.tDelete();
        assertFalse (thePerson.tValid());
        theBase.commit();
    }
    
    /** 
     * Return the suite of tests to perform. 
     * 
     * @return the suite
     */
    public static Test suite () {
        TestSuite theSuite = new TestSuite(PersonContactTest.class);
        
        return KBSetup.getSingleKBTest(new ContactCreateSetup(theSuite));
    }

}

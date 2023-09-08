/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.contact.business;

import static junit.framework.Assert.*;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.Test;

import test.com.top_logic.basic.ThreadContextSetup;

import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;


/**
 * This setup ensures that after running a test the database is unaffected!
 * Prepare the dropping of the person contacts before running the tests!
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class ContactCreateSetup extends ThreadContextSetup {

    private int numberOfPersonContacts;
    private int numberOfCompanyContacts;

    public ContactCreateSetup(Test arg0) {
		super(arg0);
    }

    /**
     * store the number of personContacts in the database during this testsetup
     * @see junit.extensions.TestSetup#setUp()
     */
    @Override
	protected void doSetUp() throws Exception {
        Collection allContacts = ContactFactory.getInstance().getAllContacts(null);
        Iterator iter = allContacts.iterator();
        while (iter.hasNext()) {
            Object element = iter.next();
            if (element instanceof PersonContact) {
                numberOfPersonContacts++;
            }
            if(element instanceof CompanyContact) {
                numberOfCompanyContacts++;
            }
        }
    }
    /**
     * assert that after test execution, the number of persons in the database is the same!
     * @see junit.extensions.TestSetup#tearDown()
     */
    @Override
	protected void doTearDown() throws Exception {
        Collection allContacts = ContactFactory.getInstance().getAllContacts(null);
        Iterator iter = allContacts.iterator();
        int personsAfter = 0;
        int companiesAfter = 0;
        while (iter.hasNext()) {
            Object element = iter.next();
            if (element instanceof PersonContact) {
                personsAfter++;
            }
            if(element instanceof CompanyContact) {
                companiesAfter++;
            }
        }
        assertEquals(
                     "The count before and after testing for the person contact differs",
                     numberOfPersonContacts, personsAfter);
        assertEquals(
                     "The count before and after testing for the company contact differs",
                     numberOfCompanyContacts, companiesAfter);
    }
}

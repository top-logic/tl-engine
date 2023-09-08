/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.proxy;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.textui.TestRunner;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.dsa.ex.UnknownDBException;
import com.top_logic.mail.proxy.MailReceiverService;


/**
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TestMailDataSourceAdaptor extends TestCase {

    /**
     * Constructor for TestMailFolder.
     * 
     * @param aName
     *            Name of the test.
     */
    public TestMailDataSourceAdaptor(String aName) {
        super(aName);
    }

    public void testInbox() throws Exception {
        String          theURL = "mail://INBOX";
        DataAccessProxy theDAP;
		try {
			theDAP = new DataAccessProxy(theURL);
		} catch (UnknownDBException ex) {
			BasicTestCase.fail("Ticket #2314: Datasource mail is not configured.", ex);
			return;
		}

        assertNotNull("DataAccessProxy is null", theDAP);
        assertTrue("Inbox doesn't exist", theDAP.exists());

        DataAccessProxy[] theEntries = theDAP.getEntries();

        assertNotNull("Null entries in Inbox", theEntries);
        assertTrue("No entries in Inbox", theEntries.length > 0);

        for (int thePos = 0; thePos < theEntries.length; thePos++) {
            DataAccessProxy theProxy = theEntries[thePos];

            assertTrue("Entry doesn't exist", theProxy.exists());

			if (theProxy.isContainer()) {
				DataAccessProxy[] theSubs = theProxy.getEntries();

				if (theSubs == null) {
					continue;
				}

				for (int theSPos = 0; theSPos < theSubs.length; theSPos++) {
					DataAccessProxy theSub = theSubs[theSPos];

					assertTrue("Sub entry doesn't exist", theSub.exists());
				}
			}
        }
    }

    /**
     * The method constructing a test suite for this class.
     * 
     * @return The test to be executed.
     */
    public static Test suite() {
		return TLTestSetup.createTLTestSetup(ServiceTestSetup.createSetup(TestMailDataSourceAdaptor.class,
			DataAccessService.Module.INSTANCE, MailReceiverService.Module.INSTANCE));
    }

    /**
     * The main program for executing this test also from console.
     * 
     * @param args
     *            Will be ignored.
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

}

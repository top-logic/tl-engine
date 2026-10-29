/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.proxy;

import java.util.Collections;

import jakarta.mail.Message;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.mail.base.TestMailHelper;

import com.top_logic.base.mail.MailHelper;
import com.top_logic.base.mail.MailHelper.SendMailResult;
import com.top_logic.base.mail.MailSenderService;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.dsa.ex.UnknownDBException;
import com.top_logic.mail.proxy.MailDataSourceAdaptor;
import com.top_logic.mail.proxy.MailReceiverService;


/**
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TestMailDataSourceAdaptor extends BasicTestCase {

	/**
	 * Number of attempts to find an entry in the listing of a container.
	 * 
	 * @see #findEntry(DataAccessProxy, String)
	 */
	private static final int FIND_ENTRY_TRIES = 3;

    /**
     * Constructor for TestMailFolder.
     * 
     * @param aName
     *            Name of the test.
     */
    public TestMailDataSourceAdaptor(String aName) {
        super(aName);
    }

	/**
	 * Tests that the INBOX contains a mail sent by this test.
	 * 
	 * <p>
	 * The mailbox is shared with other test runs. Only the mail of this test is inspected: Other
	 * mails may vanish at any time, and reading their content would mark them as seen.
	 * </p>
	 */
	public void testInbox() throws Exception {
		String theURL = "mail://INBOX";
		DataAccessProxy theDAP;
		try {
			theDAP = new DataAccessProxy(theURL);
		} catch (UnknownDBException ex) {
			BasicTestCase.fail("Ticket #2314: Datasource mail is not configured.", ex);
			return;
		}

		String theSubject = TestMailHelper.newSubject();
		SendMailResult theResult = MailHelper.getInstance().sendSystemMail(
			Collections.singletonList(TestMailHelper.getReceiver()), theSubject,
			TestMailDataSourceAdaptor.class + ".testInbox()", MailHelper.CONTENT_TYPE_TEXT);
		assertTrue(theResult.getErrorResultString().toString() + theResult.getException(), theResult.isSuccess());
		try {
			Message theMail = TestMailHelper.getSentMail(theSubject);
			String theMailPath = MailDataSourceAdaptor.getURL(theMail, null);

			assertNotNull("DataAccessProxy is null", theDAP);
			assertTrue("Inbox doesn't exist", theDAP.exists());

			DataAccessProxy theEntry = findEntry(theDAP, theMailPath);
			assertNotNull("No entry for mail '" + theSubject + "' in Inbox", theEntry);
			assertTrue("Entry doesn't exist", theEntry.exists());
		} finally {
			TestMailHelper.deleteSentMail(theSubject);
		}
	}

	/**
	 * Searches the entry with the given path in the given container.
	 * 
	 * <p>
	 * Listing the entries fails for the entries following a mail that is removed by another client
	 * during the listing. Therefore the listing is repeated, if the entry is not found.
	 * </p>
	 */
	private DataAccessProxy findEntry(DataAccessProxy container, String path) throws Exception {
		for (int theTry = 0; theTry < FIND_ENTRY_TRIES; theTry++) {
			DataAccessProxy[] theEntries = container.getEntries();
			if (theEntries == null) {
				continue;
			}
			for (DataAccessProxy theEntry : theEntries) {
				if (path.equals(theEntry.getPathWithoutProtocol())) {
					return theEntry;
				}
			}
		}
		return null;
	}

    /**
     * The method constructing a test suite for this class.
     * 
     * @return The test to be executed.
     */
    public static Test suite() {
		return KBSetup.getSingleKBTest(ServiceTestSetup.createSetup(TestMailDataSourceAdaptor.class,
			DataAccessService.Module.INSTANCE, MailSenderService.Module.INSTANCE, MailReceiverService.Module.INSTANCE,
			MailHelper.Module.INSTANCE));
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

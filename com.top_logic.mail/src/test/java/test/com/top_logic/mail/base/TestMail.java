/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.base;

import java.util.Collections;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.mail.Mail;
import com.top_logic.base.mail.MailHelper;
import com.top_logic.base.mail.MailHelper.SendMailResult;
import com.top_logic.base.mail.MailSenderService;
import com.top_logic.mail.proxy.MailReceiverService;

/** 
 * Please enter a description here.
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TestMail extends BasicTestCase {

	private static final String ERNA_FULL = "erna@top-logic.com";

	private static final String KARL_FULL = "karl@top-logic.com";

	/**
	 * Constructor for TestMail.
	 */
	public TestMail(String arg0) {
		super(arg0);
	}

	/**
	 * Test for void Mail()
	 */
	public void testMail() {
		Mail theMail = new Mail("sender");

		assertNotNull ("Unable to create mail sender",
							theMail.getSender ());
	}
    
	/**
	 * Test for void Mail(String)
	 */
	public void testMailString() {
		Mail    theMail   = new Mail ("erna");
		Address theSender = theMail.getSender ();

		assertNotNull ("Unable to create mail sender 'erna'",
							theSender);

		theMail = new Mail (ERNA_FULL);
		theSender = theMail.getSender ();

		assertNotNull ("Unable to create mail sender '" + ERNA_FULL + "'",
							theSender);
		assertEquals ("Unable to create mail sender '" + ERNA_FULL + "'",
							theSender.toString (),
							ERNA_FULL);
	}

    /**
     * Test for send()
     */
	public void testSend() throws MessagingException {
		MailHelper theHelper = MailHelper.getInstance();
		
		assertFalse(theHelper.sendSystemMail(Collections.singletonList("erna"), null, null, MailHelper.CONTENT_TYPE_TEXT).isSuccess());

		String theTitle = TestMailHelper.newSubject();
		String content = TestMail.class + ".testSend()";
		String formattedContent = "<html><head></head><body><strong>Krass fette Email!</strong>"
			+ "<br /><small>" + content + "</small></body>";
		
		SendMailResult theResult = theHelper.sendSystemMail(Collections.singletonList(TestMailHelper.getReceiver()),
			theTitle, formattedContent, MailHelper.CONTENT_TYPE_HTML);
		assertTrue(theResult.getErrorResultString().toString() + theResult.getException(), theResult.isSuccess());
		TestMailHelper.deleteSentMail(theTitle);
	}

    /**
     * Test for addReceiver()
     */
	public void testAddReceiver() {
		Mail theMail = new Mail("sender");

		assertTrue ("Adding of " + ERNA_FULL + " has failed.", 
						 theMail.addReceiver (ERNA_FULL));
		assertTrue ("Adding of " + KARL_FULL + " has failed.",
						 theMail.addReceiver (KARL_FULL));
		assertTrue ("Second adding of " + KARL_FULL + " succeeds.",
						 !theMail.addReceiver (KARL_FULL));
		assertTrue ("Second adding of " + ERNA_FULL + " succeeds.",
						 !theMail.addReceiver (ERNA_FULL));
	}
    
   
    /**
    * The method constructing a test suite for this class.
    *
    * @return    The test to be executed.
    */
    public static Test suite () {
		Test innerTest = ServiceTestSetup.createSetup(TestMail.class,
			MailSenderService.Module.INSTANCE,
			MailReceiverService.Module.INSTANCE,
			MailHelper.Module.INSTANCE);
		return KBSetup.getSingleKBTest(innerTest);
    }

    /**
     * The main program for executing this test also from console.
     *
     * @param    args    Will be ignored.
     */
     public static void main (String[] args) {
        
        TestRunner.run (suite ());
     }

}

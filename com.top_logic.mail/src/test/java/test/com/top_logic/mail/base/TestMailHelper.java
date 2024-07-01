/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.base;

import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import jakarta.mail.Address;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.contact.business.CreateDefaultTestContacts;

import com.top_logic.base.mail.I18NConstants;
import com.top_logic.base.mail.MailHelper;
import com.top_logic.base.mail.MailHelper.SendMailResult;
import com.top_logic.base.mail.MailSenderService;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.mail.proxy.MailReceiver;
import com.top_logic.mail.proxy.MailReceiverService;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * Test for {@link MailSenderService}.
 */
public class TestMailHelper extends BasicTestCase {

	/**
	 * Name of the user which has {@link #getReceiver()} as email address.
	 * 
	 * <p>
	 * See also "Users.test.xml"
	 * </p>
	 */
	public static final String VALID_USER_NAME = "test-user1";

	private static final String INVALID_ADDRESS = "fal§e@develop.local";

	private static final String SERVER_ALIAS = "%IMAP_SERVER%";

	private static final String PASSWORD_ALIAS = "%IMAP_PASS%";

	private static final String USER_ALIAS = "%IMAP_USER%";
    
    public TestMailHelper (String aName) {
        super (aName);
    }

	public void testSendMailWithoutReply() throws MessagingException {
		MailHelper theHelper = MailHelper.getInstance();

		String sender = getSender();
		List theReceiver = Collections.singletonList(getReceiver());
		String theSubject = TestMailHelper.newSubject();
		String theContent = createContent("testSendMailWithoutReply");
		theHelper.sendMail(sender, theReceiver, null, null, null, theSubject, theContent, null, null);
		Message mail = getSentMail(theSubject);

		Address[] replyTo = mail.getReplyTo();
		assertNotNull(replyTo);
		assertEquals(1, replyTo.length);
		assertEquals(sender, replyTo[0].toString());

		deleteSentMail(theSubject);
	}

	public void testSendMailWithReply() throws MessagingException {
		MailHelper theHelper = MailHelper.getInstance();

		String sender = getSender();
		List theReceiver = Collections.singletonList(getReceiver());
		String replyAddress = "reply@top-logic.com";
		List<?> reply = Collections.singletonList(replyAddress);
		String theSubject = TestMailHelper.newSubject();
		String theContent = createContent("testSendMailWithReply");
		theHelper.sendMail(sender, theReceiver, null, null, reply, theSubject, theContent, null, null);
		Message mail = getSentMail(theSubject);

		Address[] replyTo = mail.getReplyTo();
		assertNotNull(replyTo);
		assertEquals(1, replyTo.length);
		assertEquals(replyAddress, replyTo[0].toString());

		deleteSentMail(theSubject);
	}

	/**
	 * Tests that a mail without a recipient in the "TO" field can be sent, when the "CC" is not
	 * empty.
	 */
	public void testSendMailWithJustCC() throws MessagingException {
		MailHelper mailHelper = MailHelper.getInstance();
		String sender = getSender();
		List<String> to = list();
		List<String> cc = list(getReceiver());
		List<String> bcc = list();
		String subject = newSubject();
		String content = createContent("testSendMailWithJustCC");
		try {
			SendMailResult result = mailHelper.sendMail(sender, to, cc, bcc, subject, content, null, null);
			assertTrue(result.isSuccess());
			Message mail = getSentMail(subject);
			assertRecipients(to, mail, RecipientType.TO);
			assertRecipients(cc, mail, RecipientType.CC);
			// The BCC cannot be asserted, as it is not disclosed to the recipients of the mail.
		} finally {
			deleteSentMail(subject);
		}
	}

	/**
	 * Tests that a mail without a recipient in the "TO" field can be sent, when the "BCC" is not
	 * empty.
	 */
	public void testSendMailWithJustBCC() throws MessagingException {
		MailHelper mailHelper = MailHelper.getInstance();
		String sender = getSender();
		List<String> to = list();
		List<String> cc = list();
		List<String> bcc = list(getReceiver());
		String subject = newSubject();
		String content = createContent("testSendMailWithJustBCC");
		try {
			SendMailResult result = mailHelper.sendMail(sender, to, cc, bcc, subject, content, null, null);
			assertTrue(result.isSuccess());
			Message mail = getSentMail(subject);
			assertRecipients(to, mail, RecipientType.TO);
			assertRecipients(cc, mail, RecipientType.CC);
			// The BCC cannot be asserted, as it is not disclosed to the recipients of the mail.
		} finally {
			deleteSentMail(subject);
		}
	}

	/**
	 * Tests that a mail without a recipient in the "TO", "CC" and "BCC" fields can not can be sent.
	 */
	public void testSendMailWithoutRecipient() throws MessagingException {
		MailHelper mailHelper = MailHelper.getInstance();
		String sender = getSender();
		List<String> to = list();
		List<String> cc = list();
		List<String> bcc = list();
		String subject = newSubject();
		String content = createContent("testSendMailWithoutRecipient");
		SendMailResult sendResult = null;
		try {
			sendResult = mailHelper.sendMail(sender, to, cc, bcc, subject, content, null, null);
			assertFalse(sendResult.isSuccess());
			sendResult.getErrorMessage().equals(I18NConstants.ERROR_NO_RECEIVER);
		} finally {
			if ((sendResult != null) && sendResult.isSuccess()) {
				deleteSentMail(subject);
			}
		}
	}

	private void assertRecipients(List<String> expectedRecipients, Message mail, RecipientType recipientType)
			throws MessagingException {
		List<String> actualRecipients = toStrings(getRecipients(mail, recipientType));
		assertEquals(expectedRecipients, actualRecipients);
	}

	private List<Address> getRecipients(Message mail, RecipientType recipientType) throws MessagingException {
		Address[] recipients = mail.getRecipients(recipientType);
		if (recipients == null) {
			return emptyList();
		}
		return list(recipients);
	}

	private List<String> toStrings(Collection<? extends Address> addresses) {
		return addresses.stream().map(Address::toString).collect(Collectors.toList());
	}

    public void testGetEmailAddresses() throws Exception {
        
        MailHelper theHelper = MailHelper.getInstance();
        assertNotNull(theHelper);
        
        List invalidAddresses = new ArrayList();
        List receiver         = new ArrayList();
        
        //resolve address from String
		receiver.add(getReceiver()); // ok
        receiver.add("root"); // root@top-logic.com
        receiver.add(INVALID_ADDRESS); // invalid
        List theAddresses = theHelper.getEmailAddresses(receiver, invalidAddresses);
        assertEquals(2, theAddresses.size());
        assertEquals(1, invalidAddresses.size());
        receiver.clear();
        invalidAddresses.clear();
        
        //resolve address from Person
        PersonManager thePM = PersonManager.getManager();
		receiver.add(Person.byName("dau")); // info3@top-logic.com
        receiver.add(Person.byName("guest_de")); // null
        
        theAddresses = theHelper.getEmailAddresses(receiver, invalidAddresses);
        assertEquals(1, theAddresses.size());
        assertEquals(0, invalidAddresses.size());
        receiver.clear();
        invalidAddresses.clear();
        
        //resolve address from Group
        Group theGroup = Group.createGroup("testmailuser");
		theGroup.addMember(Person.byName("dau")); // info3@top-logic.com
        theGroup.addMember(Person.byName("guest_de")); // null
        
        receiver.add(theGroup);
        theAddresses = theHelper.getEmailAddresses(receiver, invalidAddresses);
        assertEquals(1, theAddresses.size());
        assertEquals(0, invalidAddresses.size());
        receiver.clear();
        invalidAddresses.clear();
        
        //resolve address from something else
        receiver.add(new Date());
        receiver.add(null);
        theAddresses = theHelper.getEmailAddresses(receiver, invalidAddresses);
        assertEquals(0, theAddresses.size());
        assertEquals(0, invalidAddresses.size());
        receiver.clear();
        invalidAddresses.clear();
        
    }
    
	public void testSendMail() throws MessagingException {
        MailHelper theHelper = MailHelper.getInstance();
        assertNotNull(theHelper);
        
		String sender = getSender();
		List theReceiver = Collections.singletonList(getReceiver());
		String theSubject = TestMailHelper.newSubject();
		String theContent = createContent("testSendMail");
        SendMailResult theResult = null;

        // no sender
        theResult = theHelper.sendMail(null, theReceiver, theSubject, theContent, null);
        assertFalse(theResult.isSuccess());
        
        // subject = null
        theResult = theHelper.sendMail(sender, theReceiver, null, theContent, null);
        assertFalse(theResult.isSuccess());
        
        // content = null
        theResult = theHelper.sendMail(sender, theReceiver, theSubject, null, null);
        assertFalse(theResult.isSuccess());

        // invalid address
        theResult = theHelper.sendMail(sender, Collections.singletonList(INVALID_ADDRESS), theSubject, theContent, null);
        assertFalse(theResult.isSuccess());
        
        // unknown receiver on remote SMTP server
        // this test will not fail because MS-Exchange does not inform us about the unknown receiver
//        theResult = theHelper.sendMail(VALID_SENDER, Collections.singletonList(UNKNOWN_ADDRESS), theSubject, theContent, null);
//        assertFalse(theResult.isSuccess());
        
        // all ok
        theResult = theHelper.sendMail(sender, theReceiver, theSubject, theContent, null);
        assertTrue(theResult.getErrorResultString().toString() + theResult.getException(), theResult.isSuccess());
		TestMailHelper.deleteSentMail(theSubject);
    }
    
	public void testSendSystemMail() throws MessagingException {
        MailHelper theHelper = MailHelper.getInstance();
        assertNotNull(theHelper);
        
		List theReceiver = Collections.singletonList(getReceiver());
		String theSubject = TestMailHelper.newSubject();
		String theContent = createContent("testSendSystemMail");
        SendMailResult theResult = null;
        
        theResult = theHelper.sendSystemMail(theReceiver, theSubject, theContent, null);
        assertTrue(theResult.getErrorResultString().toString() + theResult.getException(), theResult.isSuccess());
        assertEquals(theResult.getMail().getSender().toString(), MailSenderService.getFromAddress());
		TestMailHelper.deleteSentMail(theSubject);
    }
    
    public void testExtractMailFormat() throws Exception {
        assertEquals("my.name@example.com", MailHelper.extractEmailAddress(new InternetAddress("my.name@example.com")));
        assertEquals("my.name@example.com", MailHelper.extractEmailAddress(new InternetAddress("May Name <my.name@example.com>")));
        assertEquals("my.name@example.com", MailHelper.extractEmailAddress(new InternetAddress("May Name<my.name@example.com  >")));
        assertEquals("my.name@example.com", MailHelper.extractEmailAddress(new InternetAddress("May Name<my.name@example.com  >")));
        assertEquals("my.name@example.com", MailHelper.extractEmailAddress(new InternetAddress("May Name<my.name@example.com  > something strage")));
    }

	private String createContent(String methodName) {
		return "Class: " + MailHelper.getInstance().getClass()
			+ "\nTestcase: " + getClass()
			+ "\nTest: " + methodName + "()"
			+ "\n" + new Date();
	}

    /**
	 * Creates a new title containing the current time.
	 * 
	 * <p>
	 * Note: This method is synchronized to ensure that different thread gets different timestamps.
	 * </p>
	 * 
	 * <p>
	 * The mail sent to {@link #getReceiver()} must be removed after test using
	 * {@link #deleteSentMail(String)}.
	 */
	public static synchronized String newSubject() {
		try {
			// Ensure new timestamp
			Thread.sleep(1);
		} catch (InterruptedException ex) {
			// ignore
		}
		String time = CalendarUtil.newSimpleDateFormat("HH:mm:ss.SSS", Locale.GERMANY).format(new Date());
		return "TestMail gesendet von " + System.getProperty("user.name") + " um " + time;
	}

	/**
	 * Fetches a mail with the given mail subject.
	 * 
	 * <p>
	 * This method returns the mail formerly sent to {@link #getReceiver()} with the given subject.
	 * </p>
	 * 
	 * @param mailSubject
	 *        The subject of the mail to fetch. Use {@link #newSubject()} for unique subject.
	 */
	public static Message getSentMail(String mailSubject) throws MessagingException {
		int i = 10;
		while (true) {
			Message m = TestMailHelper.internalGetSendMail(mailSubject);
			if (m != null) {
				return m;
			}
			i--;
			if (i == 0) {
				throw new MessagingException("Unable to get mail: " + mailSubject);
			}
			try {
				// Wait until mail is arrived on server.
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				throw new MessagingException("Unable to get mail: " + mailSubject, ex);
			}
		}
	}

	private static Message internalGetSendMail(String mailSubject) throws MessagingException {
		MailReceiver mailServer = MailReceiverService.getMailReceiverInstance();
		mailServer.connect();
		try {
			Folder folder = mailServer.getInbox();
			for (Message m : folder.getMessages()) {
				if (mailSubject.equals(m.getSubject())) {
					return m;
				}
			}
			return null;
		} finally {
			mailServer.disconnect();
		}
	}

	/**
	 * Removes a mail with the given mail subject.
	 * 
	 * <p>
	 * This method removes the mail formerly sent to {@link #getReceiver()} with the given subject.
	 * </p>
	 * 
	 * @param mailSubject
	 *        The subject of the mail to remove. Use {@link #newSubject()} for unique subject.
	 */
	public static void deleteSentMail(String mailSubject) throws MessagingException {
		int i = 10;
		while (true) {
			boolean found = TestMailHelper.internalClearSendMail(mailSubject);
			if (found) {
				return;
			}
			i--;
			if (i == 0) {
				throw new MessagingException("Unable to delete mail: " + mailSubject);
			}
			try {
				// Wait until mail is arrived on server.
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				return;
			}
		}
	}

	private static boolean internalClearSendMail(String mailSubject) throws MessagingException {
		MailReceiver mailServer = MailReceiverService.getMailReceiverInstance();
		mailServer.connect();
		try {
			Folder folder = mailServer.getInbox();
			try {
				boolean found = false;
				for (Message m : folder.getMessages()) {
					if (mailSubject.equals(m.getSubject())) {
						m.setFlag(Flags.Flag.DELETED, true);
						found = true;
						break;
					}
				}
				return found;
			} finally {
				folder.expunge();
			}
		} finally {
			mailServer.disconnect();
		}
	}

	/**
	 * E-Mail address of user {@link #VALID_USER_NAME}.
	 * 
	 * <p>
	 * See also "Users.test.xml"
	 * </p>
	 */
	public static String getReceiver() {
		return AliasManager.getInstance().getAlias(USER_ALIAS);
	}

	private static String getSender() {
		return getReceiver();
	}

    /**
     * The method constructing a test suite for this class.
     *
     * @return    The test to be executed.
     */
     public static Test suite () {
        Test innerTest = new TestSuite(TestMailHelper.class);
		innerTest = new CreateDefaultTestContacts(innerTest);
		innerTest = ServiceTestSetup.createSetup(innerTest,
			MailSenderService.Module.INSTANCE,
			MailReceiverService.Module.INSTANCE,
			MailHelper.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(innerTest);
     }

    /**
     * The main program for executing this test also from console.
     *
     * @param    args    Will be ignored.
     */
     public static void main (String[] args) {
         SHOW_TIME = true;
         
         Logger.configureStdout();   // "INFO"
         
         TestRunner.run (suite ());
     }
}


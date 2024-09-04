/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.mail.MailSenderService;
import com.top_logic.basic.col.factory.CollectionFactory;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.mail.base.MailServer;
import com.top_logic.mail.base.imap.IMAPMail;
import com.top_logic.mail.proxy.Attachments;
import com.top_logic.mail.proxy.exchange.ExchangeMail;
import com.top_logic.model.TLObject;
import com.top_logic.util.sched.Scheduler;

/**
 * Tests mails with multipart content.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestMailMultipart extends BasicTestCase {

	private KnowledgeBase _kb;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
	}

	public void testMultipartRelated()
			throws MessagingException, IOException, DataObjectException, InterruptedException {
		Transaction createTx = _kb.beginTransaction();
		TestMailFolderAwareWrapper wrapper = TestMailFolderAwareWrapper.newInstance(_kb, TestMailAttachment.class);
		createTx.commit();

		try {
			checkMultipartRelated(wrapper);
		} finally {
			Transaction deleteTx = _kb.beginTransaction();
			wrapper.deleteWrapperAndFolder();
			deleteTx.commit();
		}
	}

	private void checkMultipartRelated(TestMailFolderAwareWrapper wrapper)
			throws AddressException, MessagingException, IOException, InterruptedException {
		String title =
			TestingMailProcessor.appendTarget(
				new StringBuilder("Test " + TestMailMultipart.class.getSimpleName() + "#testMultipartRelated()"),
				wrapper).toString();
		sendMultipartMail(title);
		wrapper.fetchMails();
		List<? extends TLObject> mails = CollectionFactory.list(wrapper.getMailFolder().getContent());
		assertEquals(1, mails.size());
		ExchangeMail mail = new ExchangeMail(((IMAPMail) mails.get(0)).getOriginalMail());

		Attachments attachments = mail.getAttachments();
		assertNotNull(attachments);
		assertEquals(1, attachments.getCount());
	}

	protected void sendMultipartMail(String title) throws MessagingException, IOException, AddressException {
		List<Address> invalidAddress = new ArrayList<>();
		MailSenderService mailTransport = MailSenderService.getInstance();
		Message message = mailTransport.createEmptyMessage();
		message.setSubject(title);
		MimeMultipart multipart = new MimeMultipart("related");

		// first part (the html)
		MimeBodyPart htmlBodyPart = new MimeBodyPart();
		String imageID = "image";
		String htmlText = "<html><body><h1>Image</h1><img src=\"cid:" + imageID + "\" /></body></html>";
		htmlBodyPart.setContent(htmlText, "text/html");
		String rootID = "root";
		htmlBodyPart.setContentID("<" + rootID + ">");
		multipart.addBodyPart(htmlBodyPart);

		// second part (the image)
		MimeBodyPart attachment = new MimeBodyPart();
		DataSource fds = MailTestUtils.newAttachment(TestMailAttachment.getImage());
		attachment.setDataHandler(new DataHandler(fds));
		attachment.setContentID("<" + imageID + ">");
		multipart.addBodyPart(attachment);

		message.setContent(multipart);
		message.setRecipient(Message.RecipientType.TO, new InternetAddress(TestMailHelper.getReceiver()));
		mailTransport.send(message, invalidAddress, false);
	}

	/**
	 * The method constructing a test suite for this class.
	 *
	 * @return The test to be executed.
	 */
	public static Test suite() {
		Test innerTest = ServiceTestSetup.createSetup(TestMailMultipart.class,
			MimeTypes.Module.INSTANCE,
			MailSenderService.Module.INSTANCE,
			MailServer.Module.INSTANCE,
			Scheduler.Module.INSTANCE,
			PersistencyLayer.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(innerTest);
	}

}

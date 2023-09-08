/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.mail.MessagingException;

import junit.framework.Test;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.mail.MailHelper;
import com.top_logic.base.mail.MailSenderService;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.mail.base.MailFolder;
import com.top_logic.mail.base.MailServer;
import com.top_logic.mail.base.imap.IMAPMail;
import com.top_logic.util.sched.Scheduler;

/**
 * Test for mail attachment
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestMailAttachment extends BasicTestCase {

	private final static String IMAGE_FILE = TestMailAttachment.class.getSimpleName() + "_img1.png";

	public final static String IMAGE_CONTENT_TYPE = "image/png";

	private KnowledgeBase _kb;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
	}

	public void testContentType()
			throws MessagingException, IOException, InterruptedException {
		Transaction createTx = _kb.beginTransaction();
		TestMailFolderAwareWrapper wrapper = TestMailFolderAwareWrapper.newInstance(_kb, TestMailAttachment.class);
		createTx.commit();

		try {
			checkContentType(wrapper);
		} finally {
			Transaction deleteTx = _kb.beginTransaction();
			wrapper.deleteWrapperAndFolder();
			deleteTx.commit();
		}
	}

	public void checkContentType(TestMailFolderAwareWrapper wrapper)
			throws IOException, InterruptedException {
		String subject =
			TestingMailProcessor.appendTarget(new StringBuilder("Mail for testContentType"), wrapper).toString();

		List<Object> attachments = new ArrayList<>();
		attachments.add(MailTestUtils.newAttachment("myFunnyAttachmentName", getImage()));

		sendMail(subject, attachments, "MailWithAttachement");
		wrapper.fetchMails();
		checkAttachment(wrapper);
	}

	public static BinaryData getImage() throws IOException {
		byte[] data;
		try (InputStream stream = getResourceByName(TestMailAttachment.class, IMAGE_FILE)) {
			data = StreamUtilities.readStreamContents(stream);
		}
		return BinaryDataFactory.createBinaryData(data, IMAGE_CONTENT_TYPE);
	}

	private void sendMail(String subject, List<Object> attachments, String content) {
		MailHelper mailHelper = MailHelper.getInstance();
		mailHelper.sendSystemMail(list(TestMailHelper.getReceiver()), list(), list(), subject, content,
			attachments, MailHelper.CONTENT_TYPE_TEXT);
	}

	private void checkAttachment(TestMailFolderAwareWrapper wrapper) {
		MailFolder mailFolder = wrapper.getMailFolder();
		Wrapper[] contents = mailFolder.getContent().toArray(new Wrapper[0]);
		for (Wrapper content : contents) {
			IMAPMail mail = (IMAPMail) content;
			Collection<Document> attachements = mail.getAttachements();
			assertEquals(1, attachements.size());
			Document attachment = attachements.iterator().next();
			assertEquals(IMAGE_CONTENT_TYPE, attachment.getContentType());
		}
	}

	/**
	 * The method constructing a test suite for this class.
	 *
	 * @return The test to be executed.
	 */
	public static Test suite() {
		Test innerTest = ServiceTestSetup.createSetup(TestMailAttachment.class,
			MimeTypes.Module.INSTANCE,
			MailSenderService.Module.INSTANCE,
			MailServer.Module.INSTANCE,
			Scheduler.Module.INSTANCE,
			PersistencyLayer.Module.INSTANCE,
			MailHelper.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(innerTest);
	}

}


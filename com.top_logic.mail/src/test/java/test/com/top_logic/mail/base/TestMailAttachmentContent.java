/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.base;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;

import jakarta.mail.internet.InternetHeaders;
import jakarta.mail.internet.MimeBodyPart;

import junit.framework.Test;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.mail.base.Mail;
import com.top_logic.mail.base.MailFactory;
import com.top_logic.mail.proxy.Attachments;
import com.top_logic.mail.proxy.Attachments.Attachment;

/**
 * Test of the content of mail attachments and of the data source name of mails, which need no mail
 * server.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestMailAttachmentContent extends BasicTestCase {

	private KnowledgeBase _kb;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * The document of an attachment stores the content of the attachment.
	 */
	public void testAttachmentContent() throws Exception {
		byte[] image = StreamUtilities.readStreamContents(TestMailAttachment.getImage());

		InternetHeaders headers = new InternetHeaders();
		headers.setHeader("Content-Type", TestMailAttachment.IMAGE_CONTENT_TYPE + "; name=image.png");
		headers.setHeader("Content-Disposition", "attachment; filename=image.png");
		headers.setHeader("Content-Transfer-Encoding", "base64");
		MimeBodyPart part = new MimeBodyPart(headers,
			Base64.getMimeEncoder().encode(image));
		Attachment attachment = new Attachments().new Attachment(part, 1);

		Document document;
		try (Transaction tx = _kb.beginTransaction()) {
			document = MailFactory.createAttachment(attachment, _kb);
			tx.commit();
		}
		try {
			assertEquals("image.png", document.getName());
			assertEquals(TestMailAttachment.IMAGE_CONTENT_TYPE, document.getContentType());
			assertEquals(1, document.getVersionNumber());
			assertEquals(image.length, document.getSize());

			BinaryData content = document.getStoredContent();
			assertNotNull(content);
			assertEquals(TestMailAttachment.IMAGE_CONTENT_TYPE, content.getContentType());
			try (InputStream in = document.getContent()) {
				assertTrue(Arrays.equals(image, StreamUtilities.readStreamContents(in)));
			}
		} finally {
			try (Transaction tx = _kb.beginTransaction()) {
				document.getAllDocumentVersions().forEach(version -> version.tDelete());
				document.tDelete();
				tx.commit();
			}
		}
	}

	/**
	 * The data source name of a mail is stored in the attribute {@link Mail#MAIL_URL}.
	 */
	public void testMailURL() throws Exception {
		Mail mail;
		try (Transaction tx = _kb.beginTransaction()) {
			KnowledgeObject handle = _kb.createKnowledgeObject(Mail.OBJECT_NAME);
			handle.setAttributeValue(Mail.NAME, "subject");
			handle.setAttributeValue(Mail.MAIL_ID, "id-1");
			handle.setAttributeValue(Mail.HAS_ATTACHMENT, Boolean.FALSE);
			handle.setAttributeValue(Mail.MAIL_URL, "mail://INBOX?id-1");
			mail = MailFactory.getMail(handle);
			tx.commit();
		}
		try {
			assertEquals("mail://INBOX?id-1", mail.getMailURL());
		} finally {
			try (Transaction tx = _kb.beginTransaction()) {
				mail.tDelete();
				tx.commit();
			}
		}
	}

	public static Test suite() {
		Test innerTest = ServiceTestSetup.createSetup(TestMailAttachmentContent.class,
			MimeTypes.Module.INSTANCE,
			PersistencyLayer.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(innerTest);
	}

}

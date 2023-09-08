/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.base;

import static test.com.top_logic.mail.base.MailTestUtils.*;

import javax.mail.Folder;
import javax.mail.MessagingException;

import junit.framework.AssertionFailedError;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.StringID;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.mail.base.MailFolder;
import com.top_logic.mail.base.MailFolderAware;
import com.top_logic.mail.base.MailUtils;
import com.top_logic.util.sched.task.Task;

/**
 * Wrapper class for objects of type {@link MailTestUtils#TEST_MFA_KO_TYPE}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestMailFolderAwareWrapper extends AbstractWrapper implements MailFolderAware {

	private static final String DEFAULT_FOLDER_NAME = TestMailFolderAwareWrapper.class.getSimpleName();

	/**
	 * New {@link TestMailFolderAwareWrapper} with default
	 * {@link MailFolderAware#getMailFolderName()} .
	 */
	public static TestMailFolderAwareWrapper newInstance(KnowledgeBase kb) throws DataObjectException {
		return newInstance(kb, DEFAULT_FOLDER_NAME);
	}

	/**
	 * New {@link TestMailFolderAwareWrapper} with unique
	 * {@link MailFolderAware#getMailFolderName()}.
	 * 
	 * <p>
	 * To avoid garbage on the server ensure that the mail folder is deleted after test.
	 * </p>
	 * 
	 * @see #deleteWrapperAndFolder()
	 */
	public static TestMailFolderAwareWrapper newInstance(KnowledgeBase kb, Class<?> testClass) throws DataObjectException {
		return newInstance(kb, uniqueFolderName(testClass));
	}

	private static String uniqueFolderName(Class<?> testClass) {
		return testClass.getSimpleName() + '_' + IdentifierUtil.toExternalForm(StringID.createRandomID());
	}

	static TestMailFolderAwareWrapper newInstance(KnowledgeBase kb, String folderName) throws DataObjectException {
		KnowledgeObject ko = kb.createKnowledgeObject(TEST_MFA_KO_TYPE);
		ko.setAttributeValue(TEST_MFA_FOLDER_NAME_ATTR, folderName);
		return (TestMailFolderAwareWrapper) ko.getWrapper();
	}

	/**
	 * Creates a new {@link TestMailFolderAwareWrapper}.
	 */
	public TestMailFolderAwareWrapper(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public MailFolder getMailFolder() {
		return MailUtils.getMailFolder(this);
	}

	@Override
	public String getMailFolderName() {
		return getString(TEST_MFA_FOLDER_NAME_ATTR);
	}

	/**
	 * Deletes this wrapper and the mail {@link Folder} from the server.
	 */
	public void deleteWrapperAndFolder() throws MessagingException {
		MailFolder mailFolder = getMailFolder();
		if (mailFolder != null) {
			MailTestUtils.deleteFolderAndWrapper(mailFolder);
		}
		tDelete();
	}

	/**
	 * Fetches the mails for this wrapper.
	 */
	public void fetchMails() throws InterruptedException {
		Task mailDaemon = MailTestUtils.getMailServerDaemon();
		int retry = 10;
		while (true) {
			if (retry-- == 0) {
				throw new AssertionFailedError("No folder " + getMailFolderName() + " for wrapper " + this);
			}
			if (getMailFolder() != null) {
				break;
			}
			if (!isAttachedToScheduler(mailDaemon)) {
				/* Task is not yet attached to Scheduler; calling run() leads to NPE. */
				Thread.sleep(1000);
				continue;
			}
			mailDaemon.run();
			Thread.sleep(1000);
		}
	}

	private static boolean isAttachedToScheduler(Task task) {
		return task.getLog() != null;
	}

}


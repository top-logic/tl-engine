/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.mail.Folder;
import javax.mail.MessagingException;

import test.com.top_logic.basic.ReflectionUtils;

import com.top_logic.base.mail.Mail;
import com.top_logic.base.mail.MailHelper;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dob.MOAttribute;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.mail.base.MailFolder;
import com.top_logic.mail.base.MailFolderAware;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;

/**
 * Constants used in non java files and service methods for mail tests.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class MailTestUtils {

	/**
	 * Name of the {@link Task} fetching mails using {@link TestingMailProcessor}.
	 * 
	 * <p>
	 * See mail.test.config.xml
	 * </p>
	 */
	private static final String TEST_MAIL_SERVER_DAEMON = "TestMailServerDaemon";

	/**
	 * Name of the KO type holding {@link MailFolderAware} {@link KnowledgeObject}.
	 * 
	 * <p>
	 * See mailTestMeta.xml
	 * </p>
	 */
	public static String TEST_MFA_KO_TYPE = "TestMailFolderAware";

	/**
	 * Name of the {@link MOAttribute} of {@link #TEST_MFA_KO_TYPE} holding the name of the mail
	 * folder.
	 * 
	 * <p>
	 * See mailTestMeta.xml
	 * </p>
	 */
	public static String TEST_MFA_FOLDER_NAME_ATTR = "folderName";

	/**
	 * Returns the {@link Task} with name {@link #TEST_MAIL_SERVER_DAEMON}.
	 */
	public static Task getMailServerDaemon() {
		Scheduler scheduler = Scheduler.getSchedulerInstance();
		Task task = scheduler.getTaskByName(TEST_MAIL_SERVER_DAEMON);
		if (task == null) {
			throw new ConfigurationError("No task with name " + TEST_MAIL_SERVER_DAEMON + " available.");
		}
		return task;
	}

	/**
	 * Returns the java mail {@link Folder} for a {@link MailFolder}.
	 */
	public static Folder getOriginalFolder(MailFolder mailFolder) {
		return (Folder) ReflectionUtils.executeMethod(mailFolder, "getOriginalFolder", new Class[0], new Object[0]);
	}

	/**
	 * Removes the given {@link MailFolder} from the {@link KnowledgeBase} and from the mail server.
	 * 
	 * @param mailFolder
	 *        The folder to delete.
	 * @throws MessagingException
	 *         When the corresponding {@link Folder} throws an exception.
	 */
	public static void deleteFolderAndWrapper(MailFolder mailFolder) throws MessagingException {
		Folder originalFolder = getOriginalFolder(mailFolder);
		originalFolder.close(true);
		originalFolder.delete(true);
	}

	/**
	 * Creates an object that may serve as attachment in {@link Mail}.
	 * 
	 * @see Mail#addAttachment(DataSource)
	 * @see MailHelper#sendMail(Object, java.util.List, java.util.List, java.util.List, String,
	 *      String, java.util.List, String)
	 * 
	 * @param name
	 *        The name of the attachment.
	 * @param data
	 *        The data of the attachment.
	 */
	public static DataSource newAttachment(final String name, final BinaryData data) {
		return new DataSource() {
	
			@Override
			public String getContentType() {
				return data.getContentType();
			}
	
			@Override
			public InputStream getInputStream() throws IOException {
				return data.getStream();
			}
	
			@Override
			public String getName() {
				return name;
			}
	
			@Override
			public OutputStream getOutputStream() throws IOException {
				throw new IOException("No Output here");
			}
	
		};
	}

	/**
	 * Creates an object that may serve as attachment in {@link Mail}. The name of the attachment is
	 * taken form the data.
	 * 
	 * @see #newAttachment(String, BinaryData)
	 */
	public static DataSource newAttachment(BinaryData data) {
		return newAttachment(data.getName(), data);
	}
}


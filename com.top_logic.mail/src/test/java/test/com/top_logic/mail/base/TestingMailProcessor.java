/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.base;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.LongID;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.mail.base.MailFolderAware;
import com.top_logic.mail.proxy.AbstractMailProcessor;
import com.top_logic.mail.proxy.ConfiguredMailServerDaemon;
import com.top_logic.mail.proxy.MailMessage;
import com.top_logic.mail.proxy.MailProcessor;
import com.top_logic.mail.proxy.MailServerMessage;
import com.top_logic.mail.proxy.exchange.ExchangeMeeting;

/**
 * Simple {@link MailProcessor} expecting the identifier and the type of an {@link MailFolderAware}
 * in the subject of the mail.
 * 
 * <p>
 * All test runs share the same mailbox. This processor only handles mails of the current test run
 * (see {@link TestMailHelper#isOwnMail(String)}). Mails of other test runs are left untouched, so
 * that the other test run can process or delete them itself.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestingMailProcessor extends AbstractMailProcessor {

	private static final String TARGET_NAME = " (target-id: ";

	private static final String TARGET_TYPE = ", target-type: ";

	private static final String REST = ")";

	private static final Pattern IDENTIFIER_PATTERN = Pattern
		.compile(Pattern.quote(TARGET_NAME) + "(\\d*)" + Pattern.quote(TARGET_TYPE) + "(\\w*)" + Pattern.quote(REST));

	/**
	 * Appends a string to the subject of the mail representing the given {@link MailFolderAware
	 * target} to be found by parsing mail, and the {@link TestMailHelper#getRunMarker()} that
	 * identifies the mail as mail of this test run.
	 * 
	 * @param messageSubject
	 *        The subject of the message to append target to.
	 * @param targetWrapper
	 *        The {@link MailFolderAware} to receive the mail.
	 * 
	 * @return The given message subject.
	 */
	public static StringBuilder appendTarget(StringBuilder messageSubject, MailFolderAware targetWrapper) {
		KnowledgeObject wrappedObject = targetWrapper.tHandle();
		messageSubject.append(TARGET_NAME);
		messageSubject.append(((LongID) wrappedObject.getObjectName()).longValue());
		messageSubject.append(TARGET_TYPE);
		messageSubject.append(wrappedObject.tTable().getName());
		messageSubject.append(REST);
		messageSubject.append(' ');
		messageSubject.append(TestMailHelper.getRunMarker());
		return messageSubject;
	}

	/**
	 * Processes only mails of this test run.
	 * 
	 * <p>
	 * A mail of another test run is neither processed nor deleted, and no flag of it is changed.
	 * </p>
	 */
	@Override
	protected boolean internalProcessMail(MailMessage mail, ConfiguredMailServerDaemon mailServerDaemon, Object token) {
		if (!TestMailHelper.isOwnMail(mail.getName())) {
			return false;
		}
		return super.internalProcessMail(mail, mailServerDaemon, token);
	}

	@Override
	public boolean processMeeting(ExchangeMeeting meeting, ConfiguredMailServerDaemon mailServerDaemon, Object token) {
		return false;
	}

	@Override
	public MailFolderAware getMailFolderAware(MailServerMessage message, ConfiguredMailServerDaemon mailServerDaemon,
			Object token) {
		String subject = message.getName();
		if (!TestMailHelper.isOwnMail(subject)) {
			return null;
		}
		Matcher matcher = IDENTIFIER_PATTERN.matcher(subject);
		if (matcher.find()) {
			long id = Long.parseLong(matcher.group(1));
			String type = matcher.group(2);
			KnowledgeObject ko = PersistencyLayer.getKnowledgeBase().getKnowledgeObject(type, LongID.valueOf(id));
			if (ko != null) {
				return (MailFolderAware) WrapperFactory.getWrapper(ko);
			}
		}
		return null;
	}

}


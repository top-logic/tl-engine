/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

import com.top_logic.mail.base.MailFolderAware;

/**
 * Abstract {@link MailProcessor} moving all relevant {@link MailMessage} to the folder of the
 * identified {@link MailFolderAware}, unknown mails to "unknown" folder.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractMailProcessor implements MailProcessor {

	@Override
	public boolean processMailFromSelf(MailMessage mail, ConfiguredMailServerDaemon mailServerDaemon, Object token) {
		return internalProcessMail(mail, mailServerDaemon, token);
	}

	/**
	 * Common implementation of
	 * {@link #processMailFromSelf(MailMessage, ConfiguredMailServerDaemon, Object)},
	 * {@link #processMailInternal(MailMessage, ConfiguredMailServerDaemon, Object)}, and
	 * {@link #processReportMail(MailMessage, ConfiguredMailServerDaemon, Object)}.
	 */
	protected boolean internalProcessMail(MailMessage mail, ConfiguredMailServerDaemon mailServerDaemon, Object token) {
		MailFolderAware mailFolderAware = getMailFolderAware(mail, mailServerDaemon, token);
		if (mailFolderAware == null) {
			return mailServerDaemon.processUnknownMail(mail, token);
		}
		return mailServerDaemon.moveMailToFolder(mail, mailFolderAware, token);
	}

	@Override
	public boolean processMailInternal(MailMessage mail, ConfiguredMailServerDaemon mailServerDaemon, Object token) {
		return internalProcessMail(mail, mailServerDaemon, token);
	}

	@Override
	public boolean processReportMail(MailMessage mail, ConfiguredMailServerDaemon mailServerDaemon, Object token) {
		return internalProcessMail(mail, mailServerDaemon, token);
	}


}


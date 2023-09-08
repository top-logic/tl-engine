/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

import com.top_logic.mail.base.MailFolderAware;
import com.top_logic.mail.proxy.exchange.ExchangeMeeting;

/**
 * Plugin for the {@link ConfiguredMailServerDaemon}.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface MailProcessor {

	/**
	 * Check if the Mail is from our own server (e.g. send via BCC) and handle this mail.
	 * 
	 * @param mailServerDaemon
	 *        The {@link ConfiguredMailServerDaemon} this {@link MailProcessor} belongs to.
	 * @param token
	 *        Token to access certain public methods of {@link ConfiguredMailServerDaemon}.
	 */
	boolean processMailFromSelf(MailMessage mail, ConfiguredMailServerDaemon mailServerDaemon, Object token);
	
	/**
     * Check if the mail has been send by the mail server itself (e.g. cannot deliver mail) and handle this mail.
	 * 
	 * @param mailServerDaemon
	 *        The {@link ConfiguredMailServerDaemon} this {@link MailProcessor} belongs to.
	 * @param token
	 *        Token to access certain public methods of {@link ConfiguredMailServerDaemon}.
	 */
	boolean processReportMail(MailMessage mail, ConfiguredMailServerDaemon mailServerDaemon, Object token);

	/**
	 * Handle a mail that comes from an external sender.
	 * 
	 * @param mailServerDaemon
	 *        The {@link ConfiguredMailServerDaemon} this {@link MailProcessor} belongs to.
	 * @param token
	 *        Token to access certain public methods of {@link ConfiguredMailServerDaemon}.
	 */
	boolean processMailInternal(MailMessage mail, ConfiguredMailServerDaemon mailServerDaemon, Object token);

	/**
	 * Process a meeting within the application.
	 * 
	 * @param meeting
	 *        The meeting to be processed.
	 * 
	 * @param mailServerDaemon
	 *        The {@link ConfiguredMailServerDaemon} this {@link MailProcessor} belongs to.
	 * @param token
	 *        Token to access certain public methods of {@link ConfiguredMailServerDaemon}.
	 * @return <code>true</code>, if processing succeeds and meeting can accepted,
	 *         <code>false</code>, when meeting has to be declined.
	 * @see ExchangeMeeting#reply(boolean)
	 */
	boolean processMeeting(ExchangeMeeting meeting, ConfiguredMailServerDaemon mailServerDaemon, Object token);

	/**
	 * Return a {@link MailFolderAware} for this message.
	 * 
	 * @param mailServerDaemon
	 *        The {@link ConfiguredMailServerDaemon} this {@link MailProcessor} belongs to.
	 * @param token
	 *        Token to access certain public methods of {@link ConfiguredMailServerDaemon}.
	 * 
	 * @return May be <code>null</code> if there is no {@link MailFolderAware} for the given mail.
	 */
	MailFolderAware getMailFolderAware(MailServerMessage message, ConfiguredMailServerDaemon mailServerDaemon,
			Object token);
}

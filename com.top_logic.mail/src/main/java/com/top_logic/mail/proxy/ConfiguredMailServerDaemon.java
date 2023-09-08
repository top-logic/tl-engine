/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.mail.base.MailFolder;
import com.top_logic.mail.base.MailFolderAware;
import com.top_logic.mail.proxy.exchange.ExchangeMeeting;

/**
 * {@link AbstractMailServerDaemon} whose abstract methods are redirected.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class ConfiguredMailServerDaemon extends AbstractMailServerDaemon {

	private static Object TOKEN = new NamedConstant("token");

	/**
	 * Configuration of a {@link ConfiguredMailServerDaemon}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractMailServerDaemon.Config {

		/**
		 * The actual mail handling plugin.
		 */
		@InstanceFormat
		@Mandatory
		MailProcessor getProcessor();

	}

	private final MailProcessor _processor;

	/**
	 * Creates a new {@link ConfiguredMailServerDaemon}.
	 */
	public ConfiguredMailServerDaemon(InstantiationContext context, Config config) {
		super(context, config);
		_processor = config.getProcessor();
	}

	@Override
	protected boolean processMailFromSelf(MailMessage aMail) {
		return _processor.processMailFromSelf(aMail, this, TOKEN);
	}
	
	@Override
	protected boolean processReportMail(MailMessage aMail) {
		return _processor.processReportMail(aMail, this, TOKEN);
	}

	@Override
	protected boolean processMailInternal(MailMessage aMail) {
		return _processor.processMailInternal(aMail, this, TOKEN);
	}

	@Override
	protected boolean processMeeting(ExchangeMeeting aMeeting) {
		return _processor.processMeeting(aMeeting, this, TOKEN);
	}

	@Override
	protected MailFolderAware getMailFolderAware(MailServerMessage aMessage) {
		return _processor.getMailFolderAware(aMessage, this, TOKEN);
	}

	/**
	 * Public access to {@link #moveMailToFolder(MailMessage, MailFolder)}.
	 */
	public boolean moveMailToFolder(MailMessage aMail, MailFolder aFolder, Object token) {
		checkToken(token);
		return moveMailToFolder(aMail, aFolder);
	}

	/**
	 * Public access to {@link #moveMailToFolder(MailMessage, MailFolderAware)}.
	 */
	public boolean moveMailToFolder(MailMessage aMail, MailFolderAware anAware, Object token) {
		checkToken(token);
		return moveMailToFolder(aMail, anAware);
	}

	/**
	 * Public access to {@link #processUnknownMail(MailMessage)}.
	 */
	public boolean processUnknownMail(MailMessage aMail, Object token) {
		checkToken(token);
		return processUnknownMail(aMail);
	}

	/**
	 * Public access to {@link #deleteMail(MailServerMessage)}.
	 */
	public boolean deleteMail(MailServerMessage aMailMessage, Object token) {
		checkToken(token);
		return deleteMail(aMailMessage);
	}

	/**
	 * Public access to {@link #undeleteMail(MailServerMessage)}.
	 */
	public boolean undeleteMail(MailServerMessage aMailMessage, Object token) {
		checkToken(token);
		return undeleteMail(aMailMessage);
	}

	private static void checkToken(Object token) {
		if (token != TOKEN) {
			throw new IllegalArgumentException("Called without correct token.");
		}
	}

}


/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.mail;

import com.top_logic.basic.i18n.CustomKey;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	@CustomKey("mail.send.failed.formatException")
	public static ResKey ERROR_FORMAT_EXCEPTION;

	@CustomKey("mail.send.failed.invalidAddresses")
	public static ResKey ERROR_INVALID_ADDRESSES;

	@CustomKey("mail.send.failed.messageException")
	public static ResKey ERROR_MESSAGE_EXCEPTION;

	@CustomKey("mail.send.failed.notActive")
	public static ResKey ERROR_NO_EMAIL_SUPPORT;

	@CustomKey("mail.send.failed.noContent")
	public static ResKey ERROR_NO_CONTENT;

	@CustomKey("mail.send.failed.noReceiver")
	public static ResKey ERROR_NO_RECEIVER;

	@CustomKey("mail.send.failed.noSender")
	public static ResKey ERROR_NO_SENDER;

	@CustomKey("mail.send.failed.noTitle")
	public static ResKey ERROR_NO_TITLE;

	@CustomKey("mail.send.failed.unknown")
	public static ResKey ERROR_UNKNOWN_REASON;

	/**
	 * @en The SMTP service is not active. Before sending an e-mail activate SMTP or contact your
	 *     system administrator.
	 */
	public static ResKey SMTP_NOT_ACTIVE;

	static {
		initConstants(I18NConstants.class);
	}
}

/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.mail;

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

	public static ResKey ERROR_FORMAT_EXCEPTION = legacyKey("mail.send.failed.formatException");

	public static ResKey ERROR_INVALID_ADDRESSES = legacyKey("mail.send.failed.invalidAddresses");

	public static ResKey ERROR_MESSAGE_EXCEPTION = legacyKey("mail.send.failed.messageException");

	public static ResKey ERROR_NO_EMAIL_SUPPORT = legacyKey("mail.send.failed.notActive");

	public static ResKey ERROR_NO_CONTENT = legacyKey("mail.send.failed.noContent");

	public static ResKey ERROR_NO_RECEIVER = legacyKey("mail.send.failed.noReceiver");

	public static ResKey ERROR_NO_SENDER = legacyKey("mail.send.failed.noSender");

	public static ResKey ERROR_NO_TITLE = legacyKey("mail.send.failed.noTitle");

	public static ResKey ERROR_UNKNOWN_REASON = legacyKey("mail.send.failed.unknown");

	/**
	 * @en SMTP service is not active in this instance. Before sending e-mail activate SMTP or
	 *     contact your system administrator.
	 */
	public static ResKey SMTP_NOT_ACTIVE;

	static {
		initConstants(I18NConstants.class);
	}
}

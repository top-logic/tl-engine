/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.mail;

import com.top_logic.base.mail.MailHelper;

/**
 * Contains constants used for sending mails
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class MailConstants {

	/** The ID of the knowledge object to be attached. */
	public static String ATTACHMENT = "attachment";

	/** The content of the mail. */
	public static final String CONTENT = "mailtext";

	/** The subject of the mail. */
	public static final String SUBJECT = "subject";

	/** The blind carbon copy of the mail. */
	public static final String BCC = "bcc";

	/** The carbon copy of the mail. */
	public static final String CC = "cc";

	/** The receiver of the mail. */
	public static final String TO = "to";

	/** The sender of the mail. */
	public static final String SENDER = "sender";

	/** The result of sending the mail. */
	public static final String RESULT = "result";

	/** The default content type of the mail */
	public static final String CONTENT_TYPE = MailHelper.CONTENT_TYPE_TEXT;

}


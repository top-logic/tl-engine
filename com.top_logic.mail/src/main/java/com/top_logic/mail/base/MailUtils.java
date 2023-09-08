/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.base;

import com.top_logic.basic.Logger;

/**
 * Util class to for mails.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MailUtils {

	/**
	 * Service method as implementation of {@link MailFolderAware#getMailFolder()}
	 */
	public static MailFolder getMailFolder(MailFolderAware anAware) {
		if (!MailServer.isActivated()) {
			return null;
		}
		try {
			return MailServer.getInstance().getFolder(anAware);
		} catch (NullPointerException ex) {
			// Workaround for idiotic implementation in tl-mail, that does not care about the case
			// where a mail server cannot be contracted.
			Logger.error("Access to mail folder failed.", ex, MailUtils.class);
			return null;
		}
	}

}


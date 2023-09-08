/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.base;

import com.top_logic.basic.message.AbstractMessages;

/**
 * Messages for this package.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Messages extends AbstractMessages {

	/** Message for initialising {@link MailServer#getRootFolder()}. */
	public static Template0 INITIALISING_ROOT_FOLDER;

	static {
		init(Messages.class);
	}
}

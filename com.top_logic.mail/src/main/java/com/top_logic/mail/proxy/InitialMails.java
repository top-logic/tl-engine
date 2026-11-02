/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * How to handle the mails already present in a mail folder when it is read for the first time, or
 * when the server has renumbered its messages.
 *
 * @see UIDMark#initial(long, InitialMails, long)
 */
public enum InitialMails implements ExternallyNamed {

	/**
	 * Leave the mails already present untouched and handle only mails arriving afterwards.
	 */
	SKIP,

	/**
	 * Handle all mails already present in the folder.
	 */
	PROCESS;

	@Override
	public String getExternalName() {
		return name();
	}

}

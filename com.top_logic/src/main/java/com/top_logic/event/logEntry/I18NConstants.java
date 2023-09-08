/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.logEntry;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix LOGENTRY_CONF = legacyPrefix("logentry.conf.");

	public static ResPrefix LOGENTRY_MESSAGE = legacyPrefix("logentry.message.");

	public static ResKey1 ARCHIVED_SUCCESSFULLY__COUNT;

	public static ResKey NOTHING_TO_ARCHIVE;

	public static ResKey1 CLEANUP_SUCCESS__COUNT;

	public static ResKey NOTHING_TO_CLEANUP;

	static {
		initConstants(I18NConstants.class);
	}

}

/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.error;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Error message for application error.
	 */
	public static ResKey2 COMMAND_FAILED__COMMAND_TIMESTAMP;

	/**
	 * Dialog title for error display of failed command.
	 */
	public static ResKey1 COMMAND_FAILED_TITLE__COMMAND;

	/**
	 * Error message for internal error.
	 */
	public static ResKey1 INTERNAL_ERROR__TIMESTAMP;

	/**
	 * Replacement for an exeption message that is <code>null</code>.
	 */
	public static ResKey NO_EXCEPTION_MESSAGE;

	public static ResKey REASON_IS = legacyKey("error.reasonIs");

	static {
		initConstants(I18NConstants.class);
	}
}

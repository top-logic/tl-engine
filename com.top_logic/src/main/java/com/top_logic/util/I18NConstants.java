/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalisation constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix MIMETYPE_NAMES = legacyPrefix("mimetype.");

	public static ResKey TOKEN_EMPTY_VALUE_REPLACEMENT;

	public static ResKey RENDERING_COMMAND;

	public static ResKey BUTTON_STARTUP = ResKey.legacy("tl.setup.buttonStartup");

	public static ResKey UNKNOWN_TYPE_RES_KEY;

	public static ResKey INTERNAL_ERROR;

	static {
		initConstants(I18NConstants.class);
	}
}

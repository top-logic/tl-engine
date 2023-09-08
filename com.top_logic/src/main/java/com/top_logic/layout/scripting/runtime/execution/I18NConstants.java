/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.execution;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Messages for package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {
	
	public static ResKey3 WRONG_EXECUTING_USER__ACTION__EXPECTED_USER__ACTUAL_USER;

	public static ResKey3 WRONG_LOCALE__ACTION__EXPECTED_LOCALE__ACTUAL_LOCALE;

	public static ResKey3 WRONG_TIMEZONE__ACTION__EXPECTED_TIMEZONE__ACTUAL_TIMEZONE;

	public static ResKey ACTION_FAILED_TITLE;

	public static ResKey2 ACTION_FAILED_MESSAGE__ACTION_FAILURE;

	public static ResKey SHOW_DETAILS;

	public static ResKey HIDE_DETAILS;

	static {
		initConstants(I18NConstants.class);
	}

}

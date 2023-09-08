/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml.log;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey4;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey4 AT_LOCATION__FILE_LINE_COL_DETAIL;

	public static ResKey3 AT_LOCATION__LINE_COL_DETAIL;

	static {
		initConstants(I18NConstants.class);
	}
}

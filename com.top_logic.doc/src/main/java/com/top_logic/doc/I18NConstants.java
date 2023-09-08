/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey LANGUAGE_NOT_SUPPORTED;
	public static ResPrefix SOURCE_LANGUAGE_DIALOG;
	public static ResKey SOURCE_LANGUAGE_DIALOG_WARNINGS;
	public static ResKey TRANSLATION_SERVICE_NOT_ACTIVE;

	public static ResKey2 ERROR_TRANSLATION_FAILED__PAGE_DETAIL;

	public static ResKey1 TRANSLATED_SUCCESSFUL__COUNT;

	static {
		initConstants(I18NConstants.class);
	}
}

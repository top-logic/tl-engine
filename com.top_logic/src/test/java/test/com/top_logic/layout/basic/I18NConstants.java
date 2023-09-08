/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey TEST_RESOURCE_TEXT_KEY1;

	/** Note: It is expected that this key is not translated. */
	public static ResKey TEST_RESOURCE_TEXT_NOT_TRANSLATED_KEY;

	static {
		initConstants(I18NConstants.class);
	}
}

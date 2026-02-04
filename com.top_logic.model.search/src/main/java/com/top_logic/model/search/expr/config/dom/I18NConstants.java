/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.dom;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en End tag "{0}" has no matching start tag.
	 */
	public static ResKey1 ERROR_NO_MATCHING_START_TAG__NAME;

	/**
	 * @en Unclosed tag "{0}".
	 */
	public static ResKey1 ERROR_UNCLOSED_TAG__NAME;

	/**
	 * @en Mismatched tags: expected closing tag for "{0}" but found "{1}".
	 */
	public static ResKey2 ERROR_MISMATCHED_TAGS__EXPECTED_ACTUAL;

	static {
		initConstants(I18NConstants.class);
	}
}

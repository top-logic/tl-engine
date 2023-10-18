/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey4;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Invalid nesting of tags, expected {0}, found {1} at line {2}, column {3}.
	 */
	public static ResKey4 ERROR_INVALID_NESTING__EXPECTED_FOUND_LINE_COL;

	/**
	 * @en Not a number: {0}
	 */
	public static ResKey1 ERROR_NOT_A_NUMBER__VALUE;

	/**
	 * @en Value cannot be compared: {0}
	 */
	public static ResKey1 ERROR_NOT_COMPARABLE__VALUE;

	/**
	 * @en Failed to parse template '{0}': {1}
	 */
	public static ResKey2 ERROR_TEMPLATE_SYNTAX_ERROR__NAME_DESC;

	/**
	 * @en There is no matching opening tag for tag with name "{0}" at line {1}, column {2}.
	 */
	public static ResKey3 ERROR_NO_MATCHING_OPENING_TAG__NAME_LINE_COL;

	/**
	 * @en No tag structure inside attribute "{0}" allowed at line {1}, column {2}.
	 */
	public static ResKey3 ERROR_NO_TAGS_INSIDE_ATTRIBUTE_VALUE__NAME_LINE_COL;

	/**
	 * @en Missing end tag "{0}" for start tag at line {1}, column {2}.
	 */
	public static ResKey3 ERROR_MISSING_END_TAG__NAME_LINE_COL;

	/**
	 * @en Duplicate attribute "{0}" at line {1}, column {2}.
	 */
	public static ResKey3 ERROR_DUPLICATE_ATTRIBUTE__NAME_LINE_COL;

	/**
	 * @en Tag "{0}" at line {1}, column {2} is no valid HTML 5 void element.
	 * 
	 * @see HTMLConstants#VOID_ELEMENTS
	 */
	public static ResKey3 NO_VALID_VOID_ELEMENT__NAME_LINE_COL;

	/**
	 * @en Reading template '{0}' failed.
	 */
	public static ResKey1 ERROR_TEMPLATE_READIN_FAILED__NAME;

	static {
		initConstants(I18NConstants.class);
	}
}

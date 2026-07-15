/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for filter editor field labels.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en {0} ({1})
	 */
	public static ResKey2 OPTION_LABEL__NAME_COUNT;

	/**
	 * @en Pattern
	 */
	public static ResKey PATTERN;

	/**
	 * @en Case sensitive
	 */
	public static ResKey CASE_SENSITIVE;

	/**
	 * @en Regular expression
	 */
	public static ResKey REGEXP;

	/**
	 * @en Whole field
	 */
	public static ResKey WHOLE_FIELD;

	/**
	 * @en Operator
	 */
	public static ResKey OPERATOR;

	/**
	 * @en Value
	 */
	public static ResKey VALUE;

	/**
	 * @en Upper bound
	 */
	public static ResKey UPPER_BOUND;

	/**
	 * @en True
	 */
	public static ResKey VALUE_TRUE;

	/**
	 * @en False
	 */
	public static ResKey VALUE_FALSE;

	/**
	 * @en No value
	 */
	public static ResKey VALUE_NONE;

	/**
	 * @en Invert
	 */
	public static ResKey FILTER_INVERT;

	static {
		initConstants(I18NConstants.class);
	}
}

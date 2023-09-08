/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.annotation;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey4;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey3 BOUND_CONSTRAINT_VIOLATED__VALUE_BOUND_OPERATION;

	public static ResKey4 COMPARISON_DEPENDENCY_VIOLATED__VALUE_OTHER_BOUND_OPERATION;

	/**
	 * @en The value "{1}" does not match the pattern "{0}".
	 */
	public static ResKey2 ERROR_REGEXP_CONSTRAINT_VIOLATED__PATTERN_INPUT;

	static {
		initConstants(I18NConstants.class);
	}
}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.constraint;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants for built-in constraint check adapters.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The field must not be empty.
	 */
	public static ResKey ERROR_MANDATORY_FIELD_EMPTY;

	/**
	 * @en The text is too short. Minimum length: {0}, actual: {1}.
	 */
	public static ResKey2 ERROR_STRING_TOO_SHORT__MIN_ACTUAL;

	/**
	 * @en The text is too long. Maximum length: {0}, actual: {1}.
	 */
	public static ResKey2 ERROR_STRING_TOO_LONG__MAX_ACTUAL;

	/**
	 * @en The value is below the minimum. Minimum: {0}, actual: {1}.
	 */
	public static ResKey2 ERROR_VALUE_BELOW_MINIMUM__MIN_ACTUAL;

	/**
	 * @en The value exceeds the maximum. Maximum: {0}, actual: {1}.
	 */
	public static ResKey2 ERROR_VALUE_ABOVE_MAXIMUM__MAX_ACTUAL;

	static {
		initConstants(I18NConstants.class);
	}
}

/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey ERROR_ONLY_CONSTANTS;

	public static ResKey ERROR_ONLY_STRING_FIELDS;

	public static ResKey2 ERROR_FIELD_DOES_NOT_EXIST__CLASS_FIELD;

	public static ResKey2 ERROR_CANNOT_ACCESS_FIELD__CLASS_FIELD;

	public static ResKey1 ERROR_INVALID_CONSTANT_VALUE__VALUE;

	public static ResKey2 ERROR_CANNOT_RESOLVE_ENUM_CONSTANT__VALUE_DETAIL;

	public static ResKey1 ERROR_INVALID_ENUM_CONSTANT__VALUE;

	public static ResKey1 ERROR_PROPERY_CANNOT_BE_NULL__NAME;

	public static ResKey1 NO_SUCH_TIME_UNIT__VALUE;

	public static ResKey1 INVALID_DURATION_FORMAT__VALUE;

	public static ResKey1 ERROR_TIME_RANGE_TOO_LARGE__MAX;

	/**
	 * @en Enumeration constant of wrong type configured, expected was {0}, configured is {1}.
	 */
	public static ResKey2 ERROR_ENUM_TYPE_MISMATCH__EXPECTED_CONFIGURED;

	static {
		initConstants(I18NConstants.class);
	}
}

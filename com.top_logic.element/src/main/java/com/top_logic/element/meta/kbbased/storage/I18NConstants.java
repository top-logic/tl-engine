/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey1 ERROR_EMPTY_NOT_ALLOWED_FOR_MANDATORY_ATTRIBUTE__ATTRIBUTE;

	public static ResKey2 ERROR_DUPLICATE_VALUES_NOT_ALLOWED__ATTRIBUE_VALUE;

	public static ResKey1 ERROR_LAST_VALUE_MUST_NOT_BE_REMOVED_FROM_MANDATORY_ATTRIBUTE__ATTRIBUTE;

	public static ResKey2 NOT_APPLICATION_VALUE_TYPE___EXPECTED_ACTUAL;
	
	/**
	 * @en The value must not be empty.
	 */
	public static ResKey ERROR_EMPTY_VALUE_IN_MANDATORY_ATTRIBUTE;

	/**
	 * @en The given value {0} is not a TLAnnotation.
	 */
	public static ResKey1 ERROR_VALUE_IS_NOT_TLANNOTATION__VALUE;

	/**
	 * @en Failed to access attribute "{1}" of "{0}": {2}
	 */
	public static ResKey3 ERROR_CANNOT_ACCESS_ATTRIBUTE__OBJ_ATTR_ERR;

	static {
		initConstants(I18NConstants.class);
	}
}

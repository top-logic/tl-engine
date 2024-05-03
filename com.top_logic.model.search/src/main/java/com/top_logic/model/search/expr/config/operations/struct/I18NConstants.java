/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.struct;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey4;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey2 ERROR_STRUCT_TYPE_EXPECTED__VALUE_EXPR;

	public static ResKey3 ERROR_ARGUMENT_COUNT_MISMATCH__EXPECTED_ACTUAL_EXPR;

	public static ResKey4 ERROR_DUPLICATE_KEYS__KEY_IDX1_IDX2_EXPR;

	/**
	 * @en No such class: {0}
	 */
	public static ResKey1 ERROR_NO_SUCH_CLASS__NAME;

	/**
	 * @en Failed to instantiate configuration "{0}": {1}
	 */
	public static ResKey2 ERROR_CREATING_CONFIGURATION__TYPE_MSG;

	/**
	 * @en Failed parse configuration value "{1}" for property "{0}": {2}
	 */
	public static ResKey3 ERROR_PARSING_CONFIGURATION_VALUE__PROP_VAL_MSG;

	static {
		initConstants(I18NConstants.class);
	}
}

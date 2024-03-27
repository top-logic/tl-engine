/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey3 ERROR_EVALUATION_FAILED__EXPR_ARGS_DEFS;

	public static ResKey2 ERROR_NOT_A_DATE__VAL_EXPR;

	public static ResKey2 ERROR_NOT_A_CALENDAR__VAL_EXPR;

	public static ResKey2 ERROR_NOT_A_TL_OBJECT__VAL_EXPR;

	/**
	 * @en The value "{0}" is not a type in expression: {1}
	 */
	public static ResKey2 ERROR_NOT_A_TYPE__VAL_EXPR;

	public static ResKey3 ERROR_INVALID_DATE_FORMAT__VAL_EXPR_MSG;

	public static ResKey3 ERROR_INVALID_FORMAT__STR_EXPR_MSG;

	public static ResKey2 ERROR_NOT_A_TYPE_PART__EXPR_VALUE;

	public static ResKey2 ERROR_NOT_A_REFERENCE__EXPR_VALUE;

	public static ResKey2 ERROR_MORE_THAN_A_SINGLE_ELEMENT__VAL_EXPR;

	public static ResKey1 ERROR_ACCES_TO_UNDEFINED_VARIABLE__NAME;

	public static ResKey2 ERROR_NUMBER_REQUIRED__VALUE_EXPR;

	public static ResKey3 ERROR_INVALID_LIST_INDEX__LIST_INDEX_EXPR;

	public static ResKey2 ERROR_LIST_MAP_OR_OBJECT_REQUIRED__VALUE_EXPR;

	public static ResKey2 ERROR_NOT_A_FUNCTION__VAL_EXPR;

	public static ResKey2 ERROR_NOT_A_STRUCT__VAL_EXPR;

	public static ResKey2 ERROR_NOT_A_STRUCT_ENTRY__VAL_EXPR;

	public static ResKey3 ERROR_NO_SUCH_PROPERTY__OBJ_NAME_EXPR;

	public static ResKey3 ERROR_UNEXPECTED_TYPE__EXPECTED_VAL_EXPR;

	public static ResKey1 ERROR_UNEXPECTED_NULL_VALUE__EXPR;

	public static ResKey2 ERROR_NEITHER_CLASS_NOR_ENUM__TYPE_EXPR;

	public static ResKey2 ERROR_NOT_A_RES_KEY__VALUE__EXPR;

	/**
	 * @en Encoding value of expression {1} failed: {0}
	 */
	public static ResKey2 ENCODING_FAILED__MSG_EXPR;

	/**
	 * @en The value "{0}" is not a binary data value in {1}.
	 */
	public static ResKey2 ERROR_NOT_A_BINARY_VALUE__VAL_EXPR;

	static {
		initConstants(I18NConstants.class);
	}
}

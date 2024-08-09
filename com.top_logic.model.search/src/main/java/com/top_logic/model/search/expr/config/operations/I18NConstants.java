/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Only a single argument is expected but got {0} in: {1}
	 */
	public static ResKey2 ERROR_SINGLE_ARGUMENT_EXPECTED__CNT_EXPR;

	public static ResKey1 ERROR_NO_ARGUMENTS_EXPECTED__EXPR;

	public static ResKey1 ERROR_AT_LEAST_ONE_ARGUMENT_EXPECTED__EXPR;

	/**
	 * @en Two arguments expected but got {0} in: {1}
	 */
	public static ResKey2 ERROR_TWO_ARGUMENTS_EXPECTED__CNT_EXPR;

	/**
	 * @en Three arguments expected but got {0} in: {1}
	 */
	public static ResKey2 ERROR_THREE_ARGUMENTS_EXPECTED__CNT_EXPR;

	public static ResKey2 ERROR_AT_LEAST_ARGUMENTS_EXPECTED__CNT_EXPR;

	public static ResKey2 ERROR_AT_MOST_ARGUMENTS_EXPECTED__CNT_EXPR;

	public static ResKey1 ERROR_EXPECTED_TYPE_PART_LITERAL__EXPR;

	public static ResKey1 ERROR_LITERAL_ARGUMENT__EXPR;

	public static ResKey1 ERROR_EXPECTED_REFERENCE_LITERAL__EXPR;

	public static ResKey1 ERROR_EXPECTED_STRUCTURED_TYPE_LITERAL__EXPR;

	public static ResKey1 ERROR_EXPECTED_ASSOCIATION_END_LITERAL__EXPR;

	public static ResKey1 ERROR_MUST_BE_INVOKED_WITHOUT_TARGET__EXPR;

	public static ResKey1 ERROR_EXPECTING_A_TARGET__EXPR;

	/**
	 * @en Positional arguments may occur only at the beginning of the arguments list in call to
	 *     function "{0}".
	 */
	public static ResKey1 ERROR_INVALID_NAMED_ARGUMENT_ORDER__FUN;

	/**
	 * @en There is no named argument "{1}" in function "{0}". The function has no named arguments.
	 */
	public static ResKey2 ERROR_NO_NAMED_ARGUMENTS__FUN_NAME;

	/**
	 * @en No such named argument "{1}" in function "{0}". Possible arguments are: {2}
	 */
	public static ResKey3 ERROR_NO_SUCH_NAMED_ARGUMENT__FUN_NAME_AVAIL;

	/**
	 * @en The argument "{0}" is mandatory in expression "{1}".
	 */
	public static ResKey2 ERROR_MANDATORY_ARGUMENT__ARG_EXPR;

	/**
	 * @en An argument must only be defined once in a function call. Argument "{1}" of function
	 *     "{0}" is defined more than once.
	 */
	public static ResKey2 ERROR_AMBIGUOUS_ARGUMENT__FUN_NAME;

	/**
	 * @en Unexpected number of arguments in call of function "{0}", the function expects no more
	 *     than {1} arguments.
	 */
	public static ResKey2 ERROR_UNEXPECTED_ARGUMENT__FUN_CNT;

	static {
		initConstants(I18NConstants.class);
	}
}

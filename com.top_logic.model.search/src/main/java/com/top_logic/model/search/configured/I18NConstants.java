/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.configured;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N of this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Unable to resolve the configured script ''{0}''.
	 */
	public static ResKey1 ERROR_RESOLVING_SCRIPT__NAME;

	/**
	 * @en The configured script ''{0}'' does not exist.
	 */
	public static ResKey1 ERROR_NO_SUCH_SCRIPT__NAME;

	/**
	 * @en You are not allowed to call the function "{0}": you lack the permission "{1}" on "{2}".
	 */
	public static ResKey3 ERROR_CALL_NOT_ALLOWED__NAME_OPERATION_OBJECT;

	/**
	 * @en You are not allowed to call the function "{0}": there is no object to check the
	 *     permission on.
	 */
	public static ResKey1 ERROR_CALL_NO_SECURITY_OBJECT__NAME;

	/**
	 * @en You are not allowed to call the function "{0}": the permission can not be checked on
	 *     "{1}", it is no stored object.
	 */
	public static ResKey2 ERROR_CALL_INVALID_SECURITY_OBJECT__NAME_OBJECT;

	/**
	 * @en The command group ''{0}'' of the privileged script ''{1}'' does not exist.
	 */
	public static ResKey2 ERROR_UNKNOWN_COMMAND_GROUP__GROUP_NAME;

	/**
	 * @en The argument of canCall() must be a call of a configured TL-Script function: {0}
	 */
	public static ResKey1 ERROR_CAN_CALL_NO_CALL__EXPR;

	static {
		initConstants(I18NConstants.class);
	}

}

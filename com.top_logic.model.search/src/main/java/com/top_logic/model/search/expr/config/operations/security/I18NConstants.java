/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.security;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Argument {0} is not a role in: {1}
	 */
	public static ResKey2 ERROR_NOT_A_ROLE__VAL_EXPR;

	/**
	 * @en There is no account with name {0} in: {1}
	 */
	public static ResKey2 ERROR_NO_SUCH_ACCOUNT__VAL_EXPR;

	static {
		initConstants(I18NConstants.class);
	}
}

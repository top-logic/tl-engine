/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.script;

import com.top_logic.basic.util.ResKey4;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Wrong argument to parameter "{1}" of function "{0}", expected is a value of type "{2}"
	 *     value was: {3}
	 */
	public static ResKey4 ERROR_WRONG_ARGUMENT__FUN_ARG_EXPECTED_VAL;

	static {
		initConstants(I18NConstants.class);
	}
}

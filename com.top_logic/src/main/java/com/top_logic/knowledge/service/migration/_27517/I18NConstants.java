/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration._27517;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Error: A definition for the application type "{1}" is expected, but a definition for
	 *     "{0}" was given.
	 */
	public static ResKey2 ERROR_PERSON_TABLE_DEFINITION_EXPECTED__ACTUAL_EXPECTED;

	static {
		initConstants(I18NConstants.class);
	}
}

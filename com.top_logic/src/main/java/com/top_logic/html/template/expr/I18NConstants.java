/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template.expr;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The value ''{0}'' is not of the expected type ''{1}'' at {2}.
	 */
	public static ResKey3 ERROR_UNEXPECTED_TYPE__VALUE_TYPE_LOCATION;

	/**
	 * @en Value ''{0}'' has no properties to access at {1}.
	 */
	public static ResKey2 ERROR_VALUE_HAS_NO_PROPERTIES__VALUE_LOCATION;

	/**
	 * @en line {0}, column {1}
	 */
	public static ResKey2 LOCATION__LINE_COL;

	static {
		initConstants(I18NConstants.class);
	}
}
